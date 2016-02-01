/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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
import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.processing.StringProcessor;
import org.dc.jul.processing.VariableProvider;
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
                if (!fieldEntry.getValue().toString().isEmpty()) {
                    return fieldEntry.getValue().toString();
                }
            }
        }
        throw new NotAvailableException("Value for Variable[" + variable + "]");
    }
}
