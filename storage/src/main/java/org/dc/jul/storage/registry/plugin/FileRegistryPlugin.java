/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry.plugin;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.RejectedException;
import org.dc.jul.iface.Identifiable;
import org.dc.jul.storage.file.FileSynchronizer;

/**
 *
 * @author <a href="mailto:MarianPohling@cit-ec.uni-bielefeld.de">mpohling</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public interface FileRegistryPlugin<KEY, ENTRY extends Identifiable<KEY>> extends RegistryPlugin<KEY, ENTRY> {

    public void beforeRegister(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws RejectedException;

    public void afterRegister(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void beforeRemove(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws RejectedException;

    public void afterRemove(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void beforeUpdate(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws RejectedException;

    public void afterUpdate(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void beforeGet(final KEY key, final FileSynchronizer fileSynchronizer) throws RejectedException;

}
