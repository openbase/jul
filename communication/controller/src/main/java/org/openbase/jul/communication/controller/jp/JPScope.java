package org.openbase.jul.communication.controller.jp;

/*-
 * #%L
 * JUL Extension Controller
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

import org.openbase.jps.core.AbstractJavaProperty;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.type.processing.ScopeProcessor;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.type.communication.ScopeType.Scope;

import java.util.List;

public class JPScope extends AbstractJavaProperty<Scope> {

    public final static String[] COMMAND_IDENTIFIERS = {"-s", "--scope"};

    public JPScope() {
        super(COMMAND_IDENTIFIERS);
    }

    public JPScope(String[] commandIdentifiers) {
        super(commandIdentifiers);
    }

    @Override
    protected Scope getPropertyDefaultValue() throws JPNotAvailableException {
        if (JPService.testMode()) {
            String user = ScopeProcessor.convertIntoValidScopeComponent(System.getProperty("user.name"));
            return ScopeProcessor.generateScope("/test/" + user);
        }
        return ScopeProcessor.generateScope("/");
    }

    @Override
    protected Scope parse(List<String> list) throws Exception {
        return ScopeProcessor.generateScope(getOneArgumentResult());
    }

    @Override
    public String getDescription() {
        return "Setup the application scope which is used for the communication.";
    }

    @Override
    protected String[] generateArgumentIdentifiers() {
        String[] args = {"SCOPE"};
        return args;
    }

    @Override
    public String getDefaultExample() {
        try {
            return this.propertyIdentifiers[0] + " " + ScopeProcessor.generateStringRep(getDefaultValue());
        } catch (JPNotAvailableException | CouldNotPerformException ex) {
            return this.propertyIdentifiers[0];
        }
    }

    public static String convertIntoValidScopeComponent(String scopeComponent) {
        return StringProcessor.transformToIdString(scopeComponent.toLowerCase()).replaceAll("_", "");
    }
}
