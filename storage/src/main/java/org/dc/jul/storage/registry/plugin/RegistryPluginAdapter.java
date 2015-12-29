package org.dc.jul.storage.registry.plugin;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.RejectedException;
import org.dc.jul.iface.Identifiable;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public abstract class RegistryPluginAdapter<KEY, ENTRY extends Identifiable<KEY>> implements RegistryPlugin<KEY, ENTRY> {

    @Override
    public void beforeRegister(ENTRY entry) throws RejectedException {
    }

    @Override
    public void afterRegister(ENTRY entry) throws CouldNotPerformException {
    }

    @Override
    public void beforeUpdate(ENTRY entry) throws RejectedException {
    }

    @Override
    public void afterUpdate(ENTRY entry) throws CouldNotPerformException {
    }

    @Override
    public void beforeRemove(ENTRY entry) throws RejectedException {
    }

    @Override
    public void afterRemove(ENTRY entry) throws CouldNotPerformException {
    }

    @Override
    public void beforeClear() throws CouldNotPerformException {
    }

    @Override
    public void beforeGet(KEY key) throws RejectedException {
    }

    @Override
    public void beforeGetEntries() throws CouldNotPerformException {
    }

    @Override
    public void checkAccess() throws RejectedException {
    }

    @Override
    public void shutdown() {
    }
}
