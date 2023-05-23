package org.openbase.jul.pattern.launch

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import org.openbase.jps.core.JPService
import org.openbase.jps.exception.JPNotAvailableException
import org.openbase.jul.communication.controller.AbstractIdentifiableController
import org.openbase.jul.communication.iface.RPCServer
import org.openbase.jul.exception.*
import org.openbase.jul.exception.ExceptionProcessor.getInitialCause
import org.openbase.jul.exception.ExceptionProcessor.isCausedBySystemShutdown
import org.openbase.jul.exception.MultiException.ExceptionStack
import org.openbase.jul.exception.printer.ExceptionPrinter
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.jul.iface.Launchable
import org.openbase.jul.iface.VoidInitializable
import org.openbase.jul.iface.provider.NameProvider
import org.openbase.jul.pattern.Launcher
import org.openbase.jul.pattern.launch.jp.*
import org.openbase.jul.processing.StringProcessor
import org.openbase.jul.schedule.FutureProcessor
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.jul.schedule.SyncObject
import org.openbase.type.domotic.state.ConnectionStateType
import org.openbase.type.execution.LauncherDataType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.InstantiationException
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantReadWriteLock

/*-
 * #%L
 * JUL Pattern Launch
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
 * @param <L> the launchable to launch by this launcher.
 *
 * @author [Divine Threepwood](mailto:divine@openbase.org)
</L> */
abstract class AbstractLauncher<L : Launchable<*>>
/**
 * Constructor prepares the launcher and registers already a shutdown hook.
 * The launcher class is used to instantiate a new launcher instance if the instantiateLaunchable() method is not overwritten.
 *
 *
 * After instantiation of this class the launcher must be initialized and activated before the communication interface is provided.
 *
 * @param launchableClass  the class to be launched.
 * @param applicationClass the class representing this application. Those is used for scope generation if the getName() method is not overwritten.
 *
 * @throws org.openbase.jul.exception.InstantiationException
 */(private val applicationClass: Class<*>, private val launchableClass: Class<L>) :
    AbstractIdentifiableController<LauncherDataType.LauncherData?, LauncherDataType.LauncherData.Builder?>(
        LauncherDataType.LauncherData.newBuilder()
    ), Launcher<L>, VoidInitializable, NameProvider {
    protected val logger = LoggerFactory.getLogger(javaClass)
    var launchable: L? = null
        private set
    private var launchTime: Long = -1
    private var verified = false
    var verificationFailedCause: VerificationFailedException? = null
        private set

    @Volatile
    var launcherTask: Future<Void>? = null
        private set
    private val lauchnerLock = ReentrantReadWriteLock()

    @Throws(InitializationException::class, InterruptedException::class)
    override fun init() {
        super<AbstractIdentifiableController>.init(
            SCOPE_PREFIX_LAUNCHER + ScopeProcessor.COMPONENT_SEPARATOR + JPService.getApplicationName() + ScopeProcessor.COMPONENT_SEPARATOR + ScopeProcessor.convertIntoValidScopeComponent(
                name
            )
        )
        try {
            verifyNonRedundantExecution()
        } catch (e: VerificationFailedException) {
            throw InitializationException(this, e)
        }
    }

    open val isCoreLauncher: Boolean
        /**
         * This method can be overwritten to identify a core launcher.
         * This means that if the start of this launcher fails the whole launching
         * process will be stopped.
         *
         * @return false, can be overwritten to return true
         */
        get() = false

    /**
     * Method returns the name of this launcher.
     *
     * @return the name as string.
     */
    override fun getName(): String {
        return javaClass.simpleName.replace("Launcher", "")
    }

    /**
     * Method creates a launchable instance without any arguments.. In case the launchable needs arguments you can overwrite this method and instantiate the launchable by ourself.
     *
     * @return the new instantiated launchable.
     *
     * @throws CouldNotPerformException is thrown in case the launchable could not properly be instantiated.
     */
    @Throws(CouldNotPerformException::class)
    protected fun instantiateLaunchable(): L {
        return try {
            launchableClass.getConstructor().newInstance()
        } catch (ex: InstantiationException) {
            throw CouldNotPerformException("Could not load launchable class!", ex)
        } catch (ex: IllegalAccessException) {
            throw CouldNotPerformException("Could not load launchable class!", ex)
        } catch (ex: NoSuchMethodException) {
            throw CouldNotPerformException("Could not load launchable class!", ex)
        } catch (ex: InvocationTargetException) {
            throw CouldNotPerformException("Could not load launchable class!", ex)
        }
    }

    // Load application specific java properties.
    protected abstract fun loadProperties()

    /**
     * Method verifies a running application.
     *
     * @throws VerificationFailedException is thrown if the application is started with any restrictions.
     * @throws InterruptedException        is thrown if the verification process is externally interrupted.
     */
    @Throws(VerificationFailedException::class, InterruptedException::class)
    protected open fun verify() {
        // overwrite for verification.
    }

    private fun setState(state: LauncherDataType.LauncherData.LauncherState) {
        try {
            getDataBuilder(this).use { dataBuilder -> dataBuilder.internalBuilder!!.setLauncherState(state) }
        } catch (e: Exception) {
            ExceptionPrinter.printHistory("Could not apply state change!", e, logger)
        }
    }

    @Throws(VerificationFailedException::class)
    fun verifyNonRedundantExecution() {
        // verify that launcher was not already externally started
        try {
            val launcherRemote = LauncherRemote()
            launcherRemote.init(getScope())
            try {
                launcherRemote.activate()
                launcherRemote.waitForMiddleware()
                launcherRemote.waitForConnectionState(ConnectionStateType.ConnectionState.State.CONNECTED, 1000)
                throw RedundantExecutionException("Launcher[$name]")
            } catch (e: org.openbase.jul.exception.TimeoutException) {
                // this is the default since no other instance should be launched yet.
            } finally {
                launcherRemote.shutdown()
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: VerificationFailedException) {
            throw e
        } catch (e: CouldNotPerformException) {
            ExceptionPrinter.printHistory("Could not properly detect redundant launcher!", e, logger)
        }
    }

    override fun launch(): Future<Void> {
        try {
            getManageWriteLockInterruptible(this).use { ignored ->
                if (launcherTask != null && !launcherTask!!.isDone) {
                    return FutureProcessor.canceledFuture(
                        Void::class.java, InvalidStateException(
                            "Could not launch $name! Application still running!"
                        )
                    )
                }
                launcherTask = GlobalCachedExecutorService.submit<Void> {
                    getManageWriteLockInterruptible(this).use { ignored1 ->
                        setState(LauncherDataType.LauncherData.LauncherState.INITIALIZING)
                        try {
                            init()
                            activate()
                        } catch (e: CouldNotPerformException) {
                            setState(LauncherDataType.LauncherData.LauncherState.ERROR)
                            throw e
                        } catch (e: RuntimeException) {
                            setState(LauncherDataType.LauncherData.LauncherState.ERROR)
                            throw e
                        } catch (e: InterruptedException) {
                            setState(LauncherDataType.LauncherData.LauncherState.STOPPED)
                            throw e
                        }
                        launchable = instantiateLaunchable()
                        try {
                            launchable!!.init()
                            setState(LauncherDataType.LauncherData.LauncherState.LAUNCHING)
                            launchable!!.activate()
                            launchTime = System.currentTimeMillis()
                            setState(LauncherDataType.LauncherData.LauncherState.RUNNING)
                            try {
                                verify()
                                verified = true
                            } catch (ex: VerificationFailedException) {
                                verified = false
                                verificationFailedCause = ex
                            }
                        } catch (ex: InterruptedException) {
                            setState(LauncherDataType.LauncherData.LauncherState.STOPPING)
                            return@submit null
                        } catch (ex: Exception) {
                            setState(LauncherDataType.LauncherData.LauncherState.ERROR)
                            launchable!!.shutdown()
                            if (!isCausedBySystemShutdown(ex)) {
                                ExceptionPrinter.printHistoryAndReturnThrowable(
                                    CouldNotPerformException(
                                        "Could not launch $name",
                                        ex
                                    ), logger
                                )
                            }
                        }
                        return@submit null
                    }
                }
            }
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException(ex)
        }
        return launcherTask!!
    }

    @Throws(CouldNotPerformException::class, InterruptedException::class)
    override fun relaunch() {
        getManageWriteLockInterruptible(this).use { ignored ->
            stop()
            try {
                launch().get()
            } catch (ex: ExecutionException) {
                throw CouldNotPerformException(ex)
            } catch (ex: CancellationException) {
                throw CouldNotPerformException(ex)
            }
        }
    }

    override fun stop() {
        try {
            getManageWriteLockInterruptible(this).use { ignored ->
                interruptLaunch()
                setState(LauncherDataType.LauncherData.LauncherState.STOPPING)
                if (launchable != null) {
                    launchable!!.shutdown()
                }
                setState(LauncherDataType.LauncherData.LauncherState.STOPPED)
            }
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException(ex)
        }
    }

    /**
     * Method cancels the launch process of this launcher.
     */
    private fun interruptLaunch() {
        if (isLaunching) {
            launcherTask!!.cancel(true)
        }
    }

    private val isLaunching: Boolean
        /**
         * @return true if the launcher is currently launching.
         */
        private get() = launcherTask != null && !launcherTask!!.isDone

    override fun shutdown() {
        stop()
        super.shutdown()
    }

    override fun getUpTime(): Long {
        return if (launchTime < 0) {
            0
        } else System.currentTimeMillis() - launchTime
    }

    override fun getLaunchTime(): Long {
        return launchTime
    }

    override fun isVerified(): Boolean {
        return verified
    }

    override fun registerMethods(server: RPCServer) {
        // currently, the launcher does not support any rpc methods.
    }

    companion object {
        const val LAUNCHER_TIMEOUT: Long = 60000
        const val SCOPE_PREFIX_LAUNCHER = ScopeProcessor.COMPONENT_SEPARATOR + "launcher"
        private val waitingTaskList: MutableList<Future<*>> = ArrayList()
        private val VERIFICATION_STACK_LOCK = SyncObject("VerificationStackLock")
        private val ERROR_STACK_LOCK = SyncObject("ErrorStackLock")
        private val WAITING_TASK_LIST_LOCK = SyncObject("WaitingTaskLock")
        private var errorExceptionStack: ExceptionStack? = null
        private var verificationExceptionStack: ExceptionStack? = null

        @Throws(CouldNotPerformException::class)
        private fun loadCustomLoggerSettings() {
            // assume SLF4J is bound to logback in the current environment
            val context = LoggerFactory.getILoggerFactory() as LoggerContext
            val defaultLoggerConfig: File
            val debugLoggerConfig: File
            try {
                defaultLoggerConfig = JPService.getValue(
                    JPLoggerConfigFile::class.java, JPService.getValue(
                        JPLoggerDebugConfigFile::class.java
                    )
                )
                debugLoggerConfig = JPService.getValue(JPLoggerDebugConfigFile::class.java)
            } catch (ex: JPNotAvailableException) {
                // no logger config set so just skip custom configuration.
                return
            }
            val loggerConfig: File
            loggerConfig = if (debugLoggerConfig.exists() && JPService.debugMode()) {
                // prefer debug config when available and debug mode was enabled.
                debugLoggerConfig
            } else if (defaultLoggerConfig.exists()) {
                // use default config
                defaultLoggerConfig
            } else if (debugLoggerConfig.exists()) {
                // if default config does not exist just use any available debug config
                debugLoggerConfig
            } else {
                // skip if no logger configuration files could be found.
                return
            }

            // reconfigure logger
            try {
                val configurator = JoranConfigurator()
                configurator.context = context
                // clear any previous configuration
                context.reset()

                // prepare props
                loadProperties(context)

                // configure
                configurator.doConfigure(loggerConfig)

                // print warning in case of syntax errors
                StatusPrinter.printInCaseOfErrorsOrWarnings(context)
            } catch (ex: JoranException) {
                // print warning in case of syntax errors
                StatusPrinter.printInCaseOfErrorsOrWarnings(context)
                throw CouldNotPerformException("Could not load logger settings!", ex)
            }
        }

        private fun loadProperties(context: LoggerContext) {
            // store some variables
            context.putProperty("APPLICATION_NAME", JPService.getApplicationName())
            context.putProperty("SUBMODULE_NAME", JPService.getSubmoduleName())
            context.putProperty("MODULE_SEPARATOR", if (JPService.getSubmoduleName().isEmpty()) "" else "-")
            try {
                context.putProperty("LOGGER_TARGET_DIR", JPService.getValue(JPLogDirectory::class.java).absolutePath)
                // inform user about log redirection
                println(
                    "Logs can be found in: " + JPService.getValue(
                        JPLogDirectory::class.java
                    ).absolutePath
                )
            } catch (e: JPNotAvailableException) {
                // just store nothing if the propertie could not be loaded.
            }
        }

        @JvmStatic
        fun main(
            application: Class<*>?,
            submodule: Class<*>?,
            args: Array<String?>?,
            vararg launchers: Class<out AbstractLauncher<*>>,
        ) {

            // setup application names
            JPService.setApplicationName(application)
            JPService.setSubmoduleName(submodule) // requires the application name to be set
            main(application, args, *launchers)
        }

        @JvmStatic
        fun main(
            application: Class<*>?,
            submoduleName: String?,
            args: Array<String?>?,
            vararg launchers: Class<out AbstractLauncher<*>>,
        ) {

            // setup application names
            JPService.setApplicationName(application)
            JPService.setSubmoduleName(submoduleName) // requires the application name to be set
            main(application, args, *launchers)
        }

        @JvmStatic
        fun main(application: Class<*>?, args: Array<String?>?, vararg launchers: Class<out AbstractLauncher<*>>) {

            // setup application names
            JPService.setApplicationName(application)

            // register interruption of this thread as shutdown hook
            val mainThread = Thread.currentThread()
            Runtime.getRuntime().addShutdownHook(Thread { mainThread.interrupt() })
            val launcherMap: MutableMap<Class<out AbstractLauncher<*>>, AbstractLauncher<*>> = HashMap()
            for (launcherClass in launchers) {
                try {
                    launcherMap[launcherClass] = launcherClass.getConstructor().newInstance()
                } catch (ex: InstantiationException) {
                    errorExceptionStack = MultiException.push(
                        application,
                        CouldNotPerformException("Could not load launcher class!", ex),
                        errorExceptionStack
                    )
                } catch (ex: IllegalAccessException) {
                    errorExceptionStack = MultiException.push(
                        application,
                        CouldNotPerformException("Could not load launcher class!", ex),
                        errorExceptionStack
                    )
                } catch (ex: NoSuchMethodException) {
                    errorExceptionStack = MultiException.push(
                        application,
                        CouldNotPerformException("Could not load launcher class!", ex),
                        errorExceptionStack
                    )
                } catch (ex: InvocationTargetException) {
                    errorExceptionStack = MultiException.push(
                        application,
                        CouldNotPerformException("Could not load launcher class!", ex),
                        errorExceptionStack
                    )
                }
            }
            for (launcher in launcherMap.values) {
                launcher.loadProperties()
            }

            // register launcher jps
            JPService.registerProperty(JPPrintLauncher::class.java)
            JPService.registerProperty(JPExcludeLauncher::class.java)
            JPService.registerProperty(JPLoggerConfigDirectory::class.java)
            JPService.registerProperty(JPLoggerConfigFile::class.java)
            JPService.registerProperty(JPLoggerDebugConfigFile::class.java)
            JPService.registerProperty(JPLogDirectory::class.java)

            // parse properties
            JPService.parseAndExitOnError(args)

            // load custom logger settings
            try {
                loadCustomLoggerSettings()
            } catch (e: CouldNotPerformException) {
                System.exit(2)
                return
            }
            val logger = LoggerFactory.getLogger(Launcher::class.java)

            // print launcher end exit
            try {
                if (JPService.getProperty<JPPrintLauncher>(JPPrintLauncher::class.java).value) {
                    if (launcherMap.isEmpty()) {
                        println(generateAppName() + " does not provide any launcher!")
                        System.exit(255)
                    }
                    println("Available launcher:")
                    println()
                    var maxLauncherNameSize = 0
                    for ((key) in launcherMap) {
                        maxLauncherNameSize = Math.max(maxLauncherNameSize, key.simpleName.length)
                    }
                    for ((key) in launcherMap) {
                        println(
                            "\t• " + StringProcessor.fillWithSpaces(
                                key.simpleName,
                                maxLauncherNameSize
                            ) + "  ⊳  " + key.name
                        )
                    }
                    println()
                    System.exit(0)
                }
            } catch (ex: JPNotAvailableException) {
                ExceptionPrinter.printHistory("Could not check if launcher should be printed.", ex, logger)
            }
            logger.info("Start " + generateAppName() + "...")
            for ((key, value) in HashSet<Map.Entry<Class<out AbstractLauncher<*>>, AbstractLauncher<*>>>(launcherMap.entries)) {

                // check if launcher was excluded
                var exclude = false
                try {
                    //filter excluded launcher
                    for (exclusionPatter in JPService.getProperty(JPExcludeLauncher::class.java).value) {
                        if (key.name.lowercase(Locale.getDefault()).contains(
                                exclusionPatter.replace("-", "").replace("_", "").lowercase(
                                    Locale.getDefault()
                                )
                            )
                        ) {
                            exclude = true
                        }
                    }
                } catch (ex: JPNotAvailableException) {
                    ExceptionPrinter.printHistory("Could not process launcher exclusion!", ex, logger)
                }
                if (exclude) {
                    logger.info(key.simpleName + " excluded from execution.")
                    launcherMap.remove(key)
                    continue
                }
                value.launch()
            }
            synchronized(WAITING_TASK_LIST_LOCK) {
                // start all waiting tasks in parallel
                for ((key, value) in launcherMap) {
                    val waitingTask: Future<*> = GlobalCachedExecutorService.submit<Any?> {
                        while (!Thread.interrupted()) {
                            try {
                                try {
                                    value.launcherTask!![LAUNCHER_TIMEOUT, TimeUnit.MILLISECONDS]
                                    if (!value.isVerified) {
                                        pushToVerificationExceptionStack(
                                            application,
                                            CouldNotPerformException(
                                                "Could not verify " + key.simpleName + "!",
                                                value.verificationFailedCause
                                            )
                                        )
                                    }
                                } catch (ex: CancellationException) {
                                    // cancellation means the complete launch was canceled anyway and no further steps are required.
                                } catch (ex: ExecutionException) {
                                    // recover Interrupted exception to avoid error printing during system shutdown
                                    val initialCause = getInitialCause(ex)
                                    if (initialCause is InterruptedException) {
                                        throw initialCause
                                    }
                                    throw ex
                                }
                                break
                            } catch (ex: TimeoutException) {
                                logger.warn("Launcher " + key.simpleName + " startup delay detected!")
                            } catch (ex: InterruptedException) {
                                // if a core launcher could not be started the whole startup failed so interrupt
                                if (value.isCoreLauncher) {
                                    // shutdown all launcher
                                    forceStopLauncher(launcherMap)
                                    return@submit null
                                }
                            } catch (ex: Exception) {
                                val exx = CouldNotPerformException("Could not launch " + key.simpleName + "!", ex)
                                pushToErrorExceptionStack(application, exx)

                                // if a core launcher could not be started the whole startup failed so interrupt
                                if (value.isCoreLauncher) {
                                    // shutdown all launcher
                                    forceStopLauncher(launcherMap)
                                }

                                // finish launcher
                                return@submit null
                            }
                        }
                        null
                    }
                    waitingTaskList.add(waitingTask)
                }
            }
            try {
                for (waitingTask in waitingTaskList) {
                    try {
                        waitingTask.get()
                    } catch (ex: ExecutionException) {
                        // these exception will be pushed to the error exception stack anyway and printed in the summary
                    } catch (ex: CancellationException) {
                        // if a core launcher fails a cancellation exception will be thrown
                        printSummary(
                            application,
                            logger,
                            generateAppName() + " will be stopped because at least one core laucher could not be started."
                        )
                        System.exit(200)
                    }
                }
            } catch (ex: InterruptedException) {

                // recover interruption
                Thread.currentThread().interrupt()

                // shutdown all launcher
                forceStopLauncher(launcherMap)

                // print a summary containing the exceptions
                printSummary(application, logger, generateAppName() + " caught shutdown signal during startup phase!")
                return
            }
            printSummary(application, logger, generateAppName() + " was started with restrictions!")
        }

        private fun pushToVerificationExceptionStack(source: Any?, ex: Exception) {
            synchronized(VERIFICATION_STACK_LOCK) {
                verificationExceptionStack = MultiException.push(source, ex, verificationExceptionStack)
            }
        }

        private fun pushToErrorExceptionStack(source: Any?, ex: Exception) {
            synchronized(ERROR_STACK_LOCK) {
                errorExceptionStack = MultiException.push(source, ex, errorExceptionStack)
            }
        }

        private fun stopWaiting() {
            synchronized(WAITING_TASK_LIST_LOCK) {
                for (waitingTask in waitingTaskList) {
                    if (!waitingTask.isDone) {
                        waitingTask.cancel(true)
                    }
                }
            }
        }

        private fun interruptLaunch(launcherMap: Map<Class<out AbstractLauncher<*>>, AbstractLauncher<*>>) {

            // stop boot
            stopWaiting()

            // interrupt all launcher
            for ((_, value) in launcherMap) {
                value.interruptLaunch()
            }
        }

        private fun forceStopLauncher(launcherMap: Map<Class<out AbstractLauncher<*>>, AbstractLauncher<*>>) {
            interruptLaunch(launcherMap)

            // stop all launcher. This is done in an extra loop since stop can block if the launcher is not yet fully interrupted.
            for ((_, value) in launcherMap) {
                value.stop()
            }
        }

        private fun generateAppName(): String {
            return JPService.getApplicationName() + if (JPService.getSubmoduleName()
                    .isEmpty()
            ) "" else "-" + JPService.getSubmoduleName()
        }

        private fun printSummary(application: Class<*>?, logger: Logger, errorMessage: String) {
            try {
                var exceptionStack: ExceptionStack? = null
                try {
                    MultiException.checkAndThrow({ "Errors during startup phase!" }, errorExceptionStack)
                } catch (ex: MultiException) {
                    exceptionStack = MultiException.push(application, ex, exceptionStack)
                }
                try {
                    MultiException.checkAndThrow({ "Verification process not passed!" }, verificationExceptionStack)
                } catch (ex: MultiException) {
                    exceptionStack = MultiException.push(application, ex, exceptionStack)
                }
                if (Thread.currentThread().isInterrupted) {
                    logger.info(generateAppName() + " was interrupted.")
                    return
                }
                MultiException.checkAndThrow({ errorMessage }, exceptionStack)
                logger.info(generateAppName() + " successfully started.")
            } catch (ex: MultiException) {
                ExceptionPrinter.printHistory(ex, logger)
            }
        }
    }
}
