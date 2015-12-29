/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.file;

import org.dc.jul.exception.CouldNotPerformException;
import java.io.FileFilter;

/**
 *
 * @author mpohling
 * @param <C> context
 */
public interface FileProvider<C> {
    
    public String getFileName(final C context) throws CouldNotPerformException ;
    
    public String getFileType();
    
    public FileFilter getFileFilter();
}
