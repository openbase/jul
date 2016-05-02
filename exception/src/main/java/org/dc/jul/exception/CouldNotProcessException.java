package org.dc.jul.exception;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class CouldNotProcessException extends RuntimeException {

    public CouldNotProcessException(String message) {
        super(message);
    }

    public CouldNotProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public CouldNotProcessException(Throwable cause) {
        super(cause);
    }

    public CouldNotProcessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
