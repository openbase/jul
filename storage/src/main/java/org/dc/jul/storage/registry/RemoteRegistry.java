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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.NotSupportedException;
import org.dc.jul.extension.protobuf.IdGenerator;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import org.dc.jul.storage.registry.plugin.RemoteRegistryPlugin;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <M>
 * @param <MB>
 * @param <SIB>
 */
public class RemoteRegistry<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>, SIB extends GeneratedMessage.Builder> extends AbstractRegistry<KEY, IdentifiableMessage<KEY, M, MB>, Map<KEY, IdentifiableMessage<KEY, M, MB>>, ProtoBufRegistryInterface<KEY, M, MB>, RemoteRegistryPlugin<KEY, IdentifiableMessage<KEY, M, MB>>> implements ProtoBufRegistryInterface<KEY, M, MB> {

	private final IdGenerator<KEY, M> idGenerator;

	public RemoteRegistry(final IdGenerator<KEY, M> idGenerator) throws InstantiationException, InterruptedException {
		this(idGenerator, new HashMap<KEY, IdentifiableMessage<KEY, M, MB>>());
	}

	public RemoteRegistry(final IdGenerator<KEY, M> idGenerator, final Map<KEY, IdentifiableMessage<KEY, M, MB>> internalMap) throws InstantiationException, InterruptedException {
		super(internalMap);
		this.idGenerator = idGenerator;
	}

	public synchronized void notifyRegistryUpdated(final Collection<M> values) throws CouldNotPerformException {
		Map<KEY, IdentifiableMessage<KEY, M, MB>> newRegistryMap = new HashMap<>();
		for (M value : values) {
			IdentifiableMessage<KEY, M, MB> data = new IdentifiableMessage<>(value, idGenerator);
			newRegistryMap.put(data.getId(), data);
		}
		replaceInternalMap(newRegistryMap);
	}

    public KEY getKey(final M entry) throws CouldNotPerformException {
        return idGenerator.generateId(entry);
    }

	@Override
	public M getMessage(final KEY key) throws CouldNotPerformException {
		return get(key).getMessage();
	}

	@Override
	public MB getBuilder(final KEY key) throws CouldNotPerformException {
		return (MB) getMessage(key).toBuilder();
	}

	@Override
	public M register(final M entry) throws CouldNotPerformException {
		throw new NotSupportedException("register", this, "Operation not permitted!");
	}

	@Override
	public M update(final M entry) throws CouldNotPerformException {
		throw new NotSupportedException("update", this, "Operation not permitted!");
	}

	@Override
	public M remove(final M entry) throws CouldNotPerformException {
		throw new NotSupportedException("remove", this, "Operation not permitted!");
	}

	@Override
	public boolean contains(final M key) throws CouldNotPerformException {
		return super.contains(new IdentifiableMessage<>(key, idGenerator).getId());
	}

	@Override
	public void loadRegistry() throws CouldNotPerformException {
		throw new NotSupportedException("loadRegistry", this, "Operation not permitted!");
	}

	@Override
	public void saveRegistry() throws CouldNotPerformException {
		throw new NotSupportedException("saveRegistry", this, "Operation not permitted!");
	}

	@Override
	public IdGenerator<KEY, M> getIdGenerator() {
		return idGenerator;
	}

    @Override
    public List<M> getMessages() throws CouldNotPerformException {
        List<M> messageList = new ArrayList<>();
        for(IdentifiableMessage<KEY, M, MB> messageContainer : getEntries()) {
            messageList.add(messageContainer.getMessage());
        }
        return messageList;
    }

    @Override
    public Integer getDBVersion() throws NotAvailableException {
        //TODO mpohling: implement!
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
