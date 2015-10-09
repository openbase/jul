/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import com.rits.cloning.Cloner;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.printer.LogLevel;
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
public class RegistrySandbox<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends RegistryInterface<KEY, ENTRY, R>, P extends RegistryPlugin<KEY, ENTRY>> extends AbstractRegistry<KEY, ENTRY, MAP, R, P> implements RegistrySandboxInterface<KEY, ENTRY, MAP, R> {

    private final static Cloner cloner = new Cloner();

    public RegistrySandbox(MAP entryMap) throws InstantiationException {
        super(cloner.deepClone(entryMap));
    }

    @Override
    public void replaceInternalMap(Map<KEY, ENTRY> map) {
        super.replaceInternalMap(cloner.deepClone(map));
    }

    @Override
    public ENTRY superRemove(ENTRY entry) throws CouldNotPerformException {
        return super.superRemove(cloner.deepClone(entry));
    }

    @Override
    public ENTRY update(ENTRY entry) throws CouldNotPerformException {
        return super.update(cloner.deepClone(entry)); 
    }

    @Override
    public ENTRY register(ENTRY entry) throws CouldNotPerformException {
        return super.register(cloner.deepClone(entry));
    }

    @Override
    public void sync(MAP map) {
        entryMap.clear();
        entryMap.putAll(cloner.deepClone(map));
    }

    @Override
    protected void finishTransaction() throws CouldNotPerformException {
        try {
            checkConsistency();
        } catch (CouldNotPerformException ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Given transaction is invalid because sandbox consistency check failed!", ex), logger, LogLevel.ERROR);
        }
    }
}
