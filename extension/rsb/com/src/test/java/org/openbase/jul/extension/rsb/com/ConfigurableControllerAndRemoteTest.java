package org.openbase.jul.extension.rsb.com;

/*-
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.scene.SceneDataType.SceneData;
import org.openbase.type.communication.ScopeType.Scope;

import static org.openbase.type.domotic.state.ConnectionStateType.ConnectionState.State.*;
import static org.openbase.type.domotic.state.AvailabilityStateType.AvailabilityState.State.*;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class ConfigurableControllerAndRemoteTest {

    public ConfigurableControllerAndRemoteTest() {
    }

    @BeforeClass
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 30000)
    public void initTest() throws Exception {
        System.out.println("initTest");

        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(SceneData.getDefaultInstance()));

        Scope scope = Scope.newBuilder().addComponent("test").addComponent("configurable").addComponent("controller").addComponent("and").addComponent("remote").build();
        UnitConfig unitConfig = UnitConfig.newBuilder().setId(UUID.randomUUID().toString()).setScope(scope).build();

        AbstractConfigurableController controller = new AbstractConfigurableControllerImpl();
        controller.init(unitConfig);
        controller.activate();

        AbstractConfigurableRemote remote = new AbstractConfigurableRemoteImpl(SceneData.class, UnitConfig.class);
        remote.init(unitConfig);
        remote.activate();

        remote.waitForConnectionState(CONNECTED);
        controller.waitForAvailabilityState(ONLINE);
        System.out.println("Successfully connected controller and remote!");

        scope = scope.toBuilder().clearComponent().addComponent("test").addComponent("configurables").build();
        unitConfig = unitConfig.toBuilder().setScope(scope).build();
        controller.init(unitConfig);
        controller.waitForAvailabilityState(ONLINE);
        System.out.println("Controller is online again!");
        remote.waitForConnectionState(CONNECTING);
        System.out.println("Remote switched to connecting after config change in the controller!");
        remote.init(unitConfig);
        remote.waitForConnectionState(CONNECTED);
        System.out.println("Remote reconnected after reinitialization!");

        controller.shutdown();
        remote.shutdown();
    }

    @Test(timeout = 30000)
    public void applyConfigUpdateTest() throws Exception {
        System.out.println("applyConfigUpdateTest");

        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(SceneData.getDefaultInstance()));

        Scope scope = Scope.newBuilder().addComponent("test2").addComponent("configurable2").addComponent("controller2").addComponent("and2").addComponent("remote2").build();
        UnitConfig unitConfig = UnitConfig.newBuilder().setId(UUID.randomUUID().toString()).setScope(scope).build();

        AbstractConfigurableController controller = new AbstractConfigurableControllerImpl();
        controller.init(unitConfig);
        controller.activate();

        AbstractConfigurableRemote remote = new AbstractConfigurableRemoteImpl(SceneData.class, UnitConfig.class);
        remote.init(unitConfig);
        remote.activate();

        remote.waitForConnectionState(CONNECTED);
        controller.waitForAvailabilityState(ONLINE);
        System.out.println("Succesfully connected controller and remote!");

        scope = scope.toBuilder().clearComponent().addComponent("test2").addComponent("configurables2").build();
        unitConfig = unitConfig.toBuilder().setScope(scope).build();

        controller.applyConfigUpdate(unitConfig);
        controller.waitForAvailabilityState(ONLINE);
        System.out.println("Controller is online again!");
        remote.waitForConnectionState(CONNECTING);
        System.out.println("Remote switched to connecting after config change in the controller!");
        remote.applyConfigUpdate(unitConfig);
        remote.waitForConnectionState(CONNECTED);
        System.out.println("Remote reconnected after reinitialization!");

        remote.shutdown();
        controller.shutdown();
    }

    public class AbstractConfigurableControllerImpl extends AbstractConfigurableController<SceneData, SceneData.Builder, UnitConfig> {

        public AbstractConfigurableControllerImpl() throws Exception {
            super(SceneData.newBuilder());
        }

        @Override
        public void registerMethods(RSBLocalServer server) throws CouldNotPerformException {
        }
    }

    public class AbstractConfigurableRemoteImpl extends AbstractConfigurableRemote<SceneData, UnitConfig> {

        public AbstractConfigurableRemoteImpl(Class<SceneData> dataClass, Class<UnitConfig> configClass) {
            super(dataClass, configClass);
        }
    }

}
