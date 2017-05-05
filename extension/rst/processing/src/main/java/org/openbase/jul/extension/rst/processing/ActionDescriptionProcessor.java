package org.openbase.jul.extension.rst.processing;

import java.util.List;
import java.util.UUID;
import rst.calendar.DateTimeType.DateTime;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation;
import rst.domotic.action.ActionAuthorityType.ActionAuthority;
import rst.domotic.action.ActionDescriptionType.ActionDescription;
import rst.domotic.action.ActionParameterType.ActionParameter;
import rst.domotic.action.ActionReferenceType.ActionReference;
import rst.domotic.service.ServiceStateDescriptionType.ServiceStateDescription;
import rst.domotic.state.ActionStateType.ActionState;
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

    /**
     * Get an ActionDescription which only misses unit and service information.
     * Fields which are still missing after:
     * <ul>
     * <li>ActionDescription.Label</li>
     * <li>ActionDescription.Description</li>
     * <li>ActionDescription.ResourceAllocation.ResourceId</li>
     * <li>ActionDescription.ResourceAllocation.Description</li>
     * <li>ActionDescription.ResourceAllocation.UnitId</li>
     * <li>ActionDescription.ResourceAllocation.ServiceType</li>
     * <li>ActionDescription.ResourceAllocation.ServiceAttributeType</li>
     * <li>ActionDescription.ServiceStateDescription.ServiceAttribute</li>
     * </ul>
     *
     * @param actionParameter type which contains several parameters which are updated in the actionDescription
     * @param actionAuthority the actionAuthority for the actionDescription
     * @param initiator the initiator type for the resourceAllocation in the actionDescription
     * @return an ActionDescription that only misses unit and service information
     */
    public static ActionDescription.Builder getActionDescription(final ActionParameter actionParameter, final ActionAuthority actionAuthority, final ResourceAllocation.Initiator initiator) {
        ActionDescription.Builder actionDecsription = ActionDescription.newBuilder();
        ResourceAllocation.Builder resourceAllocation = actionDecsription.getResourceAllocationBuilder();
        ServiceStateDescription.Builder serviceStateDescription = actionDecsription.getServiceStateDescriptionBuilder();

        // initialize values which are true for every ActionDescription
        actionDecsription.setId(UUID.randomUUID().toString());
        actionDecsription.setActionState(ActionState.newBuilder().setValue(ActionState.State.INITIALIZED).build());

        // add Authority and ResourceAllocation.Initiator
        actionDecsription.setActionAuthority(actionAuthority);
        resourceAllocation.setInitiator(initiator);

        // add values from ActionParameter
        resourceAllocation.setPriority(actionParameter.getPriority());
        resourceAllocation.setPolicy(actionParameter.getPolicy());
        serviceStateDescription.setUnitType(actionParameter.getUnitType());
        actionDecsription.setExecutionTimePeriod(actionParameter.getExecutionTimePeriod());
        actionDecsription.setExecutionValidity(actionParameter.getExecutionValidity());
        // if an initiator action is defined in ActionParameter the actionChain is updated
        if (actionParameter.hasInitiator()) {
            List<ActionReference> actionReferenceList = actionParameter.getInitiator().getActionChainList();
            ActionReference.Builder actionReference = ActionReference.newBuilder();
            actionReference.setActionId(actionParameter.getInitiator().getId());
            actionReference.setAuthority(actionParameter.getInitiator().getActionAuthority());
            actionReference.setServiceStateDescription(actionParameter.getInitiator().getServiceStateDescription());
            actionReferenceList.add(actionReference.build());
            actionDecsription.addAllActionChain(actionReferenceList);
        }

        return actionDecsription;
    }

    /**
     * Get an ActionDescription which only misses unit and service information.
     * Is created with default ActionParameter.
     * Fields which are still missing after:
     * <ul>
     * <li>ActionDescription.Label</li>
     * <li>ActionDescription.Description</li>
     * <li>ActionDescription.ResourceAllocation.ResourceId</li>
     * <li>ActionDescription.ResourceAllocation.Description</li>
     * <li>ActionDescription.ResourceAllocation.UnitId</li>
     * <li>ActionDescription.ResourceAllocation.ServiceType</li>
     * <li>ActionDescription.ResourceAllocation.ServiceAttributeType</li>
     * <li>ActionDescription.ServiceStateDescription.ServiceAttribute</li>
     * </ul>
     *
     * @param actionAuthority the actionAuthority for the actionDescription
     * @param initiator the initiator type for the resourceAllocation in the actionDescription
     * @return
     */
    public static ActionDescription.Builder getActionDescription(final ActionAuthority actionAuthority, final ResourceAllocation.Initiator initiator) {
        return getActionDescription(getDefaultActionParameter(), actionAuthority, initiator);
    }

    /**
     * Get default ActionParameter. These are:
     * <ul>
     * <li>Empty initiator, which means that the action has not been triggered by another action</li>
     * <li>Priority = NORMAL</li>
     * <li>ExecutionTimePeriod = 0</li>
     * <li>ExecutionValidityTime = an hour after creation of the ActionParameter type</li>
     * <li>Policy = FIRST</li>
     * <li>UnitType = UNKNOWN</li>
     * </ul>
     *
     * @return an ActionParameter type with the described values
     */
    public static ActionParameter getDefaultActionParameter() {
        ActionParameter.Builder actionParameter = ActionParameter.newBuilder();

        //actionParameter.setInitiator();
        actionParameter.setPriority(ResourceAllocation.Priority.NORMAL);

        actionParameter.setExecutionTimePeriod(0);

        long anHourFromNow = System.currentTimeMillis() + 60 * 60 * 1000;
        DateTime dateTime = DateTime.newBuilder().setDateTimeType(DateTime.Type.FLOATING).setMillisecondsSinceEpoch(anHourFromNow).build();
        actionParameter.setExecutionValidity(dateTime);

        actionParameter.setPolicy(ResourceAllocation.Policy.FIRST);

        actionParameter.setUnitType(UnitTemplate.UnitType.UNKNOWN);

        return actionParameter.build();
    }
}
