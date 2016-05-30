package org.dc.jul.extension.openhab.binding;

/*
 * #%L
 * JUL Extension OpenHAB
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.extension.openhab.binding.interfaces.OpenHABRemote;
import org.dc.jul.extension.rsb.com.RPCHelper;
import org.dc.jul.extension.rsb.com.RSBFactory;
import org.dc.jul.extension.rsb.com.RSBRemoteService;
import org.dc.jul.extension.rsb.iface.RSBListenerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Handler;
import rsb.Scope;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.homeautomation.openhab.OpenhabCommandType;
import rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand;
import rst.homeautomation.openhab.RSBBindingType;
import rst.homeautomation.openhab.RSBBindingType.RSBBinding;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public abstract class AbstractOpenHABRemote extends RSBRemoteService<RSBBinding> implements OpenHABRemote {

    public static final String ITEM_SUBSEGMENT_DELIMITER = "_";
    public static final String ITEM_SEGMENT_DELIMITER = "__";

    public static final String RPC_METHODE_SEND_COMMAND = "sendCommand";
    public static final String RPC_METHODE_POST_COMMAND = "postCommand";
    public static final String RPC_METHODE_POST_UPDATE = "postUpdate";

    public static final Scope SCOPE_OPENHAB = new Scope("/openhab");
    public static final Scope SCOPE_OPENHAB_UPDATE = SCOPE_OPENHAB.concat(new Scope("/update"));
    public static final Scope SCOPE_OPENHAB_COMMAND = SCOPE_OPENHAB.concat(new Scope("/command"));

    protected static final Logger logger = LoggerFactory.getLogger(AbstractOpenHABRemote.class);

    private String itemFilter;

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(OpenhabCommandType.OpenhabCommand.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(RSBBindingType.RSBBinding.getDefaultInstance()));
    }

    private RSBListenerInterface openhabCommandListener, openhabUpdateListener;
    private final boolean hardwareSimulationMode;

    public AbstractOpenHABRemote(final boolean hardwareSimulationMode) {
        super(RSBBinding.class);
        this.hardwareSimulationMode = hardwareSimulationMode;
    }

    @Override
    public void init(String itemFilter) throws InitializationException, InterruptedException {
        init(SCOPE_OPENHAB);
        this.itemFilter = itemFilter;
    }

    @Override
    protected void postInit() throws InitializationException, InterruptedException {
        try {
            openhabCommandListener = RSBFactory.getInstance().createSynchronizedListener(SCOPE_OPENHAB_COMMAND);
            openhabUpdateListener = RSBFactory.getInstance().createSynchronizedListener(SCOPE_OPENHAB_UPDATE);

            openhabCommandListener.addHandler((Event event) -> {
                try {
                    OpenhabCommand openhabCommand = (OpenhabCommand) event.getData();
                    if (!openhabCommand.hasItemBindingConfig() || !openhabCommand.getItemBindingConfig().startsWith(itemFilter)) {
                        return;
                    }
                    internalReceiveCommand(openhabCommand);
                } catch (ClassCastException ex) {
                    // Thats not an command hab command. Skip invocation...
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Could not handle openhab command!", ex), logger);
                }
            }, true);

            openhabUpdateListener.addHandler(new Handler() {

                @Override
                public void internalNotify(Event event) {
                    try {
                        internalReceiveUpdate((OpenhabCommand) event.getData());
                    } catch (ClassCastException ex) {
                        // Thats not an command hab command. Skip invocation...
                    } catch (CouldNotPerformException ex) {
                        ExceptionPrinter.printHistory(new CouldNotPerformException("Could not handle openhab update!", ex), logger);
                    }
                }
            }, true);
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    @Override
    public void notifyDataUpdate(final RSBBindingType.RSBBinding data) {
        switch (data.getState().getState()) {
        case ACTIVE:
            logger.info("Active dal binding state!");
            break;
        case DEACTIVE:
            logger.info("Deactive dal binding state!");
            break;
        case UNKNOWN:
            logger.info("Unkown dal binding state!");
            break;
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        if (hardwareSimulationMode) {
            return;
        }
        super.activate();
        openhabCommandListener.activate();
        openhabUpdateListener.activate();
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {

        try {
            super.deactivate();
        } catch (InterruptedException ex) {
            logger.warn("Unable to deactivate openhab remote!", ex);
            Thread.currentThread().interrupt();
        } catch (CouldNotPerformException ex) {
            logger.warn("Unable to deactivate openhab remote!", ex);
        }

        try {
            if (openhabUpdateListener != null) {
                openhabUpdateListener.deactivate();
            }
        } catch (InterruptedException ex) {
            logger.warn("Unable to deactivate openhab update listener!", ex);
            Thread.currentThread().interrupt();
        } catch (CouldNotPerformException ex) {
            logger.warn("Unable to deactivate openhab update listener!", ex);
        }

        try {
            if (openhabCommandListener != null) {
                openhabCommandListener.deactivate();
            }
        } catch (InterruptedException ex) {
            logger.warn("Unable to deactivate openhab command listener!", ex);
            Thread.currentThread().interrupt();
        } catch (CouldNotPerformException ex) {
            logger.warn("Unable to deactivate openhab command listener!", ex);
        }
    }

    @Override
    public Future<Void> postCommand(final OpenhabCommand command) throws CouldNotPerformException {
        try {
            validateCommand(command);
            if (hardwareSimulationMode) {
                internalReceiveUpdate(command);
                return CompletableFuture.completedFuture(null);
            }
            return (Future) RPCHelper.callRemoteMethod(command, this);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not post Command[" + command + "]!", ex);
        }
    }

    @Override
    public Future<Void> sendCommand(OpenhabCommandType.OpenhabCommand command) throws CouldNotPerformException {
        try {
            validateCommand(command);
            if (hardwareSimulationMode) {
                internalReceiveUpdate(command);
                return CompletableFuture.completedFuture(null);
            }
            return (Future) RPCHelper.callRemoteMethod(command, this);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not send Command[" + command + "]!", ex);
        }
    }

    @Override
    public Future<Void> postUpdate(OpenhabCommandType.OpenhabCommand command) throws CouldNotPerformException {
        try {
            validateCommand(command);
            if (hardwareSimulationMode) {
                return CompletableFuture.completedFuture(null);
            }
            return (Future) RPCHelper.callRemoteMethod(command, this);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not post Update[" + command + "]!", ex);
        }
    }

    private void validateCommand(final OpenhabCommand command) throws InvalidStateException {
        try {
            if (!command.hasItem() || command.getItem().isEmpty()) {
                throw new NotAvailableException("command item");
            }

            if (!command.hasType()) {
                throw new NotAvailableException("command type");
            }
        } catch (CouldNotPerformException ex) {
            throw new InvalidStateException("Command invalid!", ex);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[version=" + getClass().getPackage().getImplementationVersion() + "]";
    }
}
