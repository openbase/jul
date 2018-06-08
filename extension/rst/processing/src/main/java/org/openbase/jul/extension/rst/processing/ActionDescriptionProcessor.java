package org.openbase.jul.extension.rst.processing;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import rst.calendar.DateTimeType.DateTime;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation;
import rst.domotic.action.ActionAuthorityType.ActionAuthority;
import rst.domotic.action.ActionDescriptionType.ActionDescription;
import rst.domotic.action.ActionDescriptionType.ActionDescriptionOrBuilder;
import rst.domotic.action.ActionParameterType.ActionParameter;
import rst.domotic.action.ActionReferenceType.ActionReference;
import rst.domotic.service.ServiceStateDescriptionType.ServiceStateDescription;
import rst.domotic.state.ActionStateType.ActionState;
import rst.domotic.unit.UnitTemplateType.UnitTemplate;
import rst.timing.IntervalType.Interval;

/*-
 * #%L
 * JUL Extension RST Processing
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
/**
 * TODO: release : remove all action authority parts
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class ActionDescriptionProcessor {

    public static final String TOKEN_SEPARATOR = "#";

    public static final String AUTHORITY_KEY = "$AUTHORITY";
    public static final String SERVICE_TYPE_KEY = "$SERVICE_TYPE";
    public static final String LABEL_KEY = "$LABEL";
    public static final String SERVICE_ATTRIBUTE_KEY = "SERVICE_ATTRIBUTE";
    public static final String GENERIC_ACTION_LABEL = LABEL_KEY + "[" + SERVICE_ATTRIBUTE_KEY + "]";
    public static final String GENERIC_ACTION_DESCRIPTION = AUTHORITY_KEY + " changed " + SERVICE_TYPE_KEY + " of unit " + LABEL_KEY + " to " + SERVICE_ATTRIBUTE_KEY;

    public static long MIN_ALLOCATION_TIME_MILLI = 10000;

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
        ActionDescription.Builder actionDescription = ActionDescription.newBuilder();
        ResourceAllocation.Builder resourceAllocation = actionDescription.getResourceAllocationBuilder();
        ServiceStateDescription.Builder serviceStateDescription = actionDescription.getServiceStateDescriptionBuilder();

        // initialize values which are true for every ActionDescription
        actionDescription.setId(UUID.randomUUID().toString());
        actionDescription.setActionState(ActionState.newBuilder().setValue(ActionState.State.INITIALIZED).build());

        LabelProcessor.addLabel(actionDescription.getLabelBuilder(), Locale.ENGLISH, GENERIC_ACTION_LABEL);
        actionDescription.setDescription(GENERIC_ACTION_DESCRIPTION);

        // initialize other required fields from ResourceAllocation
        resourceAllocation.setId(actionDescription.getId());
        resourceAllocation.setSlot(Interval.getDefaultInstance());
        resourceAllocation.setState(ResourceAllocation.State.REQUESTED);

        // add Authority and ResourceAllocation.Initiator
//        actionDescription.setActionAuthority(actionAuthority);
        resourceAllocation.setInitiator(initiator);

        // add values from ActionParameter
        actionDescription.setExecutionTimePeriod(actionParameter.getExecutionTimePeriod());
        actionDescription.setExecutionValidity(actionParameter.getExecutionValidity());
        if (actionDescription.getExecutionTimePeriod() != 0 && actionParameter.getPolicy() != ResourceAllocation.Policy.PRESERVE) {
            resourceAllocation.setPolicy(ResourceAllocation.Policy.PRESERVE);
        } else {
            resourceAllocation.setPolicy(actionParameter.getPolicy());
        }
        resourceAllocation.setPriority(actionParameter.getPriority());
        serviceStateDescription.setUnitType(actionParameter.getUnitType());
        // if an initiator action is defined in ActionParameter the actionChain is updated
        if (actionParameter.hasInitiator()) {
            List<ActionReference> actionReferenceList = actionParameter.getInitiator().getActionChainList();
            ActionReference.Builder actionReference = ActionReference.newBuilder();
            actionReference.setActionId(actionParameter.getInitiator().getId());
//            actionReference.setAuthority(actionParameter.getInitiator().getActionAuthority());
            actionReference.setServiceStateDescription(actionParameter.getInitiator().getServiceStateDescription());
            actionReferenceList.add(actionReference.build());
            actionDescription.addAllActionChain(actionReferenceList);
        }

        return actionDescription;
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

    /**
     * Create an interval which start now and ends after the maximum of MIN_ALLCOCATION_TIME_MILLI
     * and the executionTimePeriod.
     * Updates the executionTimePeriod of the given actionDescription.
     *
     * @param actionDescription actionDescription
     * @return an Interval generated as described above
     */
    public static Interval getAllocationInterval(final ActionDescription.Builder actionDescription) {
        Interval.Builder interval = Interval.newBuilder();

        actionDescription.setExecutionTimePeriod(Math.min(actionDescription.getExecutionTimePeriod(), actionDescription.getExecutionValidity().getMillisecondsSinceEpoch() - System.currentTimeMillis()));

        interval.setBegin(TimestampProcessor.getCurrentTimestamp());
        interval.setEnd(TimestampJavaTimeTransform.transform(System.currentTimeMillis() + Math.max(MIN_ALLOCATION_TIME_MILLI, actionDescription.getExecutionTimePeriod())));

        return interval.build();
    }

    /**
     * Update the slot of the ResourceAllocation based on the current time and the
     * values of the ActionDescription.
     * To generate the slot the method {@link #getAllocationInterval(ActionDescription.Builder) getAllocationInterval} is used.
     *
     * @param actionDescription the ActionDescription inside which the ResourceAllocation is updated
     * @return the updated ActionDescription
     */
    public static ActionDescription.Builder updateResourceAllocationSlot(final ActionDescription.Builder actionDescription) {
        final ResourceAllocation.Builder resourceAllocationBuilder = actionDescription.getResourceAllocationBuilder();
        resourceAllocationBuilder.setSlot(getAllocationInterval(actionDescription));
        return actionDescription;
    }

    /**
     * Build an ActionReference from a given ActionDescription which can be added to an action chain.
     *
     * @param actionDescription the ActionDescription from which the ActionReference is generated
     * @return an ActionReference for the given ActionDescription
     */
    public static ActionReference getActionReferenceFromActionDescription(final ActionDescriptionOrBuilder actionDescription) {
        ActionReference.Builder actionReference = ActionReference.newBuilder();
        actionReference.setActionId(actionDescription.getId());
//        actionReference.setAuthority(actionDescription.getActionAuthority());
        actionReference.setServiceStateDescription(actionDescription.getServiceStateDescription());
        return actionReference.build();
    }

    /**
     * Updates the ActionChain which is a description of actions that lead to this action.
     * The action chain is updated in a way that the immediate parent is the first element of
     * the chain. So the index of the chain indicates how many actions are in between this
     * action and the causing action.
     *
     * @param actionDescription the ActionDescription which is updated
     * @param parentAction the ActionDescription of the action which is the cause for the new action
     * @return the updated ActionDescription
     */
    public static ActionDescription.Builder updateActionChain(final ActionDescription.Builder actionDescription, final ActionDescriptionOrBuilder parentAction) {
        actionDescription.addActionChain(getActionReferenceFromActionDescription(parentAction));
        actionDescription.addAllActionChain(parentAction.getActionChainList());
        return actionDescription;
    }

    /**
     * Check if the ResourceAllocation inside the ActionDescription has a token in its id field.
     *
     * @param actionDescription the ActionDescription which is checked
     * @return true if the id field contains a # which it the token separator and else false
     */
    public static boolean hasResourceAllocationToken(final ActionDescriptionOrBuilder actionDescription) {
        return actionDescription.getResourceAllocation().getId().contains(TOKEN_SEPARATOR);
    }

    /**
     * Add a token to the id field of the ResourceAllocation inside the ActionDescription.
     * This method does nothing if the id already contains a token.
     *
     * @param actionDescription the ActionDescription which is updated
     * @return the updated ActionDescription
     */
    public static ActionDescription.Builder generateToken(final ActionDescription.Builder actionDescription) {
        ResourceAllocation.Builder resourceAllocation = actionDescription.getResourceAllocationBuilder();
        if (hasResourceAllocationToken(actionDescription)) {
            return actionDescription;
        } else {
            String token = UUID.randomUUID().toString();
            resourceAllocation.setId(resourceAllocation.getId() + TOKEN_SEPARATOR + token);
            return actionDescription;
        }
    }

    /**
     * Get a new id value for the id field in the ResourceAllocation of an ActionDescription
     * while keeping the token if there si one.
     *
     * @param actionDescription the action description which is updated as described above
     * @return the action description which is updated as described above
     */
    public static ActionDescription.Builder updateResourceAllocationId(final ActionDescription.Builder actionDescription) {
        ResourceAllocation.Builder resourceAllocation = actionDescription.getResourceAllocationBuilder();
        String newId = UUID.randomUUID().toString();
        if (!hasResourceAllocationToken(actionDescription)) {
            resourceAllocation.setId(newId);
        } else {
            String token = resourceAllocation.getId().split(TOKEN_SEPARATOR)[1];
            resourceAllocation.setId(newId + TOKEN_SEPARATOR + token);
        }
        return actionDescription;
    }
    
    /**
     * Method generates a description for the given action pipeline.
     * @param actionDescriptionCollection a collection of depending action descriptions.
     * @return a human readable description of the action pipeline.
     */
    public static String getDescription(final Collection<ActionDescription> actionDescriptionCollection) {
        String description = "";
        for(ActionDescription actionDescription : actionDescriptionCollection) {
            if(!description.isEmpty()) {
                description += " > ";
            }
            description += actionDescription.getDescription();
        }
        return description;
    }
}
