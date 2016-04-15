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
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.extension.rsb.iface.RSBLocalServerInterface;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.patterns.Callback;

/**
 *
 * @author mpohling
 */
public class RPCHelper {

    static final Logger logger = LoggerFactory.getLogger(RPCHelper.class);

    public static <T> void registerInterface(final Class<T> interfaceClass, final T instance, final RSBLocalServerInterface server) throws CouldNotPerformException {
        final Logger logger = LoggerFactory.getLogger(instance.getClass());

        for (final Method method : interfaceClass.getMethods()) {
            logger.debug("Register Method[" + method.getName() + "] on Scope[" + server.getScope() + "].");
            server.addMethod(method.getName(), new Callback() {

                @Override
                public Event internalInvoke(final Event event) throws Throwable {
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

                        // Implementation of Future support by resolving result to reache inner future object.
//                        if (method.getReturnType().isAssignableFrom(Future.class)) {
                        if (result instanceof Future) {
                            result = ((Future) result).get();
                        }

                        if (result == null) {
                            payloadType = Void.class;
                        } else {
                            payloadType = result.getClass();
                        }
                        return new Event(payloadType, result);
                    } catch (Exception ex) {
                        throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Could not invoke Method[" + method.getReturnType().getClass().getSimpleName() + " " + method.getName() + "(" + eventDataToString(event) + ")]!", ex), logger);
                    }
                }
            });
        }
    }

    public static Future callRemoteMethod(final RSBRemoteService remote) throws CouldNotPerformException {
        return callRemoteMethod(null, remote, Object.class, 3);
    }

    public static Future callRemoteMethod(final Object argument, final RSBRemoteService remote) throws CouldNotPerformException {
        return callRemoteMethod(argument, remote, Object.class, 3);
    }

    public static <RETURN> Future<RETURN> callRemoteMethod(final RSBRemoteService remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return callRemoteMethod(null, remote, returnClass, 3);
    }

    public static <RETURN> Future<RETURN> callRemoteMethod(final Object argument, final RSBRemoteService remote, final Class<? extends RETURN> returnClass) throws CouldNotPerformException {
        return callRemoteMethod(argument, remote, returnClass, 3);
    }

    private static <RETURN> Future<RETURN> callRemoteMethod(final Object argument, final RSBRemoteService remote, final Class<? extends RETURN> returnClass, int methodStackDepth) throws CouldNotPerformException {

        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace == null) {
                throw new NotAvailableException("method stack");
            } else if (stackTrace.length == 0) {
                throw new InvalidStateException("Could not detect method stack!");
            }
            String methodName;
            try {
                methodName = stackTrace[methodStackDepth].getMethodName();
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not detect method name!");
            }
            return (Future<RETURN>) remote.callMethodAsync(methodName, argument);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call remote Message[]", ex);
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
