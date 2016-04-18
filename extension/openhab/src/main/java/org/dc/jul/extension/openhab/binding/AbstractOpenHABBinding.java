/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.openhab.binding;

/*
 * #%L
 * JUL Extension OpenHAB
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.extension.openhab.binding.interfaces.OpenHABBinding;
import org.dc.jul.extension.openhab.binding.interfaces.OpenHABRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public abstract class AbstractOpenHABBinding implements OpenHABBinding {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractOpenHABBinding.class);

    protected static OpenHABBinding instance;
    protected final OpenHABRemote openHABRemote;

    public AbstractOpenHABBinding(OpenHABRemote openHABRemote) throws org.dc.jul.exception.InstantiationException {
        instance = this;
        this.openHABRemote = openHABRemote;
    }

    public static OpenHABBinding getInstance() throws NotAvailableException {
        if (instance == null) {
            throw new NotAvailableException(OpenHABBinding.class);
        }
        return instance;
    }

    public void init() throws InitializationException, InterruptedException {
        try {
            this.openHABRemote.init();
            this.openHABRemote.activate();
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public void shutdown() throws InterruptedException {
        if (openHABRemote != null) {
            openHABRemote.shutdown();
        }
        instance = null;
    }

    @Override
    public OpenHABRemote getOpenHABRemote() throws NotAvailableException {
        return openHABRemote;
    }
}
