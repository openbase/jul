package org.openbase.rct;

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

import org.openbase.rct.impl.TransformCommunicator;

/**
 * This is the central class for publishing transforms. Use
 * {@link TransformerFactory} to create an instance of this. Any instance should
 * exist as long as any publishing is planned.
 *
 * @author lziegler
 *
 */
public class TransformPublisher {

    private TransformCommunicator comm;
    private TransformerConfig conf;

    /**
     * Creates a new transformer. Attention: This should not be called by the
     * user, use {@link TransformerFactory} in order to create a transformer.
     *
     * @param comm The communicator implementation
     * @param conf The configuration
     */
    public TransformPublisher(TransformCommunicator comm, TransformerConfig conf) {
        this.conf = conf;
        this.comm = comm;
    }

    /**
     * Add transform information to the rct data structure.
     * @param transform  The transform to store.
     * @param transformType Defines if this transformation is static or dynamically updated over time. (This cannot be changed after the first call.)
     * @throws TransformerException
     */
    public void sendTransform(Transform transform, TransformType transformType) throws TransformerException {
        comm.sendTransform(transform, transformType);
    }

    /**
     * Add transform information to the rct data structure.
     * @param transforms The transformations to store.
     * @param transformType Defines if this transformation is static or dynamically updated over time. (This cannot be changed after the first call.)
     * @throws TransformerException
     */
    public void sendTransform(Set<Transform> transforms, TransformType transformType) throws TransformerException {
        comm.sendTransform(transforms, transformType);
    }

    public TransformerConfig getConfig() {
        return conf;
    }

    public String getAuthorityID() {
        return comm.getAuthority();
    }

    /**
     * Shutdown the transform communicator
     */
    public void shutdown() {
        comm.shutdown();
    }

}
