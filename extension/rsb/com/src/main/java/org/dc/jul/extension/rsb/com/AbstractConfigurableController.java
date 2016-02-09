package org.dc.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Configurable;
import org.dc.jul.iface.Manageable;
import static org.dc.jul.iface.provider.LabelProvider.TYPE_FILED_LABEL;
import rst.rsb.ScopeType.Scope;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <M>
 * @param <MB>
 * @param <CONFIG>
 */
public abstract class AbstractConfigurableController<M extends GeneratedMessage, MB extends M.Builder<MB>, CONFIG extends GeneratedMessage> extends AbstractIdentifiableController<M, MB> implements Configurable<String, CONFIG>, Manageable<CONFIG> {

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
            updateConfig(config);
            super.init(detectScope());
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    @Override
    public CONFIG updateConfig(final CONFIG config) throws CouldNotPerformException {
        if (this.config == null) {
            this.config = config;
        } else {
            this.config = (CONFIG) this.config.toBuilder().mergeFrom(config).build();
        }

        if (supportsField(TYPE_FILED_ID) && hasConfigField(TYPE_FILED_ID)) {
            setField(TYPE_FILED_ID, getConfigField(TYPE_FILED_ID));
        }

        if (supportsField(TYPE_FILED_LABEL) && hasConfigField(TYPE_FILED_LABEL)) {
            setField(TYPE_FILED_LABEL, getConfigField(TYPE_FILED_LABEL));
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
