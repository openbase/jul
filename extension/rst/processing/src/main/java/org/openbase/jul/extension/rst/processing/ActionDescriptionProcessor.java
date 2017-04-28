package org.openbase.jul.extension.rst.processing;

import java.util.UUID;
import org.openbase.jul.exception.CouldNotPerformException;
import rst.calendar.DateTimeType.DateTime;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation;
import rst.domotic.action.ActionAuthorityType.ActionAuthority;
import rst.domotic.action.ActionDescriptionType.ActionDescription;
import rst.domotic.action.ActionReferenceType.ActionReference;
import rst.domotic.service.ServiceStateDescriptionType.ServiceStateDescription;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.ActionStateType.ActionState;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.unit.UnitTemplateType.UnitTemplate;

/*-
 * #%L
 * JUL Extension RST Processing
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class ActionDescriptionProcessor {

    public static ActionDescription getActionDescription(final PowerState powerState, final String string) throws CouldNotPerformException {
        ActionDescription.Builder actionDescription = ActionDescription.newBuilder();
        ResourceAllocation.Builder resourceAllocation = ResourceAllocation.newBuilder();

        ServiceStateDescription.Builder serviceStateDescription = ServiceStateDescription.newBuilder();

        // can be set without parameters
        actionDescription.setId(UUID.randomUUID().toString()); // UUID
        actionDescription.setLabel("UnitLabel[" + powerState.getValue().name() + "]");  // unit label
        actionDescription.setDescription("Mr. Pink changed " + ServiceType.POWER_STATE_SERVICE.name() + " of unit UNITLABEL to " + powerState.getValue().name()); // value to be set
        actionDescription.setResourceAllocation(resourceAllocation);
        actionDescription.setServiceStateDescription(serviceStateDescription);

        //resourceAllocation.setId(UUID.randomUUID().toString()); empty
        resourceAllocation.addResourceIds("UNITSCOPE"); // scope
        resourceAllocation.setDescription(actionDescription.getDescription());

        // enums empty or default values at beginning
        actionDescription.setActionState(ActionState.newBuilder().setValue(ActionState.State.INITIALIZED).build());
//        resourceAllocation.setState(ResourceAllocation.State.); // empty
//        resourceAllocation.setSlot(Interval.getDefaultInstance()); // computation in UnitAllocator from startTime and ExecutionTimePeriod

        // given as parameter
        // optional
        resourceAllocation.setPriority(ResourceAllocation.Priority.NORMAL); // default is normal
        actionDescription.setActionChain(0, ActionReference.getDefaultInstance()); // parameter is actionDescription of initiator to build reference and chain
        actionDescription.setExecutionTimePeriod(0); // 0 should be default value but others have to be set
        // like actionAuthority globale parameter, dependent und priority -> lower -> longer
        actionDescription.setExecutionValidity(DateTime.getDefaultInstance()); // default an hour
        resourceAllocation.setPolicy(ResourceAllocation.Policy.FIRST);
        serviceStateDescription.setUnitType(UnitTemplate.UnitType.UNKNOWN); // default unknown

        // required
        resourceAllocation.setInitiator(ResourceAllocation.Initiator.HUMAN); // no default value? like actionAuthority given once?
        actionDescription.setActionAuthority(ActionAuthority.getDefaultInstance()); // every time as a paramter
        return actionDescription.build();
    }
}
