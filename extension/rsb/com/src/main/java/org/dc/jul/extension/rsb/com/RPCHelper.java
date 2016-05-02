/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * $Id:$
 * $HeadURL:$
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.extension.rsb.iface.RSBLocalServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.patterns.Callback;

/**
 *
 * @author mpohling
 */
public class RPCHelper {

//    static final Logger logger = LoggerFactory.getLogger(RPCHelper.class);


    public static <I, T extends I> void registerInterface(final Class<I> interfaceClass, final T instance, final RSBLocalServerInterface server) throws CouldNotPerformException {
        final Logger logger = LoggerFactory.getLogger(instance.getClass());

        for (final Method method : interfaceClass.getMethods()) {
            logger.debug("Register Method[" + method.getName() + "] on Scope[" + server.getScope() + "].");
            server.addMethod(method.getName(), new Callback() {

                @Override
                public Event internalInvoke(final Event event) throws UserCodeException {
                    try {
                        if (event == null) {
                            throw new NotAvailableException("event");
                        }

                        Object result;
                        Class<?> payloadType;
//
                        if (event.getData() == null) {
                            result = method.invoke(instance);
                        } else {
                            result = method.invoke(instance, event.getData());
                        }

//                        if (method.getReturnType().isAssignableFrom(Future.class)) {
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
                    } catch (CouldNotPerformException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ExecutionException ex) {
                        throw ExceptionPrinter.printHistoryAndReturnThrowable(new UserCodeException(new CouldNotPerformException("Could not invoke Method[" + method.getReturnType().getClass().getSimpleName() + " " + method.getName() + "(" + eventDataToString(event) + ")]!", ex)), logger);
                    }
                    return new Event(Void.class);
                }
            });
        }
    }

    public static Future<Object> callRemoteMethod(final RSBRemoteService remote) throws CouldNotPerformException {
        return callRemoteMethod(null, remote, Object.class, 3);
    }

    public static Future<Object> callRemoteMethod(final Object argument, final RSBRemoteService remote) throws CouldNotPerformException {
        return callRemoteMethod(argument, remote, Object.class, 3);
    }

    public static <RETURN> Future<RETURN> callRemoteMethod(final RSBRemoteService remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return callRemoteMethod(null, remote, returnClass, 3);
    }

    public static <RETURN> Future<RETURN> callRemoteMethod(final Object argument, final RSBRemoteService remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return callRemoteMethod(argument, remote, returnClass, 3);
    }

    private static <RETURN> Future<RETURN> callRemoteMethod(final Object argument, final RSBRemoteService remote, final Class<? extends RETURN> returnClass, int methodStackDepth) throws CouldNotPerformException {

        String methodName = "?";
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace == null) {
                throw new NotAvailableException("method stack");
            } else if (stackTrace.length == 0) {
                throw new InvalidStateException("Could not detect method stack!");
            }

            try {
                methodName = stackTrace[methodStackDepth].getMethodName();
            } catch (NullPointerException | IndexOutOfBoundsException ex) {
                throw new CouldNotPerformException("Could not detect method name!");
            }
            return remote.callMethodAsync(methodName, argument);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Message[" + methodName + "]", ex);
        }
    }

    private static String eventDataToString(Event event) {
        if (event.getData() == null) {
            return "Void";
        }
        String rep = event.getData().toString();
        if (rep.length() > 10) {
            return event.getData().getClass().getSimpleName();
        }
        return rep;
    }
}
