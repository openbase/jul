package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import com.google.protobuf.Message;
import com.google.protobuf.Message;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.processing.StringProcessor;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * * @author DivineThreepwood
 */
public class BuilderProcessor {

    public static List<Message.Builder> extractRepeatedFieldBuilderList(final int repeatedFieldNumber, final Message.Builder builder) throws CouldNotPerformException {
        return extractRepeatedFieldBuilderList(builder.getDescriptorForType().findFieldByNumber(repeatedFieldNumber), builder);
    }

    public static List<Message.Builder> extractRepeatedFieldBuilderList(final Descriptors.FieldDescriptor repeatedFieldDescriptor, final Message.Builder builder) throws CouldNotPerformException {
        if (repeatedFieldDescriptor == null) {
            throw new NotAvailableException("repeatedFieldDescriptor");
        }
        return extractRepeatedFieldBuilderList(repeatedFieldDescriptor.getName(), builder);
    }

    public static List<Message.Builder> extractRepeatedFieldBuilderList(final String repeatedFieldName, final Message.Builder builder) throws CouldNotPerformException {
        try {
            Class builderClass = builder.getClass();
            Method method;

            try {
                method = builderClass.getMethod("get" + StringProcessor.transformUpperCaseToCamelCase(repeatedFieldName) + "BuilderList");
            } catch (Exception ex) {
                throw new CouldNotPerformException("Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.getClass().getName() + "]! ", ex);
            }

            try {
                return (List<Message.Builder>) method.invoke(builder);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not extract builder list!", ex);
            }

        } catch (Exception ex) {
            throw new CouldNotPerformException("Could extract builder collection out of Builder[" + builder.getClass().getName() + "]!", ex);
        }
    }

    public static Message.Builder addMessageToRepeatedField(final int repeatedFieldNumber, final Message.Builder messageBuilder, final Message.Builder builder) throws CouldNotPerformException {
        return addMessageToRepeatedField(builder.getDescriptorForType().findFieldByNumber(repeatedFieldNumber), messageBuilder, builder);
    }

    public static Message.Builder addMessageToRepeatedField(final Descriptors.FieldDescriptor repeatedFieldDescriptor, final Message.Builder messageBuilder, final Message.Builder builder) throws CouldNotPerformException {
        if (repeatedFieldDescriptor == null) {
            throw new NotAvailableException("repeatedFieldDescriptor");
        }
        return addMessageToRepeatedField(repeatedFieldDescriptor.getName(), messageBuilder, builder);
    }

    public static Message.Builder addMessageToRepeatedField(final String repeatedFieldName, final Message.Builder messageBuilder, final Message.Builder builder) throws CouldNotPerformException {
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
    public static Message.Builder addDefaultInstanceToRepeatedField(final int repeatedFieldNumber, final Message.Builder builder) throws CouldNotPerformException {
        return addDefaultInstanceToRepeatedField(builder.getDescriptorForType().findFieldByNumber(repeatedFieldNumber), builder);
    }

    /**
     * Method adds a new default message instance to the repeated field and return it's builder instance.
     * @param repeatedFieldDescriptor The field descriptor of the repeated field.
     * @param builder The builder instance of the message which contains the repeated field.
     * @return The builder instance of the new added message is returned.
     * @throws CouldNotPerformException
     */
    public static Message.Builder addDefaultInstanceToRepeatedField(final Descriptors.FieldDescriptor repeatedFieldDescriptor, final Message.Builder builder) throws CouldNotPerformException {
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
    public static Message.Builder addDefaultInstanceToRepeatedField(final String repeatedFieldName, final Message.Builder builder) throws CouldNotPerformException {
        try {
            Class builderClass = builder.getClass();
            Method method;

            try {
                method = builderClass.getMethod("add" + StringProcessor.transformUpperCaseToCamelCase(repeatedFieldName) + "Builder");
            } catch (Exception ex) {
                throw new CouldNotPerformException("Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.getClass().getName() + "]! ", ex);
            }

            try {
                return (Message.Builder) method.invoke(builder);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not add default message builder to repeated field!", ex);
            }

        } catch (Exception ex) {
            throw new CouldNotPerformException("Could add default instance to repeated Field[" + repeatedFieldName + "] of Builder[" + builder.getClass().getName() + "]!", ex);
        }
    }

    //TODO: all methods below are copies using GeneratedMessage instead of message, I think they can be removed as soon as the new registry editor is usable

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
