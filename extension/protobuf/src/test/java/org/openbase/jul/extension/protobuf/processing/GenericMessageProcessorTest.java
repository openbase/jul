package org.openbase.jul.extension.protobuf.processing;

/*-
 * #%L
 * JUL Extension Protobuf
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

import org.junit.jupiter.api.Test;
import org.openbase.type.domotic.state.ColorStateType.ColorState;
import org.openbase.type.domotic.state.PowerStateType.PowerState;
import org.openbase.type.domotic.unit.dal.ColorableLightDataType.ColorableLightData;
import org.openbase.type.domotic.unit.dal.LightDataType.LightData;
import org.openbase.type.vision.ColorType;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class GenericMessageProcessorTest {

    public GenericMessageProcessorTest() {
    }

    /**
     * Test of process method, of class GenericMessageProcessor.
     */
    @Test
    public void testProcess() throws Exception {
        System.out.println("process");

        GenericMessageProcessor<LightData> messageProcessor = new GenericMessageProcessor<>(LightData.class);

        String id = "ID";
        String testAlias = "Alias";
        PowerState powerState = PowerState.newBuilder().setValue(PowerState.State.OFF).build();
        ColorState colorState = ColorState.newBuilder().setColor(ColorType.Color.getDefaultInstance()).build();
        ColorableLightData colorableLightData = ColorableLightData.newBuilder().setId(id).setColorState(colorState).setPowerState(powerState).addAlias(testAlias).build();

        LightData lightData = messageProcessor.process(colorableLightData);

        assertEquals(id, lightData.getId(), "Id has not been set!");
        assertEquals(testAlias, lightData.getAlias(0), "Label has not been set!");
        assertEquals(powerState, lightData.getPowerState(), "PowerState has not been set!");
    }

}
