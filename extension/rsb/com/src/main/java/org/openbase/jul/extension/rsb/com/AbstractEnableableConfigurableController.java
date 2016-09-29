package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
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
import com.google.protobuf.GeneratedMessage;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.iface.Enableable;
import org.openbase.jul.schedule.SyncObject;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a> Threepwood</a>
 * @param <M>
 * @param <MB>
 * @param <CONFIG>
 */
public abstract class AbstractEnableableConfigurableController<M extends GeneratedMessage, MB extends M.Builder<MB>, CONFIG extends GeneratedMessage> extends AbstractConfigurableController<M, MB, CONFIG> implements Enableable {

    private boolean enabled;
    private final SyncObject enablingLock = new SyncObject(AbstractEnableableConfigurableController.class);

    public AbstractEnableableConfigurableController(final MB builder) throws InstantiationException {
        super(builder);
    }

    @Override
    public void enable() throws CouldNotPerformException, InterruptedException {
        synchronized (enablingLock) {
            enabled = true;
            activate();
        }
    }

    @Override
    public void disable() throws CouldNotPerformException, InterruptedException {
        synchronized (enablingLock) {
            enabled = false;
            deactivate();
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
