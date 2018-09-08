package org.openbase.jul.exception.printer;

/*
 * #%L
 * JUL Exception
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
/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <E>
 */
public interface ElementGenerator<E> {

    String LEAF_ENTRY_SPACER_SINGLE = "⚀ ";
    String LEAF_ENTRY_SPACER_MULTI = "⚄ ";
    String TREE_ELEMENT_SPACER = "   ";

    /**
     *
     * @param element
     * @return
     */
    String generateRoot(final E element);

    /**
     *
     * @param element
     * @param printer
     * @param rootPrefix
     * @param childPrefix
     */
    void printRootElement(final E element, final Printer printer, final String rootPrefix, final String childPrefix);

    /**
     *
     * @param element
     * @param printer
     * @param rootPrefix
     * @param childPrefix
     */
    void printElement(final E element, final Printer printer, final String rootPrefix, final String childPrefix);

}
