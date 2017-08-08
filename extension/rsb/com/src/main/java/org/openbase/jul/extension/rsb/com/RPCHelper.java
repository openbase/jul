package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import org.openbase.jul.iface.annotations.RPCMethod;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.patterns.Callback;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.extension.rsb.iface.RSBRemoteServer;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RPCHelper {

//    static final Logger logger = LoggerFactory.getLogger(RPCHelper.class);
    private static final String INTERNAL_CALL_REMOTE_METHOD_NAME = "internalCallRemoteMethod";

    public static <I, T extends I> void registerInterface(final Class<I> interfaceClass, final T instance, final RSBLocalServer server) throws CouldNotPerformException {
        for (final Method method : interfaceClass.getMethods()) {
            if (method.getAnnotation(RPCMethod.class) != null) {
                registerMethod(method, instance, server);
            }
        }
    }

    public static <I, T extends I> void registerMethod(final Method method, final T instance, final RSBLocalServer server) throws CouldNotPerformException {
        final Logger logger = LoggerFactory.getLogger(instance.getClass());
        logger.debug("Register Method[" + method.getName() + "] on Scope[" + server.getScope() + "].");
        try {
            server.addMethod(method.getName(), new Callback() {

                @Override
                public Event internalInvoke(final Event event) throws Callback.UserCodeException {
                    try {
                        if (event == null) {
                            throw new NotAvailableException("event");
                        }

                        Object result;
                        Class<?> payloadType;

                        if (event.getData() == null) {
                            result = method.invoke(instance);
                        } else {
                            result = method.invoke(instance, event.getData());
                        }

                        // Implementation of Future support by resolving result to reache inner future object.
                        if (result instanceof Future) {
                            result = ((Future) result).get();
                        }

                        if (result == null) {
                            payloadType = Void.class;
                        } else {
                            payloadType = result.getClass();
                        }
                        return new Event(payloadType, result);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } catch (CouldNotPerformException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ExecutionException | CancellationException ex) {
                        throw ExceptionPrinter.printHistoryAndReturnThrowable(new Callback.UserCodeException(new CouldNotPerformException("Could not invoke Method[" + method.getReturnType().getClass().getSimpleName() + " " + method.getName() + "(" + eventDataToArgumentString(event) + ")]!", ex)), logger);
                    }
                    return new Event(Void.class);
                }
            });
        } catch (CouldNotPerformException ex) {
            if(ex.getCause() instanceof InvalidStateException) {
                logger.warn("Method["+method.getName()+"] regstration failed because it is already registered");
            } else {
                throw ex;
            }
        }
    }

    public static Future<Object> callRemoteMethod(final RSBRemote remote) throws CouldNotPerformException {
        return internalCallRemoteMethod(null, remote, Object.class);
    }

    public static Future<Object> callRemoteMethod(final Object argument, final RSBRemote remote) throws CouldNotPerformException {
        return internalCallRemoteMethod(argument, remote, Object.class);
    }

    public static <RETURN> Future<RETURN> callRemoteMethod(final RSBRemote remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return internalCallRemoteMethod(null, remote, returnClass);
    }

    public static <RETURN> Future<RETURN> callRemoteMethod(final Object argument, final RSBRemote remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return internalCallRemoteMethod(argument, remote, returnClass);
    }

    private static <RETURN> Future<RETURN> internalCallRemoteMethod(final Object argument, final RSBRemote remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {

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
            return remote.callMethodAsync(methodName, argument);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Message[" + methodName + "]", ex);
        }
    }

    public static Future<Object> callRemoteServerMethod(final RSBRemoteServer remote) throws CouldNotPerformException {
        return internalCallRemoteMethod(null, remote, Object.class);
    }

    public static Future<Object> callRemoteServerMethod(final Object argument, final RSBRemoteServer remote) throws CouldNotPerformException {
        return internalCallRemoteMethod(argument, remote, Object.class);
    }

    public static <RETURN> Future<RETURN> callRemoteServerMethod(final RSBRemoteServer remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return internalCallRemoteMethod(null, remote, returnClass);
    }

    public static <RETURN> Future<RETURN> callRemoteServerMethod(final Object argument, final RSBRemoteServer remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return internalCallRemoteMethod(argument, remote, returnClass);
    }

    private static <RETURN> Future<RETURN> internalCallRemoteMethod(final Object argument, final RSBRemoteServer remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {

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
            return remote.callAsync(methodName, argument);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Message[" + methodName + "]", ex);
        }
    }

    public static String eventDataToArgumentString(final Event event) {
        if (event == null) {
            return "Void";
        }
        return argumentToString(event.getData());

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
