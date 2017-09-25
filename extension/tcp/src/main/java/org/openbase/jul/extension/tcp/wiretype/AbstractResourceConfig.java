/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.tcp.wiretype;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.dc.bco.lib.permission.AccessControllerConfig;
import de.dc.bco.lib.configuration.ConfigParser;
import de.dc.bco.lib.configuration.InvalidConfigFileException;
import de.dc.util.exceptions.DuplicatedInstanceException;
import de.dc.util.exceptions.NotAvailableException;
import de.dc.util.interfaces.Manageable;
import de.dc.util.interfaces.Nameable;
import de.dc.util.logging.Logger;
import java.io.Serializable;
import nu.xom.Document;
import nu.xom.Element;

/**
 *
 * @author divine
 */
public abstract class AbstractResourceConfig<R extends AbstractResource> implements Serializable, Manageable, Nameable {

	public final static String ATTRIBUTE_ID = "id";
	public final static String ATTRIBUTE_NAME = "name";
	@JsonProperty(ATTRIBUTE_ID)
	@JacksonXmlProperty(isAttribute = true)
	private final int id;
	@JsonProperty(ATTRIBUTE_NAME)
	@JacksonXmlProperty(isAttribute = true)
	private final String name;
	@JsonIgnore
	private ResourceKey<R> resourceKey;
	@JsonIgnore
	private transient final Element xmlElement;

	/**
	 * Json Constructor
	 */
	public AbstractResourceConfig() {
		id = -1;
		name = null;
		resourceKey = null;
		xmlElement = null;
	}

	public AbstractResourceConfig(Element element) throws InvalidConfigFileException {
		Logger.debug(this, "Load " + this);
		this.xmlElement = element;
		this.id = ConfigParser.getIntegerAttributeValue(ATTRIBUTE_ID, element, this);
		this.name = ConfigParser.getAttributeValue(ATTRIBUTE_NAME, element, this);
		this.resourceKey = new ResourceKey<R>(this);
		this.registerConfig();
	}

	/**
	 * This constructor is just for the AccessController resource
	 * initialisation.
	 *
	 * @param resource Is the parentResource of the @link AccessController.
	 */
	protected AbstractResourceConfig(AbstractSecureResourceConfig parentResourceConfig) throws InvalidConfigFileException {
		this.xmlElement = parentResourceConfig.getXMLElement();
		this.id = AccessControllerConfig.generateAccessControllerID();
		this.name = parentResourceConfig.getClass().getSimpleName() + "AccessController";
		this.resourceKey = new ResourceKey<R>(this);
	}

	private void registerConfig() throws InvalidConfigFileException {
		ResourceConfigManager resourceConfigManager;
		resourceConfigManager = ResourceConfigManager.getInstance();

		try {
			resourceConfigManager.addResourceConfig(this);
		} catch (DuplicatedInstanceException ex) {
			throw new InvalidConfigFileException("Duplicated id definition for Resource[" + resourceKey + "] with name " + name + ".", this, ex);
		}
	}
	
	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@JsonIgnore
	public ResourceKey<R> getResourceKey() {
		if(resourceKey == null) {
			resourceKey = new ResourceKey<R>(this);
		}
		return resourceKey;
	}

	@JsonIgnore
	protected final Element getXMLElement() {
		return xmlElement;
	}

	@JsonIgnore
	public String getPath() {
		return ConfigParser.getConfigURL(getClass());
	}

	@JsonIgnore
	public Document getParentConfigDocument() throws NotAvailableException {
		if (xmlElement == null) {
			throw new NotAvailableException("Parent config document");
		}
		return xmlElement.getDocument();
	}

	@JsonIgnore
	public final String getXMLConfigURI() {
		try {
			return getParentConfigDocument().getBaseURI();
		} catch (NotAvailableException ex) {
			return ex.getMessage();
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[Key:" + resourceKey + "]";
	}

	public boolean isEquals(AbstractResourceConfig config) {
		return resourceKey.equals(config.resourceKey);
	}
}
