/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage;

/**
 *
 * @author mpohling
 */
public interface FileNameProvider<C> {
    
    public String getFileName(final C context);
}
