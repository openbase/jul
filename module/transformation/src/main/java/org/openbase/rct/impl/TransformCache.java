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

public interface TransformCache {

    class TimeAndFrameID {

        public TimeAndFrameID(long time, int frameID) {
            this.time = time;
            this.frameID = frameID;
        }

        public long time;
        public int frameID;
    }

    boolean getData(long time, TransformInternal dataOut); // returns false if
    // data unavailable
    // (should be thrown
    // as lookup
    // exception

    /**
     * \brief Insert data into the cache
     *
     * @param newData
     * @return
     */
    boolean insertData(TransformInternal newData);

    /**
     * Clear the list of stored values
     */
    void clearList();

    /**
     * \brief Retrieve the parent at a specific time
     *
     * @param time
     * @return
     */
    int getParent(long time);

    /**
     * \brief Get the latest time stored in this cache, and the parent
     * associated with it. Returns parent = 0 if no data.
     *
     * @return
     */
    TimeAndFrameID getLatestTimeAndParent();

    // / Debugging information methods
    /**
     * @return Get the length of the stored list
     */
    int getListLength();

    /**
     * @return Get the latest timestamp cached
     */
    long getLatestTimestamp();

    /**
     * @return Get the oldest timestamp cached
     */
    long getOldestTimestamp();

    boolean isValid();
}
