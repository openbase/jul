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
public abstract class AbstractResourceData <D extends AbstractResourceData> implements Serializable {

    protected final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(getClass());
//	private transient D clone;

	public AbstractResourceData() {
		LOGGER.debug("Create: "+this);
	}

	
	// TODO: reimplement, with lock, and instance synchronization!!!
//	public D getData() {
//		synchronized(this) {
//			try {
//				return (D) this.clone();
//			} catch (CloneNotSupportedException e) {
//				Logger.error(this, "Could not clone "+this, e);
//				return null;
//			}
//		}
//	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"[]";
	}
}
