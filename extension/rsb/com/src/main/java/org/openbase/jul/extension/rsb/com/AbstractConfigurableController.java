package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import com.google.protobuf.GeneratedMessage;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import static org.openbase.jul.iface.provider.LabelProvider.TYPE_FIELD_LABEL;
import org.openbase.jul.pattern.ConfigurableController;
import org.openbase.jul.schedule.SyncObject;
import rst.rsb.ScopeType.Scope;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <M> the message type
 * @param <MB> builder of the message M
 * @param <CONFIG> the configuration data type
 */
public abstract class AbstractConfigurableController<M extends GeneratedMessage, MB extends M.Builder<MB>, CONFIG extends GeneratedMessage> extends AbstractIdentifiableController<M, MB> implements ConfigurableController<String, M, CONFIG> {

    public static final String FIELD_SCOPE = "scope";

    private final SyncObject CONFIG_LOCK = new SyncObject("ConfigLock");

    private CONFIG config;

    private Scope currentScope;

    public AbstractConfigurableController(MB builder) throws InstantiationException {
        super(builder);
    }

    /**
     * Initialize the controller with a configuration.
     *
     * @param config the configuration
     * @throws InitializationException if the initialization fails
     * @throws java.lang.InterruptedException if the initialization is interrupted
     */
    @Override
    public void init(final CONFIG config) throws InitializationException, InterruptedException {
        synchronized (CONFIG_LOCK) {
            try {
                if (config == null) {
                    throw new NotAvailableException("config");
                }
                currentScope = detectScope(config);
                applyConfigUpdate(config);
                super.init(currentScope);
            } catch (CouldNotPerformException ex) {
                throw new InitializationException(this, ex);
            }
        }
    }

    /**
     * Apply an update to the configuration of this controller.
     *
     * @param config the updated configuration
     * @return the updated configuration
     * @throws CouldNotPerformException if the update could not be performed
     * @throws InterruptedException if the update has been interrupted
     */
    @Override
    public CONFIG applyConfigUpdate(final CONFIG config) throws CouldNotPerformException, InterruptedException {
        synchronized (CONFIG_LOCK) {
            try {
                this.config = config;

                if (supportsDataField(TYPE_FIELD_ID) && hasConfigField(TYPE_FIELD_ID)) {
                    setDataField(TYPE_FIELD_ID, getConfigField(TYPE_FIELD_ID));
                }

                if (supportsDataField(TYPE_FIELD_LABEL) && hasConfigField(TYPE_FIELD_LABEL)) {
                    setDataField(TYPE_FIELD_LABEL, getConfigField(TYPE_FIELD_LABEL));
                }

                // detect scope change if instance is already active and reinit if needed.
                try {
                    if (isActive() && !currentScope.equals(detectScope(config))) {
                        currentScope = detectScope();
                        super.init(currentScope);
                    }
                } catch (CouldNotPerformException ex) {
                    throw new CouldNotPerformException("Could not verify scope changes!", ex);
                }

                return this.config;
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not apply config update!", ex);
            }
        }
    }

    private Scope detectScope(final CONFIG config) throws NotAvailableException {
        try {
            return (Scope) getConfigField(FIELD_SCOPE, config);
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("scope");
        }
    }

    private Scope detectScope() throws NotAvailableException {
        synchronized (CONFIG_LOCK) {
            return detectScope(getConfig());
        }
    }

    protected final Object getConfigField(String name) throws CouldNotPerformException {
        synchronized (CONFIG_LOCK) {
            return getConfigField(name, getConfig());
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
        synchronized (CONFIG_LOCK) {
            try {
                Descriptors.FieldDescriptor findFieldByName = config.getDescriptorForType().findFieldByName(name);
                if (findFieldByName == null) {
                    return false;
                }
                return config.hasField(findFieldByName);
            } catch (Exception ex) {
                return false;
            }
        }
    }

    protected final boolean supportsConfigField(final String name) throws CouldNotPerformException {
        synchronized (CONFIG_LOCK) {
            try {
                Descriptors.FieldDescriptor findFieldByName = config.getDescriptorForType().findFieldByName(name);
                return findFieldByName != null;
            } catch (NullPointerException ex) {
                return false;
            }
        }
    }

    @Override
    public CONFIG getConfig() throws NotAvailableException {
        synchronized (CONFIG_LOCK) {
            if (config == null) {
                throw new NotAvailableException("config");
            }
            return config;
        }
    }
}
