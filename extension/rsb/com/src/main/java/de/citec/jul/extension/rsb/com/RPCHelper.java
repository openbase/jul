/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.extension.rsb.iface.RSBLocalServerInterface;
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
            logger.info("Register Method[" + method.getName() + "] on Scope[" + server.getScope() + "].");
            server.addMethod(method.getName(), new Callback() {

                @Override
                public Event internalInvoke(final Event event) throws Throwable {
                    try {
                        if (event == null) {
                            throw new NotAvailableException("event");
                        }
                        
                        Object result;
                        
                        if (event.getData() == null) {
                            result = method.invoke(instance);
                        } else {
                            result = method.invoke(instance, event.getData());
                        }

                        // Implementation of Future support by resolving result.
                        if(result instanceof Future) {
                            result = ((Future) result).get();
                        }
                        
                        return new Event(result.getClass(), result);
                        
                    } catch (Exception ex) {
                        throw ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Could not invoke Method[" + method.getName() + "(" + eventDataToString(event) + ")]!", ex));
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
            return "";
        }
        return event.getData().toString();
    }
}
