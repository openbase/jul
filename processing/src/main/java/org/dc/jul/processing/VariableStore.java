package org.dc.jul.processing;

import java.util.Map;
import java.util.TreeMap;
import org.dc.jul.exception.NotAvailableException;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class VariableStore implements VariableProvider {

    private final String name;
    private final Map<String, String> variableMap;

    public VariableStore(final String name) {
        this.name = name;
        this.variableMap = new TreeMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Resolves the value for the given key.
     * @param key
     * @return
     * @throws NotAvailableException
     */
    @Override
    public String getValue(String key) throws NotAvailableException {
        return variableMap.get(key);
    }

    /**
     * Stores the key value pair into the variable Store.
     * @param key
     * @param value
     */
    public void store(final String key, final String value) {
        variableMap.put(key, value);
    }
}
