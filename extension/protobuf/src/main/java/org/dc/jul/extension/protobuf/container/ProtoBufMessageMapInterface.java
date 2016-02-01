/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.protobuf.container;

/*
 * #%L
 * JUL Extension Protobuf
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

import com.google.protobuf.Descriptors;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.extension.protobuf.BuilderSyncSetup;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public interface ProtoBufMessageMapInterface<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>> extends Map<KEY, IdentifiableMessage<KEY, M, MB>> {
    
    public IdentifiableMessage<KEY, M, MB> put(final IdentifiableMessage<KEY, M, MB> value) throws CouldNotPerformException;
    
    public IdentifiableMessage<KEY, M, MB> get(final KEY key) throws CouldNotPerformException;
    
    public IdentifiableMessage<KEY, M, MB> get(final IdentifiableMessage<KEY, M, MB> value) throws CouldNotPerformException;

    public List<M> getMessages() throws CouldNotPerformException;
    
    public M getMessage(KEY key) throws CouldNotPerformException;
}
