package org.openbase.jul.extension.type.processing;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.type.communication.ScopeType;

import java.util.Collection;

public class ScopeProcessor {

    public static final String COMPONENT_SEPARATOR = "/";

    public static String generateStringRep(final ScopeType.Scope scope) throws CouldNotPerformException {
        try {
            if (scope == null) {
                throw new NotAvailableException("scope");
            }
            return generateStringRep(scope.getComponentList());
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not generate scope string representation!", ex);
        }
    }

    public static String generateStringRep(final Collection<String> components) throws CouldNotPerformException {
        try {
            String stringRep = COMPONENT_SEPARATOR;
            for (String component : components) {
                stringRep += component;
                stringRep += COMPONENT_SEPARATOR;
            }
            return stringRep;
        } catch (RuntimeException ex) {
            throw new CouldNotPerformException("Could not generate scope string representation!", ex);
        }
    }

    public static ScopeType.Scope generateScope(final String label, final String type, final ScopeType.Scope locationScope) throws CouldNotPerformException {
        try {
            ScopeType.Scope.Builder newScope = ScopeType.Scope.newBuilder();
            newScope.addAllComponent(locationScope.getComponentList());
            newScope.addComponent(convertIntoValidScopeComponent(type));
            newScope.addComponent(convertIntoValidScopeComponent(label));

            return newScope.build();
        } catch (NullPointerException ex) {
            throw new CouldNotPerformException("Coult not generate scope!", ex);
        }
    }

    public static ScopeType.Scope generateScope(final String scope) throws CouldNotPerformException {
        ScopeType.Scope.Builder generatedScope = ScopeType.Scope.newBuilder();
        for (String component : scope.split("/")) {

            // check for empty components (/a//b/ = /a/b/)
            if (component.isEmpty()) {
                continue;
            }
            generatedScope.addComponent(convertIntoValidScopeComponent(component));
        }
        return generatedScope.build();
    }

    public static String convertIntoValidScopeComponent(String scopeComponent) {
        return StringProcessor.transformToIdString(scopeComponent.toLowerCase()).replaceAll("_", "");
    }

    public static String generateStringRepWithDelimiter(final ScopeType.Scope scope, final String delimiter) throws CouldNotPerformException {

        if (scope == null) {
            throw new NotAvailableException("scope");
        }

        String stringRep = "";

        boolean firstEntry = true;
        for (String component : scope.getComponentList()) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                stringRep += delimiter;
            }
            stringRep += component;
        }
        return stringRep;
    }
}
