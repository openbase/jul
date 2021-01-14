package org.openbase.jul.extension.type.processing;

/*-
 * #%L
 * JUL Extension Type Processing
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

                // merge to components in case they are connected by an empty one
                if(component.isEmpty()) {
                    continue;
                }

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
