/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.com;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.NotAvailableException;
import static org.dc.jul.extension.rsb.com.AbstractConfigurableController.FIELD_SCOPE;
import org.dc.jul.iface.Configurable;
import org.dc.jul.pattern.Observable;
import org.dc.jul.pattern.Observer;
import rst.rsb.ScopeType;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * @param <M>
 * @param <CONFIG>
 */
public abstract class AbstractConfigurableRemote<M extends GeneratedMessage, CONFIG extends GeneratedMessage> extends AbstractIdentifiableRemote<M> implements Configurable<String, CONFIG> {

    protected CONFIG config;
    private final Observable<CONFIG> configObservable;

    public AbstractConfigurableRemote() {
        this.configObservable = new Observable<>(true);
    }

    public void init(final CONFIG config) throws InitializationException {
        try {
            this.config = config;
            super.init(detectScope());
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    @Override
    public CONFIG updateConfig(final CONFIG config) throws CouldNotPerformException {
        this.config = (CONFIG) config.toBuilder().mergeFrom(config).build();
        this.configObservable.notifyObservers(config);
        return this.config;
    }

    private ScopeType.Scope detectScope() throws NotAvailableException {
        try {
            return (ScopeType.Scope) getConfigField(FIELD_SCOPE);
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
        if (config == null) {
            throw new NotAvailableException("config");
        }
        return config;
    }

    public void addConfigObserver(final Observer<CONFIG> observer) {
        configObservable.addObserver(observer);
    }

    public void removeConfigObserver(final Observer<CONFIG> observer) {
        configObservable.removeObserver(observer);
    }
}
