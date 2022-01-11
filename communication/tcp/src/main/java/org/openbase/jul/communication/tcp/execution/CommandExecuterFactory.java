package org.openbase.jul.communication.tcp.execution;

/*-
 * #%L
 * JUL Extension TCP
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
import org.openbase.jul.communication.tcp.TCPConnection;
import org.openbase.jul.communication.tcp.execution.command.AbstractCommand;
import org.openbase.jul.communication.tcp.execution.command.AbstractCommandExecuter;
import org.openbase.jul.communication.tcp.exception.NetworkTaskFailedException;

import java.lang.reflect.Constructor;
import org.openbase.jul.exception.CouldNotPerformException;

public class CommandExecuterFactory {

    public static AbstractCommandExecuter newInstance(final AbstractCommand command, final TCPConnection connection) throws CouldNotPerformException {
        assert command != null;
        assert connection != null;

        Class<? extends AbstractCommandExecuter> executerClass;
        AbstractCommandExecuter executer;
        String classURI = "?";

        try {
            classURI = command.getClass().getPackage().getName() + "." + (command.getClass().getSimpleName().replace("Command", "Executer"));
            executerClass = (Class<? extends AbstractCommandExecuter>) Class.forName(classURI);
            Constructor<? extends AbstractCommandExecuter> constructor = executerClass.getConstructor(command.getClass(), connection.getClass());
            executer = constructor.newInstance(command, connection);
            if (executer == null) {
                throw new NullPointerException("Executer is null!");
            }
            return executer;
        } catch (Exception ex) {
            throw new NetworkTaskFailedException("Could not create suitable executer[" + classURI + "]!", command, ex);
        }
    }
}
