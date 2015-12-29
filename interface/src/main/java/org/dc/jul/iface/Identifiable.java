/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.iface;

import org.dc.jul.exception.CouldNotPerformException;

/**
 *
 * @author Divine Threepwood
 * @param <ID>
 */
public interface Identifiable<ID> {

    public String FIELD_ID = "id";

    public ID getId() throws CouldNotPerformException;
}