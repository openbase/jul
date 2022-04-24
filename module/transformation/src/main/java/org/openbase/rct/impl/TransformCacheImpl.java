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

import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformCacheImpl implements TransformCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformCacheImpl.class);
    private final long maxStorageTime;
    private final List<TransformInternal> storage_ = new LinkedList<>();

    public TransformCacheImpl(long maxStorageTime) {
        this.maxStorageTime = maxStorageTime;
    }

    int findClosest(TransformInternal one, TransformInternal two,
            long target_time) {
        // No values stored
        if (storage_.isEmpty()) {
            LOGGER.debug("findClosest() storage is empty");
            return 0;
        }

        // If time == 0 return the latest
        if (target_time == 0) {
            one.replaceWith(storage_.get(0));
            LOGGER.debug("findClosest() time is zero. Return latest.");
            return 1;
        }

        // One value stored
        if (storage_.size() == 1) {
            TransformInternal ts = storage_.get(0);
            if (ts.stamp == target_time) {
                one.replaceWith(ts);
                LOGGER.debug("findClosest() storage has only one entry. Return it.");
                return 1;
            } else {
                throw new RuntimeException("Lookup would require extrapolation at time " + target_time + ", but only time " + ts.stamp + " is in the buffer");
            }
        }

        long latest_time = storage_.get(0).stamp;
        long earliest_time = storage_.get(storage_.size() - 1).stamp;

        if (target_time == latest_time) {
            one.replaceWith(storage_.get(0));
            LOGGER.debug("findClosest() found exact target time. Return it.");
            return 1;
        } else if (target_time == earliest_time) {
            one.replaceWith(storage_.get(storage_.size() - 1));
            LOGGER.debug("findClosest() found exact target time. Return it.");
            return 1;
        } // Catch cases that would require extrapolation
        else if (target_time > latest_time) {
            throw new RuntimeException("Lookup would require extrapolation into the future.  Requested time " + target_time + " but the latest data is at time " + latest_time);
        } else if (target_time < earliest_time) {
            throw new RuntimeException("Lookup would require extrapolation into the future.  Requested time " + target_time + " but the latest data is at time " + earliest_time);
        }

        // At least 2 values stored
        // Find the first value less than the target value
        int storage_it = -1;
        for (int i = 0; i < storage_.size(); i++) {
            if (storage_.get(i).stamp <= target_time) {
                storage_it = i;
                break;
            }
        }

        // Finally the case were somewhere in the middle Guarenteed no extrapolation :-)
        LOGGER.debug("findClosest() return the two closest.");
        one.replaceWith(storage_.get(storage_it)); // Older
        two.replaceWith(storage_.get(storage_it - 1)); // Newer
        return 2;

    }

    void interpolate(TransformInternal one, TransformInternal two, long time, TransformInternal output) {
        // Check for zero distance case
        if (two.stamp == one.stamp) {
            output.replaceWith(two);
            return;
        }
        // Calculate the ratio
        double ratio = (double) (time - one.stamp)
                / (double) (two.stamp - one.stamp);

        // Interpolate translation
        output.translation.interpolate(one.translation, two.translation, ratio);

        // Interpolate rotation
        output.rotation.interpolate(one.rotation, two.rotation, ratio);

        output.stamp = one.stamp;
        output.frame_id = one.frame_id;
        output.child_frame_id = one.child_frame_id;
    }

    @Override
    public boolean getData(long time, TransformInternal data_out) {
        TransformInternal p_temp_1 = new TransformInternal();
        TransformInternal p_temp_2 = new TransformInternal();

        LOGGER.debug("getData() find closest to time " + time);
        try {
            int num_nodes = findClosest(p_temp_1, p_temp_2, time);
            LOGGER.debug("getData() nodes: " + num_nodes);
            if (num_nodes == 0) {
                LOGGER.error("getData() no transform found");
                return false;
            } else if (num_nodes == 1) {
                LOGGER.debug("getData() found exactly one transform");
                data_out.replaceWith(p_temp_1);
            } else if (num_nodes == 2) {
                if (p_temp_1.frame_id == p_temp_2.frame_id) {
                    LOGGER.debug("getData() found two transforms. Interpolate.");
                    interpolate(p_temp_1, p_temp_2, time, data_out);
                } else {
                    data_out.replaceWith(p_temp_1);
                }
            } else {
                assert (false);
            }

            return true;
        } catch (RuntimeException ex) {
            LOGGER.error("Could not get data. Reason: " + ex.getMessage());
            LOGGER.debug("Could not get data", ex);
            return false;
        }
    }

    @Override
    public boolean insertData(TransformInternal new_data) {
        LOGGER.debug("insertData(): " + new_data);
        int storage_it = 0;

        if (!storage_.isEmpty()) {
            LOGGER.debug("storage is not empty");
            if (storage_.get(storage_it).stamp > new_data.stamp + maxStorageTime) {
                LOGGER.error("data too old for insertion");
                return false;
            }
        }

        while (storage_it != storage_.size()) {
            if (storage_.get(storage_it).stamp <= new_data.stamp) {
                break;
            }
            storage_it++;
        }
        if (storage_it >= storage_.size()) {
            LOGGER.debug("add additional storage entry (storage size:" + storage_it + ")");
            storage_.add(new_data);
        } else {
            LOGGER.debug("set new data to index " + storage_it + " in storage (size:" + storage_.size() + ")");
            storage_.add(storage_it, new_data);
        }

        LOGGER.debug("prune list");
        pruneList();
        return true;
    }

    private void pruneList() {
        long latest_time = storage_.get(0).stamp;
        LOGGER.debug("latest time: " + latest_time);
        LOGGER.debug("max storage time: " + maxStorageTime);
        LOGGER.debug("storage empty: " + storage_.isEmpty());
        while (!storage_.isEmpty() && storage_.get(storage_.size() - 1).stamp + maxStorageTime < latest_time) {
            LOGGER.debug("remove last. stamp: " + storage_.get(storage_.size() - 1).stamp);
            storage_.remove(storage_.size() - 1);
        }
    }

    @Override
    public void clearList() {
        storage_.clear();
    }

    @Override
    public int getParent(long time) {
        TransformInternal p_temp_1 = new TransformInternal();
        TransformInternal p_temp_2 = new TransformInternal();
        try {
            int num_nodes = findClosest(p_temp_1, p_temp_2, time);
            if (num_nodes == 0) {
                return 0;
            }

            return p_temp_1.frame_id;
        } catch (RuntimeException ex) {
            LOGGER.error("Could not get parent", ex);
            return 0;
        }
    }

    @Override
    public TimeAndFrameID getLatestTimeAndParent() {
        if (storage_.isEmpty()) {
            return new TimeAndFrameID(0, 0);
        }

        TransformInternal ts = storage_.get(0);
        return new TimeAndFrameID(ts.stamp, ts.frame_id);
    }

    @Override
    public int getListLength() {
        return storage_.size();
    }

    @Override
    public long getLatestTimestamp() {
        if (storage_.isEmpty()) {
            return 0l; // empty list case
        }
        return storage_.get(0).stamp;
    }

    @Override
    public long getOldestTimestamp() {
        if (storage_.isEmpty()) {
            return 0l; // empty list case
        }
        return storage_.get(storage_.size() - 1).stamp;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return "TransformCacheImpl[maxStorageTime:" + maxStorageTime + ", storage:" + storage_.size() + "]";
    }
}
