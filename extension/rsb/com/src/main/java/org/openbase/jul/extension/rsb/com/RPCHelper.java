package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.annotation.RPCMethod;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.com.jp.JPRSBLegacyMode;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.extension.rsb.iface.RSBRemoteServer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.patterns.Callback.UserCodeException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RPCHelper {

    private static final Map<String, Integer> methodCountMap = new HashMap<>();

    public static final long RPC_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

    public static final String USER_TIME_KEY = "USER_NANO_TIME";
    public static final long USER_TIME_VALUE_INVALID = -1;
    //    static final Logger logger = LoggerFactory.getLogger(RPCHelper.class);
    private static final String INTERNAL_CALL_REMOTE_METHOD_NAME = "internalCallRemoteMethod";

    //TODO: remove or enable for debugging
//    public static SyncObject syncObject = new SyncObject("MapSync");

    public static <I, T extends I> void registerInterface(final Class<I> interfaceClass, final T instance, final RSBLocalServer server) throws CouldNotPerformException {
        for (final Method method : interfaceClass.getMethods()) {
            if (method.getAnnotation(RPCMethod.class) != null) {
                boolean legacy = false;
                try {
                    legacy = JPService.getProperty(JPRSBLegacyMode.class).getValue();
                } catch (JPNotAvailableException e) {
                    // if not available just register legacy methods
                }
                // if legacy register always, else only register if not marked as legacy
                if (legacy || !method.getAnnotation(RPCMethod.class).legacy()) {
                    registerMethod(method, instance, server);
                }
            }
        }
    }

//    private static int test = 1;
//
//    private static void printMap() {
//        int sum = 0;
//        System.out.println("Registered methods");
//        for (Entry<String, Integer> stringIntegerEntry : methodCountMap.entrySet()) {
//            sum += stringIntegerEntry.getValue();
//            if (stringIntegerEntry.getValue() < 2) {
//                continue;
//            }
//            System.out.println(stringIntegerEntry.getKey() + ": " + stringIntegerEntry.getValue());
//        }
//        System.out.println("Overall " + sum + " methods");
//    }

    public static <I, T extends I> void registerMethod(final Method method, final T instance, final RSBLocalServer server) throws CouldNotPerformException {
//        synchronized (syncObject) {
//            if (!methodCountMap.containsKey(method.getName())) {
//                methodCountMap.put(method.getName(), 0);
//            }
//            methodCountMap.put(method.getName(), methodCountMap.get(method.getName()) + 1);
//
//            int sum = 0;
//            for (Integer value : methodCountMap.values()) {
//                sum += value;
//            }
//
//            if (sum > (500 * test)) {
//                test++;
//                printMap();
//            }
//
//        }
        final Logger logger = LoggerFactory.getLogger(instance.getClass());
        logger.debug("Register Method[" + method.getName() + "] on Scope[" + server.getScope() + "].");
        try {
            server.addMethod(method.getName(), event -> {
                try {
                    if (event == null) {
                        throw new NotAvailableException("event");
                    }

                    Object result;
                    Class<?> payloadType;
                    Future<Object> resultFuture = null;

                    try {
                        // Encapsulate invocation to detect method invocation stall via timeout
                        //TODO: please check via benchmark if this causes into a performance issue compared to the direct invocation. Related to openbase/jul#46 Validate performance of method invocation encapsulation
                        resultFuture = GlobalCachedExecutorService.submit(() -> {
                            if (event.getData() == null) {
                                return method.invoke(instance);
                            } else {
                                return method.invoke(instance, event.getData());
                            }
                        });
                        result = resultFuture.get(RPC_TIMEOUT, TimeUnit.MILLISECONDS);

                        // Implementation of Future support by resolving result to reach inner future object.
                        if (result instanceof Future) {
                            try {
                                result = ((Future) result).get(RPC_TIMEOUT, TimeUnit.MILLISECONDS);
                            } catch (final TimeoutException ex) {
                                ((Future) result).cancel(true);
                                throw ex;
                            }
                        }
                    } catch (final TimeoutException ex) {
                        if (resultFuture != null && !resultFuture.isDone()) {
                            resultFuture.cancel(true);
                        }
                        throw new CouldNotPerformException("Remote task was canceled!", ex);
                    }

                    if (result == null) {
                        payloadType = Void.class;
                    } else {
                        payloadType = result.getClass();
                    }
                    Event returnEvent = new Event(payloadType, result);
                    returnEvent.getMetaData().setUserTime(USER_TIME_KEY, System.nanoTime());
                    return returnEvent;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (CouldNotPerformException | IllegalArgumentException | ExecutionException | CancellationException | RejectedExecutionException ex) {
                    final CouldNotPerformException exx = new CouldNotPerformException("Could not invoke Method[" + method.getReturnType().getClass().getSimpleName() + " " + method.getName() + "(" + eventDataToArgumentString(event) + ")] of " + instance + "!", ex);
                    ExceptionPrinter.printHistoryAndReturnThrowable(exx, logger);
                    throw new UserCodeException(exx);
                }
                return new Event(Void.class);
            });
        } catch (CouldNotPerformException ex) {
            if (ex.getCause() instanceof InvalidStateException) {
                // method was already register
                ExceptionPrinter.printHistory("Skip Method[" + method.getName() + "] registration on Scope[" + server.getScope() + "] of " + instance + " because message was already registered!", ex, logger, LogLevel.DEBUG);
            } else {
                throw new CouldNotPerformException("Could not register Method[" + method.getName() + "] on Scope[" + server.getScope() + "] of " + instance + "!", ex);
            }
        }
    }

    // todo release: remove throws CouldNotPerformException because return type is a future which can be canceled.
    public static Future<Object> callRemoteMethod(final RSBRemote remote) throws CouldNotPerformException {
        return internalCallRemoteMethod(null, remote, Object.class);
    }

    // todo release: remove throws CouldNotPerformException because return type is a future which can be canceled.
    public static Future<Object> callRemoteMethod(final Object argument, final RSBRemote remote) throws CouldNotPerformException {
        return internalCallRemoteMethod(argument, remote, Object.class);
    }

    // todo release: remove throws CouldNotPerformException because return type is a future which can be canceled.
    public static <RETURN> Future<RETURN> callRemoteMethod(final RSBRemote remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return internalCallRemoteMethod(null, remote, returnClass);
    }

    // todo release: remove throws CouldNotPerformException because return type is a future which can be canceled.
    public static <RETURN> Future<RETURN> callRemoteMethod(final Object argument, final RSBRemote remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return internalCallRemoteMethod(argument, remote, returnClass);
    }

    // todo release: remove throws CouldNotPerformException because return type is a future which can be canceled.
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

    // todo release: remove throws CouldNotPerformException because return type is a future which can be canceled.
    public static Future<Object> callRemoteServerMethod(final RSBRemoteServer remote) throws CouldNotPerformException {
        return internalCallRemoteMethod(null, remote, Object.class);
    }

    // todo release: remove throws CouldNotPerformException because return type is a future which can be canceled.
    public static Future<Object> callRemoteServerMethod(final Object argument, final RSBRemoteServer remote) throws CouldNotPerformException {
        return internalCallRemoteMethod(argument, remote, Object.class);
    }
    // todo release: remove throws CouldNotPerformException because return type is a future which can be canceled.
    public static <RETURN> Future<RETURN> callRemoteServerMethod(final RSBRemoteServer remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return internalCallRemoteMethod(null, remote, returnClass);
    }
    // todo release: remove throws CouldNotPerformException because return type is a future which can be canceled.
    public static <RETURN> Future<RETURN> callRemoteServerMethod(final Object argument, final RSBRemoteServer remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return internalCallRemoteMethod(argument, remote, returnClass);
    }
    // todo release: remove throws CouldNotPerformException because return type is a future which can be canceled.
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
