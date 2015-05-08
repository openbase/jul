/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.util;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <M>
 */
public interface IdGenerator<KEY, M extends GeneratedMessage> {
    public KEY generateId(M message) throws CouldNotPerformException;
}
