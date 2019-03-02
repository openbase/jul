package org.openbase.jul.extension.tcp.datatype;

/*-
 * #%L
 * JUL Extension TCP
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import java.io.Serializable;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public final class ConnectionInfo implements Serializable {

    private final int sourceID;
    private final int targetID;

    /**
     *
     * @param sourceID
     * @param targetID
     */
    public ConnectionInfo(int sourceID, int targetID) {
        this.sourceID = sourceID;
        this.targetID = targetID;
    }

    /**
     * JSON Constructor
     */
    private ConnectionInfo() {
        this.sourceID = -1;
        this.targetID = -1;
    }

    public int getSourceID() {
        return sourceID;
    }

    public int getTargetID() {
        return targetID;
    }

    @Override
    public String toString() {
        return "ConnectionInfo[SourceID:" + sourceID + " | TargetID:" + targetID + "]";
    }
}
