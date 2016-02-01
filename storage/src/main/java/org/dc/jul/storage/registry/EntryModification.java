/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

/**
 *
 * @author mpohling
 */
public class EntryModification extends Exception {

    private final Object entry;
    private ConsistencyHandler consistencyHandler;

    public EntryModification(final Object entry, final ConsistencyHandler consistencyHandler) {
        super(entry + " has been modified by "+consistencyHandler.getClass().getSimpleName()+".");
        this.entry = entry;
        this.consistencyHandler = consistencyHandler;
    }

    public Object getEntry() {
        return entry;
    }

    public ConsistencyHandler getConsistencyHandler() {
        return consistencyHandler;
    }
}
