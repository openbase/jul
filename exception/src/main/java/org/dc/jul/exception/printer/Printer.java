package org.dc.jul.exception.printer;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public interface Printer {

        public void print(String message);

        public void print(String message, Throwable throwable);

        public boolean isDebugEnabled();
    }