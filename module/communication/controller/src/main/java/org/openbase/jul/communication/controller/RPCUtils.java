package org.openbase.jul.communication.controller;

/*
 * #%L
 * JUL Extension Controller
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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

import org.openbase.jul.communication.data.RPCResponse;
import org.openbase.jul.communication.iface.RPCClient;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.schedule.FutureProcessor;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RPCUtils {

    public static final long RPC_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    public static final String USER_TIME_KEY = "USER_NANO_TIME";
    public static final String CREATE_TIMESTAMP = "CREATE_TIMESTAMP";
    public static final long USER_TIME_VALUE_INVALID = -1;
    private static final String INTERNAL_CALL_REMOTE_METHOD_NAME = "internalCallRemoteMethod";

    public static Future<Object> callRemoteServerMethod(final RPCRemote<?> remote) {
        return FutureProcessor.postProcess(
                (input, timeout, timeUnit) -> input.getResponse(),
                internalCallRemoteMethod(null, remote, Object.class)
        );
    }

    public static Future<Object> callRemoteServerMethod(final Object argument, final RPCRemote<?> remote) {
        return FutureProcessor.postProcess(
                (input, timeout, timeUnit) -> input.getResponse(),
                internalCallRemoteMethod(argument, remote, Object.class)
        );
    }

    public static <RETURN> Future<RETURN> callRemoteServerMethod(final RPCRemote<?> remote, final Class<RETURN> returnClass) {
        return FutureProcessor.postProcess(
                (input, timeout, timeUnit) -> input.getResponse(),
                internalCallRemoteMethod(null, remote, returnClass)
        );
    }

    public static <RETURN> Future<RETURN> callRemoteServerMethod(final Object argument, final RPCRemote<?> remote, final Class<RETURN> returnClass) {
        return FutureProcessor.postProcess(
                (input, timeout, timeUnit) -> input.getResponse(),
                internalCallRemoteMethod(argument, remote, returnClass)
        );
    }

    private static <RETURN> Future<RPCResponse<RETURN>> internalCallRemoteMethod(final Object argument, final RPCRemote<?> remote, final Class<RETURN> returnClass) {

        String methodName = "?";
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace == null) {
                throw new NotAvailableException("method stack");
            } else if (stackTrace.length == 0) {
                throw new InvalidStateException("Could not detect method stack!");
            }

            try {
                for (int i = 0; i < stackTrace.length; i++) {
                    if (stackTrace[i].getMethodName().equals(INTERNAL_CALL_REMOTE_METHOD_NAME)) {
                        methodName = stackTrace[i + 2].getMethodName();
                        break;
                    }
                }
            } catch (NullPointerException | IndexOutOfBoundsException ex) {
                throw new CouldNotPerformException("Could not detect method name!");
            }
            return remote.callMethodAsync(methodName, returnClass, argument);
        } catch (CouldNotPerformException ex) {
            return (Future<RPCResponse<RETURN>>) FutureProcessor.canceledFuture(new CouldNotPerformException("Could not call remote Message[" + methodName + "]", ex));
        }
    }

    public static Future<Object> callRemoteServerMethod(final RPCClient remote) {
        return FutureProcessor.postProcess(
                (input, timeout, timeUnit) -> input.getResponse(),
                internalCallRemoteMethod(null, remote, Object.class)
        );
    }

    public static Future<Object> callRemoteServerMethod(final Object argument, final RPCClient remote) {
        return FutureProcessor.postProcess(
                (input, timeout, timeUnit) -> input.getResponse(),
                internalCallRemoteMethod(argument, remote, Object.class)
        );
    }

    public static <RETURN> Future<RETURN> callRemoteServerMethod(final RPCClient remote, final Class<RETURN> returnClass) {
        return FutureProcessor.postProcess(
                (input, timeout, timeUnit) -> input.getResponse(),
                internalCallRemoteMethod(null, remote, returnClass)
        );
    }

    public static <RETURN> Future<RETURN> callRemoteServerMethod(final Object argument, final RPCClient remote, final Class<RETURN> returnClass) {
        return FutureProcessor.postProcess(
                (input, timeout, timeUnit) -> input.getResponse(),
                internalCallRemoteMethod(argument, remote, returnClass)
        );
    }

    private static <RETURN> Future<RPCResponse<RETURN>> internalCallRemoteMethod(final Object argument, final RPCClient remote, final Class<RETURN> returnClass) {

        String methodName = "?";
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace == null) {
                throw new NotAvailableException("method stack");
            } else if (stackTrace.length == 0) {
                throw new InvalidStateException("Could not detect method stack!");
            }

            try {
                for (int i = 0; i < stackTrace.length; i++) {
                    if (stackTrace[i].getMethodName().equals(INTERNAL_CALL_REMOTE_METHOD_NAME)) {
                        methodName = stackTrace[i + 2].getMethodName();
                        break;
                    }
                }
            } catch (NullPointerException | IndexOutOfBoundsException ex) {
                throw new CouldNotPerformException("Could not detect method name!");
            }
            return remote.callMethod(methodName, returnClass, argument);
        } catch (CouldNotPerformException ex) {
            return (Future<RPCResponse<RETURN>>) FutureProcessor.canceledFuture(new CouldNotPerformException("Could not call remote Message[" + methodName + "]", ex));
        }
    }

    public static String argumentToString(final Object argument) {
        if (argument == null) {
            return "Void";
        }

        final String rep = argument.toString();

        if (rep.length() > 10) {
            return argument.getClass().getSimpleName();
        }
        return rep;
    }
}
