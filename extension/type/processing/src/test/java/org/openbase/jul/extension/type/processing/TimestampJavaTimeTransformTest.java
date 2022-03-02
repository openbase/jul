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

import org.junit.jupiter.api.Test;
import org.openbase.type.timing.TimestampType;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TimestampJavaTimeTransformTest {

    public TimestampJavaTimeTransformTest() {
    }

    /**
     * Test of transform method, of class TimestampJavaTimeTransform.
     */
    @Test
    public void testTransform_long() {
        System.out.println("transform");
        long milliseconds = 8888;
        TimestampType.Timestamp result = TimestampJavaTimeTransform.transform(milliseconds);
        assertEquals(milliseconds, result.getTime() / 1000);
        assertEquals(milliseconds, TimestampJavaTimeTransform.transform(result));
    }
}

