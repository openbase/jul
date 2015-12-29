/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.NotSupportedException;
import org.dc.jul.storage.registry.plugin.FileRegistryPlugin;
import org.dc.jul.iface.Identifiable;
import org.dc.jul.storage.registry.clone.RegistryCloner;
import java.util.Map;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <R>
 */
public class FileSynchronizedRegistrySandbox<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends FileSynchronizedRegistryInterface<KEY, ENTRY, R>> extends RegistrySandbox<KEY, ENTRY, MAP, R, FileRegistryPlugin<KEY, ENTRY>> implements FileSynchronizedRegistryInterface<KEY, ENTRY, R> {

    public FileSynchronizedRegistrySandbox(MAP entryMap, RegistryCloner<KEY, ENTRY, MAP> cloner) throws CouldNotPerformException {
        super(entryMap, cloner);
    }

    public FileSynchronizedRegistrySandbox(final MAP entryMap) throws CouldNotPerformException {
        super(entryMap);
    }

    @Override
    public void loadRegistry() throws CouldNotPerformException {
    }

    @Override
    public void saveRegistry() throws CouldNotPerformException {
    }

    @Override
    public Integer getDBVersion() throws NotAvailableException {
        throw new NotAvailableException("dbversion", new NotSupportedException("getDBVersion", this));
    }
}
