package org.openbase.jul.storage.registry;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class AbstractRegistryTest {
    
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractRegistryTest.class);
    
    public AbstractRegistryTest() {
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
    
    @Test
    public void testDependentRegistryLocking() throws Exception {
        AbstractRegistry mainRegistry = new AbstractRegistryImpl();
        AbstractRegistry dependentRegistry = new AbstractRegistryImpl();
        AbstractRegistry dependentFromAllRegistry = new AbstractRegistryImpl();
        
        mainRegistry.setName("MainRegistry");
        dependentRegistry.setName("DependentRegistry");
        dependentFromAllRegistry.setName("DependentFromAllRegistry");
        
        dependentRegistry.registerDependency(mainRegistry);
        
        dependentFromAllRegistry.registerDependency(mainRegistry);
        dependentFromAllRegistry.registerDependency(dependentRegistry);
        
        System.out.println("Locking Dependent...");
        dependentRegistry.lock();
        System.out.println("Dependent locked");
        assertTrue("MainRegistry should be locked", mainRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentRegistry should be locked", dependentRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentFromAllRegistry should be unlocked", !dependentFromAllRegistry.isWriteLockedByCurrentThread());
        
        Thread lockThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    System.out.println("Locking DependentFromAll...");
                    dependentFromAllRegistry.lock();
                    System.out.println("DependentFromAll locked");
                    assertTrue("MainRegistry should be locked", mainRegistry.isWriteLockedByCurrentThread());
                    assertTrue("DependentRegistry should be locked", dependentRegistry.isWriteLockedByCurrentThread());
                    assertTrue("DependentFromAllRegistry should be locked", dependentFromAllRegistry.isWriteLockedByCurrentThread());
                    
                    System.out.println("Unlocking DependentFromAll...");
                    dependentFromAllRegistry.unlock();
                    System.out.println("DependentFromAll unlocked");
                    assertTrue("MainRegistry should be unlocked", !mainRegistry.isWriteLockedByCurrentThread());
                    assertTrue("DependentRegistry should be unlocked", !dependentRegistry.isWriteLockedByCurrentThread());
                    assertTrue("DependentFromAllRegistry should be unlocked", !dependentFromAllRegistry.isWriteLockedByCurrentThread());
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistoryAndExit("Exception while locking", ex, logger);
                }
            }
        });
        lockThread.start();
        
        System.out.println("Unlocking Dependent...");
        dependentRegistry.unlock();
        System.out.println("Dependent unlocked");
        assertTrue("MainRegistry should be unlocked", !mainRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentRegistry should be unlocked", !dependentRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentFromAllRegistry should be unlocked", !dependentFromAllRegistry.isWriteLockedByCurrentThread());
        
        System.out.println("Locking main...");
        mainRegistry.lock();
        System.out.println("Main locked");
        assertTrue("MainRegistry should be locked", mainRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentRegistry should be unlocked", !dependentRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentFromAllRegistry should be unlocked", !dependentFromAllRegistry.isWriteLockedByCurrentThread());
        
        System.out.println("Unlocking Main...");
        mainRegistry.unlock();
        System.out.println("Main unlocked");
        assertTrue("MainRegistry should be unlocked", !mainRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentRegistry should be unlocked", !dependentRegistry.isWriteLockedByCurrentThread());
        assertTrue("DependentFromAllRegistry should be unlocked", !dependentFromAllRegistry.isWriteLockedByCurrentThread());
        
    }
    
    public class AbstractRegistryImpl extends AbstractRegistry {
        
        public AbstractRegistryImpl() throws InstantiationException {
            this(new HashMap());
        }
        
        public AbstractRegistryImpl(Map entryMap) throws InstantiationException {
            super(entryMap);
        }
    }
    
}
