/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.processing;

import de.citec.jul.exception.CouldNotPerformException;
import java.io.File;

/**
 *
 * @author mpohling
 */
public interface FileProcessor<A> extends Processor<A, File> {
    public A deserialize(File file) throws CouldNotPerformException;
}
