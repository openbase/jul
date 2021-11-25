package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ProtobufListDiffTest {

    private static List<UnitConfig> currentContext, modContext;

    public ProtobufListDiffTest() {
    }

    @BeforeAll
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
        currentContext = new ArrayList<>();
        currentContext.add(UnitConfig.newBuilder().setId("1").build());
        currentContext.add(UnitConfig.newBuilder().setId("2").build());
        currentContext.add(UnitConfig.newBuilder().setId("3").build());
        currentContext.add(UnitConfig.newBuilder().setId("4").build());
        currentContext.add(UnitConfig.newBuilder().setId("5").build());
    }

    @AfterAll
    public static void tearDownClass() {
        currentContext.clear();
    }

    

    /**
     * Test of getNewMessages method, of class ProtobufListDiff.
     */
    @Test
    public void testGetNewMessages() {
        System.out.println("getNewMessages");

        UnitConfig newUnitConfig = UnitConfig.newBuilder().setId("new").build();

        ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> diff = new ProtobufListDiff<>(currentContext);
        modContext = new ArrayList<>(currentContext);

        modContext.add(newUnitConfig);

        diff.diffMessages(modContext);
        Assertions.assertTrue(diff.getUpdatedMessageMap().isEmpty());
        Assertions.assertTrue(diff.getRemovedMessageMap().isEmpty());
        Assertions.assertEquals(1, diff.getNewMessageMap().size());
        Assertions.assertTrue(diff.getNewMessageMap().getMessages().contains(newUnitConfig));
    }

    /**
     * Test of getUpdatedMessages method, of class ProtobufListDiff.
     */
    @Test
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
        Assertions.assertTrue(diff.getNewMessageMap().isEmpty());
        Assertions.assertTrue(diff.getRemovedMessageMap().isEmpty());
        Assertions.assertEquals(1, diff.getUpdatedMessageMap().size());
        Assertions.assertTrue(diff.getUpdatedMessageMap().getMessages().contains(updatedUnitConfig));
    }

    /**
     * Test of getRemovedMessages method, of class ProtobufListDiff.
     */
    @Test
    public void testGetRemovedMessages() {
        System.out.println("getRemovedMessages");
        UnitConfig removedUnitConfig = UnitConfig.newBuilder().setId("1").build();

        ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> diff = new ProtobufListDiff<>(currentContext);
        modContext = new ArrayList<>(currentContext);

        modContext.remove(removedUnitConfig);

        diff.diffMessages(modContext);
        Assertions.assertTrue(diff.getUpdatedMessageMap().isEmpty());
        Assertions.assertTrue(diff.getNewMessageMap().isEmpty());
        Assertions.assertEquals(1, diff.getRemovedMessageMap().size());
        Assertions.assertTrue(diff.getRemovedMessageMap().getMessages().contains(removedUnitConfig));
    }

}
