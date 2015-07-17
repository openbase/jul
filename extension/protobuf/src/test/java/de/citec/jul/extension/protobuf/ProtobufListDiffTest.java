/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.protobuf;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import rst.homeautomation.device.DeviceConfigType.DeviceConfig;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class ProtobufListDiffTest {
    
    private static List<DeviceConfig> currentContext, modContext;
    
    public ProtobufListDiffTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        currentContext = new ArrayList<>();
        currentContext.add(DeviceConfig.newBuilder().setId("1").build());
        currentContext.add(DeviceConfig.newBuilder().setId("2").build());
        currentContext.add(DeviceConfig.newBuilder().setId("3").build());
        currentContext.add(DeviceConfig.newBuilder().setId("4").build());
        currentContext.add(DeviceConfig.newBuilder().setId("5").build());
    }
    
    @AfterClass
    public static void tearDownClass() {
        currentContext.clear();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getNewMessages method, of class ProtobufListDiff.
     */
    @Test
    public void testGetNewMessages() {
        System.out.println("getNewMessages");
        
        DeviceConfig newDeviceConfig = DeviceConfig.newBuilder().setId("new").build();
        
        ProtobufListDiff diff = new ProtobufListDiff(currentContext);
        modContext = new ArrayList<>(currentContext);
        
        modContext.add(newDeviceConfig);
        
        diff.diff(modContext);
        Assert.assertTrue(diff.getUpdatedMessageMap().isEmpty());
        Assert.assertTrue(diff.getRemovedMessageMap().isEmpty());
        Assert.assertEquals(1, diff.getNewMessageMap().size());
        Assert.assertTrue(diff.getNewMessageMap().getMessages().contains(newDeviceConfig));
    }

    /**
     * Test of getUpdatedMessages method, of class ProtobufListDiff.
     */
    @Test
    public void testGetUpdatedMessages() {
        System.out.println("getUpdatedMessages");
        DeviceConfig updatedDeviceConfig = DeviceConfig.newBuilder().setId("2").setDescription("updated").build();
        
        ProtobufListDiff diff = new ProtobufListDiff(currentContext);
        modContext = new ArrayList<>(currentContext);
        
        for (DeviceConfig context : currentContext) {
            if(context.getId().equals("2")) {
                modContext.remove(context);
                break;
            }
        }
        modContext.add(updatedDeviceConfig);
        diff.diff(modContext);
        Assert.assertTrue(diff.getNewMessageMap().isEmpty());
        Assert.assertTrue(diff.getRemovedMessageMap().isEmpty());
        Assert.assertEquals(1, diff.getUpdatedMessageMap().size());
        Assert.assertTrue(diff.getUpdatedMessageMap().getMessages().contains(updatedDeviceConfig));
    }

    /**
     * Test of getRemovedMessages method, of class ProtobufListDiff.
     */
    @Test
    public void testGetRemovedMessages() {
        System.out.println("getRemovedMessages");
        DeviceConfig removedDeviceConfig = DeviceConfig.newBuilder().setId("1").build();
        
        ProtobufListDiff diff = new ProtobufListDiff(currentContext);
        modContext = new ArrayList<>(currentContext);
        
        modContext.remove(removedDeviceConfig);
        
        diff.diff(modContext);
        Assert.assertTrue(diff.getUpdatedMessageMap().isEmpty());
        Assert.assertTrue(diff.getNewMessageMap().isEmpty());
        Assert.assertEquals(1, diff.getRemovedMessageMap().size());
        Assert.assertTrue(diff.getRemovedMessageMap().getMessages().contains(removedDeviceConfig));
    }
    
}
