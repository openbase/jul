package org.dc.jul.exception.printer;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <E>
 */
public interface ElementGenerator<E> {

    /**
     *
     * @param element
     * @return
     */
    public String generateRoot(final E element);

    /**
     *
     * @param element
     * @param printer
     * @param rootPrefix
     * @param childPrefix
     */
    public void printRootElement(final E element, final Printer printer, final String rootPrefix, final String childPrefix);

    /**
     *
     * @param element
     * @param printer
     * @param rootPrefix
     * @param childPrefix
     */
    public void printElement(final E element, final Printer printer, final String rootPrefix, final String childPrefix);

}
