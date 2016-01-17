/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.processing;

import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.MultiException;
import org.dc.jul.exception.NotAvailableException;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public final class VariableProcessor {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VariableProcessor.class);

    private VariableProcessor() {

    }

    public static String resolveVariables(String context, final boolean throwOnError, final Collection<VariableProvider> providers) throws MultiException {
        VariableProvider[] providerArray = new VariableProvider[providers.size()];
        return resolveVariables(context, throwOnError, providers.toArray(providerArray));
    }

    public static String resolveVariables(String context, final boolean throwOnError, final VariableProvider... providers) throws MultiException {
        String variableIdentifier, variableValue;
        MultiException.ExceptionStack exceptionStack = null;
        while (!Thread.interrupted()) {

            // Detect variables
            variableIdentifier = StringUtils.substringBetween(context, VariableProvider.VARIABLE_INITIATOR, VariableProvider.VARIABLE_TERMINATOR);
            if (variableIdentifier == null) {
                // Context does not contain any variables.
                break;
            }

            // Resolve detected variable.
            variableValue = "";
            for (VariableProvider provider : providers) {
                try {
                    variableValue = provider.getValue(variableIdentifier);
                    if (variableValue == null) {
                        continue;
                    }
                    logger.info("Variable[" + variableIdentifier + "] = Value[" + variableValue + "] resolved by Provider[" + provider.getName() + "].");
                    break;
                } catch (NotAvailableException ex) {
                    continue;
                }
            }

            // check if variable was resolved
            if (variableValue == null || variableValue.isEmpty()) {
                exceptionStack = MultiException.push(VariableProcessor.class, new NotAvailableException("Variable[" + variableIdentifier + "]"), exceptionStack);
                variableValue = "";
            }

            // Replace detected variable by it's value in the given context.
            context = StringUtils.replace(context, VariableProvider.VARIABLE_INITIATOR + variableIdentifier + VariableProvider.VARIABLE_TERMINATOR, variableValue);
        }

        try {
            MultiException.checkAndThrow("Could not resolve all variables!", exceptionStack);
        } catch (MultiException ex) {
            if (throwOnError) {
                throw ex;
            } else {
                ExceptionPrinter.printHistory(ex, logger);
            }
        }
        return context;
    }
    
    public static String resolveVariable(final String variable, final Collection<VariableProvider> providers) throws MultiException {
        VariableProvider[] providerArray = new VariableProvider[providers.size()];
        return resolveVariable(variable, providers.toArray(providerArray));
    }

    public static String resolveVariable(final String variable, final VariableProvider... providers) throws MultiException {
        MultiException.ExceptionStack exceptionStack = null;
        for (VariableProvider provider : providers) {

            try {
                return provider.getValue(variable);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(VariableProcessor.class, ex, exceptionStack);
                continue;
            }
        }
        MultiException.checkAndThrow("Could not resolve Variable[" + variable + "]!", exceptionStack);
        throw new AssertionError("Fatal error during variable resolving.");
    }
}
