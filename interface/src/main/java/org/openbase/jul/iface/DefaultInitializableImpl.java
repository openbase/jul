package org.openbase.jul.iface;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.iface.provider.DefaultConfigProvider;

/**
 * #%L
 * #L%
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <CONFIG>
 */
public interface DefaultInitializableImpl<CONFIG> extends Initializable<CONFIG>, DefaultInitializable, DefaultConfigProvider<CONFIG> {

    @Override
    public default void init() throws InitializationException, InterruptedException {
        try {
            init(getDefaultConfig());
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }
}
