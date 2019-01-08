package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import java.util.HashMap;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.jul.processing.VariableProvider;
import java.util.Map;

/**
 *
 * * @author * @author <a href="mailto:DivineThreepwood@gmail.com">DivineThreepwood</a>
 */
public class ProtobufVariableProvider implements VariableProvider {

    public static final String NAME_SUFIX = "VariableProvider";
    private final Message message;

    public ProtobufVariableProvider(final Message message) {
        this.message = message;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return message.getDescriptorForType().getName() + ProtobufVariableProvider.NAME_SUFIX;
    }

    /**
     * {@inheritDoc}
     *
     * @param variable {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public String getValue(String variable) throws NotAvailableException {
        String key;
        for (Map.Entry<Descriptors.FieldDescriptor, Object> fieldEntry : message.getAllFields().entrySet()) {
            key = StringProcessor.transformToUpperCase(fieldEntry.getKey().getName());
            if (key.equals(variable) || (StringProcessor.transformToUpperCase(message.getClass().getSimpleName()) + "/" + key).equals(variable)) {
                if (!fieldEntry.getValue().toString().isEmpty()) {
                    return fieldEntry.getValue().toString();
                }
            }
        }
        throw new NotAvailableException("Value for Variable[" + variable + "]");
    }

    /**
     * {@inheritDoc}
     *
     * @param variableContains {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Map<String, String> getValues(String variableContains) {
        final Map<String, String> variableSelection = new HashMap<>();
        String key;
        for (Map.Entry<Descriptors.FieldDescriptor, Object> fieldEntry : message.getAllFields().entrySet()) {
            key = StringProcessor.transformToUpperCase(fieldEntry.getKey().getName());
            if (key.contains(variableContains) || (StringProcessor.transformToUpperCase(message.getClass().getSimpleName()) + "/" + key).contains(variableContains)) {
                if (!fieldEntry.getValue().toString().isEmpty()) {
                    variableSelection.put(key, fieldEntry.getValue().toString());
                }
            }
        }
        return variableSelection;
    }
}
