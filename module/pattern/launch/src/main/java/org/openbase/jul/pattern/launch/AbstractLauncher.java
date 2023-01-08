package org.openbase.jul.pattern.launch;

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
 */

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.communication.controller.AbstractIdentifiableController;
import org.openbase.jul.communication.iface.RPCServer;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.*;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.protobuf.ClosableDataBuilder;
import org.openbase.jul.extension.type.processing.ScopeProcessor;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.iface.provider.NameProvider;
import org.openbase.jul.pattern.Launcher;
import org.openbase.jul.pattern.launch.jp.*;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.jul.schedule.FutureProcessor;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.SyncObject;
import org.openbase.type.domotic.state.ConnectionStateType.ConnectionState.State;
import org.openbase.type.execution.LauncherDataType.LauncherData;
import org.openbase.type.execution.LauncherDataType.LauncherData.Builder;
import org.openbase.type.execution.LauncherDataType.LauncherData.LauncherState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.*;

/**
 * @param <L> the launchable to launch by this launcher.
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class
AbstractLauncher<L extends Launchable> extends AbstractIdentifiableController<LauncherData, LauncherData.Builder> implements Launcher, VoidInitializable, NameProvider {

    public static final long LAUNCHER_TIMEOUT = 60000;
    public static final String SCOPE_PREFIX_LAUNCHER = ScopeProcessor.COMPONENT_SEPARATOR + "launcher";
    private static final List<Future<?>> waitingTaskList = new ArrayList<>();
    private static final SyncObject VERIFICATION_STACK_LOCK = new SyncObject("VerificationStackLock");
    private static final SyncObject ERROR_STACK_LOCK = new SyncObject("ErrorStackLock");
    private static final SyncObject WAITING_TASK_LIST_LOCK = new SyncObject("WaitingStopLock");
    private static MultiException.ExceptionStack errorExceptionStack = null;
    private static MultiException.ExceptionStack verificationExceptionStack = null;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final Class<L> launchableClass;
    private final SyncObject LAUNCHER_LOCK = new SyncObject(this);
    private final Class<?> applicationClass;
    private L launchable;
    private long launchTime = -1;
    private boolean verified;
    private VerificationFailedException verificationFailedException;
    private Future<Void> launcherTask;

    /**
     * Constructor prepares the launcher and registers already a shutdown hook.
     * The launcher class is used to instantiate a new launcher instance if the instantiateLaunchable() method is not overwritten.
     * <p>
     * After instantiation of this class the launcher must be initialized and activated before the communication interface is provided.
     *
     * @param launchableClass  the class to be launched.
     * @param applicationClass the class representing this application. Those is used for scope generation if the getName() method is not overwritten.
     *
     * @throws org.openbase.jul.exception.InstantiationException
     */
    public AbstractLauncher(final Class<?> applicationClass, final Class<L> launchableClass) throws InstantiationException {
        super(LauncherData.newBuilder());
        this.launchableClass = launchableClass;
        this.applicationClass = applicationClass;
    }

    private static void loadCustomLoggerSettings() throws CouldNotPerformException {
        // assume SLF4J is bound to logback in the current environment
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final File defaultLoggerConfig;
        final File debugLoggerConfig;
        try {
            defaultLoggerConfig = JPService.getValue(JPLoggerConfigFile.class, JPService.getValue(JPLoggerDebugConfigFile.class));
            debugLoggerConfig = JPService.getValue(JPLoggerDebugConfigFile.class);
        } catch (JPNotAvailableException ex) {
            // no logger config set so just skip custom configuration.
            return;
        }

        final File loggerConfig;

        if (debugLoggerConfig.exists() && JPService.debugMode()) {
            // prefer debug config when available and debug mode was enabled.
            loggerConfig = debugLoggerConfig;
        } else if (defaultLoggerConfig.exists()) {
            // use default config
            loggerConfig = defaultLoggerConfig;
        } else if (debugLoggerConfig.exists()) {
            // if default config does not exist just use any available debug config
            loggerConfig = debugLoggerConfig;
        } else {
            // skip if no logger configuration files could be found.
            return;
        }

        // reconfigure logger
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // clear any previous configuration
            context.reset();

            // prepare props
            loadProperties(context);

            // configure
            configurator.doConfigure(loggerConfig);

            // print warning in case of syntax errors
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        } catch (JoranException ex) {
            // print warning in case of syntax errors
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);

            throw new CouldNotPerformException("Could not load logger settings!", ex);
        }
    }

    private static void loadProperties(final LoggerContext context) {
        // store some variables
        context.putProperty("APPLICATION_NAME", JPService.getApplicationName());
        context.putProperty("SUBMODULE_NAME", JPService.getSubmoduleName());
        context.putProperty("MODULE_SEPARATOR", JPService.getSubmoduleName().isEmpty() ? "" : "-");
        try {
            context.putProperty("LOGGER_TARGET_DIR", JPService.getValue(JPLogDirectory.class).getAbsolutePath());
            // inform user about log redirection
            System.out.println("Logs can be found in: " + JPService.getValue(JPLogDirectory.class).getAbsolutePath());
        } catch (JPNotAvailableException e) {
            // just store nothing if the propertie could not be loaded.
        }
    }

    /**
     * @param args
     * @param application
     * @param launchers
     *
     * @deprecated please use {@code main(final Class<?> application, final String[] args, final Class<? extends AbstractLauncher>... launchers)} instead.
     */
    @Deprecated
    public static void main(final String[] args, final Class<?> application, final Class<? extends AbstractLauncher>... launchers) {
        main(application, args, launchers);
    }

    public static void main(final Class<?> application, final Class<?> submodule, final String[] args, final Class<? extends AbstractLauncher>... launchers) {

        // setup application names
        JPService.setApplicationName(application);
        JPService.setSubmoduleName(submodule); // requires the application name to be set

        main(application, args, launchers);
    }

    public static void main(final Class<?> application, final String submoduleName, final String[] args, final Class<? extends AbstractLauncher>... launchers) {

        // setup application names
        JPService.setApplicationName(application);
        JPService.setSubmoduleName(submoduleName); // requires the application name to be set

        main(application, args, launchers);
    }

    public static void main(final Class<?> application, final String[] args, final Class<? extends AbstractLauncher>... launchers) {

        // setup application names
        JPService.setApplicationName(application);

        // register interruption of this thread as shutdown hook
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> mainThread.interrupt()));

        final Map<Class<? extends AbstractLauncher>, AbstractLauncher> launcherMap = new HashMap<>();
        for (final Class<? extends AbstractLauncher> launcherClass : launchers) {
            try {
                launcherMap.put(launcherClass, launcherClass.getConstructor().newInstance());
            } catch (java.lang.InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException ex) {
                errorExceptionStack = MultiException.push(application, new CouldNotPerformException("Could not load launcher class!", ex), errorExceptionStack);
            }
        }

        for (final AbstractLauncher launcher : launcherMap.values()) {
            launcher.loadProperties();
        }

        // register launcher jps
        JPService.registerProperty(JPPrintLauncher.class);
        JPService.registerProperty(JPExcludeLauncher.class);
        JPService.registerProperty(JPLoggerConfigDirectory.class);
        JPService.registerProperty(JPLoggerConfigFile.class);
        JPService.registerProperty(JPLoggerDebugConfigFile.class);
        JPService.registerProperty(JPLogDirectory.class);

        // parse properties
        JPService.parseAndExitOnError(args);

        // load custom logger settings
        try {
            loadCustomLoggerSettings();
        } catch (CouldNotPerformException e) {
            System.exit(2);
            return;
        }
        final Logger logger = LoggerFactory.getLogger(Launcher.class);

        // print launcher end exit
        try {
            if (JPService.getProperty(JPPrintLauncher.class).getValue()) {
                if (launcherMap.isEmpty()) {
                    System.out.println(generateAppName() + " does not provide any launcher!");
                    System.exit(255);
                }
                System.out.println("Available launcher:");
                System.out.println();
                int maxLauncherNameSize = 0;
                for (final Entry<Class<? extends AbstractLauncher>, AbstractLauncher> launcherEntry : launcherMap.entrySet()) {
                    maxLauncherNameSize = Math.max(maxLauncherNameSize, launcherEntry.getKey().getSimpleName().length());
                }
                for (final Entry<Class<? extends AbstractLauncher>, AbstractLauncher> launcherEntry : launcherMap.entrySet()) {
                    System.out.println("\t• " + StringProcessor.fillWithSpaces(launcherEntry.getKey().getSimpleName(), maxLauncherNameSize) + "  ⊳  " + launcherEntry.getKey().getName());
                }
                System.out.println();
                System.exit(0);
            }
        } catch (JPNotAvailableException ex) {
            ExceptionPrinter.printHistory("Could not check if launcher should be printed.", ex, logger);
        }

        logger.info("Start " + generateAppName() + "...");

        for (final Entry<Class<? extends AbstractLauncher>, AbstractLauncher> launcherEntry : new HashSet<>(launcherMap.entrySet())) {

            // check if launcher was excluded
            boolean exclude = false;

            try {
                //filter excluded launcher
                for (String exclusionPatter : JPService.getProperty(JPExcludeLauncher.class).getValue()) {
                    if (launcherEntry.getKey().getName().toLowerCase().contains(exclusionPatter.replace("-", "").replace("_", "").toLowerCase())) {
                        exclude = true;
                    }
                }
            } catch (JPNotAvailableException ex) {
                ExceptionPrinter.printHistory("Could not process launcher exclusion!", ex, logger);
            }

            if (exclude) {
                logger.info(launcherEntry.getKey().getSimpleName() + " excluded from execution.");
                launcherMap.remove(launcherEntry.getKey());
                continue;
            }

            launcherEntry.getValue().launch();
        }

        synchronized (WAITING_TASK_LIST_LOCK) {
            // start all waiting tasks in parallel
            for (final Entry<Class<? extends AbstractLauncher>, AbstractLauncher> launcherEntry : launcherMap.entrySet()) {
                final Future waitingTask = GlobalCachedExecutorService.submit(() -> {
                    while (!Thread.interrupted()) {
                        try {
                            try {
                                launcherEntry.getValue().getLauncherTask().get(LAUNCHER_TIMEOUT, TimeUnit.MILLISECONDS);
                                if (!launcherEntry.getValue().isVerified()) {
                                    pushToVerificationExceptionStack(application, new CouldNotPerformException("Could not verify " + launcherEntry.getKey().getSimpleName() + "!", launcherEntry.getValue().getVerificationFailedCause()));
                                }
                            } catch (CancellationException ex) {
                                // cancellation means the complete launch was canceled anyway and no further steps are required.
                            } catch (ExecutionException ex) {
                                // recover Interrupted exception to avoid error printing during system shutdown
                                Throwable initialCause = ExceptionProcessor.getInitialCause(ex);
                                if (initialCause instanceof InterruptedException) {
                                    throw (InterruptedException) initialCause;
                                }
                                throw ex;
                            }
                            break;
                        } catch (TimeoutException ex) {
                            logger.warn("Launcher " + launcherEntry.getKey().getSimpleName() + " startup delay detected!");
                        } catch (InterruptedException ex) {
                            // if a core launcher could not be started the whole startup failed so interrupt
                            if (launcherEntry.getValue().isCoreLauncher()) {
                                // shutdown all launcher
                                forceStopLauncher(launcherMap);
                                return null;
                            }

                        } catch (Exception ex) {
                            final CouldNotPerformException exx = new CouldNotPerformException("Could not launch " + launcherEntry.getKey().getSimpleName() + "!", ex);

                            // if a core launcher could not be started the whole startup failed so interrupt
                            if (launcherEntry.getValue().isCoreLauncher()) {
                                pushToErrorExceptionStack(application, ExceptionPrinter.printHistoryAndReturnThrowable(exx, logger));

                                // shutdown all launcher
                                forceStopLauncher(launcherMap);
                            }

                            pushToErrorExceptionStack(application, ExceptionPrinter.printHistoryAndReturnThrowable(exx, logger));

                            // finish launcher
                            return null;
                        }
                    }
                    return null;
                });
                waitingTaskList.add(waitingTask);
            }
        }

        try {
            for (final Future waitingTask : waitingTaskList) {
                try {
                    waitingTask.get();
                } catch (ExecutionException ex) {
                    // these exception will be pushed to the error exception stack anyway and printed in the summary
                } catch (CancellationException ex) {
                    // if a core launcher fails a cancellation exception will be thrown
                    throw new InterruptedException();
                }
            }
        } catch (InterruptedException ex) {

            // shutdown all launcher
            forceStopLauncher(launcherMap);

            // recover interruption
            Thread.currentThread().interrupt();

            // print a summary containing the exceptions
            printSummary(application, logger, generateAppName() + " caught shutdown signal during startup phase!");

            return;
        }
        printSummary(application, logger, generateAppName() + " was started with restrictions!");
    }

    private static void pushToVerificationExceptionStack(Object source, Exception ex) {
        synchronized (VERIFICATION_STACK_LOCK) {
            verificationExceptionStack = MultiException.push(source, ex, verificationExceptionStack);
        }
    }

    private static void pushToErrorExceptionStack(Object source, Exception ex) {
        synchronized (ERROR_STACK_LOCK) {
            errorExceptionStack = MultiException.push(source, ex, errorExceptionStack);
        }
    }

    private static void stopWaiting() {
        synchronized (WAITING_TASK_LIST_LOCK) {
            for (final Future waitingTask : waitingTaskList) {
                if (!waitingTask.isDone()) {
                    waitingTask.cancel(true);
                }
            }
        }
    }

    private static void interruptLauncherBoot(final Map<Class<? extends AbstractLauncher>, AbstractLauncher> launcherMap) {

        // stop boot
        stopWaiting();

        // interrupt all launcher
        for (final Entry<Class<? extends AbstractLauncher>, AbstractLauncher> launcherEntryToStop : launcherMap.entrySet()) {
            launcherEntryToStop.getValue().interruptBoot();
        }
    }

    private static void forceStopLauncher(final Map<Class<? extends AbstractLauncher>, AbstractLauncher> launcherMap) {

        interruptLauncherBoot(launcherMap);

        // stop all launcher. This is done in an extra loop since stop can block if the launcher is not yet fully interrupted.
        for (final Entry<Class<? extends AbstractLauncher>, AbstractLauncher> launcherEntryToStop : launcherMap.entrySet()) {
            launcherEntryToStop.getValue().stop();
        }
    }

    private static String generateAppName() {
        return JPService.getApplicationName() + (JPService.getSubmoduleName().isEmpty() ? "" : "-" + JPService.getSubmoduleName());
    }

    private static void printSummary(final Class application, final Logger logger, final String errorMessage) {
        try {
            MultiException.ExceptionStack exceptionStack = null;
            try {
                MultiException.checkAndThrow(() -> "Errors during startup phase!", errorExceptionStack);

            } catch (MultiException ex) {
                exceptionStack = MultiException.push(application, ex, exceptionStack);
            }
            try {
                MultiException.checkAndThrow(() -> "Verification process not passed!", verificationExceptionStack);
            } catch (MultiException ex) {
                exceptionStack = MultiException.push(application, ex, exceptionStack);
            }

            if (Thread.currentThread().isInterrupted()) {
                logger.info(generateAppName() + " was interrupted.");
                return;
            }

            MultiException.checkAndThrow(() -> errorMessage, exceptionStack);
            logger.info(generateAppName() + " successfully started.");

        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, logger);
        }
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        super.init(SCOPE_PREFIX_LAUNCHER + ScopeProcessor.COMPONENT_SEPARATOR + JPService.getApplicationName() + ScopeProcessor.COMPONENT_SEPARATOR + ScopeProcessor.convertIntoValidScopeComponent(getName()));

        try {
            verifyNonRedundantExecution();
        } catch (VerificationFailedException e) {
            ExceptionPrinter.printHistoryAndExit("Application startup skipped!", e, logger);
        }
    }

    /**
     * This method can be overwritten to identify a core launcher.
     * This means that if the start of this launcher fails the whole launching
     * process will be stopped.
     *
     * @return false, can be overwritten to return true
     */
    public boolean isCoreLauncher() {
        return false;
    }

    public L getLaunchable() {
        return launchable;
    }

    /**
     * Method returns the name of this launcher.
     *
     * @return the name as string.
     */
    @Override
    public String getName() {
        return getClass().getSimpleName().replace("Launcher", "");
    }

    /**
     * Method creates a launchable instance without any arguments.. In case the launchable needs arguments you can overwrite this method and instantiate the launchable by ourself.
     *
     * @return the new instantiated launchable.
     *
     * @throws CouldNotPerformException is thrown in case the launchable could not properly be instantiated.
     */
    protected L instantiateLaunchable() throws CouldNotPerformException {
        try {
            return launchableClass.getConstructor().newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException ex) {
            throw new CouldNotPerformException("Could not load launchable class!", ex);
        }
    }

    // Load application specific java properties.
    protected abstract void loadProperties();

    /**
     * Method verifies a running application.
     *
     * @throws VerificationFailedException is thrown if the application is started with any restrictions.
     * @throws InterruptedException        is thrown if the verification process is externally interrupted.
     */
    protected void verify() throws VerificationFailedException, InterruptedException {
        // overwrite for verification.
    }

    private void setState(final LauncherState state) {
        try (ClosableDataBuilder<Builder> dataBuilder = getDataBuilder(this)) {
            dataBuilder.getInternalBuilder().setLauncherState(state);
        } catch (Exception e) {
            ExceptionPrinter.printHistory("Could not apply state change!", e, logger);
        }
    }

    public void verifyNonRedundantExecution() throws VerificationFailedException {
        // verify that launcher was not already externally started
        try {
            final LauncherRemote launcherRemote = new LauncherRemote();
            launcherRemote.init(getScope());
            try {
                launcherRemote.activate();
                launcherRemote.waitForConnectionState(State.CONNECTED, 1000);
                throw new VerificationFailedException("Redundant execution of Launcher[" + getName() + "] detected!");
            } catch (org.openbase.jul.exception.TimeoutException e) {
                // this is the default since no other instant should be launched yet.
            } finally {
                launcherRemote.shutdown();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (VerificationFailedException e) {
            throw e;
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory("Could not properly detect redundant launcher!", e, logger);
        }
    }

    @Override
    public Future<Void> launch() {

        if (launcherTask != null && !launcherTask.isDone()) {
            return FutureProcessor.canceledFuture(Void.class, new InvalidStateException("Could not launch " + getName() + "! Application still running!"));
        }

        launcherTask = GlobalCachedExecutorService.submit(() -> {

            try {
                init();
                activate();
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not activate Launcher[" + getName() + "]!", ex, logger);
            } catch (InterruptedException e) {
                throw e;
            }

            synchronized (LAUNCHER_LOCK) {
                setState(LauncherState.INITIALIZING);
                launchable = instantiateLaunchable();
                try {
                    launchable.init();
                    setState(LauncherState.LAUNCHING);

                    launchable.activate();
                    launchTime = System.currentTimeMillis();
                    setState(LauncherState.RUNNING);
                    try {
                        verify();
                        verified = true;
                    } catch (VerificationFailedException ex) {
                        verified = false;
                        verificationFailedException = ex;
                    }
                } catch (InterruptedException ex) {
                    setState(LauncherState.STOPPING);
                    return null;
                } catch (Exception ex) {
                    setState(LauncherState.ERROR);
                    launchable.shutdown();
                    if (!ExceptionProcessor.isCausedBySystemShutdown(ex)) {
                        ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Could not launch " + getName(), ex), logger);
                    }
                }
                return null;
            }
        });
        return launcherTask;
    }

    @Override
    public void relaunch() throws CouldNotPerformException, InterruptedException {
        synchronized (LAUNCHER_LOCK) {
            stop();
            try {
                launch().get();
            } catch (ExecutionException | CancellationException ex) {
                throw new CouldNotPerformException(ex);
            }
        }
    }

    @Override
    public void stop() {

        interruptBoot();

        synchronized (LAUNCHER_LOCK) {
            setState(LauncherState.STOPPING);
            if (launchable != null) {
                launchable.shutdown();
            }
            setState(LauncherState.STOPPED);
        }
    }

    /**
     * Method cancels the boot process of this launcher.
     */
    private void interruptBoot() {
        if (isBooting()) {
            launcherTask.cancel(true);
        }
    }

    /**
     * @return true if the launcher is currently booting.
     */
    private boolean isBooting() {
        return launcherTask != null && !launcherTask.isDone();
    }

    @Override
    public void shutdown() {
        stop();
        super.shutdown();
    }

    @Override
    public long getUpTime() {
        if (launchTime < 0) {
            return 0;
        }
        return (System.currentTimeMillis() - launchTime);
    }

    @Override
    public long getLaunchTime() {
        return launchTime;
    }

    @Override
    public boolean isVerified() {
        return verified;
    }

    public VerificationFailedException getVerificationFailedCause() {
        return verificationFailedException;
    }

    public Future<Void> getLauncherTask() {
        return launcherTask;
    }

    @Override
    public void registerMethods(RPCServer server) {
        // currently, the launcher does not support any rpc methods.
    }
}
