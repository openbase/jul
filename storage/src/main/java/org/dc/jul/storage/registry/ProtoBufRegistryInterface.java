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

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import java.util.List;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public interface ProtoBufRegistryInterface<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> extends FileSynchronizedRegistryInterface<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufRegistryInterface<KEY, M, MB>> {

    public M register(final M entry) throws CouldNotPerformException ;
    
    public boolean contains(final M key) throws CouldNotPerformException ;

    public M update(final M entry) throws CouldNotPerformException ;

    public M remove(final M entry) throws CouldNotPerformException ;

    public M getMessage(final KEY key) throws CouldNotPerformException;

    public List<M> getMessages() throws CouldNotPerformException;

    public MB getBuilder(final KEY key) throws CouldNotPerformException;
    
//    public IdGenerator<KEY, M> getIdGenerator();
}
