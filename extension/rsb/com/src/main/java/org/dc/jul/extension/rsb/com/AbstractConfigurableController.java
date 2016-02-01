package org.dc.jul.extension.rsb.com;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Configurable;
import rst.rsb.ScopeType.Scope;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <M>
 * @param <MB>
 * @param <CONFIG>
 */
public abstract class AbstractConfigurableController<M extends GeneratedMessage, MB extends M.Builder<MB>, CONFIG extends GeneratedMessage> extends AbstractIdentifiableController<M, MB> implements Configurable<String, CONFIG> {

    public static final String FIELD_SCOPE = "scope";

    protected CONFIG config;

    public AbstractConfigurableController(MB builder) throws InstantiationException {
        super(builder);
    }

    public void init(final CONFIG config) throws InitializationException {
        try {
            updateConfig(config);
            super.init(detectScope());
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    @Override
    public CONFIG updateConfig(final CONFIG config) throws CouldNotPerformException {
        this.config = (CONFIG) config.toBuilder().mergeFrom(config).build();
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

    @Override
    public CONFIG getConfig() throws NotAvailableException {
        if(config == null) {
            throw new NotAvailableException("config");
        }
        return config;
    }
}
