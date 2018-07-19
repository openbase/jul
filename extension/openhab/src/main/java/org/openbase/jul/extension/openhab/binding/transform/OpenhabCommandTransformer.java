package org.openbase.jul.extension.openhab.binding.transform;

/*
 * #%L
 * JUL Extension OpenHAB
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

import com.google.protobuf.Message;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.exception.NotSupportedException;
import org.openbase.jul.extension.rst.processing.TimestampProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.binding.openhab.OpenhabCommandType.OpenhabCommand;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public final class OpenhabCommandTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenhabCommandTransformer.class);

    public static Message getServiceData(OpenhabCommand command, ServiceType serviceType) throws CouldNotPerformException {
        Message msg;

        // Transform service data.
        switch (command.getType()) {
            case DECIMAL:
                switch (serviceType) {
                    case POWER_CONSUMPTION_STATE_SERVICE:
                        msg = PowerConsumptionStateTransformer.transform(command.getDecimal());
                        break;
                    case MOTION_STATE_SERVICE:
                        msg = MotionStateTransformer.transform(command.getDecimal());
                        break;
                    case TAMPER_STATE_SERVICE:
                        msg = TamperStateTransformer.transform(command.getDecimal());
                        break;
                    case BATTERY_STATE_SERVICE:
                        msg = BatteryStateTransformer.transform(command.getDecimal());
                        break;
                    case TEMPERATURE_ALARM_STATE_SERVICE:
                    case SMOKE_ALARM_STATE_SERVICE:
                        msg = AlarmStateTransformer.transform(command.getDecimal());
                        break;
                    case SMOKE_STATE_SERVICE:
                        msg = SmokeStateTransformer.transform(command.getDecimal());
                        break;
                    case TEMPERATURE_STATE_SERVICE:
                    case TARGET_TEMPERATURE_STATE_SERVICE:
                        msg = TemperatureStateTransformer.transform(command.getDecimal());
                        break;
                    case BRIGHTNESS_STATE_SERVICE:
                        msg = BrightnessStateTransformer.transform(command.getDecimal());
                        break;
                    case ILLUMINANCE_STATE_SERVICE:
                        msg = IlluminanceStateTransformer.transform(command.getDecimal());
                        break;
                    default:
                        // native double type
                        //return command.getDecimal();
                        throw new NotSupportedException(serviceType, OpenhabCommandTransformer.class);
                }
                break;
            case HSB:
                switch (serviceType) {
                    case COLOR_STATE_SERVICE:
                        msg = ColorStateTransformer.transform(command.getHsb());
                        break;
                    default:
                        throw new NotSupportedException(serviceType, OpenhabCommandTransformer.class);
                }
                break;
            case INCREASEDECREASE:
//				return IncreaseDecreaseTransformer(command.getIncreaseDecrease());
                throw new NotSupportedException(command.getType(), OpenhabCommandTransformer.class);
            case ONOFF:
                switch (serviceType) {
                    case BUTTON_STATE_SERVICE:
                        msg = ButtonStateTransformer.transform(command.getOnOff().getState());
                        break;
                    case POWER_STATE_SERVICE:
                        msg = PowerStateTransformer.transform(command.getOnOff().getState());
                        break;
                    // openhab posts the on/off state for dimmer in color items which we already receive for powerItems and thus can be ignored
                    case BRIGHTNESS_STATE_SERVICE:
                    case COLOR_STATE_SERVICE:
                        return null;
                    default:
                        throw new NotSupportedException(serviceType, OpenhabCommandTransformer.class);
                }
                break;
            case OPENCLOSED:
                msg = OpenClosedStateTransformer.transform(command.getOpenClosed().getState());
                break;
            case PERCENT:
                switch (serviceType) {
                    case BRIGHTNESS_STATE_SERVICE:
                        msg = BrightnessStateTransformer.transform(command.getPercent().getValue());
                        break;
                    case BLIND_STATE_SERVICE:
                        msg = BlindStateTransformer.transform(command.getPercent().getValue());
                        break;
                    default:
                        //return command.getPercent().getValue();
                        throw new NotSupportedException(serviceType, OpenhabCommandTransformer.class);
                }
                break;
            case STOPMOVE:
                msg = StopMoveStateTransformer.transform(command.getStopMove().getState());
                break;
            case STRING:
                switch (serviceType) {
                    case HANDLE_STATE_SERVICE:
                        msg = HandleStateTransformer.transform(command.getText());
                        break;
                    default:
                        // native string type
                        //return command.getText();
                        throw new NotSupportedException(serviceType, OpenhabCommandTransformer.class);                }
                break;
            case UPDOWN:
                msg = UpDownStateTransformer.transform(command.getUpDown().getState());
                break;
            default:
                throw new CouldNotTransformException("No corresponding data found for " + command + ".");
        }

        return TimestampProcessor.updateTimestamp(command.getTimestamp().getTime(), msg, TimeUnit.MICROSECONDS);
    }

    public static Object getCommandData(final OpenhabCommand command) throws CouldNotPerformException {

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
