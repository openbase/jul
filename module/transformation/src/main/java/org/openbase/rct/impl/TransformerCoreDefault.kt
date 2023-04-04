package org.openbase.rct.impl

import org.openbase.rct.Transform
import org.openbase.rct.TransformerException
import org.openbase.rct.impl.TransformCache.TimeAndFrameID
import org.openbase.rct.impl.TransformRequest.FutureTransform
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.media.j3d.Transform3D
import javax.vecmath.Matrix3d
import javax.vecmath.Quat4d
import javax.vecmath.Vector3d
import kotlin.concurrent.withLock

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
 */   class TransformerCoreDefault(private val cacheTime: Long) : TransformerCore {
    private enum class WalkEnding {
        Identity, TargetParentOfSource, SourceParentOfTarget, FullPath
    }

    private interface TransformAccum {
        fun gather(cache: TransformCache?, time: Long): Int
        fun accum(source: Boolean)
        fun finalize(end: WalkEnding, time: Long)
    }

    private inner class TransformAccumDummy : TransformAccum {
        override fun gather(cache: TransformCache?, time: Long): Int {
            return cache!!.getParent(time)
        }

        override fun accum(source: Boolean) {}
        override fun finalize(end: WalkEnding, time: Long) {}
    }

    private inner class TransformAccumImpl : TransformAccum {
        private val st = TransformInternal()
        var time: Long = 0
        private val sourceToTopQuat = Quat4d(0.0, 0.0, 0.0, 1.0)
        private var sourceToTopVec = Vector3d(0.0, 0.0, 0.0)
        private val targetToTopQuat = Quat4d(0.0, 0.0, 0.0, 1.0)
        private var targetToTopVec = Vector3d(0.0, 0.0, 0.0)
        var resultQuat = Quat4d(0.0, 0.0, 0.0, 1.0)
        var resultVec = Vector3d(0.0, 0.0, 0.0)
        fun quatRotate(r: Quat4d?, v: Vector3d?): Vector3d {
            val rotMat = Matrix3d()
            rotMat.set(r)
            val result = Vector3d()
            rotMat.transform(v, result)
            return result
        }

        override fun gather(cache: TransformCache?, time: Long): Int {
            return if (!cache!!.getData(time, st)) {
                0
            } else st.frame_id
        }

        override fun accum(source: Boolean) {
            if (source) {
                sourceToTopVec = quatRotate(st.rotation, sourceToTopVec)
                sourceToTopVec.add(st.translation)
                sourceToTopQuat.mul(st.rotation, sourceToTopQuat)
            } else {
                targetToTopVec = quatRotate(st.rotation, targetToTopVec)
                targetToTopVec.add(st.translation)
                targetToTopQuat.mul(st.rotation, targetToTopQuat)
            }
        }

        override fun finalize(end: WalkEnding, time: Long) {
            when (end) {
                WalkEnding.Identity -> {}
                WalkEnding.TargetParentOfSource -> {
                    resultVec = sourceToTopVec
                    resultQuat = sourceToTopQuat
                }

                WalkEnding.SourceParentOfTarget -> {
                    val invTargetQuat = Quat4d(targetToTopQuat)
                    invTargetQuat.inverse()
                    val targetToTopvecNeg = Vector3d(targetToTopVec)
                    targetToTopvecNeg.negate()
                    val invTargetVec = quatRotate(invTargetQuat, targetToTopvecNeg)
                    resultVec = invTargetVec
                    resultQuat = invTargetQuat
                }

                WalkEnding.FullPath -> {
                    val invTargetQuat = Quat4d(targetToTopQuat)
                    invTargetQuat.inverse()
                    val targetToTopVecNeg = Vector3d(targetToTopVec)
                    targetToTopVecNeg.negate()
                    val invTargetVec = quatRotate(invTargetQuat, targetToTopVecNeg)
                    resultVec = quatRotate(invTargetQuat, sourceToTopVec)
                    resultVec.add(invTargetVec)
                    resultQuat.mul(invTargetQuat, sourceToTopQuat)
                }

            }
            this.time = time
        }
    }

    private val transformationFrameMapLock = ReentrantLock()
    private val transformationFrameMapCondition = transformationFrameMapLock.newCondition()

    private val frameIds: MutableMap<String, Int> = HashMap()
    private val frames: MutableList<TransformCache> = LinkedList()
    private val frameIdsReverse: MutableList<String> = LinkedList()
    private val frameAuthority: MutableMap<Int, String> = HashMap()
    private val requests: MutableSet<TransformRequest> = HashSet()
    private val lctCache: MutableList<TimeAndFrameID> = LinkedList()
    private val executor = Executors.newCachedThreadPool()

    init {
        transformationFrameMapLock.withLock {
            frameIds["NO_PARENT"] = 0
            frames.add(TransformCacheNull())
            frameIdsReverse.add("NO_PARENT")
            transformationFrameMapCondition.signalAll()
        }
    }

    override fun clear() {
        transformationFrameMapLock.withLock {
            if (frames.size > 1) {
                for (f in frames) {
                    if (f.isValid) {
                        f.clearList()
                    }
                }
                transformationFrameMapCondition.signalAll()
            }
        }
    }

    @Throws(TransformerException::class)
    override fun setTransform(transforms: List<Transform>, isStatic: Boolean): Boolean {

        transforms.map { transform ->

            // prepare data
            val authority: String? = transform.authority
            val childNode: String = transform.childNode.replace("/", "").trim()
            val parentNode: String = transform.parentNode.replace("/", "").trim()
            val quat: Quat4d = transform.rotationQuat
            val vec: Vector3d = transform.translation
            val stripped = transform.copy(
                childNode = childNode,
                parentNode = parentNode,
            )

            // check input data validity
            if (childNode == parentNode) {
                LOGGER.error("Frames for parent and child are the same: $childNode")
                throw TransformerException("Frames for parent and child are the same: $childNode")
            }
            if (childNode.isEmpty()) {
                LOGGER.error("Child frame is empty")
                throw TransformerException("Child frame is empty")
            }
            if (parentNode.isEmpty()) {
                LOGGER.error("Parent frame is empty")
                throw TransformerException("Parent frame is empty")
            }
            if (java.lang.Double.isNaN(quat.w) || java.lang.Double.isNaN(quat.x) || java.lang.Double.isNaN(quat.y) || java.lang.Double.isNaN(
                    quat.z
                )
                || java.lang.Double.isNaN(vec.x) || java.lang.Double.isNaN(vec.y) || java.lang.Double.isNaN(vec.z)
            ) {
                LOGGER.error("Transform contains nan: $transform")
                throw TransformerException("Transform contains nan: $transform")
            }

            // perform the insertion
            transformationFrameMapLock.withLock {
                val frameNumberChild = lookupOrInsertFrameNumber(childNode)
                var frame = getFrame(frameNumberChild)
                if (!frame!!.isValid) {
                    frame = allocateFrame(frameNumberChild, isStatic)
                }
                val frameNumberParent = lookupOrInsertFrameNumber(stripped.parentNode)
                if (frame.insertData(TransformInternal(stripped, frameNumberParent, frameNumberChild))) {
                    authority?.let { frameAuthority[frameNumberChild] = it }
                } else {
                    LOGGER.warn(
                        """TF_OLD_DATA ignoring data from the past for frame ${stripped.childNode} at time ${stripped.time} according to authority $authority
Possible reasons are listed at http://wiki.ros.org/tf/Errors%%20explained"""
                    )
                    return false
                }
                executor.execute { checkRequests() }
            }
        }
        return true
    }

    private fun lookupOrInsertFrameNumber(frameId: String): Int {
        var retval = 0
        transformationFrameMapLock.withLock {
            if (!frameIds.containsKey(frameId)) {
                retval = frames.size
                frames.add(TransformCacheNull())
                frameIds[frameId] = retval
                frameIdsReverse.add(frameId)
                transformationFrameMapCondition.signalAll()
            } else {
                retval = frameIds[frameId]!!
            }
        }
        return retval
    }

    private fun getFrame(frameId: Int): TransformCache? =
        transformationFrameMapLock.withLock {
            // @todo check larger values too
            if (frameId == 0 || frameId > frames.size) {
                null
            } else {
                frames[frameId]
            }
        }

    private fun allocateFrame(cfid: Int, isStatic: Boolean): TransformCache {
        transformationFrameMapLock.withLock {
            if (isStatic) {
                frames[cfid] = TransformCacheStatic()
            } else {
                frames[cfid] = TransformCacheImpl(cacheTime)
            }
            return frames[cfid]
        }
    }

    @Throws(TransformerException::class)
    override fun lookupTransform(targetFrame: String, sourceFrame: String, time: Long): Transform {
        transformationFrameMapLock.withLock {
            return try {
                if (targetFrame == sourceFrame) {
                    val newTime: Long = if (time == 0L) {
                        val targetId = lookupFrameNumber(targetFrame)
                        val cache = getFrame(targetId)
                        if (cache!!.isValid) {
                            cache.latestTimestamp
                        } else {
                            time
                        }
                    } else {
                        time
                    }
                    return Transform(Transform3D(), targetFrame, sourceFrame, newTime)
                }
                lookupTransformNoLock(targetFrame, sourceFrame, time)
            } catch (ex: TransformerException) {
                throw TransformerException("Could not lookup transformation", ex)
            }
        }
    }

    @Throws(TransformerException::class)
    private fun lookupTransformNoLock(targetFrame: String, sourceFrame: String, time: Long): Transform {

        // Identify case does not need to be validated above
        val targetId = validateFrameId("lookupTransform argument target_frame", targetFrame)
        val sourceId = validateFrameId("lookupTransform argument source_frame", sourceFrame)
        val accum = TransformAccumImpl()
        try {
            walkToTopParent(accum, time, targetId, sourceId)
        } catch (ex: TransformerException) {
            throw TransformerException("No matching transform found", ex)
        }
        val t3d = Transform3D(accum.resultQuat, accum.resultVec, 1.0)
        return Transform(t3d, targetFrame, sourceFrame, accum.time)
    }

    @Throws(TransformerException::class)
    private fun walkToTopParent(f: TransformAccum, time: Long, targetId: Int, sourceId: Int) {
        // Short circuit if zero length transform to allow lookups on non
        // existent links
        var time = time
        if (sourceId == targetId) {
            f.finalize(WalkEnding.Identity, time)
            return
        }
        // If getting the latest get the latest common time
        if (time == 0L) {
            time = getLatestCommonTime(targetId, sourceId)
        }

        // Walk the tree to its root from the source frame, accumulating the
        // transform
        var frame = sourceId
        var topParent = frame
        var depth = 0
        var extrapolationMightHaveOccurred = false
        while (frame != 0) {
            val cache = getFrame(frame)
            if (!cache!!.isValid) {
                // There will be no cache for the very root of the tree
                topParent = frame
                break
            }
            val parent = f.gather(cache, time)
            if (parent == 0) {
                // Just break out here... there may still be a path from source
                // -> target
                topParent = frame
                extrapolationMightHaveOccurred = true
                break
            }

            // Early out... target frame is a direct parent of the source frame
            if (frame == targetId) {
                f.finalize(WalkEnding.TargetParentOfSource, time)
                return
            }
            f.accum(true)
            topParent = frame
            frame = parent
            ++depth
            if (depth > MAX_GRAPH_DEPTH) {
                throw TransformerException("The tf tree is invalid because it contains a loop.")
            }
        }

        // Now walk to the top parent from the target frame, accumulating its
        // transform
        frame = targetId
        depth = 0
        while (frame != topParent) {
            val cache = getFrame(frame)
            if (!cache!!.isValid) {
                throw TransformerException(
                    "Invalid cache when looking up transform from frame [" + lookupFrameString(
                        sourceId
                    ) + "] to frame [" + lookupFrameString(targetId) + "]"
                )
            }
            val parent = f.gather(cache, time)
            if (parent == 0) {
                throw TransformerException(
                    "when looking up transform from frame [" + lookupFrameString(sourceId) + "] to frame [" + lookupFrameString(
                        targetId
                    ) + "]"
                )
            }

            // Early out... source frame is a direct parent of the target frame
            if (frame == sourceId) {
                f.finalize(WalkEnding.SourceParentOfTarget, time)
                return
            }
            f.accum(false)
            frame = parent
            ++depth
            if (depth > MAX_GRAPH_DEPTH) {
                throw TransformerException("The tf tree is invalid because it contains a loop." + allFramesAsStringNoLock())
            }
        }
        if (frame != topParent) {
            if (extrapolationMightHaveOccurred) {
                throw TransformerException(
                    ", when looking up transform from frame [" + lookupFrameString(sourceId) + "] to frame [" + lookupFrameString(
                        targetId
                    ) + "]"
                )
            }
        }
        f.finalize(WalkEnding.FullPath, time)
    }

    @Throws(TransformerException::class)
    private fun getLatestCommonTime(targetId: Int, sourceId: Int): Long {
        if (sourceId == targetId) {
            val cache = getFrame(sourceId)
            // Set time to latest timestamp of frameid in case of target and
            // source node ID are the same
            return if (cache!!.isValid) {
                cache.latestTimestamp
            } else {
                0
            }
        }
        lctCache.clear()

        // Walk the tree to its root from the source frame, accumulating the
        // list of parent/time as well as the latest time
        // in the target is a direct parent
        var frame = sourceId
        var depth = 0
        var commonTime = Long.MAX_VALUE
        while (frame != 0) {
            val cache = getFrame(frame)
            if (!cache!!.isValid) {
                // There will be no cache for the very root of the tree
                break
            }
            val latest = cache.latestTimeAndParent
            if (latest.frameID == 0) {
                // Just break out here... there may still be a path from source
                // -> target
                break
            }
            if (latest.time != 0L) {
                commonTime = latest.time.coerceAtMost(commonTime)
            }
            lctCache.add(latest)
            frame = latest.frameID

            // Early out... target frame is a direct parent of the source frame
            if (frame == targetId) {
                var time = commonTime
                if (time == Long.MAX_VALUE) {
                    time = 0
                }
                return time
            }
            ++depth
            if (depth > MAX_GRAPH_DEPTH) {
                throw TransformerException("The tf tree is invalid because it contains a loop." + allFramesAsStringNoLock())
            }
        }

        // Now walk to the top parent from the target frame, accumulating the
        // latest time and looking for a common parent
        frame = targetId
        depth = 0
        commonTime = Long.MAX_VALUE
        var commonParent = 0
        while (true) {
            val cache = getFrame(frame)
            if (!cache!!.isValid) {
                break
            }
            val latest = cache.latestTimeAndParent
            if (latest.frameID == 0) {
                break
            }
            if (latest.time != 0L) {
                commonTime = Math.min(latest.time, commonTime)
            }
            var found = false
            for (t in lctCache) {
                if (t.frameID == latest.frameID) {
                    found = true
                    break
                }
            }
            if (found) { // found a common parent
                commonParent = latest.frameID
                break
            }
            frame = latest.frameID

            // Early out... source frame is a direct parent of the target frame
            if (frame == sourceId) {
                var time = commonTime
                if (time == Long.MAX_VALUE) {
                    time = 0
                }
                return time
            }
            ++depth
            if (depth > MAX_GRAPH_DEPTH) {
                throw TransformerException("The tf tree is invalid because it contains a loop." + allFramesAsStringNoLock())
            }
        }
        if (commonParent == 0) {
            throw TransformerException(
                "Could not find a connection between '"
                        + lookupFrameString(targetId) + "' and '"
                        + lookupFrameString(sourceId)
                        + "' because they are not part of the same tree."
                        + "Tf has two or more unconnected trees."
            )
        }

        // Loop through the source -> root list until we hit the common parent
        for (it in lctCache) {
            if (it.time != 0L) {
                commonTime = Math.min(commonTime, it.time)
            }
            if (it.frameID == commonParent) {
                break
            }
        }
        if (commonTime == Long.MAX_VALUE) {
            commonTime = 0
        }
        return commonTime
    }

    @Throws(TransformerException::class)
    private fun lookupFrameString(frameId: Int): String {
        transformationFrameMapLock.withLock {
            return if (frameId >= frameIdsReverse.size) {
                throw TransformerException("Reverse lookup of node ID $frameId failed!")
            } else {
                frameIdsReverse[frameId]
            }
        }
    }

    @Throws(TransformerException::class)
    private fun lookupFrameNumber(frameId: String): Int {
        transformationFrameMapLock.withLock {
            if (!frameIds.containsKey(frameId)) {
                throw TransformerException("FrameId[$frameId]")
            }
            return frameIds[frameId]!!
        }
    }

    @Throws(TransformerException::class)
    private fun validateFrameId(functionNameArg: String, frameId: String): Int {
        if (frameId.isEmpty()) {
            throw TransformerException("Invalid argument passed to $functionNameArg in tf2 frameIds cannot be empty")
        }
        if (frameId.startsWith("/")) {
            throw TransformerException("Invalid argument \"$frameId\" passed to $functionNameArg in tf2 frame_ids cannot start with a '/' like: ")
        }
        return try {
            lookupFrameNumber(frameId)
        } catch (ex: TransformerException) {
            throw TransformerException("\"$frameId\" passed to $functionNameArg does not exist. ", ex)
        }
    }

    @Throws(TransformerException::class)
    override fun lookupTransform(
        targetFrame: String,
        targetTime: Long,
        sourceFrame: String,
        sourceTime: Long,
        fixedFrame: String,
    ): Transform {
        validateFrameId("lookupTransform argument target_frame", targetFrame)
        validateFrameId("lookupTransform argument source_frame", sourceFrame)
        validateFrameId("lookupTransform argument fixed_frame", fixedFrame)
        val temp1 = lookupTransform(fixedFrame, sourceFrame, sourceTime)
        val temp2 = lookupTransform(targetFrame, fixedFrame, targetTime)
        val t = Transform3D()
        t.mul(temp2.transform, temp1.transform)
        return Transform(t, targetFrame, sourceFrame, temp2.time)
    }

    override fun requestTransform(targetFrame: String, sourceFrame: String, time: Long): Future<Transform> {
        val future = FutureTransform()
        transformationFrameMapLock.withLock {
            if (canTransform(targetFrame, sourceFrame, time)) {
                try {
                    future.set(lookupTransformNoLock(targetFrame, sourceFrame, time))
                } catch (ex: TransformerException) {
                    LOGGER.warn("Transformation from [" + sourceFrame + "] to [" + targetFrame + "] failed!" + ex.message)
                }
            }
            requests.add(TransformRequest(targetFrame, sourceFrame, time, future))
            return future
        }
    }

    /**
     * Method blocks until new transformation updates are available.
     * @throws InterruptedException is thrown if the current thread is externally interrupted.
     */
    @Throws(InterruptedException::class)
    fun waitForTransformationUpdates(timeout: Long) {
        transformationFrameMapLock.withLock { transformationFrameMapCondition.await(timeout, TimeUnit.MILLISECONDS) }
    }

    /**
     * Method blocks until new transformation updates are available.
     * @throws InterruptedException is thrown if the current thread is externally interrupted.
     */
    @Throws(InterruptedException::class)
    fun waitForTransformationUpdates() {
        transformationFrameMapLock.withLock { transformationFrameMapCondition.await() }
    }

    private fun checkRequests() {
        // go through all request and check if they can be answered
        transformationFrameMapLock.withLock {
            for (request in ArrayList(requests)) {
                try {
                    // request can be answered. publish the transform through
                    // the future object and remove the request.
                    request.future.set(lookupTransformNoLock(request.target_frame, request.source_frame, request.time))
                    requests.remove(request)
                } catch (ex: TransformerException) {
                    LOGGER.debug("Request:" + request.source_frame + " -> " + request.target_frame + " still not available")
                    // expected, just proceed
                }
            }
        }
    }

    override fun canTransform(targetFrame: String, sourceFrame: String, time: Long): Boolean {
        if (targetFrame == sourceFrame) {
            return true
        }
        if (warnFrameId("canTransform argument target_frame", targetFrame)) {
            return false
        }
        if (warnFrameId("canTransform argument source_frame", sourceFrame)) {
            return false
        }
        transformationFrameMapLock.withLock {
            return try {
                val targetId = lookupFrameNumber(targetFrame)
                val sourceId = lookupFrameNumber(sourceFrame)
                canTransformNoLock(targetId, sourceId, time)
            } catch (ex: TransformerException) {
                false
            }
        }
    }

    override fun canTransform(
        targetFrame: String,
        targetTime: Long,
        sourceFrame: String,
        sourceTime: Long,
        fixedFrame: String,
    ): Boolean {
        if (warnFrameId("canTransform argument target_frame", targetFrame)) {
            return false
        }
        if (warnFrameId("canTransform argument source_frame", sourceFrame)) {
            return false
        }
        return if (warnFrameId("canTransform argument fixed_frame", fixedFrame)) {
            false
        } else canTransform(targetFrame, fixedFrame, targetTime) && canTransform(fixedFrame, sourceFrame, sourceTime)
    }

    private fun warnFrameId(functionNameArg: String, frameId: String): Boolean {
        if (frameId.length == 0) {
            LOGGER.warn("Invalid argument passed to $functionNameArg in tf2 frame_ids cannot be empty")
            return true
        }
        if (frameId.startsWith("/")) {
            LOGGER.warn("Invalid argument \"$frameId\" passed to $functionNameArg in tf2 frame_ids cannot start with a '/' like: ")
            return true
        }
        return false
    }

    override fun getFrameStrings(): Set<String> {
        transformationFrameMapLock.withLock {
            val vec: MutableSet<String> = HashSet()
            for (counter in 1 until frameIdsReverse.size) {
                vec.add(frameIdsReverse[counter])
            }
            return vec
        }
    }

    override fun frameExists(frameId: String): Boolean {
        transformationFrameMapLock.withLock { return frameIds.containsKey(frameId) }
    }

    @Throws(TransformerException::class)
    override fun getParent(frameId: String, time: Long): String {
        transformationFrameMapLock.withLock {
            return try {
                val frameNumber = lookupFrameNumber(frameId)
                val frame = getFrame(frameNumber)
                if (!frame!!.isValid) {
                    return ""
                }
                val parentId = frame.getParent(time)
                if (parentId == 0) {
                    ""
                } else lookupFrameString(parentId)
            } catch (ex: TransformerException) {
                throw TransformerException("Could not resolfe parent transformation!", ex)
            }
        }
    }

    override fun allFramesAsDot(): String {
        var mstream = ""
        mstream += "digraph G {\n"
        transformationFrameMapLock.withLock {
            val temp = TransformInternal()
            if (frames.size == 1) {
                mstream += "\"no tf data recieved\""
            }

            // one referenced for 0 is no frame
            for (counter in 1 until frames.size) {
                var frameId: Int
                val counter_frame = getFrame(counter)
                if (!counter_frame!!.isValid) {
                    continue
                }
                frameId = if (!counter_frame.getData(0, temp)) {
                    continue
                } else {
                    temp.frame_id
                }
                var authority: String? = "no recorded authority"
                if (frameAuthority.containsKey(counter)) {
                    authority = frameAuthority[counter]
                }
                val rate = (counter_frame.listLength
                        / Math.max(
                    counter_frame.latestTimestamp / 1000.0 - counter_frame
                        .oldestTimestamp / 1000.0, 0.0001
                ))
                mstream += (("\""
                        + frameIdsReverse[frameId]
                        + "\" -> \""
                        + frameIdsReverse[counter]
                        + "\"[label=\"Broadcaster: "
                        + authority
                        + "\\nAverage rate: "
                        + rate
                        + " Hz\\nMost recent transform: "
                        + (counter_frame.latestTimestamp
                        / 1000.0) + " \\nBuffer length: ") + (counter_frame.latestTimestamp - counter_frame
                    .oldestTimestamp) / 1000.0 + " sec\\n"
                        + "\"];\n")
            }

            // one referenced for 0 is no frame
            for (counter in 1 until frames.size) {
                var frameId: Int
                val counter_frame = getFrame(counter)
                if (!counter_frame!!.isValid) {
                    continue
                }
                frameId = if (counter_frame.getData(0, temp)) {
                    temp.frame_id
                } else {
                    0
                }
                if (frameIdsReverse[frameId] == "NO_PARENT") {
                    mstream += "edge [style=invis];\n"
                    mstream += """ subgraph cluster_legend { style=bold; color=black; label ="view_frames Result";
}->"${frameIdsReverse[counter]}";
"""
                }
            }
            mstream += "}"
            return mstream
        }
    }

    override fun allFramesAsYAML(): String {
        var mstream = ""
        transformationFrameMapLock.withLock {
            val temp = TransformInternal()
            if (frames.size == 1) {
                mstream += "[]"
            }

            // for (std::vector< TimeCache*>::iterator it = frames_.begin(); it
            // != frames_.end(); ++it)
            for (counter in 1 until frames.size) {
                // one referenced for 0 is no frame
                var frameId: Int
                val cache = getFrame(counter)
                if (!cache!!.isValid) {
                    continue
                }
                if (!cache.getData(0, temp)) {
                    continue
                }
                frameId = temp.frame_id
                var authority: String? = "no recorded authority"
                if (frameAuthority.containsKey(counter)) {
                    authority = frameAuthority[counter]
                }
                val rate = (cache.listLength
                        / Math.max(
                    cache.latestTimestamp / 1000.0 - cache
                        .oldestTimestamp / 1000.0, 0.0001
                ))
                mstream += """
                ${frameIdsReverse[counter]}: 
                
                """.trimIndent()
                mstream += """  parent: '${frameIdsReverse[frameId]}'
"""
                mstream += "  broadcaster: '$authority'\n"
                mstream += "  rate: $rate\n"
                mstream += "  most_recent_transform: " + cache.latestTimestamp / 1000.0 + "\n"
                mstream += "  oldest_transform: " + cache.oldestTimestamp / 1000.0 + "\n"
                mstream += "  buffer_length: " + (cache.latestTimestamp - cache
                    .oldestTimestamp) / 1000.0 + "\n"
            }
            return mstream
        }
    }

    override fun allFramesAsString(): String {
        transformationFrameMapLock.withLock { return allFramesAsStringNoLock() }
    }

    private fun canTransformNoLock(targetId: Int, sourceId: Int, time: Long): Boolean {
        if (targetId == 0 || sourceId == 0) {
            return false
        }
        if (targetId == sourceId) {
            return true
        }
        val accum = TransformAccumDummy()
        try {
            walkToTopParent(accum, time, targetId, sourceId)
        } catch (ex: TransformerException) {
            return false
        }
        return true
    }

    private fun allFramesAsStringNoLock(): String {
        val temp = TransformInternal()
        var mstring = ""
        transformationFrameMapLock.withLock {

            // regular transforms
            LOGGER.debug("frames size: " + frames.size)
            for (counter in 1 until frames.size) {
                val frame_ptr = getFrame(counter)
                LOGGER.debug("got frame: $frame_ptr")
                if (!frame_ptr!!.isValid) {
                    continue
                }
                var frame_id_num = 0
                if (frame_ptr.getData(0, temp)) {
                    LOGGER.debug("got frame transform: $temp")
                    frame_id_num = temp.frame_id
                }
                mstring += """Frame ${frameIdsReverse[counter]} exists with parent ${frameIdsReverse[frame_id_num]}."""
            }
        }
        return mstring
    }

    override fun newTransformAvailable(transforms: List<Transform>, isStatic: Boolean) {
        try {
            setTransform(transforms, isStatic)
        } catch (ex: TransformerException) {
            LOGGER.error(ex.message, ex)
        }
    }

    companion object {
        private const val MAX_GRAPH_DEPTH = 1000
        private val LOGGER = LoggerFactory.getLogger(TransformerCoreDefault::class.java)
    }
}
