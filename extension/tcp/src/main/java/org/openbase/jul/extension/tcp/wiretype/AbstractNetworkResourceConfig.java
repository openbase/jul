/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openbase.jul.extension.tcp.wiretype;

import de.dc.bco.lib.configuration.InvalidConfigFileException;
import nu.xom.Element;


/**
 *
 * @author divine
 */
public abstract class AbstractNetworkResourceConfig<R extends AbstractNetworkResource> extends AbstractSecureResourceConfig<R> {

	/**
	 * Json Constructor 
	 */
	protected AbstractNetworkResourceConfig() {
	}

	public AbstractNetworkResourceConfig(Element element) throws InvalidConfigFileException {
		super(element);
	}

	protected AbstractNetworkResourceConfig(AbstractSecureResourceConfig parentResourceConfig) throws InvalidConfigFileException {
		super(parentResourceConfig);
	}
}
