/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.exception;

/**
 *
 * @author divine
 */
public class VerificationFailedException extends CouldNotPerformException {

    public VerificationFailedException(String message) {
        super(message);
    }

    public VerificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public VerificationFailedException(Throwable cause) {
        super(cause);
    }

    public VerificationFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
