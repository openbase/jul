package org.openbase.jul.extension.openhab.binding;

/*
 * #%L
 * JUL Extension OpenHAB
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.openhab.binding.interfaces.OpenHABBinding;
import org.openbase.jul.extension.openhab.binding.interfaces.OpenHABRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public abstract class AbstractOpenHABBinding implements OpenHABBinding {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractOpenHABBinding.class);

    protected static OpenHABBinding instance;
    protected OpenHABRemote openHABRemote;

    public AbstractOpenHABBinding() throws org.openbase.jul.exception.InstantiationException {
        instance = this;
    }

    public static OpenHABBinding getInstance() throws NotAvailableException {
        if (instance == null) {
            throw new NotAvailableException(OpenHABBinding.class);
        }
        return instance;
    }

    public void init(final String itemFilter, final OpenHABRemote openHABRemote) throws InitializationException, InterruptedException {
        try {
            this.openHABRemote = openHABRemote;
            this.openHABRemote.init(itemFilter);
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
