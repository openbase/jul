package org.openbase.jul.exception

/*-
 * #%L
 * JUL Exception
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
 */ /**
 * @author vdasilva
 */
object ExceptionProcessor {
    /**
     * Method returns the message of the initial cause of the given throwable.
     * If the throwable does not provide a message its class name is returned.
     *
     * @param throwable the throwable to detect the message.
     *
     * @return the message as string.
     */
    @JvmStatic
    fun getInitialCauseMessage(throwable: Throwable): String {
        val cause = getInitialCause(throwable)
        return if (cause!!.localizedMessage == null) {
            cause.javaClass.simpleName
        } else cause.localizedMessage
    }

    /**
     * Method returns the initial cause of the given throwable.
     *
     * @param throwable the throwable to detect the message.
     *
     * @return the cause as throwable.
     */
    @JvmStatic
    fun getInitialCause(throwable: Throwable): Throwable {
        if (throwable == null) {
            FatalImplementationErrorException(ExceptionProcessor::class.java, NotAvailableException("cause"))
        }
        var cause = throwable

        while (cause.cause != null) {
            cause = cause.cause!!
        }
        return cause
    }

    /**
     * Set the given `initialCause` as initial cause of the given `throwable`.
     *
     * @param throwable    the throwable to extend.
     * @param initialCause the new initial cause.
     *
     * @return the new cause chain.
     */
    @JvmStatic
    fun setInitialCause(throwable: Throwable, initialCause: Throwable?): Throwable? {
        getInitialCause(throwable)!!.initCause(initialCause)
        return throwable
    }

    /**
     * Method checks if the initial cause of the given throwable is related to any system shutdown routine.
     * In more detail, an initial cause is related to the system shutdown when it is an instance of the `ShutdownInProgressException` class.
     *
     * @param throwable the top level cause.
     *
     * @return returns true if the given throwable is caused by a system shutdown, otherwise false.
     */
    @JvmStatic
    fun isCausedBySystemShutdown(throwable: Throwable): Boolean {
        return getInitialCause(throwable) is ShutdownInProgressException
    }

    /**
     * Method checks if any cause of the given throwable is related to any thread interruption.
     *
     * @param throwable the top level cause.
     *
     * @return returns true if the given throwable is caused by any thread interruption, otherwise false.
     */
    @JvmStatic
    fun isCausedByInterruption(throwable: Throwable?): Boolean {
        var cause = throwable
            ?: return false

        // initial check
        if (cause is InterruptedException) {
            return true
        }

        // check causes
        while (cause.cause != null) {
            cause = cause.cause ?: return false
            if (cause is InterruptedException) {
                return true
            }
        }

        // no interruption found
        return false
    }

    /**
     * Method throws an interrupted exception if the given `throwable` is caused by a system shutdown.
     *
     * @param throwable the throwable to check.
     * @param <T>       the type of the `throwable`
     *
     * @return the bypassed `throwable`
     *
     * @throws InterruptedException is thrown if the system shutdown was initiated.
    </T> */
    @JvmStatic
    @Throws(InterruptedException::class)
    fun <T : Throwable> interruptOnShutdown(throwable: T): T {
        return if (isCausedBySystemShutdown(throwable)) {
            throw InterruptedException(getInitialCauseMessage(throwable))
        } else {
            throwable
        }
    }
}

val Throwable.initialCauseMessage get() = ExceptionProcessor.getInitialCauseMessage(this)
val Throwable.initialCause get() = ExceptionProcessor.getInitialCause(this)
val Throwable.causedBySystemShutdown get() = ExceptionProcessor.isCausedBySystemShutdown(this)
val Throwable.causedByInterruption get() = ExceptionProcessor.isCausedByInterruption(this)
fun Throwable.setInitialCause(initialCause: Throwable?) =
    ExceptionProcessor.setInitialCause(this, initialCause)
