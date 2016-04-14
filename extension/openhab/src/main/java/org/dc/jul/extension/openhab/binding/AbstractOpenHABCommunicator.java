/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

import java.util.concurrent.Future;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.InvocationFailedException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import org.dc.jul.extension.openhab.binding.interfaces.OpenHABCommunicator;
import org.dc.jul.extension.protobuf.ClosableDataBuilder;
import org.dc.jul.extension.rsb.com.RSBCommunicationService;
import org.dc.jul.extension.rsb.com.RSBRemoteService;
import org.dc.jul.extension.rsb.iface.RSBLocalServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Scope;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.EventCallback;
import rst.homeautomation.openhab.DALBindingType;
import rst.homeautomation.openhab.OpenhabCommandType;
import rst.homeautomation.openhab.RSBBindingType;
import rst.homeautomation.state.ActiveDeactiveType;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public abstract class AbstractOpenHABCommunicator implements OpenHABCommunicator {

    public static final String RPC_METHODE_INTERNAL_RECEIVE_UPDATE = "internalReceiveUpdate";
    public static final String RPC_METHODE_INTERNAL_RECEIVE_COMMAND = "internalReceiveCommand";
    public static final String RPC_METHODE_EXECUTE_COMMAND = "executeCommand";
    public static final String ITEM_SUBSEGMENT_DELIMITER = "_";
    public static final String ITEM_SEGMENT_DELIMITER = "__";

    public static final Scope SCOPE_OPENHAB_IN = new Scope("/openhab/in");
    public static final Scope SCOPE_OPENHAB_OUT = new Scope("/openhab/out");

    protected static final Logger logger = LoggerFactory.getLogger(AbstractOpenHABCommunicator.class);

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(OpenhabCommandType.OpenhabCommand.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(RSBBindingType.RSBBinding.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(DALBindingType.DALBinding.getDefaultInstance()));
    }

    private RSBRemoteService<RSBBindingType.RSBBinding> openhabCommandExecutionRemote;
    private RSBCommunicationService<DALBindingType.DALBinding, DALBindingType.DALBinding.Builder> openhabItemUpdateListener;

    private final boolean hardwareSimulationMode;

    public AbstractOpenHABCommunicator(final boolean hardwareSimulationMode) {
        this.hardwareSimulationMode = hardwareSimulationMode;
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        try {

            openhabCommandExecutionRemote = new RSBRemoteService<RSBBindingType.RSBBinding>() {

                @Override
                public void notifyUpdated(final RSBBindingType.RSBBinding data) {
                    AbstractOpenHABCommunicator.this.notifyUpdated(data);
                }
            };

            openhabItemUpdateListener = new RSBCommunicationService<DALBindingType.DALBinding, DALBindingType.DALBinding.Builder>(DALBindingType.DALBinding.newBuilder()) {

                @Override
                public void registerMethods(RSBLocalServerInterface server) throws CouldNotPerformException {
                    AbstractOpenHABCommunicator.this.registerMethods(server);
                }
            };
            openhabCommandExecutionRemote.init(SCOPE_OPENHAB_IN);
            openhabItemUpdateListener.init(SCOPE_OPENHAB_OUT);
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public final void notifyUpdated(final RSBBindingType.RSBBinding data) {
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

    public final void registerMethods(final RSBLocalServerInterface server) {
        try {
            server.addMethod(RPC_METHODE_INTERNAL_RECEIVE_UPDATE, new InternalReceiveUpdateCallback());
            server.addMethod(RPC_METHODE_INTERNAL_RECEIVE_COMMAND, new InternalReceiveCommandCallback());
        } catch (CouldNotPerformException ex) {
            logger.warn("Could not add methods to local server in [" + getClass().getSimpleName() + "]", ex);
        }
    }

    public class InternalReceiveUpdateCallback extends EventCallback {

        @Override
        public Event invoke(final Event request) throws Throwable {
            try {
                AbstractOpenHABCommunicator.this.internalReceiveUpdate((OpenhabCommandType.OpenhabCommand) request.getData());
            } catch (Throwable cause) {
                throw ExceptionPrinter.printHistoryAndReturnThrowable(new InvocationFailedException(this, AbstractOpenHABCommunicator.this, cause), logger, LogLevel.ERROR);
            }
            return new Event(Void.class);
        }
    }

    public class InternalReceiveCommandCallback extends EventCallback {

        @Override
        public Event invoke(final Event request) throws Throwable {
            try {
                AbstractOpenHABCommunicator.this.internalReceiveCommand((OpenhabCommandType.OpenhabCommand) request.getData());
            } catch (Throwable cause) {
                throw ExceptionPrinter.printHistoryAndReturnThrowable(new InvocationFailedException(this, AbstractOpenHABCommunicator.this, cause), logger, LogLevel.ERROR);
            }
            return new Event(Void.class);
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        try {
            if (hardwareSimulationMode) {
                return;
            }

            // Init Openhab connection
            openhabCommandExecutionRemote.activate();
            openhabItemUpdateListener.activate();

            try (ClosableDataBuilder<DALBindingType.DALBinding.Builder> dataBuilder = openhabItemUpdateListener.getDataBuilder(this)) {
                dataBuilder.getInternalBuilder().setState(ActiveDeactiveType.ActiveDeactive.newBuilder().setState(ActiveDeactiveType.ActiveDeactive.ActiveDeactiveState.ACTIVE));
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not setup dalCommunicationService as active.", ex);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not activate " + OpenHABCommunicator.class.getSimpleName() + "!", ex);
        }
    }

    @Override
    public Future executeCommand(final OpenhabCommandType.OpenhabCommand command) throws CouldNotPerformException {
        try {

            if (!command.hasItem() || command.getItem().isEmpty()) {
                throw new NotAvailableException("command item");
            }

            if (!command.hasType()) {
                throw new NotAvailableException("command type");
            }

            if (hardwareSimulationMode) {
                internalReceiveUpdate(command);
                return null;
            }

            if (!openhabCommandExecutionRemote.isConnected()) {
                throw new InvalidStateException("Dal openhab binding could not reach openhab server! Please check if openhab is still running!");
            }

            openhabCommandExecutionRemote.callMethod(RPC_METHODE_EXECUTE_COMMAND, command);
            return null; // TODO: mpohling implement future handling.
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not execute " + command + "!", ex);
        }
    }

    @Override
    public void shutdown() throws InterruptedException {
        openhabCommandExecutionRemote.shutdown();
        openhabItemUpdateListener.shutdown();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[version=" + getClass().getPackage().getImplementationVersion() + "]";
    }
}
