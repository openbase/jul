package de.citec.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.extension.protobuf.IdentifiableMessage;
import de.citec.jul.storage.registry.version.DBVersionControl;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public abstract class AbstractVersionConsistencyHandler<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>> extends AbstractProtoBufRegistryConsistencyHandler<KEY, M, MB> {

    protected final DBVersionControl versionControl;
    protected final FileSynchronizedRegistryInterface<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufRegistryInterface<KEY, M, MB>> registry;

    public AbstractVersionConsistencyHandler(final DBVersionControl versionControl, final FileSynchronizedRegistryInterface<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufRegistryInterface<KEY, M, MB>> registry) throws de.citec.jul.exception.InstantiationException {
        this.versionControl = versionControl;
        this.registry = registry;
    }

    @Override
    public boolean shutdown() {
        if(registry.isConsistent()) {
            try {
                versionControl.registerConsistencyHandlerExecution(this);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, logger);
            }
        }
        return super.shutdown();
    }
}
