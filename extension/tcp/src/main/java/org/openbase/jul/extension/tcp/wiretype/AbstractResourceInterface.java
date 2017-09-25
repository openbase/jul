/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openbase.jul.extension.tcp.wiretype;

import org.openbase.jul.iface.Manageable;
import org.openbase.jul.iface.provider.NameProvider;

/**
 *
 * @author divine
 */
public interface AbstractResourceInterface extends Manageable, NameProvider {
	public ResourceKey getResourceKey();
}
