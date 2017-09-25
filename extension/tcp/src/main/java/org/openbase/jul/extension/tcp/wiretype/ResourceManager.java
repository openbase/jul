/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.tcp.wiretype;

import de.dc.bco.lib.action.tags.Tag;
import de.dc.bco.lib.action.tags.TagLoader;
import de.dc.bco.lib.action.tags.TagProvider;
import de.dc.util.exceptions.DoesNotExistException;
import de.dc.util.exceptions.DuplicatedInstanceException;
import de.dc.util.logging.Logger;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author divine
 */
public class ResourceManager implements TagProvider {

	public static final String RESOURCE_ADDED = "ResourceAdded";
	private final Object mapLock = new Object();
	private static ResourceManager instance;
	private final Map<ResourceKey, AbstractResource> resourceMap;
	private final DataSaveManager dataSaveManager;
	private final PropertyChangeSupport change;

	private ResourceManager(final String saveFilePath, final String saveFileType) {
		this.resourceMap = new HashMap<ResourceKey, AbstractResource>();
		this.dataSaveManager = new DataSaveManager(saveFilePath, saveFileType);
		this.change = new PropertyChangeSupport(this);
	}

	public static synchronized ResourceManager init(final String saveFilePath, final String saveFileType) throws DuplicatedInstanceException {
		if (instance != null) {
			throw new DuplicatedInstanceException(instance);
		}
		instance = new ResourceManager(saveFilePath, saveFileType);
		return instance;
	}

	public Collection<AbstractResource> getResources() {
		return Collections.unmodifiableCollection(resourceMap.values());
	}

	public void addResource(AbstractResource resource) throws DuplicatedInstanceException {
		Logger.debug(this, "Add " + resource);

		synchronized (mapLock) {
			if (resourceMap.containsKey(resource.getResourceKey())) {
				throw new DuplicatedInstanceException(resource);
			}

			resourceMap.put(resource.getResourceKey(), resource);
			dataSaveManager.putResource(resource);
			change.firePropertyChange(RESOURCE_ADDED, null, resource);
		}
	}

	public AbstractResourceData getResourceData(final ResourceKey key) throws ResourceDataNotAvailableException {
		Logger.debug(this, "Order ResourceData[" + key + "]");
		return dataSaveManager.getResourceData(key);
	}

	public AbstractResource getResource(final ResourceKey key) throws ResourceNotAvailableException {
		Logger.debug(this, "Order Resource[" + key + "]");
		synchronized (mapLock) {
			if (!resourceMap.containsKey(key)) {
				throw new ResourceNotAvailableException(key);
			}
			return resourceMap.get(key);
		}
	}

	@Override
	public <T extends Tag> T getFirstResourceByTag(final String name, final Class<T> tagClazz) throws ResourceNotAvailableException {
		synchronized (mapLock) {
			return TagLoader.getFirstResourceByTag(name, tagClazz, this, resourceMap.values());
		}
	}

	@Override
	public <T extends Tag> List<T> getResourcesByTag(final String name, final Class<T> tagClazz) throws ResourceNotAvailableException {
		synchronized (mapLock) {
			return TagLoader.getResourcesByTag(name, tagClazz, this, resourceMap.values());
		}
	}

	public <T extends AbstractResource> List<T> getResourcesByClass(final Class<T> resourceClazz) throws ResourceNotAvailableException {
		List<T> resourceList = new ArrayList<T>();
		synchronized (mapLock) {
			for (AbstractResource resource : resourceMap.values()) {
				if(resource.getClass().equals(resourceClazz)) {
					resourceList.add((T) resource);
				}
			}
		}
		return resourceList;
	}

	public Collection<Tag> getTags() {
		synchronized (mapLock) {
			List<Tag> tags = new ArrayList<Tag>();
			for (AbstractResource resource : resourceMap.values()) {
				if (resource instanceof Tag) {
					tags.add((Tag) resource);
				}
			}
			return tags;
		}
	}

	public void crashSave() {
		dataSaveManager.crashSave();
	}

	public synchronized void reset() {
		synchronized (mapLock) {
			dataSaveManager.finalSave();
			resourceMap.clear();
		}
	}

	public static synchronized ResourceManager getInstance() throws DoesNotExistException {
		if (instance == null) {
			throw new DoesNotExistException(ResourceManager.class.getSimpleName() + " not initialized!");
		}
		return instance;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		change.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		change.removePropertyChangeListener(listener);
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
}
