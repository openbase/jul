package org.openbase.jul.extension.rsb.scope.jp;

/*
 * #%L
 * JUL Extension RSB Scope
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import java.util.List;
import org.openbase.jps.core.JPService;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import rsb.Scope;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPScope extends AbstractJavaProperty<Scope> {

	public final static String[] COMMAND_IDENTIFIERS = {"-s", "--scope"};
    
	public JPScope() {
		super(COMMAND_IDENTIFIERS);
	}
    
	public JPScope(String[] commandIdentifiers) {
        super(commandIdentifiers);
    }
    
    @Override
    protected Scope getPropertyDefaultValue() {
        if(JPService.testMode()) {
            String user = ScopeGenerator.convertIntoValidScopeComponent(System.getProperty("user.name"));
            return new Scope("/preset/"+user);
        }
        return new Scope("/");
    }

    @Override
    protected Scope parse(List<String> list) throws Exception {
        return new Scope(getOneArgumentResult());
    }
    
    @Override
	public String getDescription() {
		return "Setup the application scope which is used for the rsb communication.";
    }

    @Override
    protected String[] generateArgumentIdentifiers() {
        String[] args = {"SCOPE"};
        return args;
    }
}
