
package org.openbase.jul.iface;

import org.openbase.jul.exception.InitializationException;

/**
 * #%L
 * #L%
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface VoidInitializable extends DefaultInitializableImpl<Void> {

    @Override
    public default void init(Void config) throws InitializationException, InterruptedException {
        init();
    }
    
    @Override
    public default Void getDefaultConfig() {
        return null;
    }
}
