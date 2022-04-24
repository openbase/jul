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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.openbase.rct.Transform;
import org.openbase.rct.TransformerException;
import org.openbase.rct.impl.TransformCache.TimeAndFrameID;
import org.openbase.rct.impl.TransformRequest.FutureTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformerCoreDefault implements TransformerCore {

    private enum WalkEnding {

        Identity, TargetParentOfSource, SourceParentOfTarget, FullPath,
    }

    private interface TransformAccum {

        int gather(TransformCache cache, long time);

        void accum(boolean source);

        void finalize(WalkEnding end, long time);
    }

    private class TransformAccumDummy implements TransformAccum {

        @Override
        public int gather(TransformCache cache, long time) {
            return cache.getParent(time);
        }

        @Override
        public void accum(boolean source) {
        }

        @Override
        public void finalize(WalkEnding end, long time) {
        }
    }

    private class TransformAccumImpl implements TransformAccum {

        private final TransformInternal st = new TransformInternal();
        private long time = 0;
        private final Quat4d sourceToTopQuat = new Quat4d(0.0, 0.0, 0.0, 1.0);
        private Vector3d sourceToTopVec = new Vector3d(0, 0, 0);
        private final Quat4d targetToTopQuat = new Quat4d(0, 0, 0, 1);
        private Vector3d targetToTopVec = new Vector3d(0, 0, 0);
        private Quat4d resultQuat = new Quat4d(0.0, 0.0, 0.0, 1.0);
        private Vector3d resultVec = new Vector3d(0, 0, 0);

        public TransformAccumImpl() {
        }

        public Vector3d quatRotate(Quat4d r, Vector3d v) {
            Matrix3d rotMat = new Matrix3d();
            rotMat.set(r);
            Vector3d result = new Vector3d();
            rotMat.transform(v, result);
            return result;
        }

        @Override
        public int gather(TransformCache cache, long time) {
            if (!cache.getData(time, st)) {
                return 0;
            }
            return st.frame_id;
        }

        @Override
        public void accum(boolean source) {
            if (source) {
                sourceToTopVec = quatRotate(st.rotation, sourceToTopVec);
                sourceToTopVec.add(st.translation);
                sourceToTopQuat.mul(st.rotation, sourceToTopQuat);
            } else {
                targetToTopVec = quatRotate(st.rotation, targetToTopVec);
                targetToTopVec.add(st.translation);
                targetToTopQuat.mul(st.rotation, targetToTopQuat);
            }
        }

        @Override
        public void finalize(WalkEnding end, long time) {
            switch (end) {
                case Identity:
                    break;
                case TargetParentOfSource:
                    resultVec = sourceToTopVec;
                    resultQuat = sourceToTopQuat;
                    break;
                case SourceParentOfTarget: {
                    Quat4d invTargetQuat = new Quat4d(targetToTopQuat);
                    invTargetQuat.inverse();
                    Vector3d targetToTopvecNeg = new Vector3d(targetToTopVec);
                    targetToTopvecNeg.negate();
                    Vector3d invTargetVec = quatRotate(invTargetQuat, targetToTopvecNeg);
                    resultVec = invTargetVec;
                    resultQuat = invTargetQuat;
                    break;
                }
                case FullPath: {
                    Quat4d invTargetQuat = new Quat4d(targetToTopQuat);
                    invTargetQuat.inverse();
                    Vector3d targetToTopVecNeg = new Vector3d(targetToTopVec);
                    targetToTopVecNeg.negate();
                    Vector3d invTargetVec = quatRotate(invTargetQuat, targetToTopVecNeg);
                    resultVec = quatRotate(invTargetQuat, sourceToTopVec);
                    resultVec.add(invTargetVec);
                    resultQuat.mul(invTargetQuat, sourceToTopQuat);
                }
                break;
            }
            this.time = time;
        }
    }

    private static final int MAX_GRAPH_DEPTH = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerCoreDefault.class);

    private final Object transformationFrameMapLock = new Object() {
        @Override
        public String toString() {
            return "TransformationFrameMapLock";
        }
    };

    private final Map<String, Integer> frameIds = new HashMap<>();
    private final List<TransformCache> frames = new LinkedList<>();
    private final List<String> frameIdsReverse = new LinkedList<>();
    private final Map<Integer, String> frameAuthority = new HashMap<>();
    private final long cacheTime;
    private final Set<TransformRequest> requests = new HashSet<>();
    private final List<TimeAndFrameID> lctCache = new LinkedList<>();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public TransformerCoreDefault(long cacheTime) {
        this.cacheTime = cacheTime;
        synchronized (transformationFrameMapLock) {
            frameIds.put("NO_PARENT", 0);
            frames.add(new TransformCacheNull());
            frameIdsReverse.add("NO_PARENT");
            transformationFrameMapLock.notifyAll();
        }
    }

    @Override
    public void clear() {
        synchronized (transformationFrameMapLock) {
            if (frames.size() > 1) {
                for (TransformCache f : frames) {
                    if (f.isValid()) {
                        f.clearList();
                    }
                }
                transformationFrameMapLock.notifyAll();
            }
        }
    }

    @Override
    public boolean setTransform(Transform transform, boolean isStatic) throws TransformerException {

        // prepare data
        String authority = transform.getAuthority();
        String frameChild = transform.getFrameChild().replace("/", "").trim();
        String frameParent = transform.getFrameParent().replace("/", "").trim();
        Quat4d quat = transform.getRotationQuat();
        Vector3d vec = transform.getTranslation();
        Transform stripped = new Transform(transform);
        stripped.setFrameChild(frameChild);
        stripped.setFrameParent(frameParent);

        // check input data validity
        if (frameChild.equals(frameParent)) {
            LOGGER.error("Frames for parent and child are the same: " + frameChild);
            throw new TransformerException("Frames for parent and child are the same: " + frameChild);
        }
        if (frameChild.isEmpty()) {
            LOGGER.error("Child frame is empty");
            throw new TransformerException("Child frame is empty");
        }
        if (frameParent.isEmpty()) {
            LOGGER.error("Parent frame is empty");
            throw new TransformerException("Parent frame is empty");
        }
        if (Double.isNaN(quat.w) || Double.isNaN(quat.x) || Double.isNaN(quat.y) || Double.isNaN(quat.z)
                || Double.isNaN(vec.x) || Double.isNaN(vec.y) || Double.isNaN(vec.z)) {
            LOGGER.error("Transform contains nan: " + transform);
            throw new TransformerException("Transform contains nan: " + transform);
        }

        // perform the insertion
        synchronized (transformationFrameMapLock) {
            LOGGER.debug("lookup child frame number");
            int frameNumberChild = lookupOrInsertFrameNumber(frameChild);
            LOGGER.debug("get frame \"" + frameNumberChild + "\"");
            TransformCache frame = getFrame(frameNumberChild);

            if (!frame.isValid()) {
                LOGGER.debug("allocate frame " + frameNumberChild);
                frame = allocateFrame(frameNumberChild, isStatic);
            }

            LOGGER.debug("lookup parent frame number");
            int frameNumberParent = lookupOrInsertFrameNumber(stripped.getFrameParent());
            LOGGER.debug("insert transform " + frameNumberParent + " -> " + frameNumberChild + " to " + frame);
            if (frame.insertData(new TransformInternal(stripped, frameNumberParent, frameNumberChild))) {
                LOGGER.debug("transform inserted. Add authority.");
                frameAuthority.put(frameNumberChild, authority);
            } else {
                LOGGER.warn("TF_OLD_DATA ignoring data from the past for frame "
                        + stripped.getFrameChild()
                        + " at time "
                        + stripped.getTime()
                        + " according to authority "
                        + authority
                        + "\nPossible reasons are listed at http://wiki.ros.org/tf/Errors%%20explained");
                return false;
            }
            LOGGER.debug("trigger check requests.");
            executor.execute(this::checkRequests);
            LOGGER.debug("set transform done");
        }
        return true;
    }

    private int lookupOrInsertFrameNumber(String frameId) {
        int retval = 0;
        synchronized (transformationFrameMapLock) {
            if (!frameIds.containsKey(frameId)) {
                LOGGER.debug("frame id is not known for string \"" + frameId + "\"");
                retval = frames.size();
                LOGGER.debug("add null transform to cache");
                frames.add(new TransformCacheNull());
                LOGGER.debug("generated mapping \"" + frameId + "\" -> " + retval + " (and reverse)");
                frameIds.put(frameId, retval);
                frameIdsReverse.add(frameId);
                transformationFrameMapLock.notifyAll();
            } else {
                retval = frameIds.get(frameId);
                LOGGER.debug("known mapping \"" + frameId + "\" -> " + retval);
            }
        }

        return retval;
    }

    private TransformCache getFrame(int frameId) {
        // / @todo check larger values too
        synchronized (transformationFrameMapLock) {
            if (frameId == 0 || frameId > frames.size()) {
                return null;
            } else {
                return frames.get(frameId);
            }
        }
    }

    private TransformCache allocateFrame(int cfid, boolean isStatic) {
        synchronized (transformationFrameMapLock) {
            if (isStatic) {
                frames.set(cfid, new TransformCacheStatic());
            } else {
                frames.set(cfid, new TransformCacheImpl(cacheTime));
            }

            return frames.get(cfid);
        }
    }

    @Override
    public Transform lookupTransform(String targetFrame, String sourceFrame, long time) throws TransformerException {
        synchronized (transformationFrameMapLock) {
            try {
                if (targetFrame.equals(sourceFrame)) {

                    long newTime;
                    if (time == 0) {
                        int targetId = lookupFrameNumber(targetFrame);
                        TransformCache cache = getFrame(targetId);
                        if (cache.isValid()) {
                            newTime = cache.getLatestTimestamp();
                        } else {
                            newTime = time;
                        }
                    } else {
                        newTime = time;
                    }

                    return new Transform(new Transform3D(), targetFrame, sourceFrame, newTime);
                }

                return lookupTransformNoLock(targetFrame, sourceFrame, time);
            } catch (TransformerException ex) {
                throw new TransformerException("Could not lookup transformation", ex);
            }
        }
    }

    private Transform lookupTransformNoLock(String targetFrame, String sourceFrame, long time) throws TransformerException {

        // Identify case does not need to be validated above
        int targetId = validateFrameId("lookupTransform argument target_frame", targetFrame);
        int sourceId = validateFrameId("lookupTransform argument source_frame", sourceFrame);

        final TransformAccumImpl accum = new TransformAccumImpl();
        try {
            walkToTopParent(accum, time, targetId, sourceId);
        } catch (TransformerException ex) {
            throw new TransformerException("No matching transform found", ex);
        }

        final Transform3D t3d = new Transform3D(accum.resultQuat, accum.resultVec, 1.0);
        return new Transform(t3d, targetFrame, sourceFrame, accum.time);
    }

    private void walkToTopParent(TransformAccum f, long time, int targetId, int sourceId) throws TransformerException {
        // Short circuit if zero length transform to allow lookups on non
        // existent links
        if (sourceId == targetId) {
            f.finalize(WalkEnding.Identity, time);
            return;
        }
        // If getting the latest get the latest common time
        if (time == 0) {
            time = getLatestCommonTime(targetId, sourceId);
        }

        // Walk the tree to its root from the source frame, accumulating the
        // transform
        int frame = sourceId;
        int topParent = frame;
        int depth = 0;

        boolean extrapolationMightHaveOccurred = false;
        while (frame != 0) {
            TransformCache cache = getFrame(frame);

            if (!cache.isValid()) {
                // There will be no cache for the very root of the tree
                topParent = frame;
                break;
            }

            int parent = f.gather(cache, time);
            if (parent == 0) {
                // Just break out here... there may still be a path from source
                // -> target
                topParent = frame;
                extrapolationMightHaveOccurred = true;
                break;
            }

            // Early out... target frame is a direct parent of the source frame
            if (frame == targetId) {
                f.finalize(WalkEnding.TargetParentOfSource, time);
                return;
            }

            f.accum(true);

            topParent = frame;
            frame = parent;

            ++depth;
            if (depth > MAX_GRAPH_DEPTH) {
                throw new TransformerException("The tf tree is invalid because it contains a loop.");
            }
        }

        // Now walk to the top parent from the target frame, accumulating its
        // transform
        frame = targetId;
        depth = 0;
        while (frame != topParent) {
            TransformCache cache = getFrame(frame);

            if (!cache.isValid()) {
                throw new TransformerException("Invalid cache when looking up transform from frame [" + lookupFrameString(sourceId) + "] to frame [" + lookupFrameString(targetId) + "]");
            }

            int parent = f.gather(cache, time);
            if (parent == 0) {
                throw new TransformerException("when looking up transform from frame [" + lookupFrameString(sourceId) + "] to frame [" + lookupFrameString(targetId) + "]");
            }

            // Early out... source frame is a direct parent of the target frame
            if (frame == sourceId) {
                f.finalize(WalkEnding.SourceParentOfTarget, time);
                return;
            }

            f.accum(false);
            frame = parent;
            ++depth;
            if (depth > MAX_GRAPH_DEPTH) {
                throw new TransformerException("The tf tree is invalid because it contains a loop." + allFramesAsStringNoLock());
            }
        }

        if (frame != topParent) {
            if (extrapolationMightHaveOccurred) {
                throw new TransformerException(", when looking up transform from frame [" + lookupFrameString(sourceId) + "] to frame [" + lookupFrameString(targetId) + "]");
            }
        }
        f.finalize(WalkEnding.FullPath, time);
    }

    private long getLatestCommonTime(int targetId, int sourceId)
            throws TransformerException {
        if (sourceId == targetId) {
            TransformCache cache = getFrame(sourceId);
            // Set time to latest timestamp of frameid in case of target and
            // source frame id are the same
            if (cache.isValid()) {
                return cache.getLatestTimestamp();
            } else {
                return 0;
            }
        }

        lctCache.clear();

        // Walk the tree to its root from the source frame, accumulating the
        // list of parent/time as well as the latest time
        // in the target is a direct parent
        int frame = sourceId;
        int depth = 0;
        long commonTime = Long.MAX_VALUE;
        while (frame != 0) {
            TransformCache cache = getFrame(frame);

            if (!cache.isValid()) {
                // There will be no cache for the very root of the tree
                break;
            }

            TimeAndFrameID latest = cache.getLatestTimeAndParent();

            if (latest.frameID == 0) {
                // Just break out here... there may still be a path from source
                // -> target
                break;
            }

            if (latest.time != 0) {
                commonTime = Math.min(latest.time, commonTime);
            }

            lctCache.add(latest);

            frame = latest.frameID;

            // Early out... target frame is a direct parent of the source frame
            if (frame == targetId) {
                long time = commonTime;
                if (time == Long.MAX_VALUE) {
                    time = 0;
                }
                return time;
            }

            ++depth;
            if (depth > MAX_GRAPH_DEPTH) {
                throw new TransformerException("The tf tree is invalid because it contains a loop." + allFramesAsStringNoLock());
            }
        }

        // Now walk to the top parent from the target frame, accumulating the
        // latest time and looking for a common parent
        frame = targetId;
        depth = 0;
        commonTime = Long.MAX_VALUE;
        int commonParent = 0;
        while (true) {
            TransformCache cache = getFrame(frame);

            if (!cache.isValid()) {
                break;
            }

            TimeAndFrameID latest = cache.getLatestTimeAndParent();

            if (latest.frameID == 0) {
                break;
            }

            if (latest.time != 0) {
                commonTime = Math.min(latest.time, commonTime);
            }

            boolean found = false;
            for (TimeAndFrameID t : lctCache) {
                if (t.frameID == latest.frameID) {
                    found = true;
                    break;
                }
            }
            if (found) { // found a common parent
                commonParent = latest.frameID;
                break;
            }

            frame = latest.frameID;

            // Early out... source frame is a direct parent of the target frame
            if (frame == sourceId) {
                long time = commonTime;
                if (time == Long.MAX_VALUE) {
                    time = 0;
                }
                return time;
            }

            ++depth;
            if (depth > MAX_GRAPH_DEPTH) {
                throw new TransformerException("The tf tree is invalid because it contains a loop." + allFramesAsStringNoLock());
            }
        }

        if (commonParent == 0) {
            throw new TransformerException(
                    "Could not find a connection between '"
                    + lookupFrameString(targetId) + "' and '"
                    + lookupFrameString(sourceId)
                    + "' because they are not part of the same tree."
                    + "Tf has two or more unconnected trees.");
        }

        // Loop through the source -> root list until we hit the common parent
        for (TimeAndFrameID it : lctCache) {
            if (it.time != 0) {
                commonTime = Math.min(commonTime, it.time);
            }

            if (it.frameID == commonParent) {
                break;
            }
        }

        if (commonTime == Long.MAX_VALUE) {
            commonTime = 0;
        }

        return commonTime;
    }

    private String lookupFrameString(int frameId) throws TransformerException {
        synchronized (transformationFrameMapLock) {
            if (frameId >= frameIdsReverse.size()) {
                throw new TransformerException("Reverse lookup of frame id " + frameId + " failed!");
            } else {
                return frameIdsReverse.get(frameId);
            }
        }
    }

    private int lookupFrameNumber(String frameId) throws TransformerException {
        synchronized (transformationFrameMapLock) {
            if (!frameIds.containsKey(frameId)) {
                throw new TransformerException("FrameId[" + frameId + "]");
            }
            return frameIds.get(frameId);
        }
    }

    private int validateFrameId(String functionNameArg, String frameId) throws TransformerException {
        if (frameId.isEmpty()) {
            throw new TransformerException("Invalid argument passed to " + functionNameArg + " in tf2 frameIds cannot be empty");
        }

        if (frameId.startsWith("/")) {
            throw new TransformerException("Invalid argument \"" + frameId + "\" passed to " + functionNameArg + " in tf2 frame_ids cannot start with a '/' like: ");
        }

        try {
            return lookupFrameNumber(frameId);
        } catch (TransformerException ex) {
            throw new TransformerException("\"" + frameId + "\" passed to " + functionNameArg + " does not exist. ", ex);
        }
    }

    @Override
    public Transform lookupTransform(String targetFrame, long targetTime, String sourceFrame, long sourceTime, String fixedFrame) throws TransformerException {
        validateFrameId("lookupTransform argument target_frame", targetFrame);
        validateFrameId("lookupTransform argument source_frame", sourceFrame);
        validateFrameId("lookupTransform argument fixed_frame", fixedFrame);

        Transform temp1 = lookupTransform(fixedFrame, sourceFrame, sourceTime);
        Transform temp2 = lookupTransform(targetFrame, fixedFrame, targetTime);

        Transform3D t = new Transform3D();
        t.mul(temp2.getTransform(), temp1.getTransform());

        return new Transform(t, targetFrame, sourceFrame, temp2.getTime());
    }

    @Override
    public Future<Transform> requestTransform(final String targetFrame, final String sourceFrame, long time) {
        final FutureTransform future = new FutureTransform();
        synchronized (transformationFrameMapLock) {
            if (canTransform(targetFrame, sourceFrame, time)) {
                try {
                    future.set(lookupTransformNoLock(targetFrame, sourceFrame, time));
                } catch (TransformerException ex) {
                    LOGGER.warn("Transformation from [" + sourceFrame + "] to [" + targetFrame + "] failed!" + ex.getMessage());
                }
            }
            requests.add(new TransformRequest(targetFrame, sourceFrame, time, future));
            return future;
        }
    }

    /**
     * Method blocks until new transformation updates are available.
     * @throws InterruptedException is thrown if the current thread is externally interrupted.
     */
    public void waitForTransformationUpdates(long timeout) throws InterruptedException {
        synchronized (transformationFrameMapLock) {
            transformationFrameMapLock.wait(timeout);
        }
    }

    /**
     * Method blocks until new transformation updates are available.
     * @throws InterruptedException is thrown if the current thread is externally interrupted.
     */
    public void waitForTransformationUpdates() throws InterruptedException {
        synchronized (transformationFrameMapLock) {
            transformationFrameMapLock.wait();
        }
    }

    private void checkRequests() {
        // go through all request and check if they can be answered
        synchronized (transformationFrameMapLock) {
            for (final TransformRequest request : new ArrayList<>(requests)) {
                try {
                    // request can be answered. publish the transform through
                    // the future object and remove the request.
                    request.future.set(lookupTransformNoLock(request.target_frame, request.source_frame, request.time));
                    requests.remove(request);
                } catch (TransformerException ex) {
                    LOGGER.debug("Request:" + request.source_frame + " -> " + request.target_frame + " still not available");
                    // expected, just proceed
                }
            }
        }
    }

    @Override
    public boolean canTransform(String targetFrame, String sourceFrame, long time) {
        if (targetFrame.equals(sourceFrame)) {
            return true;
        }

        if (warnFrameId("canTransform argument target_frame", targetFrame)) {
            return false;
        }
        if (warnFrameId("canTransform argument source_frame", sourceFrame)) {
            return false;
        }

        synchronized (transformationFrameMapLock) {
            try {
                int targetId = lookupFrameNumber(targetFrame);
                int sourceId = lookupFrameNumber(sourceFrame);
                return canTransformNoLock(targetId, sourceId, time);
            } catch (TransformerException ex) {
                return false;
            }
        }
    }

    @Override
    public boolean canTransform(String targetFrame, long targetTime, String sourceFrame, long sourceTime, String fixedFrame) {
        if (warnFrameId("canTransform argument target_frame", targetFrame)) {
            return false;
        }
        if (warnFrameId("canTransform argument source_frame", sourceFrame)) {
            return false;
        }
        if (warnFrameId("canTransform argument fixed_frame", fixedFrame)) {
            return false;
        }

        return canTransform(targetFrame, fixedFrame, targetTime) && canTransform(fixedFrame, sourceFrame, sourceTime);
    }

    private boolean warnFrameId(String functionNameArg, String frameId) {
        if (frameId.length() == 0) {
            LOGGER.warn("Invalid argument passed to " + functionNameArg + " in tf2 frame_ids cannot be empty");
            return true;
        }

        if (frameId.startsWith("/")) {
            LOGGER.warn("Invalid argument \"" + frameId + "\" passed to " + functionNameArg + " in tf2 frame_ids cannot start with a '/' like: ");
            return true;
        }

        return false;
    }

    @Override
    public Set<String> getFrameStrings() {
        synchronized (transformationFrameMapLock) {
            Set<String> vec = new HashSet<>();
            for (int counter = 1; counter < frameIdsReverse.size(); counter++) {
                vec.add(frameIdsReverse.get(counter));
            }
            return vec;
        }
    }

    @Override
    public boolean frameExists(String frameId) {
        synchronized (transformationFrameMapLock) {
            return frameIds.containsKey(frameId);
        }
    }

    @Override
    public String getParent(String frameId, long time) throws TransformerException {
        synchronized (transformationFrameMapLock) {
            try {
                int frameNumber = lookupFrameNumber(frameId);
                TransformCache frame = getFrame(frameNumber);

                if (!frame.isValid()) {
                    return "";
                }

                int parentId = frame.getParent(time);
                if (parentId == 0) {
                    return "";
                }

                return lookupFrameString(parentId);
            } catch (TransformerException ex) {
                throw new TransformerException("Could not resolfe parent transformation!", ex);
            }
        }
    }

    @Override
    public String allFramesAsDot() {
        String mstream = "";
        mstream += "digraph G {\n";
        synchronized (transformationFrameMapLock) {

            TransformInternal temp = new TransformInternal();

            if (frames.size() == 1) {
                mstream += "\"no tf data recieved\"";
            }

            // one referenced for 0 is no frame
            for (int counter = 1; counter < frames.size(); counter++) {
                int frameId;
                TransformCache counter_frame = getFrame(counter);
                if (!counter_frame.isValid()) {
                    continue;
                }
                if (!counter_frame.getData(0, temp)) {
                    continue;
                } else {
                    frameId = temp.frame_id;
                }
                String authority = "no recorded authority";
                if (frameAuthority.containsKey(counter)) {
                    authority = frameAuthority.get(counter);
                }

                double rate = counter_frame.getListLength()
                        / Math.max(
                                (counter_frame.getLatestTimestamp() / 1000.0 - counter_frame
                                .getOldestTimestamp() / 1000.0), 0.0001);

                mstream += "\""
                        + frameIdsReverse.get(frameId)
                        + "\" -> \""
                        + frameIdsReverse.get(counter)
                        + "\"[label=\"Broadcaster: "
                        + authority
                        + "\\nAverage rate: "
                        + rate
                        + " Hz\\nMost recent transform: "
                        + (counter_frame.getLatestTimestamp())
                        / 1000.0
                        + " \\nBuffer length: "
                        + (counter_frame.getLatestTimestamp() - counter_frame
                        .getOldestTimestamp()) / 1000.0 + " sec\\n"
                        + "\"];\n";
            }

            // one referenced for 0 is no frame
            for (int counter = 1; counter < frames.size(); counter++) {
                int frameId;
                TransformCache counter_frame = getFrame(counter);
                if (!counter_frame.isValid()) {
                    continue;
                }
                if (counter_frame.getData(0, temp)) {
                    frameId = temp.frame_id;
                } else {
                    frameId = 0;
                }

                if (frameIdsReverse.get(frameId).equals("NO_PARENT")) {
                    mstream += "edge [style=invis];\n";
                    mstream += " subgraph cluster_legend { style=bold; color=black; label =\"view_frames Result\";\n"
                            + "}->\"" + frameIdsReverse.get(counter) + "\";\n";
                }
            }
            mstream += "}";
            return mstream;
        }
    }

    @Override
    public String allFramesAsYAML() {
        String mstream = "";
        synchronized (transformationFrameMapLock) {

            TransformInternal temp = new TransformInternal();

            if (frames.size() == 1) {
                mstream += "[]";
            }

            // for (std::vector< TimeCache*>::iterator it = frames_.begin(); it
            // != frames_.end(); ++it)
            for (int counter = 1; counter < frames.size(); counter++) {
                // one referenced for 0 is no frame
                int cfid = counter;
                int frameId;
                TransformCache cache = getFrame(cfid);
                if (!cache.isValid()) {
                    continue;
                }

                if (!cache.getData(0, temp)) {
                    continue;
                }

                frameId = temp.frame_id;

                String authority = "no recorded authority";
                if (frameAuthority.containsKey(cfid)) {
                    authority = frameAuthority.get(cfid);
                }

                double rate = cache.getListLength()
                        / Math.max((cache.getLatestTimestamp() / 1000.0 - cache
                                .getOldestTimestamp() / 1000.0), 0.0001);

                mstream += frameIdsReverse.get(cfid) + ": \n";
                mstream += "  parent: '" + frameIdsReverse.get(frameId)
                        + "'\n";
                mstream += "  broadcaster: '" + authority + "'\n";
                mstream += "  rate: " + rate + "\n";
                mstream += "  most_recent_transform: "
                        + (cache.getLatestTimestamp()) / 1000.0 + "\n";
                mstream += "  oldest_transform: "
                        + (cache.getOldestTimestamp()) / 1000.0 + "\n";
                mstream += "  buffer_length: "
                        + (cache.getLatestTimestamp() - cache
                        .getOldestTimestamp()) / 1000.0 + "\n";
            }

            return mstream;
        }
    }

    @Override
    public String allFramesAsString() {
        synchronized (transformationFrameMapLock) {
            return allFramesAsStringNoLock();
        }
    }

    private boolean canTransformNoLock(int targetId, int sourceId, long time) {
        if (targetId == 0 || sourceId == 0) {
            return false;
        }

        if (targetId == sourceId) {
            return true;
        }

        TransformAccumDummy accum = new TransformAccumDummy();
        try {
            walkToTopParent(accum, time, targetId, sourceId);
        } catch (TransformerException ex) {
            return false;
        }
        return true;

    }

    private String allFramesAsStringNoLock() {

        TransformInternal temp = new TransformInternal();
        String mstring = "";
        synchronized (transformationFrameMapLock) {
            // /regular transforms
            LOGGER.debug("frames size: " + frames.size());
            for (int counter = 1; counter < frames.size(); counter++) {
                TransformCache frame_ptr = getFrame(counter);
                LOGGER.debug("got frame: " + frame_ptr);
                if (!frame_ptr.isValid()) {
                    continue;
                }
                int frame_id_num = 0;
                if (frame_ptr.getData(0, temp)) {
                    LOGGER.debug("got frame transform: " + temp);
                    frame_id_num = temp.frame_id;
                }
                mstring += "Frame " + frameIdsReverse.get(counter) + " exists with parent " + frameIdsReverse.get(frame_id_num) + ".\n";
            }
        }
        return mstring;
    }

    @Override
    public void newTransformAvailable(Transform transform, boolean isStatic) {
        try {
            setTransform(transform, isStatic);
        } catch (TransformerException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
