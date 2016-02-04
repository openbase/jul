package org.dc.jul.storage.registry.plugin;

/*
 * #%L
 * JUL Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.RejectedException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import org.dc.jul.iface.Identifiable;
import org.dc.jul.storage.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <P>
 */
public class RegistryPluginPool<KEY, ENTRY extends Identifiable<KEY>, P extends RegistryPlugin<KEY, ENTRY>> implements RegistryPlugin<KEY, ENTRY> {

    protected final Logger logger = LoggerFactory.getLogger(RegistryPluginPool.class);

    protected final List<P> pluginList;
    protected Registry<KEY, ENTRY, ?> registry;

    public RegistryPluginPool() {
        this.pluginList = new ArrayList<>();
    }

    @Override
    public void init(Registry<KEY, ENTRY, ?> registry) throws InitializationException, InterruptedException {
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
        for (P plugin : pluginList) {
            try {
                plugin.beforeRegister(entry);
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + entry + "] registration!", ex), logger, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void afterRegister(ENTRY entry) throws CouldNotPerformException {
        pluginList.stream().forEach((plugin) -> {
            try {
                plugin.afterRegister(entry);
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about successfully Entry[" + entry + "] registration!", ex), logger, LogLevel.ERROR);
            }
        });
    }

    @Override
    public void beforeUpdate(ENTRY entry) throws RejectedException {
        for (P plugin : pluginList) {
            try {
                plugin.beforeUpdate(entry);
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + entry + "] update!", ex), logger, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void afterUpdate(ENTRY entry) throws CouldNotPerformException {
        pluginList.stream().forEach((plugin) -> {
            try {
                plugin.afterUpdate(entry);
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about successfully Entry[" + entry + "] update!", ex), logger, LogLevel.ERROR);
            }
        });
    }

    @Override
    public void beforeRemove(ENTRY entry) throws RejectedException {
        for (P plugin : pluginList) {
            try {
                plugin.beforeRemove(entry);
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + entry + "] removal!", ex), logger, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void afterRemove(ENTRY entry) throws CouldNotPerformException {
        pluginList.stream().forEach((plugin) -> {
            try {
                plugin.afterRemove(entry);
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about successfully Entry[" + entry + "] removal!", ex), logger, LogLevel.ERROR);
            }
        });
    }

    @Override
    public void beforeClear() throws CouldNotPerformException {
        pluginList.stream().forEach((plugin) -> {
            try {
                plugin.beforeClear();
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about planned registry earsure!", ex), logger, LogLevel.ERROR);
            }
        });
    }

    @Override
    public void beforeGet(KEY key) throws RejectedException {
        for (P plugin : pluginList) {
            try {
                plugin.beforeGet(key);
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + key + "] publishment!", ex), logger, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void beforeGetEntries() throws CouldNotPerformException {
        pluginList.stream().forEach((plugin) -> {
            try {
                plugin.beforeGetEntries();
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about planned registry publishment!", ex), logger, LogLevel.ERROR);
            }
        });
    }

    @Override
    public void checkAccess() throws RejectedException {
        for (P plugin : pluginList) {
            try {
                plugin.checkAccess();
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not check registry access with RegistryPlugin[" + plugin + "]!", ex), logger, LogLevel.ERROR);
            }
        }
    }
}
