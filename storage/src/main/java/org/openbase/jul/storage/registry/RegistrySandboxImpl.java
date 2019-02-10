package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.storage.registry.clone.RITSCloner;
import org.openbase.jul.storage.registry.clone.RegistryCloner;
import org.openbase.jul.storage.registry.plugin.RegistryPlugin;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <REGISTRY>
 * @param <P>
 */
public class RegistrySandboxImpl<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, REGISTRY extends Registry<KEY, ENTRY>, P extends RegistryPlugin<KEY, ENTRY, REGISTRY>> extends AbstractRegistry<KEY, ENTRY, MAP, REGISTRY, P> implements RegistrySandbox<KEY, ENTRY, MAP, REGISTRY> {

    private RegistryCloner<KEY, ENTRY, MAP> cloner;
    private Registry<KEY, ENTRY> originRegistry;

    public RegistrySandboxImpl(final MAP entryMap, final REGISTRY originRegistry) throws CouldNotPerformException, InterruptedException {
        this(entryMap, new RITSCloner<>(), originRegistry);
    }

    public RegistrySandboxImpl(final MAP entryMap, final RegistryCloner<KEY, ENTRY, MAP> cloner, final REGISTRY originRegistry) throws CouldNotPerformException, InterruptedException {
        super(cloner.deepCloneRegistryMap(entryMap));
        this.cloner = cloner;
        this.originRegistry = originRegistry;
    }

    @Override
    public void replaceInternalMap(Map<KEY, ENTRY> map, boolean finishTransaction) throws CouldNotPerformException {
        super.replaceInternalMap(cloner.deepCloneMap(map), finishTransaction);
    }

    @Override
    public ENTRY superRemove(ENTRY entry) throws CouldNotPerformException {
        return super.superRemove(cloner.deepCloneEntry(entry));
    }

    @Override
    public ENTRY update(ENTRY entry) throws CouldNotPerformException {
        return super.update(cloner.deepCloneEntry(entry));
    }

    @Override
    public ENTRY register(ENTRY entry) throws CouldNotPerformException {
        return super.register(cloner.deepCloneEntry(entry));
    }

    @Override
    public void sync(MAP map) {
        try {
            replaceInternalMap(map, false);
            consistent = true;
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new FatalImplementationErrorException("Sandbox sync failed!", this, ex), logger);
        }
    }

    @Override
    public void addObserver(Observer<DataProvider<Map<KEY, ENTRY>>, Map<KEY, ENTRY>> observer) {
        logger.warn("Observer registration on sandbox instance skiped!");
    }

    @Override
    protected void finishTransaction() throws CouldNotPerformException {
        try {
            checkConsistency();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Given transaction is invalid because " + this + " consistency check failed!", ex);
        }
    }

    @Override
    public boolean isSandbox() {
        return true;
    }

    @Override
    public String toString() {
        return originRegistry + "Sandbox";
    }
}
