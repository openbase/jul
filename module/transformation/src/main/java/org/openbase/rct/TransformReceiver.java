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

import java.util.concurrent.Future;

import org.openbase.rct.impl.TransformCommunicator;
import org.openbase.rct.impl.TransformerCore;

/**
 * This is the central class for receiving transforms. Use
 * {@link TransformerFactory} to create an instance of this. Any instance should
 * exist as long as any receiving action is planned, because it
 * caches the known coordinate frames tree including the defined history.
 * Creation of the frame tree creates overhead and should not be done on a
 * regular basis. Instead the transformer should exist for a longer period while
 * updating itself when changes to the frame tree occur.
 *
 * @author lziegler
 *
 */
public class TransformReceiver {

    private final TransformerCore core;
    private final TransformerConfig conf;

    @SuppressWarnings("unused")
    private final TransformCommunicator comm;

    /**
     * Creates a new transformer. Attention: This should not be called by the
     * user, use {@link TransformerFactory} in order to create a transformer.
     *
     * @param core The core functionality implementation
     * @param comm The communicator implementation
     * @param conf The configuration
     */
    public TransformReceiver(TransformerCore core, TransformCommunicator comm, TransformerConfig conf) {
        this.core = core;
        this.conf = conf;
        this.comm = comm;
    }

    /**
     * Get the transform between two frames by node ID.
     * @param targetFrame The frame to which data should be transformed
     * @param sourceFrame The frame where the data originated
     * @param time he time at which the value of the transform is desired. (0 will get the latest)
     * @return The transform between the frames
     * @throws TransformerException
     *
     */
    public Transform lookupTransform(String targetFrame, String sourceFrame, long time) throws TransformerException {
        return core.lookupTransform(targetFrame, sourceFrame, time);
    }

    /**
     * Get the transform between two frames by node ID assuming fixed frame.
     * @param targetFrame The frame to which data should be transformed
     * @param targetTime The time to which the data should be transformed. (0 will get the latest)
     * @param sourceFrame The frame where the data originated
     * @param sourceTime The time at which the sourceFrame should be evaluated. (0 will get the latest)
     * @param fixedFrame The frame in which to assume the transform is constant in
     * time.
     * @return The transform between the frames
     * @throws TransformerException
     */
    public Transform lookupTransform(String targetFrame, long targetTime, String sourceFrame, long sourceTime, String fixedFrame) throws TransformerException {
        return core.lookupTransform(targetFrame, targetTime, sourceFrame, sourceTime, fixedFrame);
    }

    /**
     * Request the transform between two frames by node ID.
     * @param targetFrame The frame to which data should be transformed
     * @param sourceFrame The frame where the data originated
     * @param time The time at which the value of the transform is desired. (0 will get the latest)
     * @return A future object representing the request status and transform
     * between the frames
     */
    public Future<Transform> requestTransform(String targetFrame, String sourceFrame, long time) {
        return core.requestTransform(targetFrame, sourceFrame, time);
    }

    /**
     * Test if a transform is possible
     * @param targetFrame The frame into which to transform
     * @param sourceFrame frame from which to transform
     * @param time The time at which to transform
     * @return True if the transform is possible, false otherwise
     */
    public boolean canTransform(String targetFrame, String sourceFrame, long time) {
        return core.canTransform(targetFrame, sourceFrame, time);
    }

    /**
     * Test if a transform is possible
     * @param targetFrame The frame into which to transform
     * @param targetTime The time into which to transform
     * @param sourceFrame The frame from which to transform
     * @param sourceTime The time from which to transform
     * @param fixedFrame The frame in which to treat the transform as ant in time
     * @return True if the transform is possible, false otherwise
     */
    public boolean canTransform(String targetFrame, long targetTime, String sourceFrame, long sourceTime, String fixedFrame) {
        return core.canTransform(targetFrame, targetTime, sourceFrame, sourceTime, fixedFrame);
    }

    public TransformerConfig getConfig() {
        return conf;
    }

    /**
     * Shutdown the transform communicator
     */
    public void shutdown() {
        comm.shutdown();
    }
}
