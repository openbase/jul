package org.openbase.jul.storage.registry.plugin;

/*
 * #%L
 * JUL Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.RejectedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.storage.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <P>
 */
public class RegistryPluginPool<KEY, ENTRY extends Identifiable<KEY>, P extends RegistryPlugin<KEY, ENTRY>> implements RegistryPlugin<KEY, ENTRY> {

    protected final Logger logger = LoggerFactory.getLogger(RegistryPluginPool.class);

    protected final List<P> pluginList;
    protected Registry<KEY, ENTRY> registry;
    private final ReentrantReadWriteLock lock;

    public RegistryPluginPool() {
        this.pluginList = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public void init(Registry<KEY, ENTRY> registry) throws InitializationException {
        this.registry = registry;
    }

    @Override
    public void shutdown() {
        pluginList.stream().forEach((plugin) -> {
            try {
                plugin.shutdown();
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not shutdown RegistryPlugin[" + plugin + "]!", ex), logger, LogLevel.ERROR);
            }
        });
    }

    public void addPlugin(P plugin) throws InitializationException, InterruptedException {
        try {
            plugin.init(registry);
            pluginList.add(plugin);
        } catch (CouldNotPerformException ex) {
            throw new InitializationException("Could not add Plugin[" + plugin.getClass().getName() + "] to Registry[" + registry.getClass().getSimpleName() + "]", ex);
        }
    }

    @Override
    public void beforeRegister(ENTRY entry) throws RejectedException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (P plugin : pluginList) {
                plugin.beforeRegister(entry);
            }
        } catch (RejectedException ex) {
            throw ex;
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about planned Entry[" + entry + "] registration!", ex), logger, LogLevel.ERROR);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void afterRegister(ENTRY entry) throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (P plugin : pluginList) {
                plugin.afterRegister(entry);
            }
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about planned Entry[" + entry + "] registration!", ex), logger, LogLevel.ERROR);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void beforeUpdate(ENTRY entry) throws RejectedException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (P plugin : pluginList) {
                plugin.beforeUpdate(entry);
            }
        } catch (RejectedException ex) {
            throw ex;
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about planned Entry[" + entry + "] update!", ex), logger, LogLevel.ERROR);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void afterUpdate(ENTRY entry) throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (P plugin : pluginList) {
                plugin.afterUpdate(entry);
            }
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about successfully Entry[" + entry + "] update!", ex), logger, LogLevel.ERROR);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void beforeRemove(ENTRY entry) throws RejectedException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (P plugin : pluginList) {
                plugin.beforeRemove(entry);
            }
        } catch (RejectedException ex) {
            throw ex;
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about planned Entry[" + entry + "] removal!", ex), logger, LogLevel.ERROR);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void afterRemove(ENTRY entry) throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (P plugin : pluginList) {
                plugin.afterRemove(entry);
            }
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about successfully Entry[" + entry + "] removal!", ex), logger, LogLevel.ERROR);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void beforeClear() throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (P plugin : pluginList) {
                plugin.beforeClear();
            }
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about planned registry earsure!", ex), logger, LogLevel.ERROR);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void beforeGet(KEY key) throws RejectedException {
        if (pluginList.isEmpty()) {
            return;
        }

        try {
            for (P plugin : pluginList) {
                plugin.beforeGet(key);
            }
        } catch (RejectedException ex) {
            throw ex;
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about planned Entry[" + key + "] publishment!", ex), logger, LogLevel.ERROR);
        }
    }

    @Override
    public void beforeGetEntries() throws CouldNotPerformException {
        if (pluginList.isEmpty()) {
            return;
        }

        try {
            for (P plugin : pluginList) {
                plugin.beforeGetEntries();
            }
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about planned registry publishment!", ex), logger, LogLevel.ERROR);
        }
    }

    @Override
    public void checkAccess() throws RejectedException {
        if (pluginList.isEmpty()) {
            return;
        }

        try {
            for (P plugin : pluginList) {
                plugin.checkAccess();
            }
        } catch (RejectedException ex) {
            throw ex;
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not check registry access with RegistryPlugins!", ex), logger, LogLevel.ERROR);
        }
    }

    @Override
    public void afterRegistryChange() throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (P plugin : pluginList) {
                plugin.afterRegistryChange();
            }
        } catch (RejectedException ex) {
            throw ex;
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about registry change!", ex), logger, LogLevel.ERROR);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void afterConsistencyCheck() throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (P plugin : pluginList) {
                plugin.afterConsistencyCheck();
            }
        } catch (RejectedException ex) {
            throw ex;
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugins about finished consistency check!", ex), logger, LogLevel.ERROR);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
