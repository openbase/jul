package org.openbase.jul.extension.tcp;

/*-
 * #%L
 * JUL Extension TCP
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.tcp.wiretype.AbstractResourceData;
import org.openbase.jul.extension.tcp.wiretype.ResourceChangeEvent;
import org.openbase.jul.extension.tcp.wiretype.ResourceKey;
import org.openbase.jul.extension.tcp.wiretype.ResourceManager;
import org.slf4j.LoggerFactory;

/**
 *
 * @author divine
 */
public final class NetworkResourceChangeEvent implements Serializable {

    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NetworkResourceChangeEvent.class);

    private String changeEventName;
    private ResourceKey sourceKey;
    private AbstractResourceData resourceData;
    private long timeStamp;

    /**
     * JSON Constructor
     */
    public NetworkResourceChangeEvent() {
    }

    public NetworkResourceChangeEvent(ResourceChangeEvent evt) {
        this(evt.getChangeEventName(), evt.getResourceData(), evt.getSource().getResourceKey());
        LOGGER.debug("Create Event: " + this);
    }

    public NetworkResourceChangeEvent(String changeEventName, AbstractResourceData resourceData, ResourceKey sourceKey) {
        assert changeEventName != null;
        assert resourceData != null;
        assert sourceKey != null;

        this.changeEventName = changeEventName;
        this.sourceKey = sourceKey;
        this.resourceData = resourceData;
        this.timeStamp = System.currentTimeMillis();

        if (resourceData == null) {
            LOGGER.error("Resource Data is null!!! Recover from ResourceManager.");
            try {
                this.resourceData = ResourceManager.getInstance().getResource(sourceKey).getClone();
            } catch (Exception ex) {
                ExceptionPrinter.printHistory("Could not recover resource data of Resource[" + sourceKey + "].", ex, LOGGER);
            }
        }
        LOGGER.debug("Create Event: " + this);
    }

    public String getChangeEventName() {
        return changeEventName;
    }

    public AbstractResourceData getResourceData() {
        return resourceData;
    }

    public ResourceKey getSourceKey() {
        return sourceKey;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean resourceDataExists() {
        return resourceData != null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[SourceKey:" + sourceKey + "|Event:" + changeEventName + "|Data:" + resourceData + "]";
    }
}
