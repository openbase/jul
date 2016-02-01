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
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.processing.StringProcessor;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * @author DivineThreepwood
 */
public class BuilderProcessor {

    public static List<GeneratedMessage.Builder> extractRepeatedFieldBuilderList(final int repeatedFieldNumber, final GeneratedMessage.Builder builder) throws CouldNotPerformException {
        return extractRepeatedFieldBuilderList(builder.getDescriptorForType().findFieldByNumber(repeatedFieldNumber), builder);
    }

    public static List<GeneratedMessage.Builder> extractRepeatedFieldBuilderList(final Descriptors.FieldDescriptor repeatedFieldDescriptor, final GeneratedMessage.Builder builder) throws CouldNotPerformException {
        if (repeatedFieldDescriptor == null) {
            throw new NotAvailableException("repeatedFieldDescriptor");
        }
        return extractRepeatedFieldBuilderList(repeatedFieldDescriptor.getName(), builder);
    }

    public static List<GeneratedMessage.Builder> extractRepeatedFieldBuilderList(final String repeatedFieldName, final GeneratedMessage.Builder builder) throws CouldNotPerformException {
        try {
            Class builderClass = builder.getClass();
            Method method;

            try {
                method = builderClass.getMethod("get" + StringProcessor.transformUpperCaseToCamelCase(repeatedFieldName) + "BuilderList");
            } catch (Exception ex) {
                throw new CouldNotPerformException("Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.getClass().getName() + "]! ", ex);
            }

            try {
                return (List<GeneratedMessage.Builder>) method.invoke(builder);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not extract builder list!", ex);
            }

        } catch (Exception ex) {
            throw new CouldNotPerformException("Could extract builder collection out of Builder[" + builder.getClass().getName() + "]!", ex);
        }
    }

    public static GeneratedMessage.Builder addMessageToRepeatedField(final int repeatedFieldNumber, final GeneratedMessage.Builder messageBuilder, final GeneratedMessage.Builder builder) throws CouldNotPerformException {
        return addMessageToRepeatedField(builder.getDescriptorForType().findFieldByNumber(repeatedFieldNumber), messageBuilder, builder);
    }

    public static GeneratedMessage.Builder addMessageToRepeatedField(final Descriptors.FieldDescriptor repeatedFieldDescriptor, final GeneratedMessage.Builder messageBuilder, final GeneratedMessage.Builder builder) throws CouldNotPerformException {
        if (repeatedFieldDescriptor == null) {
            throw new NotAvailableException("repeatedFieldDescriptor");
        }
        return addMessageToRepeatedField(repeatedFieldDescriptor.getName(), messageBuilder, builder);
    }

    public static GeneratedMessage.Builder addMessageToRepeatedField(final String repeatedFieldName, final GeneratedMessage.Builder messageBuilder, final GeneratedMessage.Builder builder) throws CouldNotPerformException {
        try {
            Class builderClass = builder.getClass();
            Method method;

            try {
                method = builderClass.getMethod("add" + StringProcessor.transformUpperCaseToCamelCase(repeatedFieldName), messageBuilder.getClass());
            } catch (Exception ex) {
                throw new CouldNotPerformException("Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.getClass().getName() + "]! ", ex);
            }

            try {
                method.invoke(builder, messageBuilder);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not add message builder to repeated field!", ex);
            }

            return builder;

        } catch (Exception ex) {
            throw new CouldNotPerformException("Could add message Builder[" + messageBuilder.getClass().getName() + "] to repeated Field[" + repeatedFieldName + "] of Builder[" + builder.getClass().getName() + "]!", ex);
        }
    }

    /**
     * Method adds a new default message instance to the repeated field and return it's builder instance.
     * @param repeatedFieldNumber The field number of the repeated field.
     * @param builder The builder instance of the message which contains the repeated field.
     * @return The builder instance of the new added message is returned.
     * @throws CouldNotPerformException
     */
    public static GeneratedMessage.Builder addDefaultInstanceToRepeatedField(final int repeatedFieldNumber, final GeneratedMessage.Builder builder) throws CouldNotPerformException {
        return addDefaultInstanceToRepeatedField(builder.getDescriptorForType().findFieldByNumber(repeatedFieldNumber), builder);
    }

    /**
     * Method adds a new default message instance to the repeated field and return it's builder instance.
     * @param repeatedFieldDescriptor The field descriptor of the repeated field.
     * @param builder The builder instance of the message which contains the repeated field.
     * @return The builder instance of the new added message is returned.
     * @throws CouldNotPerformException
     */
    public static GeneratedMessage.Builder addDefaultInstanceToRepeatedField(final Descriptors.FieldDescriptor repeatedFieldDescriptor, final GeneratedMessage.Builder builder) throws CouldNotPerformException {
        if (repeatedFieldDescriptor == null) {
            throw new NotAvailableException("repeatedFieldDescriptor");
        }
        return addDefaultInstanceToRepeatedField(repeatedFieldDescriptor.getName(), builder);
    }

    /**
     * Method adds a new default message instance to the repeated field and return it's builder instance.
     * @param repeatedFieldName The name of the repeated field.
     * @param builder The builder instance of the message which contains the repeated field.
     * @return The builder instance of the new added message is returned.
     * @throws CouldNotPerformException
     */
    public static GeneratedMessage.Builder addDefaultInstanceToRepeatedField(final String repeatedFieldName, final GeneratedMessage.Builder builder) throws CouldNotPerformException {
        try {
            Class builderClass = builder.getClass();
            Method method;

            try {
                method = builderClass.getMethod("add" + StringProcessor.transformUpperCaseToCamelCase(repeatedFieldName) + "Builder");
            } catch (Exception ex) {
                throw new CouldNotPerformException("Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.getClass().getName() + "]! ", ex);
            }

            try {
                return (GeneratedMessage.Builder) method.invoke(builder);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not add default message builder to repeated field!", ex);
            }

        } catch (Exception ex) {
            throw new CouldNotPerformException("Could add default instance to repeated Field[" + repeatedFieldName + "] of Builder[" + builder.getClass().getName() + "]!", ex);
        }
    }
}
