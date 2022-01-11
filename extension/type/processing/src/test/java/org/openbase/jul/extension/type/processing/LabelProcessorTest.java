package org.openbase.jul.extension.type.processing;

/*-
 * #%L
 * JUL Extension Type Processing
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

import org.junit.Test;

import static org.junit.Assert.*;

public class LabelProcessorTest {

    @Test
    public void format() {
        System.out.println("format");
        assertEquals("Lamp 1", LabelProcessor.format(" Lamp1"));
        assertEquals("LAMP9", LabelProcessor.format(" LAMP9"));
        assertEquals("My Lamp 1", LabelProcessor.format("MyLamp1"));
        assertEquals("XXX 1", LabelProcessor.format("XXX 1"));
        assertEquals("lamp 1", LabelProcessor.format("lamp1"));
        assertEquals("lamp", LabelProcessor.format("lamp"));
        assertEquals("Living Ceiling Lamp", LabelProcessor.format("LivingCeilingLamp "));
        assertEquals("Living Ceiling Lamp", LabelProcessor.format("LivingCeilingLamp"));
        assertEquals("Living Cei Ling Lamp", LabelProcessor.format("Living   CeiLing      Lamp"));
    }
}
