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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.RSBException;
import rsb.patterns.Callback;
import rsb.patterns.LocalServer;

/**
 *
 * @author mpohling
 */
public class RPCHelper {

    public static <T> void registerInterface(final Class<T> interfaceClass, final T instance, final RSBLocalServerInterface server) throws CouldNotPerformException {
        final Logger logger = LoggerFactory.getLogger(instance.getClass());

        for (final Method methode : interfaceClass.getMethods()) {
            logger.info("Register Method[" + methode.getName() + "] on Scope[" + server.getScope() + "].");
            server.addMethod(methode.getName(), new Callback() {

                @Override
                public Event internalInvoke(final Event event) throws Throwable {
                    try {
                        if (event == null) {
                            throw new NotAvailableException("event");
                        }

                        Class<?> returnType = methode.getReturnType();

                        if (returnType.isAssignableFrom(Future.class)) {
                            returnType = Void.class;
                        }

                        return new Event(returnType, methode.invoke(instance, event.getData()));
                    } catch (Exception ex) {
                        throw ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Could not invoke Method[" + methode.getName() + "(" + event.getData() + ")]!", ex));
                    }
                }
            });
        }
    }

    public static <T> void registerInterface(final Class<T> interfaceClass, final T instance, final LocalServer server) throws RSBException {
        final Logger logger = LoggerFactory.getLogger(instance.getClass());

        for (final Method methode : interfaceClass.getMethods()) {
            logger.info("Register Method[" + methode.getName() + "] on Scope[" + server.getScope() + "].");
            server.addMethod(methode.getName(), new Callback() {

                @Override
                public Event internalInvoke(final Event event) throws Throwable {
                    try {
                        if (event == null) {
                            throw new NotAvailableException("event");
                        }
                        return new Event(methode.getReturnType(), methode.invoke(instance, event.getData()));
                    } catch (Exception ex) {
                        throw ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Could not invoke Method[" + methode.getName() + "(" + event.getData() + ")]!", ex));
                    }
                }
            });
        }
    }

    public static <RETURN> Future<RETURN> callRemoteMethod(final Object argument, final Class<? extends RETURN> returnClass, final RSBRemoteService remote) throws CouldNotPerformException {
        return (Future<RETURN>) callRemoteMethod(argument, remote);
    }
    
    public static Future callRemoteMethod(final Object argument, final RSBRemoteService remote) throws CouldNotPerformException {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace == null) {
                throw new NotAvailableException("method stack");
            } else if (stackTrace.length == 0) {
                throw new InvalidStateException("Could not detect method stack!");
            }
            String methodName;
            try {
                methodName = stackTrace[2].getMethodName();
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not detect method name!");
            }
            System.out.println("Call " + stackTrace[2].getMethodName());
            return remote.callMethodAsync(methodName, argument);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call remote Message[]", ex);
        }
    }
}
