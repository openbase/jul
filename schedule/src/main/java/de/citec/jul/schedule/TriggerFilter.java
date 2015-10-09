/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.schedule;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public abstract class TriggerFilter extends LastValueHandler<Void>{

    public TriggerFilter(String name) {
        super(name);
    }

    public TriggerFilter(String name, long delayUntilNext) {
        super(name, delayUntilNext);
    }
    
    @Override
    public void handle(Void value) {
        internalTrigger();
    }
    
    public void trigger() {
        setValue(null);
        forceValueChange();
    }
    
    public abstract void internalTrigger();
    
}
