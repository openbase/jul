/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openbase.jul.extension.tcp.wiretype;

import de.dc.util.exceptions.CouldNotPerformException;
import java.io.ObjectInputStream;

/**
 *
 * @author divine
 */
public interface AbstractNetworkResourceInterface extends AbstractSecureResourceInterface {
	// TODO implement with reflextion framework
	public void executeMethod(String methodName, ObjectInputStream objectInputStream) throws CouldNotPerformException;
}
