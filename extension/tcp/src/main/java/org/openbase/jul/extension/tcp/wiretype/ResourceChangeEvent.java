/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.tcp.wiretype;

import java.io.Serializable;
import org.slf4j.LoggerFactory;

;

/**
 *
 * @author divine
 */
public final class ResourceChangeEvent implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ResourceChangeEvent.class);

    private final String changeEventName;
    private final AbstractResource source;
    private final AbstractResourceData resourceData;
    private final long timeStamp;

    public ResourceChangeEvent(String changeEventName, AbstractResourceData resourceData, AbstractResource source) {
        this.changeEventName = changeEventName;
        this.source = source;
        this.resourceData = resourceData;
        if (resourceData == null) {
            LOGGER.warn("resource data null!");
        }
        this.timeStamp = System.currentTimeMillis();
        LOGGER.debug("Create: " + this);
    }

    public String getChangeEventName() {
        return changeEventName;
    }

    public AbstractResourceData getResourceData() {
        return resourceData;
    }

    public AbstractResource getSource() {
        return source;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean resourceDataExists() {
        return resourceData != null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[EventName: " + changeEventName + ", Source:" + source + ", Data:" + resourceData + "]";
    }
}
