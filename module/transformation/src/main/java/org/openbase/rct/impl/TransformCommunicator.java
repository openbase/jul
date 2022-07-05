package org.openbase.rct.impl;

/*-
 * #%L
 * RCT
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

import java.util.Set;

import org.openbase.rct.Transform;
import org.openbase.rct.TransformType;
import org.openbase.rct.TransformerConfig;
import org.openbase.rct.TransformerException;

public interface TransformCommunicator {

    void init(TransformerConfig conf) throws TransformerException;

    /**
     * Shutdown the transform communicator
     */
    void shutdown();

    /**
     *
     * @param transform
     * @param type
     * @throws TransformerException
     */
    void sendTransform(Transform transform, TransformType type) throws TransformerException;

    /**
     *
     * @param transforms
     * @param type
     * @throws TransformerException
     */
    void sendTransform(Set<Transform> transforms, TransformType type) throws TransformerException;

    void addTransformListener(TransformListener listener);

    void addTransformListener(Set<TransformListener> listeners);

    void removeTransformListener(TransformListener listener);

    String getAuthority();
}
