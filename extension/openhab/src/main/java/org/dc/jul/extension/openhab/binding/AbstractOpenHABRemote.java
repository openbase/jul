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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.extension.openhab.binding.interfaces.OpenHABRemote;
import org.dc.jul.extension.rsb.com.RPCHelper;
import org.dc.jul.extension.rsb.com.RSBRemoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Scope;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.homeautomation.openhab.DALBindingType;
import rst.homeautomation.openhab.OpenhabCommandType;
import rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand;
import rst.homeautomation.openhab.RSBBindingType;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public abstract class AbstractOpenHABRemote extends RSBRemoteService<RSBBindingType.RSBBinding> implements OpenHABRemote {

//    public static final String RPC_METHODE_INTERNAL_RECEIVE_UPDATE = "internalReceiveUpdate";
//    public static final String RPC_METHODE_INTERNAL_RECEIVE_COMMAND = "internalReceiveCommand";
//    public static final String RPC_METHODE_EXECUTE_COMMAND = "executeCommand";
//    public static final String RPC_METHODE_EXECUTE_COMMAND = "executeCommand";
//    public static final String RPC_METHODE_EXECUTE_COMMAND = "executeCommand";
    public static final String ITEM_SUBSEGMENT_DELIMITER = "_";
    public static final String ITEM_SEGMENT_DELIMITER = "__";

//    public static final Scope SCOPE_OPENHAB_IN = new Scope("/openhab/in");
//    public static final Scope SCOPE_OPENHAB_OUT = new Scope("/openhab/out");
    public static final String RPC_METHODE_SEND_COMMAND = "sendCommand";
    public static final String RPC_METHODE_POST_COMMAND = "postCommand";
    public static final String RPC_METHODE_POST_UPDATE = "postUpdate";

    public static final Scope SCOPE_OPENHAB_IN = new Scope("/openhab/in");
    public static final String SCOPE_OPENHAB_OUT_UPDATE = "/openhab/out/update";
    public static final String SCOPE_OPENHAB_OUT_COMMAND = "/openhab/out/command";

    protected static final Logger logger = LoggerFactory.getLogger(AbstractOpenHABRemote.class);

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(OpenhabCommandType.OpenhabCommand.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(RSBBindingType.RSBBinding.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(DALBindingType.DALBinding.getDefaultInstance()));
    }

//    private RSBRemoteService<RSBBindingType.RSBBinding> openhabRemote;
//    private RSBCommunicationService<DALBindingType.DALBinding, DALBindingType.DALBinding.Builder> openhabItemUpdateListener;
    private final boolean hardwareSimulationMode;

    public AbstractOpenHABRemote(final boolean hardwareSimulationMode) {
        this.hardwareSimulationMode = hardwareSimulationMode;
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        init(SCOPE_OPENHAB_IN);
    }

//    @Override
//    public void init(final Scope scope) throws InitializationException, InterruptedException {
//        try {
////
////            openhabRemote = new RSBRemoteService<RSBBindingType.RSBBinding>() {
////
////            };
//
////            openhabItemUpdateListener = new RSBCommunicationService<DALBindingType.DALBinding, DALBindingType.DALBinding.Builder>(DALBindingType.DALBinding.newBuilder()) {
////
////                @Override
////                public void registerMethods(RSBLocalServerInterface server) throws CouldNotPerformException {
////                    AbstractOpenHABRemote.this.registerMethods(server);
////                }
////            };
//            openhabRemote.init(scope);
////            openhabItemUpdateListener.init(scope);
//        } catch (CouldNotPerformException ex) {
//            throw new InitializationException(this, ex);
//        }
//    }
    @Override
    public void notifyUpdated(final RSBBindingType.RSBBinding data) {
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

//    public final void registerMethods(final RSBLocalServerInterface server) {
//        try {
//            server.addMethod(RPC_METHODE_INTERNAL_RECEIVE_UPDATE, new InternalReceiveUpdateCallback());
//            server.addMethod(RPC_METHODE_INTERNAL_RECEIVE_COMMAND, new InternalReceiveCommandCallback());
//        } catch (CouldNotPerformException ex) {
//            logger.warn("Could not add methods to local server in [" + getClass().getSimpleName() + "]", ex);
//        }
//    }
    

//    public class InternalReceiveUpdateCallback extends EventCallback {
//
//        @Override
//        public Event invoke(final Event request) throws Throwable {
//            try {
//                AbstractOpenHABRemote.this.internalReceiveUpdate((OpenhabCommandType.OpenhabCommand) request.getData());
//            } catch (Throwable cause) {
//                throw ExceptionPrinter.printHistoryAndReturnThrowable(new InvocationFailedException(this, AbstractOpenHABRemote.this, cause), logger, LogLevel.ERROR);
//            }
//            return new Event(Void.class);
//        }
//    }
//
//    public class InternalReceiveCommandCallback extends EventCallback {
//
//        @Override
//        public Event invoke(final Event request) throws Throwable {
//            try {
//                AbstractOpenHABRemote.this.internalReceiveCommand((OpenhabCommandType.OpenhabCommand) request.getData());
//            } catch (Throwable cause) {
//                throw ExceptionPrinter.printHistoryAndReturnThrowable(new InvocationFailedException(this, AbstractOpenHABRemote.this, cause), logger, LogLevel.ERROR);
//            }
//            return new Event(Void.class);
//        }
//    }
    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        if (hardwareSimulationMode) {
            return;
        }
        super.activate();
    }

        // Init Openhab connection
//            openhabRemote.activate();
//            openhabItemUpdateListener.activate();
//            try (ClosableDataBuilder<DALBindingType.DALBinding.Builder> dataBuilder = openhabItemUpdateListener.getDataBuilder(this)) {
//                dataBuilder.getInternalBuilder().setState(ActiveDeactiveType.ActiveDeactive.newBuilder().setState(ActiveDeactiveType.ActiveDeactive.ActiveDeactiveState.ACTIVE));
//            } catch (Exception ex) {
//                throw new CouldNotPerformException("Could not setup dalCommunicationService as active.", ex);
//            }
//        } catch (CouldNotPerformException ex) {
//            throw new CouldNotPerformException("Could not activate " + OpenHABRemote.class.getSimpleName() + "!", ex);
//        }

//    @Override
//    public void deactivate() throws CouldNotPerformException, InterruptedException {
////        try (ClosableDataBuilder<DALBindingType.DALBinding.Builder> dataBuilder = openhabItemUpdateListener.getDataBuilder(this)) {
////            dataBuilder.getInternalBuilder().setState(ActiveDeactiveType.ActiveDeactive.newBuilder().setState(ActiveDeactiveType.ActiveDeactive.ActiveDeactiveState.DEACTIVE));
////        } catch (Exception ex) {
////            throw new CouldNotPerformException("Could not setup dalCommunicationService as active.", ex);
////        }
//        openhabRemote.deactivate();
//        openhabItemUpdateListener.deactivate();
//    }

    @Override
    protected void incomingEvent(Event event) {
        try {
            OpenhabCommand command = (OpenhabCommand) event.getData();
            switch (event.toString()) {
            case SCOPE_OPENHAB_OUT_UPDATE:
                internalReceiveUpdate(command);
            case SCOPE_OPENHAB_OUT_COMMAND:
                internalReceiveCommand(command);
            }
        } catch (ClassCastException ex) {
            // Thats not an command hab command. Skip invocation...
        } catch (CouldNotPerformException ex) {
            logger.error("Could not handle openhab event!", ex);
        }
    }

    @Override
    public Future postCommand(final OpenhabCommand command) throws CouldNotPerformException {
        try {
            validateCommand(command);
            if (hardwareSimulationMode) {
                internalReceiveUpdate(command);
                return CompletableFuture.completedFuture(null);
            }
            return RPCHelper.callRemoteMethod(command, this);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not post Command[" + command + "]!", ex);
        }
    }

    @Override
    public Future sendCommand(OpenhabCommandType.OpenhabCommand command) throws CouldNotPerformException {
        try {
            validateCommand(command);
            if (hardwareSimulationMode) {
                internalReceiveUpdate(command);
                return CompletableFuture.completedFuture(null);
            }
            return RPCHelper.callRemoteMethod(command, this);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not send Command[" + command + "]!", ex);
        }
    }

    @Override
    public Future postUpdate(OpenhabCommandType.OpenhabCommand command) throws CouldNotPerformException {
        try {
            validateCommand(command);
            if (hardwareSimulationMode) {
                return CompletableFuture.completedFuture(null);
            }
            return RPCHelper.callRemoteMethod(command, this);
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
