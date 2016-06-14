package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import rst.rsb.ScopeType.Scope;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine
 * Threepwood</a>
 * @param <M>
 * @param <MB>
 * @param <CONFIG>
 */
public abstract class AbstractConfigurableController<M extends GeneratedMessage, MB extends M.Builder<MB>, CONFIG extends GeneratedMessage> extends AbstractIdentifiableController<M, MB> implements ConfigurableController<String, M, MB, CONFIG> {

    public static final String FIELD_SCOPE = "scope";

    private CONFIG config;

    public AbstractConfigurableController(MB builder) throws InstantiationException {
        super(builder);
    }

    /**
     *
     * @param config
     * @throws InitializationException
     * @throws java.lang.InterruptedException
     */
    @Override
    public void init(final CONFIG config) throws InitializationException, InterruptedException {
        try {
            if (config == null) {
                throw new NotAvailableException("config");
            }
            applyConfigUpdate(config);
            super.init(detectScope());
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    /**
     *
     * @param config
     * @return
     * @throws CouldNotPerformException
     * @throws InterruptedException
     */
    @Override
    public CONFIG applyConfigUpdate(final CONFIG config) throws CouldNotPerformException, InterruptedException {
        this.config = config;

        if (supportsDataField(TYPE_FIELD_ID) && hasConfigField(TYPE_FIELD_ID)) {
            setDataField(TYPE_FIELD_ID, getConfigField(TYPE_FIELD_ID));
        }

        if (supportsDataField(TYPE_FIELD_LABEL) && hasConfigField(TYPE_FIELD_LABEL)) {
            setDataField(TYPE_FIELD_LABEL, getConfigField(TYPE_FIELD_LABEL));
        }

        return this.config;
    }

    private Scope detectScope() throws NotAvailableException {
        try {
            return (Scope) getConfigField(FIELD_SCOPE);
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException(scope);
        }
    }

    protected final Object getConfigField(String name) throws CouldNotPerformException {
        try {
            final CONFIG currentConfig = getConfig();
            Descriptors.FieldDescriptor findFieldByName = currentConfig.getDescriptorForType().findFieldByName(name);
            if (findFieldByName == null) {
                throw new NotAvailableException("Field[" + name + "] does not exist for type " + currentConfig.getClass().getName());
            }
            return currentConfig.getField(findFieldByName);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not return value of config field [" + name + "] for " + this, ex);
        }
    }

    protected final boolean hasConfigField(final String name) throws CouldNotPerformException {
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

    protected final boolean supportsConfigField(final String name) throws CouldNotPerformException {
        try {
            Descriptors.FieldDescriptor findFieldByName = config.getDescriptorForType().findFieldByName(name);
            return findFieldByName != null;
        } catch (NullPointerException ex) {
            return false;
        }
    }

    @Override
    public CONFIG getConfig() throws NotAvailableException {
        if (config == null) {
            throw new NotAvailableException("config");
        }
        return config;
    }
}
