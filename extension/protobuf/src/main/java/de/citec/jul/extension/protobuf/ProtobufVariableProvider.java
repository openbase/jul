/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.processing.StringProcessor;
import de.citec.jul.processing.VariableProvider;
import java.util.Map;

/**
 *
 * @author @author <a href="mailto:DivineThreepwood@gmail.com">Divine
 * Threepwood</a>
 */
public class ProtobufVariableProvider implements VariableProvider {

    public static final String NAME_SUFIX = "VariableProvider";
    private final GeneratedMessage message;

    public ProtobufVariableProvider(final GeneratedMessage message) {
        this.message = message;
    }

    @Override
    public String getName() {
        return message.getDescriptorForType().getName() + ProtobufVariableProvider.NAME_SUFIX;
    }

    @Override
    public String getValue(String variable) throws NotAvailableException {

        for (Map.Entry<Descriptors.FieldDescriptor, Object> fieldEntry : message.getAllFields().entrySet()) {
            if (StringProcessor.transformToUpperCase(fieldEntry.getKey().getName()).equals(variable)) {
                return fieldEntry.getValue().toString();
            }
        }
        throw new NotAvailableException("Value for Variable[" + variable + "]");
    }
}
