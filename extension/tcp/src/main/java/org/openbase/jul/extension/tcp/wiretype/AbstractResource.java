/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.tcp.wiretype;

import de.dc.util.exceptions.DoesNotExistException;
import de.dc.util.exceptions.DuplicatedInstanceException;
import de.dc.util.logging.Logger;
import de.dc.util.tools.ObjectManipulator;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author divine
 */
public abstract class AbstractResource<C extends AbstractResourceConfig, D extends AbstractResourceData<D>, P extends AbstractResource> implements AbstractResourceInterface {
	// Resouce change Events

	// TODO move property change handling in own class struct
	protected final Logger LOGGER = Logger.getLogger(getClass());
	public final P ROOT_RESOURCE = null;
	public static final String DATA_UPDATE = "DataUpdate";
	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 10, 1, TimeUnit.HOURS, new ArrayBlockingQueue<Runnable>(10));
	private transient final ResourceManager resourceManager;
	private final P parentResource;
	protected final C config;
	protected final D data;
	private final LinkedList<ResourceListener> listeners;

	public AbstractResource(final C config, final P parentResource) throws ResourceCreationException {
		this.config = config;
		if (parentResource == ROOT_RESOURCE) {
			this.parentResource = (P) this;
		} else {
			this.parentResource = parentResource;
		}

		try {
			resourceManager = ResourceManager.getInstance();
		} catch (DoesNotExistException ex) {
			throw new ResourceCreationException(config, ex);
		}

		Logger.debug(this, "Load " + this);
		this.data = createDataInstance();
		this.listeners = new LinkedList<ResourceListener>();

		try {
			resourceManager.addResource(this);
		} catch (DuplicatedInstanceException ex) {
			throw new ResourceCreationException(config, ex);
		}
	}

	private D createDataInstance() throws ResourceCreationException {
		D resourceData;
		try {
			resourceData = (D) resourceManager.getResourceData(getResourceKey());
		} catch (ResourceDataNotAvailableException e) {
			Logger.debug(this, "Could not load data instance. Create new instance of [" + getClass().getName() + "Data" + "].");

			try {
				resourceData = (D) Class.forName(getClass().getName() + "Data").newInstance();
			} catch (Exception ex) {
				throw new ResourceCreationException("Could not create data instance of " + config + "!", ex);
			}
		}

		if (resourceData == null) {
			throw new ResourceCreationException("Could not create data instance!", config);
		}

		return resourceData;
	}

	@Override
	public int getId() {
		return config.getId();
	}

	@Override
	public String getName() {
		return config.getName();
	}

	public P getParentResource() {
		return parentResource;
	}

	public D getData() {
		return data;
	}

	public D getClone() {
		try {
			return ObjectManipulator.deepCopy(data);
		} catch (Exception ex) {
			LOGGER.warn("Could not clone " + data);
			return null;
		}
	}

	public C getConfig() {
		return config;
	}

	@Override
	public final ResourceKey getResourceKey() {
		return config.getResourceKey();
	}

	protected void notifyDataUpdate() {
		notifyChanges(DATA_UPDATE);
	}

	protected void notifyChanges(final String changeEvent) {
//		List<ResourceListener> iterator;#

		synchronized (listeners) {
//			iterator = listeners.iterator();
			final D dataCopy = getClone();
			for (final ResourceListener listener : listeners) {
				try {
					executor.submit(new Runnable() {
						@Override
						public void run() {
							listener.resourceChanged(new ResourceChangeEvent(changeEvent, dataCopy, AbstractResource.this));
						}
					}).get(10, TimeUnit.SECONDS);
				} catch (Exception ex) {
					LOGGER.error("Could not notify data changes to " + listener, ex);
				}
			}
		}
	}

	public boolean addListener(final ResourceListener listener) {
		synchronized (listeners) {
			final boolean success = listeners.add(listener);
			listener.resourceChanged(new ResourceChangeEvent(DATA_UPDATE, getClone(), this));
			return success;
		}
	}

	public boolean removeListener(final ResourceListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + config + " | " + data + "]";
	}
}
