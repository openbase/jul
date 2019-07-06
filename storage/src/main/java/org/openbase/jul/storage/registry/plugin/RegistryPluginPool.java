package org.openbase.jul.storage.registry.plugin;

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

import org.openbase.jul.exception.*;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.storage.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @param <KEY>
 * @param <ENTRY>
 * @param <PLUGIN>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RegistryPluginPool<KEY, ENTRY extends Identifiable<KEY>, PLUGIN extends RegistryPlugin<KEY, ENTRY, REGISTRY>, REGISTRY extends Registry<KEY, ENTRY>> implements RegistryPlugin<KEY, ENTRY, REGISTRY> {

    protected final Logger logger = LoggerFactory.getLogger(RegistryPluginPool.class);

    protected final List<PLUGIN> pluginList;
    protected REGISTRY registry;
    private final ReentrantReadWriteLock lock;

    public RegistryPluginPool() {
        this.pluginList = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public void init(final REGISTRY registry) throws InitializationException {
        try {
            if (this.registry != null) {
                throw new InvalidStateException("PluginPool already initialized!");
            }
            this.registry = registry;
        } catch (final CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    @Override
    public void shutdown() {
        for (PLUGIN plugin : pluginList) {
            try {
                plugin.shutdown();
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not shutdown RegistryPlugin[" + plugin + "]!", plugin, ex), logger, LogLevel.ERROR);
            }
        }
    }

    public void addPlugin(final PLUGIN plugin) throws CouldNotPerformException, InterruptedException {
        try {
            plugin.init(registry);
            pluginList.add(plugin);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not add Plugin[" + plugin.getClass().getName() + "] to Registry[" + registry.getClass().getSimpleName() + "]", ex);
        }
    }

    @Override
    public void prepareRegistry(final File registryDirectory) throws RejectedException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.prepareRegistry(registryDirectory);
                } catch (RejectedException ex) {
                    throw ex;
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not prepare RegistryDirectory[" + registryDirectory + "] via RegistryPlugin[" + plugin + "]   registration!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void beforeRegister(final ENTRY entry) throws RejectedException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.beforeRegister(entry);
                } catch (RejectedException ex) {
                    throw ex;
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + entry + "] registration!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void afterRegister(final ENTRY entry) throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.afterRegister(entry);
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + entry + "] registration!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void beforeUpdate(final ENTRY entry) throws RejectedException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.beforeUpdate(entry);
                } catch (RejectedException ex) {
                    throw ex;
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + entry + "] update!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void afterUpdate(final ENTRY entry) throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.afterUpdate(entry);
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about successfully Entry[" + entry + "] update!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void afterConsistencyModification(final ENTRY entry) throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.afterConsistencyModification(entry);
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about Entry[" + entry + "] modification!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void beforeRemove(final ENTRY entry) throws RejectedException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.beforeRemove(entry);
                } catch (RejectedException ex) {
                    throw ex;
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + entry + "] removal!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void afterRemove(final ENTRY entry) throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.afterRemove(entry);
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about successfully Entry[" + entry + "] removal!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
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
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.beforeClear();
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about planned registry earsure!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void beforeGet(final KEY key) throws RejectedException {
        if (pluginList.isEmpty()) {
            return;
        }

        for (PLUGIN plugin : pluginList) {
            try {
                plugin.beforeGet(key);
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + key + "] publishment!", plugin, ex), logger, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void checkAccess() throws RejectedException {
        if (pluginList.isEmpty()) {
            return;
        }

        for (PLUGIN plugin : pluginList) {
            try {
                plugin.checkAccess();
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not check registry access with RegistryPlugin[" + plugin + "]!", plugin, ex), logger, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void beforeUpstreamDependencyNotification(Registry dependency) throws CouldNotPerformException {
        if (pluginList.isEmpty()) {
            return;
        }

        for (PLUGIN plugin : pluginList) {
            try {
                plugin.beforeUpstreamDependencyNotification(dependency);
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about upstream dependency notification!", plugin, ex), logger, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void afterRegistryChange() throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.afterRegistryChange();
                } catch (RejectedException ex) {
                    throw ex;
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about registry change!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void beforeConsistencyCheck() throws CouldNotPerformException {
        if (pluginList.isEmpty() || lock.isWriteLockedByCurrentThread()) {
            return;
        }

        lock.writeLock().lock();
        try {
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.beforeConsistencyCheck();
                } catch (RejectedException ex) {
                    throw ex;
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about starting consistency check!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
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
            for (PLUGIN plugin : pluginList) {
                try {
                    plugin.afterConsistencyCheck();
                } catch (RejectedException ex) {
                    throw ex;
                } catch (Exception ex) {
                    ExceptionPrinter.printHistory(new FatalImplementationErrorException("Could not inform RegistryPlugin[" + plugin + "] about finished consistency check!", plugin, ex), logger, LogLevel.ERROR);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
