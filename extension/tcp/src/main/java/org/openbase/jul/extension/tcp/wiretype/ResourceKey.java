/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.tcp.wiretype;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author divine
 */
public final class ResourceKey<R extends AbstractResource> implements Serializable {

	public final static String ATTRIBUTE_KEY = "key";

	public static final String SEPARATOR = "-";

	private final Integer resourceID;
	private final String key, resourceClassName, resourceClassPackageAndName;

	/**
	 * JSON Constructor
	 */
	public ResourceKey() {
		this.resourceID = null;
		this.key = null;
		this.resourceClassName = null;
		this.resourceClassPackageAndName = null;
	}

	ResourceKey(AbstractResourceConfig<R> resourceConfig) {
		this.resourceID = resourceConfig.getId();
		this.resourceClassName = resourceConfig.getClass().getSimpleName().replaceAll("Config", "");
		this.resourceClassPackageAndName = resourceConfig.getClass().getPackage().getName() + "." + resourceClassName;
		this.key = buildKey(resourceID, resourceClassPackageAndName);
	}

	public ResourceKey(Integer id, String resourceClassName, String resourceClassPackageAndName) {
		this.resourceID = id;
		this.resourceClassName = resourceClassName;
		this.resourceClassPackageAndName = resourceClassPackageAndName;
		this.key = buildKey(resourceID, resourceClassName);
	}

	public ResourceKey(Integer id, Class<? extends AbstractResource> clazz) {
		this.resourceID = id;
		this.resourceClassName = clazz.getSimpleName();
		this.resourceClassPackageAndName = clazz.getName();
		this.key = buildKey(resourceID, resourceClassPackageAndName);
	}

	public ResourceKey(String key) {
		int separatorIndex = key.indexOf(SEPARATOR);
		this.key = key;
		this.resourceID = Integer.parseInt(key.substring(separatorIndex+1));
		this.resourceClassPackageAndName = key.substring(0, separatorIndex-1);
		String[] split = resourceClassPackageAndName.split("\\.");
		this.resourceClassName = split[split.length-1];
	}

	private static String buildKey(int resourceID, String resourceClassPackageAndName) {
		return resourceClassPackageAndName + SEPARATOR + resourceID;
	}

	public String getKey() {
		return key;
	}

	public int getResourceID() {
		return resourceID;
	}

	@JsonIgnore
	public Class<R> getResourceClass() throws ClassNotFoundException {
		return (Class<R>) Class.forName(resourceClassPackageAndName);
	}


	public String getResourceClassName() {
		return resourceClassName;
	}

	public String getResourceClassPackageAndName() {
		return resourceClassPackageAndName;
	}

	@Override
	public String toString() {
		return resourceClassName + SEPARATOR + resourceID;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
				append(key).
				toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}

		ResourceKey resourceKey = (ResourceKey) obj;
		// if deriving: appendSuper(super.equals(obj)).
		return new EqualsBuilder().
				append(key, resourceKey.key).
				isEquals();
	}
}
