package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.RejectedException;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.iface.Writable;
import org.openbase.jul.iface.provider.NameProvider;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public interface Registry<KEY, ENTRY extends Identifiable<KEY>> extends Writable, Observable<Map<KEY, ENTRY>>, NameProvider {

    /**
     * Method returns the name of this registry.
     *
     * @return the name as string.
     * @throws org.openbase.jul.exception.NotAvailableException is thrown if the name is not available.
     */
    @Override
    public String getName() throws NotAvailableException;

    /**
     * Method registers the given entry.
     *
     * @param entry the new entry to register which is not yet included in the registry
     * @return the registered entry updated by all consistency checks this registry provides.
     * @throws CouldNotPerformException is thrown if the given entry is not valid, already registered or something else went wrong during the registration.
     */
    public ENTRY register(final ENTRY entry) throws CouldNotPerformException;

    public ENTRY update(final ENTRY entry) throws CouldNotPerformException;

    public ENTRY remove(final KEY key) throws CouldNotPerformException;

    public ENTRY remove(final ENTRY entry) throws CouldNotPerformException;

    public ENTRY get(final KEY key) throws CouldNotPerformException;

    default public ENTRY get(final ENTRY entry) throws CouldNotPerformException {
        return get(entry.getId());
    }

    /**
     * Copies all entries into a list.
     *
     * @return a list with all values of the entry map
     * @throws CouldNotPerformException if something fails
     */
    public List<ENTRY> getEntries() throws CouldNotPerformException;

    /**
     * An unmodifiable map of the current entry map.
     *
     * @return the current entry map of the registry but unmodifiable
     */
    public Map<KEY, ENTRY> getEntryMap();

    public boolean contains(final ENTRY entry) throws CouldNotPerformException;

    public boolean contains(final KEY key) throws CouldNotPerformException;

    public void clear() throws CouldNotPerformException;

    /**
     * Method returns the amount of registry entries.
     *
     * @return the count of entries as integer.
     */
    public int size();

    public boolean isReadOnly();

    public boolean isConsistent();

    public boolean isSandbox();

    /**
     * This method checks if the registry is not handling any tasks or notification and is currently consistent.
     *
     * @return Returns true if this registry is consistent and not busy.
     */
    public boolean isReady();

    /**
     * Return true if this registry does not contain any entries.
     *
     * @return is true if the registry is empty.
     */
    public boolean isEmpty();

    /**
     * Return true if this registry does not contain any entries.
     *
     * @return is true if the registry is empty.
     * @deprecated since 1.3: removed because of typo. Please use isEmpty() instead!
     */
    @Deprecated
    default public boolean isEmtpy() {
        return isEmpty();
    }

    /**
     * Try to acquire the write lock for this registry.
     * If this method returns true then this thread now holds the write
     * lock for this registry. If it returns false then the lock could
     * not currently be acquired.
     *
     * @return If the lock could be acquired.
     * @throws RejectedException If the registry does not support to be locked externally which is for example the case for remote registries.
     */
    public boolean tryLockRegistry() throws RejectedException;

    /**
     * Try to acquire the write lock for this registry and the registries it depends
     * on recursively. This method returns true if all locks could be acquired and
     * false if at least one lock could not be acquired. In any case the set lockedRegistries
     * contains any locked registry afterwards. So if the method returns false any registry
     * inside this set should be unlocked before trying it again to prevent dead locks.
     * Additionally this method should only be performed while already holding the write lock
     * for the registry. Else it can happen that a thread that tries to recursively lock the
     * same registry by using the same set as another thread thinks that he acquired it even though the other
     * thread is currently in the process of acquiring it. 
     * 
     * @param lockedRegistries Set of already locked registries. Should be given empty to this method and will contain all locked registries afterwards.
     * @return If all registries could be locked.
     * @throws RejectedException If the registry does not support to be locked externally which is for example the case for remote registries.
     */
    public boolean recursiveTryLockRegistry(final Set<Registry> lockedRegistries) throws RejectedException;

    /**
     * Unlock the write lock of the registry.
     */
    public void unlockRegistry();

    public void addDependencyObserver(final Observer<Map<KEY, ENTRY>> observer);

    public void removeDependencyObserver(final Observer<Map<KEY, ENTRY>> observer);
}
