package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * $Id:$
 * $HeadURL:$
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

import org.openbase.jul.extension.protobuf.ProtobufListDiff;
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
