/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.processing;

import org.dc.jul.exception.CouldNotPerformException;

/**
 *
 * @author mpohling
 * @param <A>
 * @param <B>
 */
public interface Processor<A, B>{
    public A deserialize(final B file, final A message) throws CouldNotPerformException;
    public B serialize(final A message, final B file) throws CouldNotPerformException;
}
