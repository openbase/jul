package org.openbase.jul.storage.registry.plugin;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import com.google.protobuf.AbstractMessage;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.storage.registry.ProtoBufRegistry;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 */
public abstract class ProtobufRegistryPluginAdapter<KEY, M extends AbstractMessage, MB extends M.Builder<MB>> extends FileRegistryPluginAdapter<KEY, IdentifiableMessage<KEY,M,MB>, ProtoBufRegistry<KEY, M, MB>> {
}
