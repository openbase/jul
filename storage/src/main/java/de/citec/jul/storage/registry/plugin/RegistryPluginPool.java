package de.citec.jul.storage.registry.plugin;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.RejectedException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.printer.LogLevel;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.storage.registry.RegistryInterface;
import java.util.ArrayList;
import java.util.List;
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
    protected RegistryInterface<KEY, ENTRY, ?> registry;

    public RegistryPluginPool() {
        this.pluginList = new ArrayList<>();
    }

    @Override
    public void init(RegistryInterface<KEY, ENTRY, ?> registry) throws CouldNotPerformException {
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

    public void addPlugin(P plugin) throws CouldNotPerformException {
        try {
            plugin.init(registry);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not add Plugin[" + plugin.getClass().getName() + "] to Registry[" + registry.getClass().getSimpleName() + "]", ex);
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
