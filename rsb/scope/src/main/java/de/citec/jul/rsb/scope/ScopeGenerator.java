/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb.scope;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.rsb.container.ProtoBufMessageMapInterface;
import java.util.Collection;
import rsb.Scope;
import rst.homeautomation.device.DeviceConfigType.DeviceConfig;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;
import rst.rsb.ScopeType;
import rst.spatial.LocationConfigType.LocationConfig;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
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

    public static ScopeType.Scope setupLocationScope(final LocationConfig locationConfig, final ProtoBufMessageMapInterface<String, LocationConfig, LocationConfig.Builder> registry) throws CouldNotPerformException {

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
        scope.addComponent(locationConfig.getId().toLowerCase());

        return scope.build();
    }

    public static ScopeType.Scope setupDeviceScope(final DeviceConfig deviceConfig) throws CouldNotPerformException {

        if (deviceConfig == null) {
            throw new NotAvailableException("deviceConfig");
        }

        if (!deviceConfig.hasId()) {
            throw new NotAvailableException("device id");
        }

        if (!deviceConfig.hasPlacementConfig()) {
            throw new NotAvailableException("placement config");
        }

        if (!deviceConfig.getPlacementConfig().hasLocation()) {
            throw new NotAvailableException("location");
        }

        // add location scope
        ScopeType.Scope.Builder scope = deviceConfig.getPlacementConfig().getLocation().getScope().toBuilder();

        // add device scope
        scope.addComponent(deviceConfig.getId().toLowerCase());

        return scope.build();
    }

    public static ScopeType.Scope setupUnitScope(final UnitConfig unitConfig) throws CouldNotPerformException {

        if (unitConfig == null) {
            throw new NotAvailableException("unitConfig");
        }

        if (!unitConfig.hasLabel()) {
            throw new NotAvailableException("unitConfig.label");
        }

        if (!unitConfig.hasPlacement()) {
            throw new NotAvailableException("placement config");
        }

        if (!unitConfig.getPlacement().hasLocation()) {
            throw new NotAvailableException("location");
        }

        // add location scope
        ScopeType.Scope.Builder scope = unitConfig.getPlacement().getLocation().getScope().toBuilder();

        // add device scope
        scope.addComponent(unitConfig.getLabel().toLowerCase());

        return scope.build();
    }
}
