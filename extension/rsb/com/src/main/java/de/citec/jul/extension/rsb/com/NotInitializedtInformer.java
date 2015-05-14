/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import rsb.Event;
import rsb.Scope;

/**
 *
 * @author mpohling
 * @param <M>
 */
public class NotInitializedtInformer<M extends GeneratedMessage> implements RSBInformerInterface<M> {

    private final Scope scope;
    
    public NotInitializedtInformer() {
        this.scope = null;
    }

    public NotInitializedtInformer(Scope scope) {
        this.scope = scope;
    }
    
    @Override
    public Event send(Event event) throws CouldNotPerformException {
        throw new InvalidStateException("Informer not initialized!");
    }

    @Override
    public Event send(M data) throws CouldNotPerformException {
        throw new InvalidStateException("Informer not initialized!");
    }

    @Override
    public Class<?> getTypeInfo() throws NotAvailableException {
        throw new NotAvailableException("type info", new InvalidStateException("Informer not initialized!"));
    }

    @Override
    public void setTypeInfo(Class<M> typeInfo) throws CouldNotPerformException {
        throw new InvalidStateException("Informer not initialized!");
    }

    @Override
    public Scope getScope() throws NotAvailableException {
        if(scope == null) {
            throw new NotAvailableException("scope", new InvalidStateException("Informer not initialized!"));
        }
        return scope;
    }

    @Override
    public void activate() throws CouldNotPerformException {
        throw new InvalidStateException("Informer not initialized!");
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        throw new InvalidStateException("Informer not initialized!");
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
