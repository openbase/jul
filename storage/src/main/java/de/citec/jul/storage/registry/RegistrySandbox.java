/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.storage.registry.plugin.RegistryPlugin;
import java.util.Map;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <R>
 * @param <P>
 */
public class RegistrySandbox<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends RegistryInterface<KEY, ENTRY, R>, P extends RegistryPlugin> extends AbstractRegistry<KEY, ENTRY, MAP, R, P> implements RegistrySandboxInterface<KEY, ENTRY, MAP, R> {

    public RegistrySandbox(MAP entryMap) throws InstantiationException {
        super(entryMap, new MockSandbox<KEY, ENTRY, MAP, R>());
    }

    @Override
    public void sync(MAP map) {
        entryMap.clear();
        entryMap.putAll(map);
    }

    public static class MockSandbox<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends RegistryInterface<KEY, ENTRY, R>> implements RegistrySandboxInterface<KEY, ENTRY, MAP, R> {

        public MockSandbox() {
        }

        @Override
        public ENTRY register(ENTRY entry) throws CouldNotPerformException {
            return entry;
        }

        @Override
        public ENTRY update(ENTRY entry) throws CouldNotPerformException {
            return entry;
        }

        @Override
        public ENTRY remove(KEY key) throws CouldNotPerformException {
            return null;
        }

        @Override
        public void sync(MAP map) {
        }

        @Override
        public void registerConsistencyHandler(ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler) throws CouldNotPerformException {
        }
    }
}
