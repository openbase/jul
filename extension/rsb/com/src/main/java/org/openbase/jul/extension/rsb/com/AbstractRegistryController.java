/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.rsb.com;

import com.google.protobuf.GeneratedMessage;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;

/**
 *
 * @author divine
 * @param <M>
 * @param <MB>
 */
public abstract class AbstractRegistryController<M extends GeneratedMessage, MB extends M.Builder<MB>> extends RSBCommunicationService<M, MB> {

    protected ProtoBufJSonFileProvider protoBufJSonFileProvider = new ProtoBufJSonFileProvider();

    public AbstractRegistryController(MB builder) throws InstantiationException {
        super(builder);
        this.protoBufJSonFileProvider = new ProtoBufJSonFileProvider();
    }

    @Override
    protected void postInit() throws InitializationException, InterruptedException {
        super.postInit();
        try {
            try {
                activateVersionControl();
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not activate version control for all internal registries!", ex);
            }
            try {
                loadRegistries();
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not load all internal registries!", ex);
            }
            try {
                registerConsistencyHandler();
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not register consistency handler for all internal registries!", ex);
            }
            try {
                registerObserver();
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not register observer for all internal registries!", ex);
            }
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    @Override
    public void activate() throws InterruptedException, CouldNotPerformException {
        try {
            super.activate();
            activateRegistries();
            registerDependencies();
            performInitialConsistencyCheck();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not activate location registry!", ex);
        }
    }
    
    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {
        super.deactivate();
        removeDependencies();
    }

    protected abstract void activateVersionControl() throws CouldNotPerformException;

    protected abstract void loadRegistries() throws CouldNotPerformException;

    protected abstract void registerConsistencyHandler() throws CouldNotPerformException;

    protected abstract void registerObserver() throws CouldNotPerformException;
    
    protected abstract void registerDependencies() throws CouldNotPerformException;
    
    protected abstract void removeDependencies() throws CouldNotPerformException;
    
    protected abstract void performInitialConsistencyCheck() throws CouldNotPerformException;
    
    protected abstract void activateRegistries() throws CouldNotPerformException, InterruptedException;
    
    protected abstract void deactivateRegistries() throws CouldNotPerformException, InterruptedException;
    
    
    
}
