package org.openbase.jul.extension.protobuf

import com.google.protobuf.AbstractMessage
import com.google.protobuf.Descriptors
import com.google.protobuf.Message
import com.google.protobuf.MessageOrBuilder
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.NotAvailableException
import org.openbase.jul.extension.protobuf.processing.ProtoBufFieldProcessor
import org.openbase.jul.processing.StringProcessor
import java.lang.reflect.Method

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
 */ /**
 * * @author DivineThreepwood
 */
object ProtoBufBuilderProcessor {
    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun extractRepeatedFieldBuilderList(repeatedFieldNumber: Int, builder: Message.Builder): List<Message.Builder> {
        return extractRepeatedFieldBuilderList(
            builder.descriptorForType.findFieldByNumber(repeatedFieldNumber),
            builder
        )
    }

    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun extractRepeatedFieldBuilderList(
        repeatedFieldDescriptor: Descriptors.FieldDescriptor?,
        builder: Message.Builder
    ): List<Message.Builder> {
        if (repeatedFieldDescriptor == null) {
            throw NotAvailableException("repeatedFieldDescriptor")
        }
        return extractRepeatedFieldBuilderList(repeatedFieldDescriptor.name, builder)
    }

    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun extractRepeatedFieldBuilderList(repeatedFieldName: String, builder: Message.Builder): List<Message.Builder> {
        return try {
            val builderClass: Class<*> = builder.javaClass
            val method: Method
            method = try {
                builderClass.getMethod("get" + StringProcessor.transformUpperCaseToPascalCase(repeatedFieldName) + "BuilderList")
            } catch (ex: Exception) {
                throw CouldNotPerformException(
                    "Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.javaClass.name + "]! ",
                    ex
                )
            }
            try {
                method.invoke(builder) as List<Message.Builder>
            } catch (ex: Exception) {
                throw CouldNotPerformException("Could not extract builder list!", ex)
            }
        } catch (ex: Exception) {
            throw CouldNotPerformException(
                "Could extract builder collection out of Builder[" + builder.javaClass.name + "]!",
                ex
            )
        }
    }

    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addMessageToRepeatedField(
        repeatedFieldNumber: Int,
        messageBuilder: Message.Builder,
        builder: Message.Builder
    ): Message.Builder {
        return addMessageToRepeatedField(
            builder.descriptorForType.findFieldByNumber(repeatedFieldNumber),
            messageBuilder,
            builder
        )
    }

    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addMessageToRepeatedField(
        repeatedFieldDescriptor: Descriptors.FieldDescriptor?,
        messageBuilder: Message.Builder,
        builder: Message.Builder
    ): Message.Builder {
        if (repeatedFieldDescriptor == null) {
            throw NotAvailableException("repeatedFieldDescriptor")
        }
        return addMessageToRepeatedField(repeatedFieldDescriptor.name, messageBuilder, builder)
    }

    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addMessageToRepeatedField(
        repeatedFieldName: String,
        messageBuilder: Message.Builder,
        builder: Message.Builder
    ): Message.Builder {
        return try {
            val builderClass: Class<*> = builder.javaClass
            val method: Method
            method = try {
                builderClass.getMethod(
                    "add" + StringProcessor.transformUpperCaseToPascalCase(repeatedFieldName),
                    messageBuilder.javaClass
                )
            } catch (ex: Exception) {
                throw CouldNotPerformException(
                    "Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.javaClass.name + "]! ",
                    ex
                )
            }
            try {
                method.invoke(builder, messageBuilder)
            } catch (ex: Exception) {
                throw CouldNotPerformException("Could not add message builder to repeated field!", ex)
            }
            builder
        } catch (ex: Exception) {
            throw CouldNotPerformException(
                "Could add message Builder[" + messageBuilder.javaClass.name + "] to repeated Field[" + repeatedFieldName + "] of Builder[" + builder.javaClass.name + "]!",
                ex
            )
        }
    }

    /**
     * Method adds a new default message instance to the repeated field and return it's builder instance.
     *
     * @param repeatedFieldNumber The field number of the repeated field.
     * @param builder             The builder instance of the message which contains the repeated field.
     * @return The builder instance of the new added message is returned.
     * @throws CouldNotPerformException
     */
    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addDefaultInstanceToRepeatedField(repeatedFieldNumber: Int, builder: Message.Builder): Message.Builder {
        return addDefaultInstanceToRepeatedField(
            builder.descriptorForType.findFieldByNumber(repeatedFieldNumber),
            builder
        )
    }

    /**
     * Method adds a new default message instance to the repeated field and return it's builder instance.
     *
     * @param repeatedFieldDescriptor The field descriptor of the repeated field.
     * @param builder                 The builder instance of the message which contains the repeated field.
     * @return The builder instance of the new added message is returned.
     * @throws CouldNotPerformException
     */
    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addDefaultInstanceToRepeatedField(
        repeatedFieldDescriptor: Descriptors.FieldDescriptor?,
        builder: Message.Builder
    ): Message.Builder {
        if (repeatedFieldDescriptor == null) {
            throw NotAvailableException("repeatedFieldDescriptor")
        }
        return addDefaultInstanceToRepeatedField(repeatedFieldDescriptor.name, builder)
    }

    /**
     * Method adds a new default message instance to the repeated field and return it's builder instance.
     *
     * @param repeatedFieldName The name of the repeated field.
     * @param builder           The builder instance of the message which contains the repeated field.
     * @return The builder instance of the new added message is returned.
     * @throws CouldNotPerformException
     */
    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addDefaultInstanceToRepeatedField(repeatedFieldName: String, builder: Message.Builder): Message.Builder {
        return try {
            val builderClass: Class<*> = builder.javaClass
            val method: Method
            method = try {
                builderClass.getMethod("add" + StringProcessor.transformUpperCaseToPascalCase(repeatedFieldName) + "Builder")
            } catch (ex: Exception) {
                throw CouldNotPerformException(
                    "Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.javaClass.name + "]! ",
                    ex
                )
            }
            try {
                method.invoke(builder) as Message.Builder
            } catch (ex: Exception) {
                throw CouldNotPerformException("Could not add default message builder to repeated field!", ex)
            }
        } catch (ex: Exception) {
            throw CouldNotPerformException(
                "Could add default instance to repeated Field[" + repeatedFieldName + "] of Builder[" + builder.javaClass.name + "]!",
                ex
            )
        }
    }

    //TODO: all methods below are copies using AbstractMessage instead of message, I think they can be removed as soon as the new registry editor is usable
    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun extractRepeatedFieldBuilderList(
        repeatedFieldNumber: Int,
        builder: AbstractMessage.Builder<*>
    ): List<AbstractMessage.Builder<*>> {
        return extractRepeatedFieldBuilderList(
            builder.descriptorForType.findFieldByNumber(repeatedFieldNumber),
            builder
        )
    }

    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun extractRepeatedFieldBuilderList(
        repeatedFieldDescriptor: Descriptors.FieldDescriptor?,
        builder: AbstractMessage.Builder<*>
    ): List<AbstractMessage.Builder<*>> {
        if (repeatedFieldDescriptor == null) {
            throw NotAvailableException("repeatedFieldDescriptor")
        }
        return extractRepeatedFieldBuilderList(repeatedFieldDescriptor.name, builder)
    }

    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun extractRepeatedFieldBuilderList(
        repeatedFieldName: String,
        builder: AbstractMessage.Builder<*>
    ): List<AbstractMessage.Builder<*>> {
        return try {
            val builderClass: Class<*> = builder.javaClass
            val method: Method
            method = try {
                builderClass.getMethod("get" + StringProcessor.transformUpperCaseToPascalCase(repeatedFieldName) + "BuilderList")
            } catch (ex: Exception) {
                throw CouldNotPerformException(
                    "Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.javaClass.name + "]! ",
                    ex
                )
            }
            try {
                method.invoke(builder) as List<AbstractMessage.Builder<*>>
            } catch (ex: Exception) {
                throw CouldNotPerformException("Could not extract builder list!", ex)
            }
        } catch (ex: Exception) {
            throw CouldNotPerformException(
                "Could extract builder collection out of Builder[" + builder.javaClass.name + "]!",
                ex
            )
        }
    }

    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addMessageToRepeatedField(
        repeatedFieldNumber: Int,
        messageBuilder: AbstractMessage.Builder<*>,
        builder: AbstractMessage.Builder<*>
    ): AbstractMessage.Builder<*> {
        return addMessageToRepeatedField(
            builder.descriptorForType.findFieldByNumber(repeatedFieldNumber),
            messageBuilder,
            builder
        )
    }

    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addMessageToRepeatedField(
        repeatedFieldDescriptor: Descriptors.FieldDescriptor?,
        messageBuilder: AbstractMessage.Builder<*>,
        builder: AbstractMessage.Builder<*>
    ): AbstractMessage.Builder<*> {
        if (repeatedFieldDescriptor == null) {
            throw NotAvailableException("repeatedFieldDescriptor")
        }
        return addMessageToRepeatedField(repeatedFieldDescriptor.name, messageBuilder, builder)
    }

    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addMessageToRepeatedField(
        repeatedFieldName: String,
        messageBuilder: AbstractMessage.Builder<*>,
        builder: AbstractMessage.Builder<*>
    ): AbstractMessage.Builder<*> {
        return try {
            val builderClass: Class<*> = builder.javaClass
            val method: Method
            method = try {
                builderClass.getMethod(
                    "add" + StringProcessor.transformUpperCaseToPascalCase(repeatedFieldName),
                    messageBuilder.javaClass
                )
            } catch (ex: Exception) {
                throw CouldNotPerformException(
                    "Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.javaClass.name + "]! ",
                    ex
                )
            }
            try {
                method.invoke(builder, messageBuilder)
            } catch (ex: Exception) {
                throw CouldNotPerformException("Could not add message builder to repeated field!", ex)
            }
            builder
        } catch (ex: Exception) {
            throw CouldNotPerformException(
                "Could add message Builder[" + messageBuilder.javaClass.name + "] to repeated Field[" + repeatedFieldName + "] of Builder[" + builder.javaClass.name + "]!",
                ex
            )
        }
    }

    /**
     * Method adds a new default message instance to the repeated field and return it's builder instance.
     *
     * @param repeatedFieldNumber The field number of the repeated field.
     * @param builder             The builder instance of the message which contains the repeated field.
     * @return The builder instance of the new added message is returned.
     * @throws CouldNotPerformException
     */
    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addDefaultInstanceToRepeatedField(
        repeatedFieldNumber: Int,
        builder: AbstractMessage.Builder<*>
    ): AbstractMessage.Builder<*> {
        return addDefaultInstanceToRepeatedField(
            builder.descriptorForType.findFieldByNumber(repeatedFieldNumber),
            builder
        )
    }

    /**
     * Method adds a new default message instance to the repeated field and return it's builder instance.
     *
     * @param repeatedFieldDescriptor The field descriptor of the repeated field.
     * @param builder                 The builder instance of the message which contains the repeated field.
     * @return The builder instance of the new added message is returned.
     * @throws CouldNotPerformException
     */
    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addDefaultInstanceToRepeatedField(
        repeatedFieldDescriptor: Descriptors.FieldDescriptor?,
        builder: AbstractMessage.Builder<*>
    ): AbstractMessage.Builder<*> {
        if (repeatedFieldDescriptor == null) {
            throw NotAvailableException("repeatedFieldDescriptor")
        }
        return addDefaultInstanceToRepeatedField(repeatedFieldDescriptor.name, builder)
    }

    /**
     * Method adds a new default message instance to the repeated field and return it's builder instance.
     *
     * @param repeatedFieldName The name of the repeated field.
     * @param builder           The builder instance of the message which contains the repeated field.
     * @return The builder instance of the new added message is returned.
     * @throws CouldNotPerformException
     */
    @Throws(CouldNotPerformException::class)
    @JvmStatic
    fun addDefaultInstanceToRepeatedField(
        repeatedFieldName: String,
        builder: AbstractMessage.Builder<*>
    ): AbstractMessage.Builder<*> {
        return try {
            val builderClass: Class<*> = builder.javaClass
            val method: Method
            method = try {
                builderClass.getMethod("add" + StringProcessor.transformUpperCaseToPascalCase(repeatedFieldName) + "Builder")
            } catch (ex: Exception) {
                throw CouldNotPerformException(
                    "Missing RepeatedField[" + repeatedFieldName + "] in protobuf Type[" + builder.javaClass.name + "]! ",
                    ex
                )
            }
            try {
                method.invoke(builder) as AbstractMessage.Builder<*>
            } catch (ex: Exception) {
                throw CouldNotPerformException("Could not add default message builder to repeated field!", ex)
            }
        } catch (ex: Exception) {
            throw CouldNotPerformException(
                "Could add default instance to repeated Field[" + repeatedFieldName + "] of Builder[" + builder.javaClass.name + "]!",
                ex
            )
        }
    }

    /**
     * Merge the values of a message or builder into another message.
     * This method accepts message of different types and tries to merge all fields
     * which have the same field name in both types.
     *
     * @param mergedInto the builder into which new values are merged
     * @param toMerge    the message or builder from which values are merged
     * @return the builder merged into
     */
    @JvmStatic
    fun merge(mergedInto: Message.Builder, toMerge: MessageOrBuilder): Message.Builder {
        for (toMergeField in toMerge.allFields.keys) {
            // Note: getAllFields return only the fields which are not empty
            for (mergedIntoField in mergedInto.descriptorForType.fields) {
                if (mergedIntoField.name == toMergeField.name) {
                    mergedInto.setField(mergedIntoField, toMerge.getField(toMergeField))
                    break
                }
            }
        }
        return mergedInto
    }

    @Throws(NotAvailableException::class)
    @JvmStatic
    fun <MB> getBuilder(builder: Message.Builder, fieldName: String, builderClass: Class<MB>?): MB {
        return try {
            builder.getFieldBuilder(ProtoBufFieldProcessor.getFieldDescriptor(builder, fieldName)) as MB
        } catch (ex: Exception) {
            throw NotAvailableException("Builder[$fieldName]", ex)
        }
    }

    /**
     * This method clears all repeated fields from the given message before the `mergeFrom` is performed.
     * This functionality can be useful, since `mergeFrom` would always duplicate all repeated fields
     * which can be avoided by using this function instead.
     * @param message the message to merge from.
     */
    @JvmStatic
    fun <MB : Message.Builder> MB.mergeFromWithoutRepeatedFields(builder: Message.Builder): MB =
        // we should not modify the passed builder, so we need to transform it into a message first.
        mergeFromWithoutRepeatedFields(builder.build())

    /**
     * This method clears all repeated fields from the given message before the `mergeFrom` is performed.
     * This functionality can be useful, since `mergeFrom` would always duplicate all repeated fields
     * which can be avoided by using this function instead.
     * @param message the message to merge from.
     */
    @JvmStatic
    fun <MB : Message.Builder> MB.mergeFromWithoutRepeatedFields(message: Message): MB =
        message.toBuilder()
            .let { messageBuilder -> messageBuilder.clearRepeatedFields() }
            .let { messageBuilder -> mergeFrom(messageBuilder.build()) as MB }

    /**
     * This method recursively clears all repeated fields from the builder instance.
     */
    @JvmStatic
    fun <MB : Message.Builder> MB.clearRepeatedFields(): MB = also { builder ->
        allFields.keys.forEach { descriptor ->
            descriptor
                .takeIf { it.isRepeated }
                ?.also {clearField(it) ; return@forEach }
            descriptor
                .takeIf { it.javaType == Descriptors.FieldDescriptor.JavaType.MESSAGE }
                ?.also { builder.getFieldBuilder(it).clearRepeatedFields() }
        }
    }
}
