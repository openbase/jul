package org.openbase.jul.pattern.provider;

import java.util.concurrent.TimeUnit;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.pattern.Observer;

/**
 * #%L
 * #L%
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <Data>
 */
public interface DataProvider<Data> {

    /**
     * Check if the data object is already available.
     *
     * @return if data is available
     */
    public boolean isDataAvailable();

    /**
     * Method returns the class of the data object.
     *
     * @return the class of the data object
     */
    public Class<Data> getDataClass();
    
    /**
     * Method returns the data object of this instance.
     *
     * In case the data is not available a NotAvailableException is thrown.
     *
     * @return the data object.
     * @throws NotAvailableException is thrown in case the data is not available.
     */
    public Data getData() throws NotAvailableException;

    /**
     * This method allows the registration of data observers to get informed about data updates.
     *
     * @param observer the observer added
     */
    public void addDataObserver(final Observer<Data> observer);

    /**
     * This method removes already registered data observers.
     *
     * @param observer the observer removed
     */
    public void removeDataObserver(final Observer<Data> observer);
    
    /**
     * Method blocks until an initial data is available.
     *
     * @throws CouldNotPerformException is thrown if any error occurs.
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
    public void waitForData() throws CouldNotPerformException, InterruptedException;

    /**
     * Method blocks until an initial data is available or the given timeout is reached.
     *
     * @param timeout maximal time to wait for the data. After the timeout is reached a NotAvailableException is thrown which is caused by a TimeoutException.
     * @param timeUnit the time unit of the timeout.
     * @throws NotAvailableException is thrown in case the any error occurs, or if the given timeout is reached. In this case a TimeoutException is thrown.
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
    public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException;
}
