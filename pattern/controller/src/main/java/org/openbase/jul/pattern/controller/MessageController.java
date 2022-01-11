package org.openbase.jul.pattern.controller;

/*-
 * #%L
 * JUL Pattern Controller
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
import org.openbase.jul.extension.protobuf.iface.DataBuilderProvider;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 * @param <M> Message
 * @param <MB> Builder
 */
public interface MessageController<M extends AbstractMessage, MB extends M.Builder<MB>> extends Controller<M>, DataBuilderProvider<M, MB> {

}
