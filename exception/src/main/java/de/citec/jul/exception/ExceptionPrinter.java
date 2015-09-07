package de.citec.jul.exception;

import de.citec.jul.exception.printer.LogLevel;
import org.slf4j.Logger;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @deprecated please use de.citec.jul.exception.printer.ExceptionPrinter instead!
 */
@Deprecated
public class ExceptionPrinter {

    /**
     *
     * @param <T>
     * @param logger
     * @param th
     * @return
     * @deprecated please use de.citec.jul.exception.printer.ExceptionPrinter instead!
     */
    @Deprecated
    public static <T extends Throwable> T printHistoryAndReturnThrowableprintHistoryAndReturnThrowable(final Logger logger, final T th) {
        return de.citec.jul.exception.printer.ExceptionPrinter.printHistoryAndReturnThrowable(th, logger, LogLevel.ERROR);
    }

    /**
     *
     * @param <T>
     * @param logger
     * @param th
     * @deprecated please use de.citec.jul.exception.printer.ExceptionPrinter instead!
     */
    @Deprecated
    public static <T extends Throwable> void printHistory(final Logger logger, final T th) {
        de.citec.jul.exception.printer.ExceptionPrinter.printHistory(th, logger, LogLevel.ERROR);
    }
}
