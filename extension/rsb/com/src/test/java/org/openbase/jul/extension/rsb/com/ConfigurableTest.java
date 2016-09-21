/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.rsb.com;

import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.pattern.Remote;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.homeautomation.control.scene.SceneConfigType.SceneConfig;
import rst.homeautomation.control.scene.SceneDataType.SceneData;
import rst.rsb.ScopeType.Scope;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class ConfigurableTest {

    public ConfigurableTest() {
    }

    @BeforeClass
    public static void setUpClass() {
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

    @Test(timeout = 10000)
    public void initTest() throws Exception {
        System.out.println("initTest");

        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(SceneData.getDefaultInstance()));

        Scope scope = Scope.newBuilder().addComponent("test").addComponent("configurable").addComponent("controller").addComponent("and").addComponent("remote").build();
        SceneConfig sceneConfig = SceneConfig.newBuilder().setId(UUID.randomUUID().toString()).setLabel("TestScene").setScope(scope).build();

        AbstractConfigurableController controller = new AbstractConfigurableControllerImpl();
        controller.init(sceneConfig);
        controller.activate();

        AbstractConfigurableRemote remote = new AbstractConfigurableRemoteImpl(SceneData.class, SceneConfig.class);
        remote.init(sceneConfig);
        remote.activate();

        remote.waitForConnectionState(Remote.ConnectionState.CONNECTED);
        System.out.println("Succesfully connected controller and remote!");

        scope = scope.toBuilder().clearComponent().addComponent("test").addComponent("configurables").build();
        sceneConfig = sceneConfig.toBuilder().setScope(scope).build();

        controller.init(sceneConfig);

        remote.waitForConnectionState(Remote.ConnectionState.DISCONNECTED);
        System.out.println("Remote switched to disconnected after config change in the controller!");

        remote.init(sceneConfig);

        remote.waitForConnectionState(Remote.ConnectionState.CONNECTED);
        System.out.println("Remote reconnected after reinitialization!");
    }

    public class AbstractConfigurableControllerImpl extends AbstractConfigurableController<SceneData, SceneData.Builder, SceneConfig> {

        public AbstractConfigurableControllerImpl() throws Exception {
            super(SceneData.newBuilder());
        }

        @Override
        public void registerMethods(RSBLocalServer server) throws CouldNotPerformException {
        }
    }

    public class AbstractConfigurableRemoteImpl extends AbstractConfigurableRemote<SceneData, SceneConfig> {

        public AbstractConfigurableRemoteImpl(Class<SceneData> dataClass, Class<SceneConfig> configClass) {
            super(dataClass, configClass);
        }
    }

}
