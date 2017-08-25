package org.openbase.jul.exception.printer;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class LogLevelFilter {

    /**
     * Method forces the debug log level if the {@code forceDebug} flag is {@code true}.
     * Otherwise the given log level is bypassed.
     *
     * @param logLevel the log level to return if the {@code forceDebug} is not true.
     * @param forceDebug the flag to force the debug mode.
     * @return the desired log level.
     */
    public static LogLevel getFilteredLogLevel(final LogLevel logLevel, final boolean forceDebug) {
        if (forceDebug) {
            return LogLevel.DEBUG;
        }
        return logLevel;
    }
}
