package org.openbase.jul.extension.protobuf.processing;

/*-
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotSupportedException;
import org.openbase.jul.processing.StringProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

public class ProtoBufJSonProcessor {

    private static final String UTF8 = "UTF8";
    private static final String javaPrimitvePrefix = "java.lang.";

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final JsonFormat jsonFormat;

    public ProtoBufJSonProcessor() {
        this.jsonFormat = new JsonFormat();
    }

    /**
     * Serialize a serviceState which can be a proto message, enumeration or
     * a java primitive to string. If its a primitive toString is called while
     * messages or enumerations will be serialized into JSon
     *
     * @param serviceState
     *
     * @return
     *
     * @throws org.openbase.jul.exception.InvalidStateException in case the given service argument does not contain any context.
     * @throws CouldNotPerformException                         in case the serialization failed.
     */
    public String serialize(final Message serviceState) throws InvalidStateException, CouldNotPerformException {
        try {
            return jsonFormat.printToString(serviceState);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not serialize service argument to string!", ex);
        }
    }

    /**
     * Serialize a serviceStateBuilder which can be a proto message, enumeration or
     * a java primitive to string. If its a primitive toString is called while
     * messages or enumerations will be serialized into JSon
     *
     * @param serviceStateBuilder
     *
     * @return
     *
     * @throws org.openbase.jul.exception.InvalidStateException in case the given service argument does not contain any context.
     * @throws CouldNotPerformException                         in case the serialization failed.
     */
    public String serialize(final Message.Builder serviceStateBuilder) throws InvalidStateException, CouldNotPerformException {
        try {
            return jsonFormat.printToString(serviceStateBuilder.build());
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not serialize service argument to string!", ex);
        }
    }

    /**
     * Deserialise a JSon string representation for a protobuf message given the class of the type.
     *
     * @param jsonStringRep    the string representation of the rst value
     * @param messageClassName the message class name of the type to load the class.
     *
     * @return the deserialized message.
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown in case the deserialization fails.
     */
    public Message deserialize(final String jsonStringRep, final String messageClassName) throws CouldNotPerformException {
        try {
            Class<? extends Message> messageClass = (Class<? extends Message>) Class.forName(messageClassName);
            if (messageClass.isEnum()) {
                throw new NotSupportedException(messageClass, this, "Service attribute type must be a protobuf message!");
            }
            return deserialize(jsonStringRep, messageClass);
        } catch (ClassNotFoundException ex) {
            throw new CouldNotPerformException("Could not load Class[" + messageClassName + "]", ex);
        }
    }

    /**
     * Deserialise a JSon string representation for a protobuf message given the class of the type.
     *
     * @param jsonStringRep the string representation of the rst value
     * @param messageClass  the message class of the type.
     *
     * @return the deserialized message.
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown in case the deserialization fails.
     */
    public <M extends Message> M deserialize(final String jsonStringRep, final Class<M> messageClass) throws CouldNotPerformException {
        try {
            try {
                Message.Builder builder = (Message.Builder) messageClass.getMethod("newBuilder").invoke(null);
                jsonFormat.merge(new ByteArrayInputStream(jsonStringRep.getBytes(Charset.forName(UTF8))), builder);
                return (M) builder.build();
            } catch (IOException ex) {
                throw new CouldNotPerformException("Could not merge [" + jsonStringRep + "] into builder", ex);
            } catch (NoSuchMethodException | SecurityException ex) {
                throw new CouldNotPerformException("Could not find or access newBuilder method of type [" + messageClass + "]", ex);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new CouldNotPerformException("Could not invoke newBuilder method of type [" + messageClass + "]", ex);
            }
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not deserialize json String[" + jsonStringRep + "] into protobuf type of Class[" + messageClass + "]!", ex);
        }
    }

    public String getJavaPrimitiveClassName(final FieldDescriptor.Type protoType) {
        switch (protoType.getJavaType()) {
            case INT:
                return javaPrimitvePrefix + "Integer";
            default:
                return javaPrimitvePrefix + StringProcessor.transformUpperCaseToPascalCase(protoType.getJavaType().name());
        }
    }

    /**
     * Enumeration that maps from java primitives to proto field descriptor
     * types.
     */
    public enum JavaTypeToProto {

        BOOLEAN(Descriptors.FieldDescriptor.Type.BOOL),
        INTEGER(Descriptors.FieldDescriptor.Type.INT32),
        FLOAT(Descriptors.FieldDescriptor.Type.FLOAT),
        DOUBLE(Descriptors.FieldDescriptor.Type.DOUBLE),
        LONG(Descriptors.FieldDescriptor.Type.INT64),
        STRING(Descriptors.FieldDescriptor.Type.STRING),
        ENUM(Descriptors.FieldDescriptor.Type.ENUM);

        private final Descriptors.FieldDescriptor.Type protoType;

        JavaTypeToProto(final Descriptors.FieldDescriptor.Type protoType) {
            this.protoType = protoType;
        }

        public Descriptors.FieldDescriptor.Type getProtoType() {
            return protoType;
        }
    }
}
