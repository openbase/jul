package org.openbase.jul.pattern.launch;

/*-
 * #%L
 * JUL Pattern Launch
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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.*;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.com.AbstractIdentifiableController;
import org.openbase.jul.extension.rsb.com.RPCHelper;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.iface.provider.NameProvider;
import org.openbase.jul.pattern.Launcher;
import org.openbase.jul.pattern.launch.jp.JPExcludeLauncher;
import org.openbase.jul.pattern.launch.jp.JPPrintLauncher;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.SyncObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Scope;
import rst.domotic.state.ActivationStateType.ActivationState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;

/**
 * @param <L> the launchable to launch by this launcher.
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class AbstractLauncher<L extends Launchable> extends AbstractIdentifiableController<ActivationState, ActivationState.Builder> implements Launcher, VoidInitializable, NameProvider {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final long LAUNCHER_TIMEOUT = 60000;
    public static final String SCOPE_PREFIX_LAUNCHER = Scope.COMPONENT_SEPARATOR + "launcher";

    private final Class<L> launchableClass;
    private final Class applicationClass;
    private L launchable;
    private long launchTime = -1;
    private LauncherState state;
    private boolean verified;
    private VerificationFailedException verificationFailedException;

    /**
     * Constructor prepares the launcher and registers already a shutdown hook.
     * The launcher class is used to instantiate a new launcher instance if the instantiateLaunchable() method is not overwritten.
     * <p>
     * After instantiation of this class the launcher must be initialized and activated before the RSB interface is provided.
     *
     * @param launchableClass  the class to be launched.
     * @param applicationClass the class representing this application. Those is used for scope generation if the getName() method is not overwritten.
     *
     * @throws org.openbase.jul.exception.InstantiationException
     */
    public AbstractLauncher(final Class applicationClass, final Class<L> launchableClass) throws InstantiationException {
        super(ActivationState.newBuilder());
//        try {
        this.launchableClass = launchableClass;
        this.applicationClass = applicationClass;
//        } catch (CouldNotPerformException ex) {
//            throw new InstantiationException(this, ex);
//        }
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        try {
            super.init(SCOPE_PREFIX_LAUNCHER + Scope.COMPONENT_SEPARATOR + ScopeGenerator.convertIntoValidScopeComponent(getName()));
        } catch (NotAvailableException ex) {
            throw new InitializationException(this, ex);
        }
    }

    @Override
    public void registerMethods(RSBLocalServer server) throws CouldNotPerformException {
        RPCHelper.registerInterface(Launcher.class, this, server);
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
     * Method returns the application name.
     * <p>
     * By default the application name is the name of the given application class name.
     *
     * @return the name as string.
     *
     * @throws NotAvailableException
     */
    @Override
    public String getName() throws NotAvailableException {
        return applicationClass.getSimpleName();
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
            return launchableClass.newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException ex) {
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

    private final SyncObject LAUNCHER_LOCK = new SyncObject(this);

    public enum LauncherState {

        INITIALIZING,
        LAUNCHING,
        RUNNING,
        STOPPING,
        STOPPED,
        ERROR
    }

    private void setState(final LauncherState state) {
        this.state = state;
    }

    @Override
    public void launch() throws CouldNotPerformException, InterruptedException {
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
            } catch (CouldNotPerformException ex) {
                setState(LauncherState.ERROR);
                launchable.shutdown();
                throw new CouldNotPerformException("Could not launch " + getName(), ex);
            }
        }
    }

    @Override
    public void relaunch() throws CouldNotPerformException, InterruptedException {
        synchronized (LAUNCHER_LOCK) {
            stop();
            launch();
        }
    }

    @Override
    public void stop() {
        synchronized (LAUNCHER_LOCK) {
            setState(LauncherState.STOPPING);
            if (launchable != null) {
                launchable.shutdown();
            }
            setState(LauncherState.STOPPED);
        }
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

    private static MultiException.ExceptionStack errorExceptionStack = null;
    private static MultiException.ExceptionStack verificationExceptionStack = null;

    private static final List<Future> waitingTaskList = new ArrayList<>();

    public static void main(final String args[], final Class application, final Class<? extends AbstractLauncher>... launchers) {
        final Logger logger = LoggerFactory.getLogger(Launcher.class);
        JPService.setApplicationName(application);

        // register interruption of this thread as shutdown hook
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(mainThread::interrupt));

        Map<Class<? extends AbstractLauncher>, AbstractLauncher> launcherMap = new HashMap<>();
        for (final Class<? extends AbstractLauncher> launcherClass : launchers) {
            try {
                launcherMap.put(launcherClass, launcherClass.newInstance());
            } catch (java.lang.InstantiationException | IllegalAccessException ex) {
                errorExceptionStack = MultiException.push(application, new CouldNotPerformException("Could not load launcher class!", ex), errorExceptionStack);
            }
        }

        for (final AbstractLauncher launcher : launcherMap.values()) {
            launcher.loadProperties();
        }

        // register launcher jps
        JPService.registerProperty(JPPrintLauncher.class);
        JPService.registerProperty(JPExcludeLauncher.class);

        JPService.parseAndExitOnError(args);

        // print launcher end exit
        try {
            if (JPService.getProperty(JPPrintLauncher.class).getValue()) {
                if (launcherMap.isEmpty()) {
                    System.out.println(JPService.getApplicationName() + " does not provide any launcher!");
                    System.exit(255);
                }
                System.out.println("Available launcher:");
                System.out.println("");
                int maxLauncherNameSize = 0;
                for (final Entry<Class<? extends AbstractLauncher>, AbstractLauncher> launcherEntry : launcherMap.entrySet()) {
                    maxLauncherNameSize = Math.max(maxLauncherNameSize, launcherEntry.getKey().getSimpleName().length());
                }
                for (final Entry<Class<? extends AbstractLauncher>, AbstractLauncher> launcherEntry : launcherMap.entrySet()) {
                    System.out.println("\t• " + StringProcessor.fillWithSpaces(launcherEntry.getKey().getSimpleName(), maxLauncherNameSize) + "  ⊳  " + launcherEntry.getKey().getName());
                }
                System.out.println("");
                System.exit(0);
            }
        } catch (JPNotAvailableException ex) {
            ExceptionPrinter.printHistory("Could not check if launcher should be printed.", ex, logger);
        }

        logger.info("Start " + JPService.getApplicationName() + "...");

        final Map<Entry<Class<? extends AbstractLauncher>, AbstractLauncher>, Future> launchableFutureMap = new HashMap<>();
        for (final Entry<Class<? extends AbstractLauncher>, AbstractLauncher> launcherEntry : launcherMap.entrySet()) {

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
                continue;
            }

            launchableFutureMap.put(launcherEntry, GlobalCachedExecutorService.submit((Callable<Void>) () -> {
                launcherEntry.getValue().launch();
                return null;
            }));
        }

        // start all waiting tasks in parallel
        for (final Entry<Entry<Class<? extends AbstractLauncher>, AbstractLauncher>, Future> launcherEntry : new ArrayList<>(launchableFutureMap.entrySet())) {
            final Future waitingTask = GlobalCachedExecutorService.submit(() -> {
                while (!Thread.interrupted()) {
                    try {
                        try {
                            launcherEntry.getValue().get(LAUNCHER_TIMEOUT, TimeUnit.MILLISECONDS);
                            if (!launcherEntry.getKey().getValue().isVerified()) {
                                pushToVerificationExceptionStack(application, new CouldNotPerformException("Could not verify " + launcherEntry.getKey().getKey().getSimpleName() + "!", launcherEntry.getKey().getValue().getVerificationFailedCause()));
                            }
                        } catch (ExecutionException ex) {
                            // recover Interrupted exception to avoid error printing during system shutdown
                            Throwable initialCause = ExceptionProcessor.getInitialCause(ex);
                            if(initialCause instanceof InterruptedException) {
                                throw (InterruptedException) initialCause;
                            }
                            throw ex;
                        }
                        break;
                    } catch (InterruptedException ex) {
                        launcherEntry.getValue().cancel(true);
                        return null;
                    } catch (TimeoutException ex) {
                        ExceptionPrinter.printHistory(new CouldNotPerformException("Launcher " + launcherEntry.getKey().getKey().getSimpleName() + " startup delay detected!"), logger, LogLevel.WARN);
                    } catch (Exception ex) {
                        final CouldNotPerformException exx = new CouldNotPerformException("Could not launch " + launcherEntry.getKey().getKey().getSimpleName() + "!", ex);

                        // if a core launcher could not be started the whole startup failed so interrupt
                        if (launcherEntry.getKey().getValue().isCoreLauncher()) {
                            pushToErrorExceptionStack(application, ExceptionPrinter.printHistoryAndReturnThrowable(exx, logger));
                            stopWaiting();
                            return null;
                        }

                        pushToErrorExceptionStack(application, ExceptionPrinter.printHistoryAndReturnThrowable(exx, logger));
                        return null;
                    }
                }
                return null;
            });
            waitingTaskList.add(waitingTask);
        }

        try {
            for (final Future waitingTask : waitingTaskList) {
                try {
                    waitingTask.get();
                } catch (ExecutionException ex) {
                    // these exception will be pushed to the error exception stack in printed in the summary
                } catch (CancellationException ex) {
                    // if a core launcher fails a cancellation exception will be thrown
                    throw new InterruptedException();
                }
            }
        } catch (InterruptedException ex) {
            // kill all remaining waiting tasks
            stopWaiting();

            // shutdown all launcher
            for (Entry<?, AbstractLauncher> entry : launcherMap.entrySet()) {
                entry.getValue().shutdown();
            }
            // print a summary containing the exceptions
            printSummary(application, logger, JPService.getApplicationName() + " caught shutdown signal during startup phase!");

//            TODO: remove after fixing https://github.com/openbase/bco.registry/issues/84
            try {
                Thread.sleep(5000);
            } catch (InterruptedException exx) {
            }
            System.exit(1);
            return;
        }
        printSummary(application, logger, JPService.getApplicationName() + " was started with restrictions!");
    }

    private static final SyncObject VERIFICATION_STACK_LOCK = new SyncObject("VerificationStackLock");
    private static final SyncObject ERROR_STACK_LOCK = new SyncObject("ErrorStackLock");
    private static final SyncObject WAITING_STOP_LOCK = new SyncObject("WaitingStopLock");

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
        synchronized (WAITING_STOP_LOCK) {
            for (final Future waitingTask : waitingTaskList) {
                if (!waitingTask.isDone()) {
                    waitingTask.cancel(true);
                }
            }
        }
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
                logger.info(JPService.getApplicationName() + " was interrupted.");
                return;
            }

            MultiException.checkAndThrow(() -> errorMessage, exceptionStack);
            logger.info(JPService.getApplicationName() + " successfully started.");

        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, logger);
        }
    }
}
