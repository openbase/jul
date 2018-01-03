package org.openbase.jul.extension.rct;

/*-
 * #%L
 * JUL Extension RCT
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rct.TransformReceiver;
import rct.TransformerFactory;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class GlobalTransformReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTransformReceiver.class);

    private static TransformReceiver instance;

    public static synchronized TransformReceiver getInstance() {
        if (instance == null) {
            try {
                instance = TransformerFactory.getInstance().createTransformReceiver();
            } catch (TransformerFactory.TransformerFactoryException ex) {
                ExceptionPrinter.printHistory("Could not establish rct receiver connection.", ex, LOGGER);
            }
        }
        return instance;
    }
}
