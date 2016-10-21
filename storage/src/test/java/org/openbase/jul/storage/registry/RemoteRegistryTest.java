package org.openbase.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openbase.jul.exception.InstantiationException;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RemoteRegistryTest {
    
    private static RemoteRegistry remoteRegistry;
    
    public RemoteRegistryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws InstantiationException {
         remoteRegistry = new RemoteRegistry();
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

    /**
     * Test of notifyRegistryUpdate method, of class RemoteRegistry.
     * @throws java.lang.Exception
     */
    @Test
    public void testNotifyRegistryUpdate() throws Exception {
        System.out.println("notifyRegistryUpdate");
        remoteRegistry.notifyRegistryUpdate(new ArrayList());
    }
}
