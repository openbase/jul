package org.openbase.jul.communication.controller;

/*
 * #%L
 * JUL Extension Controller
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
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.JPTestMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import static org.openbase.jul.communication.controller.AbstractConfigurableController.FIELD_SCOPE;
import org.openbase.jul.iface.Configurable;
import org.openbase.jul.pattern.controller.ConfigurableRemote;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.type.communication.ScopeType.Scope;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import static org.openbase.jul.iface.provider.LabelProvider.TYPE_FIELD_LABEL;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <M>
 * @param <CONFIG>
 */
public abstract class AbstractConfigurableRemote<M extends Message, CONFIG extends Message> extends AbstractIdentifiableRemote<M> implements ConfigurableRemote<String, M, CONFIG>, Configurable<String, CONFIG> {

    private final ReentrantReadWriteLock CONFIG_LOCK = new ReentrantReadWriteLock(true);

    private volatile String cachedId = null;
    private volatile String cachedLabel = null;

    private final Class<CONFIG> configClass;
    private CONFIG config;
    private Scope currentScope;
    private final ObservableImpl<ConfigurableRemote<String, M, CONFIG>, CONFIG> configObservable;

    public AbstractConfigurableRemote(final Class<M> dataClass, final Class<CONFIG> configClass) {
        super(dataClass);
        this.configClass = configClass;
        this.configObservable = new ObservableImpl<>(true);
    }

    /**
     * {@inheritDoc}
     *
     * @param config {@inheritDoc}
     * @throws InitializationException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void init(final CONFIG config) throws InitializationException, InterruptedException {
        CONFIG_LOCK.writeLock().lock();
        try {
            if (config == null) {
                throw new NotAvailableException("config");
            }
            currentScope = detectScope(config);
            try {
                applyConfigUpdate(config);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not apply config update for " + this, ex, logger);
                try {
                    if (JPService.getProperty(JPTestMode.class).getValue()) {
                        throw new FatalImplementationErrorException("Could not apply config update for " + this, this, ex);
                    }
                } catch (JPNotAvailableException ex1) {
                    // to nothing if property could not be resovled.
                }
            }
            super.init(currentScope);
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        } finally {
            CONFIG_LOCK.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param config {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public CONFIG applyConfigUpdate(final CONFIG config) throws CouldNotPerformException, InterruptedException {
        CONFIG_LOCK.writeLock().lock();
        try {
            this.config = config;
            configObservable.notifyObservers(config);

            // detect scope change if instance is already active and reinit if needed.
            try {
                if (isActive() && !currentScope.equals(detectScope(config))) {
                    currentScope = detectScope();
                    reinit(currentScope);
                }
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not verify scope changes!", ex);
            }

            try {
                notifyConfigUpdate(config);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify config update!", ex), logger);
            }

            // Invalidate ID and label caches since the config has changed
            cachedId = null;
            cachedLabel = null;

            return this.config;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not apply config update!", ex);
        } finally {
            CONFIG_LOCK.writeLock().unlock();
        }
    }

    /**
     * Method can be overwritten to get internally informed about config updates.
     *
     * @param config new arrived config messages.
     * @throws CouldNotPerformException
     */
    protected void notifyConfigUpdate(final CONFIG config) throws CouldNotPerformException {
        // dummy method, please overwrite if needed.
    }

    private Scope detectScope() throws NotAvailableException {
        CONFIG_LOCK.readLock().lock();
        try {
            return detectScope(getConfig());
        } finally {
            CONFIG_LOCK.readLock().unlock();
        }
    }

    private Scope detectScope(final CONFIG config) throws NotAvailableException {
        try {
            return (Scope) getConfigField(FIELD_SCOPE, config);
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("scope");
        }
    }

    protected final Object getConfigField(String name) throws CouldNotPerformException {
        CONFIG_LOCK.readLock().lock();
        try {
            return getConfigField(name, getConfig());
        } finally {
            CONFIG_LOCK.readLock().unlock();
        }
    }

    protected final Object getConfigField(String name, final CONFIG config) throws CouldNotPerformException {
        try {
            Descriptors.FieldDescriptor findFieldByName = config.getDescriptorForType().findFieldByName(name);
            if (findFieldByName == null) {
                throw new NotAvailableException("Field[" + name + "] does not exist for type " + config.getClass().getName());
            }
            return config.getField(findFieldByName);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not return value of config field [" + name + "] for " + this, ex);
        }
    }

    protected final boolean hasConfigField(final String name) throws CouldNotPerformException {
        CONFIG_LOCK.readLock().lock();
        try {
            Descriptors.FieldDescriptor findFieldByName = config.getDescriptorForType().findFieldByName(name);
            if (findFieldByName == null) {
                return false;
            }
            return config.hasField(findFieldByName);
        } catch (Exception ex) {
            return false;
        } finally {
            CONFIG_LOCK.readLock().unlock();
        }
    }

    protected final boolean supportsConfigField(final String name) throws CouldNotPerformException {
        CONFIG_LOCK.readLock().lock();
        try {
            Descriptors.FieldDescriptor findFieldByName = config.getDescriptorForType().findFieldByName(name);
            return findFieldByName != null;
        } catch (NullPointerException ex) {
            return false;
        } finally {
            CONFIG_LOCK.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public CONFIG getConfig() throws NotAvailableException {
        CONFIG_LOCK.readLock().lock();
        try {
            if (config == null) {
                throw new NotAvailableException("config");
            }
            return config;
        } finally {
            CONFIG_LOCK.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public String getId() throws NotAvailableException {
        // Check cache first
        if (cachedId != null) {
            return cachedId;
        }

        try {
            String tmpId = (String) getConfigField(TYPE_FIELD_ID);
            if (tmpId.isEmpty()) {
                throw new InvalidStateException("config.id is empty!");
            }
            // Update cache
            this.cachedId = tmpId;
            return tmpId;
        } catch (CouldNotPerformException ex) {
            logger.debug("Config does not contain the remote id!");
        }
        return super.getId();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Class<CONFIG> getConfigClass() {
        return configClass;
    }

    /**
     * {@inheritDoc}
     *
     * @param observer {@inheritDoc}
     */
    @Override
    public void addConfigObserver(final Observer<ConfigurableRemote<String, M, CONFIG>, CONFIG> observer) {
        configObservable.addObserver(observer);
    }

    /**
     * {@inheritDoc}
     *
     * @param observer {@inheritDoc}
     */
    @Override
    public void removeConfigObserver(final Observer<ConfigurableRemote<String, M, CONFIG>, CONFIG> observer) {
        configObservable.removeObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        configObservable.shutdown();
        super.shutdown();
    }
}
