/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.pattern;

/**
 *
 * @author divine
 */
public interface Observer<T> {
    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @throws java.lang.Exception
     */
    public void update(final Observable<T> source, final T data ) throws Exception;
}
