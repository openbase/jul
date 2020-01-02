package org.openbase.jul.visual.javafx.transform;

/*-
 * #%L
 * JUL Visual JavaFX
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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

import javafx.scene.paint.Color;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JFXColorToHSBColorTransformerTest {
    
    public JFXColorToHSBColorTransformerTest() {
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

    /**
     * Test of transform method, of class JFXColorToHSBColorTransformer.
     */
    @Test
    public void testTransform() throws Exception {
        System.out.println("transform color");
        assertEquals(JFXColorToHSBColorTransformer.transform(JFXColorToHSBColorTransformer.transform(Color.VIOLET)), Color.VIOLET);
        assertEquals(JFXColorToHSBColorTransformer.transform(JFXColorToHSBColorTransformer.transform(Color.BISQUE)), Color.BISQUE);
        assertEquals(JFXColorToHSBColorTransformer.transform(JFXColorToHSBColorTransformer.transform(Color.CORAL)), Color.CORAL);
    }
}
