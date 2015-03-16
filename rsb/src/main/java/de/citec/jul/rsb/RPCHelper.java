/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb;

import de.citec.jul.exception.ExceptionPrinter;
import static de.citec.jul.rsb.RSBCommunicationService.RPC_REQUEST_STATUS;
import static de.citec.jul.rsb.RSBCommunicationService.RPC_SUCCESS;
import java.lang.reflect.Method;
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

    public static <T> void registerInterface(final Class<T> interfaceClass, final T instance, final LocalServer server) throws RSBException {
        final Logger logger = LoggerFactory.getLogger(instance.getClass());

        for (final Method methode : interfaceClass.getMethods()) {
            logger.info("Register Method[" + methode.getName() + "] on Scope[" + server.getScope() + "].");
            server.addMethod(methode.getName(), new Callback() {

                @Override
                public Event internalInvoke(Event event) throws Throwable {
                    try {
                        return new Event(methode.getReturnType(), methode.invoke(instance, event.getData()));
                    } catch (Exception ex) {
                        throw ExceptionPrinter.printHistory(logger, ex);
                    }
                }
            });
        }
    }
}
