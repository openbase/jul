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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.openbase.rct.Transform;
import org.openbase.rct.TransformerException;

public interface TransformerCore extends TransformListener {

    /**
     * Clear all data
     */
    void clear();

    /**
     * Add transform information to the rct data structure
     *
     * @param transforms The transforms to store
     * @param isStatic Record this transform as a static transform. It will be good across all time. (This cannot be changed after the first call.)
     * @return True unless an error occured
     * @throws TransformerException
     */
    boolean setTransform(List<Transform> transforms, boolean isStatic) throws TransformerException;

    /**
     * Get the transform between two frames by node ID.
     *
     * @param targetFrame The frame to which data should be transformed
     * @param sourceFrame The frame where the data originated
     * @param time The time at which the value of the transform is desired. (0 will get the latest)
     * @return The transform between the frames
     * @throws TransformerException is thrown if the transformation is not available.
     *
     */
    Transform lookupTransform(String targetFrame, String sourceFrame, long time) throws TransformerException;

    /**
     * Get the transform between two frames by node ID assuming fixed frame.
     *
     * @param targetFrame The frame to which data should be transformed
     * @param targetTime The time to which the data should be transformed. (0 will get the latest)
     * @param sourceFrame The frame where the data originated
     * @param sourceTime The time at which the sourceFrame should be evaluated. (0 will get the latest)
     * @param fixedFrame The frame in which to assume the transform is ant in time.
     * @return The transform between the frames
     * @throws TransformerException is thrown if the transformation is not available.
     *
     */
    Transform lookupTransform(String targetFrame, long targetTime, String sourceFrame, long sourceTime, String fixedFrame) throws TransformerException;

    /**
     * Request the transform between two frames by node ID.
     *
     * @param targetFrame The frame to which data should be transformed
     * @param sourceFrame The frame where the data originated
     * @param time The time at which the value of the transform is desired. (0 will get the latest)
     * @return A future object representing the request status and transform between the frames
     *
     */
    Future<Transform> requestTransform(String targetFrame, String sourceFrame, long time);

    /**
     * Test if a transform is possible
     *
     * @param targetFrame The frame into which to transform
     * @param sourceFrame The frame from which to transform
     * @param time The time at which to transform
     * @return True if the transform is possible, false otherwise
     */
    boolean canTransform(String targetFrame, String sourceFrame, long time);

    /**
     * Test if a transform is possible
     *
     * @param targetFrame The frame into which to transform
     * @param targetTime The time into which to transform
     * @param sourceFrame The frame from which to transform
     * @param sourceTime The time from which to transform
     * @param fixedFrame The frame in which to treat the transform as ant in time
     * @return True if the transform is possible, false otherwise
     */
    boolean canTransform(String targetFrame, long targetTime, String sourceFrame, long sourceTime, String fixedFrame);

    /**
     * A way to get a set of available node IDs
     *
     * @return
     */
    Set<String> getFrameStrings();

    /**
     * Check if a frame exists in the tree
     * @param frameId The node ID in question
     * @return if the frame with the id exists
     */
    boolean frameExists(String frameId);

    /**
     * Returns the parent of a frame.
     * @param time the timestamp used for the lookup.
     * @param frameId The node ID of the frame in question
     * @throws TransformerException
     * @return the id of the parent
     */
    String getParent(String frameId, long time) throws TransformerException;

    /**
     * Backwards compatability. A way to see what frames have been cached Useful for debugging
     *
     * @return
     */
    String allFramesAsDot();

    /**
     * A way to see what frames have been cached in yaml format Useful for debugging tools
     *
     * @return
     */
    String allFramesAsYAML();

    /**
     * A way to see what frames have been cached Useful for debugging
     *
     * @return
     */
    String allFramesAsString();
}
