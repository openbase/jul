/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.scope;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.extension.protobuf.container.ProtoBufMessageMapInterface;
import java.util.Collection;
import rsb.Scope;
import rst.homeautomation.device.DeviceConfigType.DeviceConfig;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;
import rst.rsb.ScopeType;
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

    public static ScopeType.Scope generateLocationScope(final LocationConfig locationConfig, final ProtoBufMessageMapInterface<String, LocationConfig, LocationConfig.Builder> registry) throws CouldNotPerformException {

        if (locationConfig == null) {
            throw new NotAvailableException("locationConfig");
        }

        if (registry == null) {
            throw new NotAvailableException("registry");
        }

        if (!locationConfig.hasId()) {
            throw new NotAvailableException("location id");
        }

        ScopeType.Scope.Builder scope = ScopeType.Scope.newBuilder();
        if (!locationConfig.getRoot() && locationConfig.hasParentId()) {
            scope.addAllComponent(registry.get(locationConfig.getParentId()).getMessage().getScope().getComponentList());
        }
        scope.addComponent(convertIntoValidScopeComponent(locationConfig.getLabel()));

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

        // add location scope
        ScopeType.Scope.Builder scope = locationConfig.getScope().toBuilder();

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

        // add location scope
        ScopeType.Scope.Builder scope = locationConfig.getScope().toBuilder();

        // add unit type
        scope.addComponent(convertIntoValidScopeComponent(unitConfig.getType().name().replace("_", "")));

        // add unit label
        scope.addComponent(convertIntoValidScopeComponent(unitConfig.getLabel()));

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
