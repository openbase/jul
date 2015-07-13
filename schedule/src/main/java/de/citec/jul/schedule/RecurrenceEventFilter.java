/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.schedule;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * 
 * RecurrenceEventFilter helps to filter high frequency events. 
 * After a new incoming event is processed, all further incoming events are skipped except of the last event which is executed after the defined timeout is reached. 
 */
public abstract class RecurrenceEventFilter {

    
    
    public abstract void relay();
}
