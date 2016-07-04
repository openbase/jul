package org.openbase.jul.extension.rsb.scope;

/*
 * #%L
 * JUL Extension RSB Scope
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
import java.util.Collection;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMapInterface;
import rsb.Scope;
import rst.authorization.UserConfigType.UserConfig;
import rst.authorization.UserGroupConfigType.UserGroupConfig;
import rst.homeautomation.control.agent.AgentConfigType.AgentConfig;
import rst.homeautomation.control.app.AppConfigType.AppConfig;
import rst.homeautomation.control.scene.SceneConfigType.SceneConfig;
import rst.homeautomation.device.DeviceConfigType.DeviceConfig;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;
import rst.rsb.ScopeType;
import rst.spatial.ConnectionConfigType.ConnectionConfig;
import rst.spatial.LocationConfigType.LocationConfig;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class ScopeGenerator {

    public static String generateStringRep(final ScopeType.Scope scope) {
        return generateStringRep(scope.getComponentList());
    }

    public static String generateStringRep(final Scope scope) {
        return generateStringRep(scope.getComponents());
    }

    public static String generateStringRep(final Collection<String> components) {
        String stringRep = Scope.COMPONENT_SEPARATOR;
        for (String component : components) {
            stringRep += component;
            stringRep += Scope.COMPONENT_SEPARATOR;
        }
        return stringRep;
    }

    public static ScopeType.Scope generateScope(final String label, final String type, final ScopeType.Scope locationScope) throws CouldNotPerformException {
        try {
            ScopeType.Scope.Builder newScope = ScopeType.Scope.newBuilder();
            newScope.addAllComponent(locationScope.getComponentList());
            newScope.addComponent(convertIntoValidScopeComponent(type));
            newScope.addComponent(convertIntoValidScopeComponent(label));

            return newScope.build();
        } catch (NullPointerException ex) {
            throw new CouldNotPerformException("Coult not generate scope!", ex);
        }
    }

    public static ScopeType.Scope generateScope(final String scope) throws CouldNotPerformException {
        ScopeType.Scope.Builder generatedScope = ScopeType.Scope.newBuilder();
        for (String component : scope.split("/")) {
            
            // check for empty components (/a//b/ = /a/b/)
            if(component.isEmpty()) {
                continue;
            }
            generatedScope.addComponent(convertIntoValidScopeComponent(component));
        }
        return generatedScope.build();
    }

    public static ScopeType.Scope generateLocationScope(final LocationConfig locationConfig, final ProtoBufMessageMapInterface<String, LocationConfig, LocationConfig.Builder> registry) throws CouldNotPerformException {

        if (locationConfig == null) {
            throw new NotAvailableException("locationConfig");
        }

        if (registry == null) {
            throw new NotAvailableException("registry");
        }

        if (!locationConfig.hasLabel()) {
            throw new NotAvailableException("location.label");
        }

        if (!locationConfig.hasPlacementConfig()) {
            throw new NotAvailableException("location.placementconfig");
        }

        if (!locationConfig.getPlacementConfig().hasLocationId() || locationConfig.getPlacementConfig().getLocationId().isEmpty()) {
            throw new NotAvailableException("location.placementconfig.locationid");
        }

        ScopeType.Scope.Builder scope = ScopeType.Scope.newBuilder();
        if (!locationConfig.getRoot()) {
            scope.addAllComponent(registry.get(locationConfig.getPlacementConfig().getLocationId()).getMessage().getScope().getComponentList());
        }
        scope.addComponent(convertIntoValidScopeComponent(locationConfig.getLabel()));

        return scope.build();
    }

    public static ScopeType.Scope generateConnectionScope(final ConnectionConfig connectionConfig, final LocationConfig locationConfig) throws CouldNotPerformException {

        if (connectionConfig == null) {
            throw new NotAvailableException("connectionConfig");
        }

        if (!connectionConfig.hasLabel()) {
            throw new NotAvailableException("connectionConfig.label");
        }

        if (connectionConfig.getLabel().isEmpty()) {
            throw new NotAvailableException("Field connectionConfig.label isEmpty");
        }

        if (locationConfig == null) {
            throw new NotAvailableException("location");
        }
        
        if (!locationConfig.hasScope() || locationConfig.getScope().getComponentList().isEmpty()) {
            throw new NotAvailableException("location scope");
        }

        // add location scope
        ScopeType.Scope.Builder scope = locationConfig.getScope().toBuilder();

        // add unit type
        scope.addComponent(convertIntoValidScopeComponent(connectionConfig.getType().name().replace("_", "")));

        // add unit label
        scope.addComponent(convertIntoValidScopeComponent(connectionConfig.getLabel()));

        return scope.build();
    }

    public static ScopeType.Scope generateDeviceScope(final DeviceConfig deviceConfig, final LocationConfig locationConfig) throws CouldNotPerformException {

        if (deviceConfig == null) {
            throw new NotAvailableException("deviceConfig");
        }

        if (!deviceConfig.hasLabel()) {
            throw new NotAvailableException("device label");
        }

        if (!deviceConfig.hasPlacementConfig()) {
            throw new NotAvailableException("placement config");
        }

        if (locationConfig == null) {
            throw new NotAvailableException("location");
        }

        if (!locationConfig.hasScope() || locationConfig.getScope().getComponentList().isEmpty()) {
            throw new NotAvailableException("location scope");
        }
        
        // add location scope
        ScopeType.Scope.Builder scope = locationConfig.getScope().toBuilder();

        // add type 'device'
        scope.addComponent(convertIntoValidScopeComponent("device"));

        // add device scope
        scope.addComponent(convertIntoValidScopeComponent(deviceConfig.getLabel()));

        return scope.build();
    }

    public static ScopeType.Scope generateUnitScope(final UnitConfig unitConfig, final LocationConfig locationConfig) throws CouldNotPerformException {

        if (unitConfig == null) {
            throw new NotAvailableException("unitConfig");
        }

        if (!unitConfig.hasLabel()) {
            throw new NotAvailableException("unitConfig.label");
        }

        if (unitConfig.getLabel().isEmpty()) {
            throw new NotAvailableException("Field unitConfig.label isEmpty");
        }

        if (!unitConfig.hasPlacementConfig()) {
            throw new NotAvailableException("placement config");
        }

        if (locationConfig == null) {
            throw new NotAvailableException("location");
        }
        
        if (!locationConfig.hasScope() || locationConfig.getScope().getComponentList().isEmpty()) {
            throw new NotAvailableException("location scope");
        }

        // add location scope
        ScopeType.Scope.Builder scope = locationConfig.getScope().toBuilder();

        // add unit type
        scope.addComponent(convertIntoValidScopeComponent(unitConfig.getType().name().replace("_", "")));

        // add unit label
        scope.addComponent(convertIntoValidScopeComponent(unitConfig.getLabel()));

        return scope.build();
    }

    public static ScopeType.Scope generateAgentScope(final AgentConfig agentConfig, final LocationConfig locationConfig) throws CouldNotPerformException {

        if (agentConfig == null) {
            throw new NotAvailableException("agentConfig");
        }

        if (!agentConfig.hasLabel()) {
            throw new NotAvailableException("agentConfig.label");
        }

        if (agentConfig.getLabel().isEmpty()) {
            throw new NotAvailableException("Field agentConfig.label isEmpty");
        }

        if (locationConfig == null) {
            throw new NotAvailableException("location");
        }
        
        if (!locationConfig.hasScope() || locationConfig.getScope().getComponentList().isEmpty()) {
            throw new NotAvailableException("location scope");
        }

        // add location scope
        ScopeType.Scope.Builder scope = locationConfig.getScope().toBuilder();

        // add agent type
        scope.addComponent(convertIntoValidScopeComponent(agentConfig.getType().name().replace("_", "")));

        // add unit label
        scope.addComponent(convertIntoValidScopeComponent(agentConfig.getLabel()));

        return scope.build();
    }

    public static ScopeType.Scope generateAppScope(final AppConfig appConfig, final LocationConfig locationConfig) throws CouldNotPerformException {

        if (appConfig == null) {
            throw new NotAvailableException("appConfig");
        }

        if (!appConfig.hasLabel()) {
            throw new NotAvailableException("appConfig.label");
        }

        if (appConfig.getLabel().isEmpty()) {
            throw new NotAvailableException("Field appConfig.label isEmpty");
        }

        if (locationConfig == null) {
            throw new NotAvailableException("location");
        }
        
        if (!locationConfig.hasScope() || locationConfig.getScope().getComponentList().isEmpty()) {
            throw new NotAvailableException("location scope");
        }

        // add location scope
        ScopeType.Scope.Builder scope = locationConfig.getScope().toBuilder();

        // add unit app
        scope.addComponent(convertIntoValidScopeComponent("app"));

        // add unit label
        scope.addComponent(convertIntoValidScopeComponent(appConfig.getLabel()));

        return scope.build();
    }

    public static ScopeType.Scope generateSceneScope(final SceneConfig sceneConfig, final LocationConfig locationConfig) throws CouldNotPerformException {

        if (sceneConfig == null) {
            throw new NotAvailableException("sceneConfig");
        }

        if (!sceneConfig.hasLabel()) {
            throw new NotAvailableException("sceneConfig.label");
        }

        if (sceneConfig.getLabel().isEmpty()) {
            throw new NotAvailableException("Field sceneConfig.label isEmpty");
        }

        if (locationConfig == null) {
            throw new NotAvailableException("location");
        }
        
        if (!locationConfig.hasScope() || locationConfig.getScope().getComponentList().isEmpty()) {
            throw new NotAvailableException("location scope");
        }

        // add location scope
        ScopeType.Scope.Builder scope = locationConfig.getScope().toBuilder();

        // add unit app
        scope.addComponent(convertIntoValidScopeComponent("scene"));

        // add unit label
        scope.addComponent(convertIntoValidScopeComponent(sceneConfig.getLabel()));

        return scope.build();
    }

    public static ScopeType.Scope generateSceneScope(final UserConfig userConfig) throws CouldNotPerformException {

        if (userConfig == null) {
            throw new NotAvailableException("userConfig");
        }

        if (!userConfig.hasUserName()) {
            throw new NotAvailableException("userConfig.userName");
        }

        if (userConfig.getUserName().isEmpty()) {
            throw new NotAvailableException("Field userConfig.userName isEmpty");
        }

        // add manager
        ScopeType.Scope.Builder scope = ScopeType.Scope.newBuilder().addComponent(convertIntoValidScopeComponent("manager"));
        // add user
        scope.addComponent(convertIntoValidScopeComponent("user"));
        // add user name
        scope.addComponent(convertIntoValidScopeComponent(userConfig.getUserName()));

        return scope.build();
    }

    public static ScopeType.Scope generateSceneScope(final UserGroupConfig userGroupConfig) throws CouldNotPerformException {

        if (userGroupConfig == null) {
            throw new NotAvailableException("userGroupConfig");
        }

        if (!userGroupConfig.hasLabel()) {
            throw new NotAvailableException("userGroupConfig.label");
        }

        if (userGroupConfig.getLabel().isEmpty()) {
            throw new NotAvailableException("Field userGroupConfig.label isEmpty");
        }

        // add manager
        ScopeType.Scope.Builder scope = ScopeType.Scope.newBuilder().addComponent(convertIntoValidScopeComponent("manager"));
        // add user
        scope.addComponent(convertIntoValidScopeComponent("user"));
        // add group
        scope.addComponent(convertIntoValidScopeComponent("group"));
        // add user name
        scope.addComponent(convertIntoValidScopeComponent(userGroupConfig.getLabel()));

        return scope.build();
    }

    public static String convertIntoValidScopeComponent(String scopeComponent) {
        scopeComponent = scopeComponent.toLowerCase();
        scopeComponent = scopeComponent.replaceAll("ä", "ae");
        scopeComponent = scopeComponent.replaceAll("ö", "oe");
        scopeComponent = scopeComponent.replaceAll("ü", "ue");
        scopeComponent = scopeComponent.replaceAll("ß", "ss");
        scopeComponent = scopeComponent.replaceAll("[^0-9a-z-_]+", "_");
        return scopeComponent;
    }

    public static String generateStringRepWithDelimiter(final ScopeType.Scope scope, final String delimiter) throws CouldNotPerformException {

        if (scope == null) {
            throw new NotAvailableException("scope");
        }

        String stringRep = "";

        boolean firstEntry = true;
        for (String component : scope.getComponentList()) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                stringRep += delimiter;
            }
            stringRep += component;
        }
        return stringRep;
    }
}
