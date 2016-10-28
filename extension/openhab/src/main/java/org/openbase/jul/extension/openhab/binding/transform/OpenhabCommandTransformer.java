package org.openbase.jul.extension.openhab.binding.transform;

/*
 * #%L
 * COMA DeviceManager Binding OpenHAB
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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.exception.NotSupportedException;
import rst.domotic.binding.openhab.OpenhabCommandType;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public final class OpenhabCommandTransformer {

    public static Object getServiceData(OpenhabCommandType.OpenhabCommand command, ServiceType serviceType) throws CouldNotPerformException {

        // Transform service data.
        switch (command.getType()) {
            case DECIMAL:
                switch (serviceType) {
                    case POWER_CONSUMPTION_STATE_SERVICE:
                        return PowerConsumptionStateTransformer.transform(command.getDecimal());
                    case MOTION_STATE_SERVICE:
                        return MotionStateTransformer.transform(command.getDecimal());
                    case TAMPER_STATE_SERVICE:
                        return TamperStateTransformer.transform(command.getDecimal());
                    case BATTERY_STATE_SERVICE:
                        return BatteryStateTransformer.transform(command.getDecimal());
                    case TEMPERATURE_ALARM_STATE_SERVICE:
                    case SMOKE_ALARM_STATE_SERVICE:
                        return AlarmStateTransformer.transform(command.getDecimal());
                    case SMOKE_STATE_SERVICE:
                        return SmokeStateTransformer.transform(command.getDecimal());
                    case TEMPERATURE_STATE_SERVICE:
                    case TARGET_TEMPERATURE_STATE_SERVICE:
                        return TemperatureStateTransformer.transform(command.getDecimal());
                    default:
                        // native double type
                        return command.getDecimal();
                }
            case HSB:
                switch (serviceType) {
                    case COLOR_STATE_SERVICE:
                        return ColorStateTransformer.transform(command.getHsb());
                    default:
                        throw new NotSupportedException(serviceType, OpenhabCommandTransformer.class);
                }
            case INCREASEDECREASE:
//				return IncreaseDecreaseTransformer(command.getIncreaseDecrease());
                throw new NotSupportedException(command.getType(), OpenhabCommandTransformer.class);
            case ONOFF:
                switch (serviceType) {
                    case BUTTON_STATE_SERVICE:
                        return ButtonStateTransformer.transform(command.getOnOff().getState());
                    case POWER_STATE_SERVICE:
                        return PowerStateTransformer.transform(command.getOnOff().getState());
                    default:
                        throw new NotSupportedException(serviceType, OpenhabCommandTransformer.class);
                }
            case OPENCLOSED:
                return OpenClosedStateTransformer.transform(command.getOpenClosed().getState());
            case PERCENT:
                return command.getPercent().getValue();
            case STOPMOVE:
                return StopMoveStateTransformer.transform(command.getStopMove().getState());
            case STRING:
                switch (serviceType) {
                    case HANDLE_STATE_SERVICE:
                        return HandleStateTransformer.transform(command.getText());
                    default:
                        // native string type
                        return command.getText();
                }

            case UPDOWN:
                return UpDownStateTransformer.transform(command.getUpDown().getState());
            default:
                throw new CouldNotTransformException("No corresponding data found for " + command + ".");
        }
    }

    public static Object getCommandData(final OpenhabCommandType.OpenhabCommand command) throws CouldNotPerformException {

        switch (command.getType()) {
            case DECIMAL:
                return command.getDecimal();
            case HSB:
                return command.getHsb();
            case INCREASEDECREASE:
                return command.getIncreaseDecrease();
            case ONOFF:
                return command.getOnOff();
            case OPENCLOSED:
                return command.getOpenClosed();
            case PERCENT:
                return command.getPercent();
            case STOPMOVE:
                return command.getStopMove();
            case STRING:
                return command.getText();
            case UPDOWN:
                return command.getUpDown();
            default:
                throw new CouldNotTransformException("No corresponding data found for " + command + ".");
        }
    }
}
