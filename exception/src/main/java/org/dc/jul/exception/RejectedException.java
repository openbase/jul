package org.dc.jul.exception;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class RejectedException extends CouldNotPerformException {

    public RejectedException(String message) {
        super(message);
    }

    public RejectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RejectedException(Throwable cause) {
        super(cause);
    }

    public RejectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
