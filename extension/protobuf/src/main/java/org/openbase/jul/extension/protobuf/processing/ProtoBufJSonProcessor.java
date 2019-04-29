package org.openbase.jul.extension.protobuf.processing;

/*-
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
    private static final String EMPTY_MESSAGE = "{}";
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
     *                                                          <p>
     *                                                          TODO: release: change parameter type to message since java primitives cannot be de-/serialized anymore anyway
     */
    public String   serialize(final Object serviceState) throws InvalidStateException, CouldNotPerformException {
        String jsonStringRep;
        if (serviceState instanceof Message) {
            try {
                jsonStringRep = jsonFormat.printToString((Message) serviceState);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not serialize service argument to string!", ex);
            }
        } else {
            throw new InvalidStateException("Service attribute Class["+serviceState.getClass().getSimpleName()+"] not a protobuf message!");
        }

        return jsonStringRep;
    }

    /**
     * Get the string representation for a given serviceState which can be a
     * proto message, enumeration or a java primitive.
     *
     * @param serviceState the serviceState
     *
     * @return a string representation of the serviceState type
     *
     * @throws CouldNotPerformException
     */
    public String getServiceStateClassName(final Object serviceState) throws CouldNotPerformException {
        if (serviceState.getClass().getName().startsWith("org.openbase.type")) {
            return serviceState.getClass().getName();
        }

        if (serviceState.getClass().isEnum()) {
            logger.info(serviceState.getClass().getName());
            return serviceState.getClass().getName();
        }

        logger.debug("Simple class name of attribute to upper case [" + serviceState.getClass().getSimpleName().toUpperCase() + "]");
        JavaTypeToProto javaToProto;
        try {
            javaToProto = JavaTypeToProto.valueOf(serviceState.getClass().getSimpleName().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new CouldNotPerformException("ServiceState is not a supported java primitive nor a supported rst type", ex);
        }
        logger.debug("According proto type [" + javaToProto.getProtoType().name() + "]");
        return javaToProto.getProtoType().name();
    }

    public <SAT> SAT deserialize(String jsonStringRep, Class<SAT> serviceStateClass) throws CouldNotPerformException {
        return (SAT) deserialize(jsonStringRep, serviceStateClass.getSimpleName());
    }

    /**
     * Deserialise a JSon string representation for a protobuf message given the class
     * name for the value or the type if its a primitive.
     *
     * @param jsonStringRep        the string representation of the rst value
     * @param serviceStateClassName the class name or the type of the value
     *
     * @return the deserialized message
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public Message deserialize(String jsonStringRep, String serviceStateClassName) throws CouldNotPerformException {
        try {
            try {
                Class serviceStateClass = Class.forName(serviceStateClassName);
                if (serviceStateClass.isEnum()) {
                    throw new NotSupportedException(serviceStateClassName, this, "Service attribute type must be a protobuf message!");
                }
                Message.Builder builder = (Message.Builder) serviceStateClass.getMethod("newBuilder").invoke(null);
                jsonFormat.merge(new ByteArrayInputStream(jsonStringRep.getBytes(Charset.forName(UTF8))), builder);
                return builder.build();
            } catch (ClassNotFoundException ex) {
                throw new CouldNotPerformException("Could not find class for serviceStateClassName [" + serviceStateClassName + "]", ex);
            } catch (IOException ex) {
                throw new CouldNotPerformException("Could not merge [" + jsonStringRep + "] into builder", ex);
            } catch (NoSuchMethodException | SecurityException ex) {
                throw new CouldNotPerformException("Could not find or access newBuilder method for rst type [" + serviceStateClassName + "]", ex);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new CouldNotPerformException("Could not invoke newBuilder method for rst type [" + serviceStateClassName + "]", ex);
            }
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not deserialize json String[" + jsonStringRep + "] into ServiceStateClassName[" + serviceStateClassName + "]!", ex);
        }
    }

    public String getJavaPrimitiveClassName(Descriptors.FieldDescriptor.Type protoType) {
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
