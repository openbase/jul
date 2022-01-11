package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ProtobufListDiffTest {

    private static List<UnitConfig> currentContext, modContext;

    public ProtobufListDiffTest() {
    }

    @BeforeClass
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
        currentContext = new ArrayList<>();
        currentContext.add(UnitConfig.newBuilder().setId("1").build());
        currentContext.add(UnitConfig.newBuilder().setId("2").build());
        currentContext.add(UnitConfig.newBuilder().setId("3").build());
        currentContext.add(UnitConfig.newBuilder().setId("4").build());
        currentContext.add(UnitConfig.newBuilder().setId("5").build());
    }

    @AfterClass
    public static void tearDownClass() {
        currentContext.clear();
    }

    

    /**
     * Test of getNewMessages method, of class ProtobufListDiff.
     */
    @Test(timeout = 5000)
    public void testGetNewMessages() {
        System.out.println("getNewMessages");

        UnitConfig newUnitConfig = UnitConfig.newBuilder().setId("new").build();

        ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> diff = new ProtobufListDiff<>(currentContext);
        modContext = new ArrayList<>(currentContext);

        modContext.add(newUnitConfig);

        diff.diffMessages(modContext);
        Assert.assertTrue(diff.getUpdatedMessageMap().isEmpty());
        Assert.assertTrue(diff.getRemovedMessageMap().isEmpty());
        Assert.assertEquals(1, diff.getNewMessageMap().size());
        Assert.assertTrue(diff.getNewMessageMap().getMessages().contains(newUnitConfig));
    }

    /**
     * Test of getUpdatedMessages method, of class ProtobufListDiff.
     */
    @Test(timeout = 5000)
    public void testGetUpdatedMessages() {
        System.out.println("getUpdatedMessages");
        UnitConfig updatedUnitConfig = UnitConfig.newBuilder().setId("2").addAlias("coolUnit").build();

        ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> diff = new ProtobufListDiff<>(currentContext);
        modContext = new ArrayList<>(currentContext);

        for (UnitConfig context : currentContext) {
            if (context.getId().equals("2")) {
                modContext.remove(context);
                break;
            }
        }
        modContext.add(updatedUnitConfig);
        diff.diffMessages(modContext);
        Assert.assertTrue(diff.getNewMessageMap().isEmpty());
        Assert.assertTrue(diff.getRemovedMessageMap().isEmpty());
        Assert.assertEquals(1, diff.getUpdatedMessageMap().size());
        Assert.assertTrue(diff.getUpdatedMessageMap().getMessages().contains(updatedUnitConfig));
    }

    /**
     * Test of getRemovedMessages method, of class ProtobufListDiff.
     */
    @Test(timeout = 5000)
    public void testGetRemovedMessages() {
        System.out.println("getRemovedMessages");
        UnitConfig removedUnitConfig = UnitConfig.newBuilder().setId("1").build();

        ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> diff = new ProtobufListDiff<>(currentContext);
        modContext = new ArrayList<>(currentContext);

        modContext.remove(removedUnitConfig);

        diff.diffMessages(modContext);
        Assert.assertTrue(diff.getUpdatedMessageMap().isEmpty());
        Assert.assertTrue(diff.getNewMessageMap().isEmpty());
        Assert.assertEquals(1, diff.getRemovedMessageMap().size());
        Assert.assertTrue(diff.getRemovedMessageMap().getMessages().contains(removedUnitConfig));
    }

}
