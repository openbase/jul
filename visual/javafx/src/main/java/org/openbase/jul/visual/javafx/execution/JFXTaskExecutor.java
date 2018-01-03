package org.openbase.jul.visual.javafx.execution;

/*-
 * #%L
 * JUL Visual JavaFX
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.EnumNotSupportedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.schedule.FutureProcessor;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JFXTaskExecutor {

    public enum TargetThread {
        GUI_THREAD,
        NON_GUI_THREAD;
    }

    /**
     * Method forces the task execution on the gui/nongui thread.
     * @param <V> the type of the future
     * @param callable the task
     * @param targetThread the target thread where the task will be executed independent of the current thread which has called this method.
     * @param logger the logger to print logging in error case.
     * @return a future representing the task result.
     * @throws CouldNotPerformException is thrown if the task execution has been failed.
     */
    private <V> Future<V> executeTask(final Callable<V> callable, final TargetThread targetThread, final Logger logger) throws CouldNotPerformException {
        try {
            switch (targetThread) {
                // force execution on gui thread
                case GUI_THREAD:
                    if (Platform.isFxApplicationThread()) {
                        return CompletableFuture.completedFuture(callable.call());
                    }

                    FutureTask<V> future = new FutureTask(() -> {
                        try {
                            return callable.call();
                        } catch (Exception ex) {
                            throw ExceptionPrinter.printHistoryAndReturnThrowable(ex, logger);
                        }
                    });
                    Platform.runLater(future);
                    return future;
                // force execution on external thread
                case NON_GUI_THREAD:
                    if (Platform.isFxApplicationThread()) {
                        return GlobalCachedExecutorService.submit(callable);
                    }

                    return CompletableFuture.completedFuture(callable.call());
                default:
                    throw new EnumNotSupportedException(targetThread, this);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return FutureProcessor.canceledFuture(ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not perform task!", ex);
        }
    }
}
