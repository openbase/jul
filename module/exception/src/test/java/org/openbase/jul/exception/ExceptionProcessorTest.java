package org.openbase.jul.exception;

/*-
 * #%L
 * JUL Exception
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 *
 * @author vdasilva
 */
public class ExceptionProcessorTest {
    
    public ExceptionProcessorTest() {
    }

    @Test
    @Timeout(5)
    public void getFromNested() throws Exception {
        Throwable cause = new RuntimeException("The Message");
        Exception exception = new Exception(cause);

        Assertions.assertEquals(cause, ExceptionProcessor.getInitialCause(exception));
        Assertions.assertEquals("The Message", ExceptionProcessor.getInitialCauseMessage(exception));
    }

    @Test
    @Timeout(5)
    public void get() throws Exception {
        Exception exception = new Exception("The Message");

        Assertions.assertEquals(exception, ExceptionProcessor.getInitialCause(exception));
        Assertions.assertEquals("The Message", ExceptionProcessor.getInitialCauseMessage(exception));
    }
}
