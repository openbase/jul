package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import java.util.concurrent.Future;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Enableable;
import rst.homeautomation.state.ActivationStateType;
import rst.homeautomation.state.ActivationStateType.ActivationState;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.protobuf.ClosableDataBuilder;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine
 * Threepwood</a>
 */
public abstract class AbstractExecutableController<M extends GeneratedMessage, MB extends M.Builder<MB>, CONFIG extends GeneratedMessage> extends AbstractEnableableConfigurableController<M, MB, CONFIG> implements Enableable {

    public static final String ACTIVATION_STATE = "activation_state";

    private boolean executing;
    private final boolean autostart;

    public AbstractExecutableController(final MB builder, final boolean autostart) throws InstantiationException {
        super(builder);
        this.autostart = autostart;
    }

    @Override
    public void init(final CONFIG config) throws InitializationException, InterruptedException {
        this.executing = false;
        super.init(config);
    }

    public ActivationState getActivationState() throws NotAvailableException {
        return (ActivationState) getDataField(ACTIVATION_STATE);
    }

    public Future<Void> setActivationState(final ActivationState activation) throws CouldNotPerformException {
        if (activation.getValue().equals(ActivationState.State.UNKNOWN)) {
            throw new InvalidStateException("Unknown is not a valid state!");
        }

        if (activation.getValue().equals(getActivationState().getValue())) {
            return null;
        }

        try (ClosableDataBuilder<MB> dataBuilder = getDataBuilder(this)) {
            Descriptors.FieldDescriptor findFieldByName = dataBuilder.getInternalBuilder().getDescriptorForType().findFieldByName(ACTIVATION_STATE);
            if (findFieldByName == null) {
                throw new NotAvailableException("Field[" + ACTIVATION_STATE + "] does not exist for type " + dataBuilder.getClass().getName());
            }
            dataBuilder.getInternalBuilder().setField(findFieldByName, activation);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not apply data change!", ex);
        }

//        try {
//            setField(ACTIVATION_STATE, activation);
//        } catch (Exception ex) {
//            throw new CouldNotPerformException("Could not apply data change!", ex);
//        }
        try {
            if (activation.getValue().equals(ActivationState.State.ACTIVE)) {
                if (!executing) {
                    executing = true;
                    execute();
                }
            } else {
                if (executing) {
                    executing = false;
                    stop();
                }
            }
        } catch (CouldNotPerformException | InterruptedException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not update execution state!", ex), logger);
        }
        return null;
    }

    public boolean isExecuting() {
        return executing;
    }

    @Override
    public void enable() throws CouldNotPerformException, InterruptedException {
        super.enable();
        if (autostart) {
            setActivationState(ActivationStateType.ActivationState.newBuilder().setValue(ActivationStateType.ActivationState.State.ACTIVE).build());
        }
    }

    @Override
    public void disable() throws CouldNotPerformException, InterruptedException {
        executing = false;
        setActivationState(ActivationStateType.ActivationState.newBuilder().setValue(ActivationStateType.ActivationState.State.DEACTIVE).build());
        super.disable();
    }

    protected abstract void execute() throws CouldNotPerformException, InterruptedException;

    protected abstract void stop() throws CouldNotPerformException, InterruptedException;
}
