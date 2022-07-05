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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformCacheStatic implements TransformCache {

    private TransformInternal storage = new TransformInternal();
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformCacheImpl.class);

    public TransformCacheStatic() {
    }

    @Override
    public boolean getData(long time, TransformInternal dataOut) {
        dataOut.replaceWith(storage);
        dataOut.stamp = time;
        return true;
    }

    @Override
    public boolean insertData(TransformInternal newData) {
        LOGGER.debug("insertData(): " + newData);
        storage = newData;
        return true;
    }

    @Override
    public void clearList() {
    }

    @Override
    public int getParent(long time) {
        return storage.frame_id;
    }

    @Override
    public TimeAndFrameID getLatestTimeAndParent() {
        return new TimeAndFrameID(0, storage.frame_id);
    }

    @Override
    public int getListLength() {
        return 1;
    }

    @Override
    public long getLatestTimestamp() {
        return 0;
    }

    @Override
    public long getOldestTimestamp() {
        return 0;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
