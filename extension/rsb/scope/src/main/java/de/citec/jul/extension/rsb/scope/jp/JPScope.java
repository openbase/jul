/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.scope.jp;

import org.dc.jps.core.AbstractJavaProperty;
import java.util.List;
import rsb.Scope;

/**
 *
 * @author mpohling
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
