/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

/**
 *
 * @author mpohling
 */
public class EntryModification extends Exception {

    private final Object entry;
    private ConsistencyHandler consistencyHandler;

    public EntryModification(final Object entry, final ConsistencyHandler consistencyHandler) {
        super(entry + " has been modified by "+consistencyHandler.getClass().getSimpleName()+".");
        this.entry = entry;
        this.consistencyHandler = consistencyHandler;
    }

    public Object getEntry() {
        return entry;
    }

    public ConsistencyHandler getConsistencyHandler() {
        return consistencyHandler;
    }
}
