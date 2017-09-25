/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.tcp.wiretype;

import de.dc.bco.lib.communication.network.execution.serverCommand.ChangeNetworkResourceCommand;
import de.dc.bco.lib.permission.PermissionProvider;
import de.dc.util.exceptions.CouldNotPerformException;
import de.dc.util.logging.Logger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 *
 * @author divine
 */
public abstract class AbstractNetworkResource<C extends AbstractNetworkResourceConfig, D extends AbstractNetworkResourceData<D>, P extends AbstractSecureResource> extends AbstractSecureResource<C, D, P> implements AbstractNetworkResourceInterface {

	public AbstractNetworkResource(C config, P parentResource, PermissionProvider permissionProvider) throws ResourceCreationException {
		super(config, parentResource, permissionProvider);
	}

	public void executeNetworkResourceCommand(ChangeNetworkResourceCommand command) { //TODO add Exception handling! and send error over network to give client feedback
		ObjectInputStream objectInputStream = null;
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(command.getArguments());
			objectInputStream = new ObjectInputStream(byteArrayInputStream);
			executeMethod(command.getMethodName(), objectInputStream);
		} catch (Exception ex) {
			Logger.error(this, "Could not execute method " + command.getMethodName(), ex);
		} finally {
			try {
				if (objectInputStream != null) {
					objectInputStream.close();
				}
			} catch (IOException ex) {
				Logger.warn(this, "Could not close object stream!", ex);
			}
		}
	}

	@Override
	public void executeMethod(String methodName, ObjectInputStream objectInputStream) throws CouldNotPerformException {
		throw new CouldNotPerformException("Could not find method "+methodName);
	}
}
