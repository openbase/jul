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
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import static org.dc.jul.extension.rsb.com.AbstractConfigurableController.FIELD_SCOPE;
import org.dc.jul.pattern.ConfigurableRemote;
import org.dc.jul.pattern.ObservableImpl;
import org.dc.jul.pattern.Observer;
import rst.rsb.ScopeType;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * @param <M>
 * @param <CONFIG>
 */
public abstract class AbstractConfigurableRemote<M extends GeneratedMessage, CONFIG extends GeneratedMessage> extends AbstractIdentifiableRemote<M> implements ConfigurableRemote<String, M, CONFIG> {

    protected CONFIG config;
    private final ObservableImpl<CONFIG> configObservable;

    public AbstractConfigurableRemote() {
        this.configObservable = new ObservableImpl<>(true);
    }

    @Override
    public void init(final CONFIG config) throws InitializationException, InterruptedException {
        try {
            this.config = config;
            super.init(detectScope());
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    protected CONFIG applyConfigUpdate(final CONFIG config) throws CouldNotPerformException, InterruptedException {
        this.config = (CONFIG) config.toBuilder().mergeFrom(config).build();
        configObservable.notifyObservers(config);
        try {
            notifyConfigUpdate(config);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify config update!", ex), logger);
        }
        return this.config;
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

    @Override
    public void addConfigObserver(final Observer<CONFIG> observer) {
        configObservable.addObserver(observer);
    }

    @Override
    public void removeConfigObserver(final Observer<CONFIG> observer) {
        configObservable.removeObserver(observer);
    }

    @Override
    public void shutdown() {
        configObservable.shutdown();
        super.shutdown();
    }
}
