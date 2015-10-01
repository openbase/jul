package de.citec.jul.storage.registry.plugin;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.RejectedException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.printer.LogLevel;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.storage.file.FileSynchronizer;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <P>
 */
public class FileRegistryPluginPool<KEY, ENTRY extends Identifiable<KEY>, P extends FileRegistryPlugin<KEY, ENTRY>> extends RegistryPluginPool<KEY, ENTRY, P> implements FileRegistryPlugin<KEY, ENTRY>{

    @Override
    public void beforeRegister(ENTRY entry, FileSynchronizer fileSynchronizer) throws RejectedException {
        for (P plugin : pluginList) {
            try {
                plugin.beforeRegister(entry, fileSynchronizer);
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + entry + "] registration!", ex), logger, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void afterRegister(ENTRY entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        pluginList.stream().forEach((plugin) -> {
            try {
                plugin.afterRegister(entry, fileSynchronizer);
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about successfully Entry[" + entry + "] registration!", ex), logger, LogLevel.ERROR);
            }
        });
    }

    @Override
    public void beforeRemove(ENTRY entry, FileSynchronizer fileSynchronizer) throws RejectedException {
        for (P plugin : pluginList) {
            try {
                plugin.beforeRemove(entry, fileSynchronizer);
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + entry + "] removal!", ex), logger, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void afterRemove(ENTRY entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        pluginList.stream().forEach((plugin) -> {
            try {
                plugin.afterRemove(entry, fileSynchronizer);
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about successfully Entry[" + entry + "] removal!", ex), logger, LogLevel.ERROR);
            }
        });
    }

    @Override
    public void beforeUpdate(ENTRY entry, FileSynchronizer fileSynchronizer) throws RejectedException {
        for (P plugin : pluginList) {
            try {
                plugin.beforeUpdate(entry, fileSynchronizer);
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + entry + "] update!", ex), logger, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void afterUpdate(ENTRY entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
         pluginList.stream().forEach((plugin) -> {
            try {
                plugin.afterUpdate(entry, fileSynchronizer);
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about successfully Entry[" + entry + "] update!", ex), logger, LogLevel.ERROR);
            }
        });
    }

    @Override
    public void beforeGet(KEY key, FileSynchronizer fileSynchronizer) throws RejectedException {
        for (P plugin : pluginList) {
            try {
                plugin.beforeGet(key, fileSynchronizer);
            } catch (RejectedException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform RegistryPlugin[" + plugin + "] about planned Entry[" + key + "] publishment!", ex), logger, LogLevel.ERROR);
            }
        }
    }
}
