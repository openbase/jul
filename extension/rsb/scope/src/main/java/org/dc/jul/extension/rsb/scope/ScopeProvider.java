/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.scope;

import org.dc.jul.exception.CouldNotPerformException;
import rsb.Scope;

/**
 *
 * @author mpohling
 */
public interface ScopeProvider {

    public Scope getScope() throws CouldNotPerformException;
}
