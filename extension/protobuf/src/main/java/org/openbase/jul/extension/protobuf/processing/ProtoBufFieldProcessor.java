package org.openbase.jul.extension.protobuf.processing;

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

import com.google.protobuf.*;
import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message.Builder;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.iface.provider.LabelProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ProtoBufFieldProcessor {

    //TODO: write java doc for all methods
    public static Descriptors.FieldDescriptor getFieldDescriptor(final MessageOrBuilder builder, final int fieldNumber) {
        return builder.getDescriptorForType().findFieldByNumber(fieldNumber);
    }

    public static FieldDescriptor[] getFieldDescriptors(final Class<? extends GeneratedMessage> messageClass, final int... fieldNumbers) throws CouldNotPerformException {
        try {
            FieldDescriptor[] fieldDescriptors = new FieldDescriptor[fieldNumbers.length];
            GeneratedMessage defaultMessage = (GeneratedMessage) messageClass.getMethod("getDefaultInstance").invoke(null);
            for (int i = 0; i < fieldNumbers.length; i++) {
                fieldDescriptors[i] = getFieldDescriptor(defaultMessage, fieldNumbers[i]);
            }
            return fieldDescriptors;
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            throw new CouldNotPerformException("Could not detect field descriptors!", ex);
        }
    }

    public static FieldDescriptor getFieldDescriptor(final Class<? extends GeneratedMessage> messageClass, final int fieldNumber) throws CouldNotPerformException {
        try {
            return getFieldDescriptor((GeneratedMessage) messageClass.getMethod("getDefaultInstance").invoke(null), fieldNumber);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            throw new CouldNotPerformException("Could not detect field descriptor!", ex);
        }
    }

    public static Descriptors.FieldDescriptor getFieldDescriptor(final MessageOrBuilder msg, final String fieldName) {
        return msg.getDescriptorForType().findFieldByName(fieldName);
    }

    public static String getId(final Message msg) throws CouldNotPerformException {
        return getId(msg.toBuilder());
    }

    public static String getId(final Builder msg) throws CouldNotPerformException {
        try {
            return (String) msg.getField(getFieldDescriptor(msg, Identifiable.TYPE_FIELD_ID));
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not get id of [" + msg + "]", ex);
        }
    }

    public static String getDescription(final Message.Builder msg) throws CouldNotPerformException {
        try {
            return (String) msg.getField(getFieldDescriptor(msg, "description"));
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not get description of [" + msg + "]", ex);
        }
    }

    public static String getLabel(final Message.Builder msg) throws CouldNotPerformException {
        try {
            return (String) msg.getField(getFieldDescriptor(msg, LabelProvider.TYPE_FIELD_LABEL));
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not get label of [" + msg + "]", ex);
        }
    }

    public static void initRequiredFieldsWithDefault(final Message.Builder builder) {
        List<String> missingFieldList = builder.findInitializationErrors();
        missingFieldList.stream().forEach((initError) -> {
            initFieldWithDefault(builder, initError);
        });
    }

    /**
     * Tests if a message is empty. If a message is empty none of its fields are set.
     *
     * @param messageOrBuilder the message or builder which is tested
     *
     * @return true if none of the fields is set, else false
     */
    public static boolean isMessageEmpty(final MessageOrBuilder messageOrBuilder) {
        for (FieldDescriptor fieldDescriptor : messageOrBuilder.getDescriptorForType().getFields()) {
            if (messageOrBuilder.hasField(fieldDescriptor)) {
                return false;
            }
        }
        return true;
    }

    public static Message.Builder initFieldWithDefault(final Message.Builder builder, final String fieldPath) {
        Descriptors.FieldDescriptor fieldDescriptor;
        Message.Builder tmpBuilder = builder;

        String[] fields = fieldPath.split("\\.");
        for (int i = 0; i < fields.length - 1; ++i) {
            if (fields[i].endsWith("]")) {
                String fieldName = fields[i].split("\\[")[0];
                int number = Integer.parseInt(fields[i].split("\\[")[1].split("\\]")[0]);
                fieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(tmpBuilder, fieldName);

                Message.Builder subBuilder = ((Message) tmpBuilder.getRepeatedField(fieldDescriptor, number)).toBuilder();
                String subPath = fields[i + 1];
                for (int j = i + 2; j < fields.length; ++j) {
                    subPath += "." + fields[j];
                }
                tmpBuilder.setRepeatedField(fieldDescriptor, number, initFieldWithDefault(subBuilder, subPath).buildPartial());
                return builder;
            } else {
                fieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(tmpBuilder, fields[i]);
                tmpBuilder = tmpBuilder.getFieldBuilder(fieldDescriptor);
            }
        }
        fieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(tmpBuilder, fields[fields.length - 1]);
        Object field = tmpBuilder.getField(fieldDescriptor);
        //Todo: remove with protobuf 3
        if (fieldDescriptor.getName().equals("qw")) {
            field = 1.0;
        }
        tmpBuilder.setField(fieldDescriptor, field);
        return builder;
    }

    public static void clearRequiredFields(final Message.Builder builder) {
        builder.findInitializationErrors().stream().forEach((initError) -> {
            clearRequiredField(builder, initError);
        });
    }

    public static Message.Builder clearRequiredField(final Message.Builder builder, final String fieldPath) {
        Descriptors.FieldDescriptor fieldDescriptor;
        Message.Builder tmpBuilder = builder;

        String[] fields = fieldPath.split("\\.");
        boolean alreadyRemoved = false;
        for (int i = 0; i < fields.length - 2; ++i) {
            if (fields[i].endsWith("]")) {
                String fieldName = fields[i].split("\\[")[0];
                int number = Integer.parseInt(fields[i].split("\\[")[1].split("\\]")[0]);
                fieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(tmpBuilder, fieldName);

                Message.Builder subBuilder = ((Message) tmpBuilder.getRepeatedField(fieldDescriptor, number)).toBuilder();
                String subPath = fields[i + 1];
                for (int j = i + 2; j < fields.length; ++j) {
                    subPath += "." + fields[j];
                }
                tmpBuilder.setRepeatedField(fieldDescriptor, number, clearRequiredField(subBuilder, subPath).buildPartial());
                return builder;
            } else {
                fieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(tmpBuilder, fields[i]);
                if (!tmpBuilder.hasField(fieldDescriptor)) {
                    alreadyRemoved = true;
                    continue;
                }
                tmpBuilder = tmpBuilder.getFieldBuilder(fieldDescriptor);
            }
        }
        if (!alreadyRemoved) {
            fieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(tmpBuilder, fields[fields.length - 2]);
            tmpBuilder.clearField(fieldDescriptor);
        }
        return builder;
    }

    public enum BuilderInitializationStatus {

        ALL_REQUIRED_FIELDS_SET,
        NO_REQUIRED_FIELDS_SET,
        SOME_REQUIRED_FIELDS_SET;
    }

    //    public static boolean checkIfSomeButNotAllRequiredFieldsAreSet(final Message.Builder builder) {
//        return checkBuilderInitialization(builder) == BuilderInitializationStatus.SOME_REQUIRED_FIELDS_SET;
//    }
//
//    /**
//     * Recursively check the initialization of a builder.
//     *
//     * @param builder the builder which is checked
//     * @return the initialization status of the builder
//     */
//    public static BuilderInitializationStatus checkBuilderInitialization(final Message.Builder builder) {
//        if (builder.isInitialized()) {
//            // all required fields are set, thus no problem
//            return BuilderInitializationStatus.ALL_REQUIRED_FIELDS_SET;
//        }
//
//        BuilderInitializationStatus status = null, tmp;
//        for (Descriptors.FieldDescriptor field : builder.getDescriptorForType().getFields()) {
//            // check if the field is set or a repeated field that does not contain further messages, if not continue
//            if (!field.isRepeated() && !builder.hasField(field)) {
//                continue;
//            }
//            if (field.isRepeated() && field.getType() != Descriptors.FieldDescriptor.Type.MESSAGE) {
//                continue;
//            }
//
//            // recursively check for all sub-messages
//            if (field.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
//                if (field.isRepeated()) {
//                    for (int i = 0; i < builder.getRepeatedFieldCount(field); ++i) {
//                        tmp = checkBuilderInitialization(((Message) builder.getRepeatedField(field, i)).toBuilder());
//                        if (tmp == BuilderInitializationStatus.SOME_REQUIRED_FIELDS_SET) {
//                            return BuilderInitializationStatus.SOME_REQUIRED_FIELDS_SET;
//                        } else {
//                            if (status == null) {
//                                status = tmp;
//                            } else if (tmp != status) {
//                                return BuilderInitializationStatus.SOME_REQUIRED_FIELDS_SET;
//                            }
//                        }
//                    }
//                } else {
//                    tmp = checkBuilderInitialization(builder.getFieldBuilder(field));
//                    if (tmp == BuilderInitializationStatus.SOME_REQUIRED_FIELDS_SET) {
//                        return BuilderInitializationStatus.SOME_REQUIRED_FIELDS_SET;
//                    } else {
//                        if (status == null) {
//                            status = tmp;
//                        } else if (tmp != status) {
//                            return BuilderInitializationStatus.SOME_REQUIRED_FIELDS_SET;
//                        }
//                    }
//                }
//            } else if (field.isRequired()) {
//                // field is no message but still required
//                return BuilderInitializationStatus.SOME_REQUIRED_FIELDS_SET;
//            }
//        }
//        return BuilderInitializationStatus.NO_REQUIRED_FIELDS_SET;
//    }
    public static boolean checkIfSomeButNotAllRequiredFieldsAreSet(final Message.Builder builder) {
        if (builder.isInitialized()) {
            // all required fields are set, thus no problem
            return false;
        }

        BuilderInitializationStatus status = null, tmp;
        for (Descriptors.FieldDescriptor field : builder.getDescriptorForType().getFields()) {
            // check if the field is set or a repeated field that does not contain further messages, if not continue
            if (!field.isRepeated() && !builder.hasField(field)) {
                continue;
            }
            if (field.isRepeated() && field.getType() != Descriptors.FieldDescriptor.Type.MESSAGE) {
                continue;
            }

            // recursively check for all sub-messages
            if (field.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                if (field.isRepeated()) {
                    for (int i = 0; i < builder.getRepeatedFieldCount(field); ++i) {
                        if (checkIfSomeButNotAllRequiredFieldsAreSet(((Message) builder.getRepeatedField(field, i)).toBuilder())) {
                            return true;
                        }
                    }
                } else {
                    if (checkIfSomeButNotAllRequiredFieldsAreSet(builder.getFieldBuilder(field))) {
                        return true;
                    }
                }
            } else if (field.isRequired()) {
                // field is no message but still required
                return true;
            }
        }
        return false;
    }

    /**
     * Method can be used within protobuf 2 to already have some kind of map support. The map is actually a repeated field of an entry with a key and a value field.
     * To simplify the map access this method can be used to put new entry in the list while this method takes care of the unique key handling.
     *
     * @param entryMessage       the entry to put into the map.
     * @param mapFieldDescriptor the descriptor which refers the map field of the {@code mapHolder}.
     * @param mapHolder          the message builder providing the map field.
     *
     * @throws CouldNotPerformException is thrown if something went wrong during the reflection process which mostly means the data types are not compatible.
     */
    public static void putMapEntry(final Message entryMessage, FieldDescriptor mapFieldDescriptor, GeneratedMessage.Builder mapHolder) throws CouldNotPerformException {
        try {
            final FieldDescriptor keyDescriptor = entryMessage.getDescriptorForType().findFieldByName("key");
            final FieldDescriptor valueDescriptor = entryMessage.getDescriptorForType().findFieldByName("value");

            if (keyDescriptor == null) {
                throw new NotAvailableException("Field[KEY] does not exist for type " + entryMessage.getClass().getName());
            }

            if (valueDescriptor == null) {
                throw new NotAvailableException("Field[VALUE] does not exist for type " + entryMessage.getClass().getName());
            }

            final boolean keyIsEnum = keyDescriptor.getJavaType() == JavaType.ENUM;

            // build map representation
            final TreeMap<Object, Object> latestValueOccurrenceMap = new TreeMap<>();
            for (int i = 0; i < mapHolder.getRepeatedFieldCount(mapFieldDescriptor); i++) {
                final Message entry = (Message) mapHolder.getRepeatedField(mapFieldDescriptor, i);

                if (keyIsEnum) {
                    latestValueOccurrenceMap.put(((EnumValueDescriptor) entry.getField(keyDescriptor)).getNumber(), entry.getField(valueDescriptor));
                } else {
                    latestValueOccurrenceMap.put(entry.getField(keyDescriptor), entry.getField(valueDescriptor));
                }
            }

            // insert entry to update
            Object field = entryMessage.getField(keyDescriptor);
            if (keyIsEnum) {
                latestValueOccurrenceMap.put(((EnumValueDescriptor) field).getNumber(), entryMessage.getField(valueDescriptor));
            } else {
                latestValueOccurrenceMap.put(field, entryMessage.getField(valueDescriptor));
            }

            // update map holder
            mapHolder.clearField(mapFieldDescriptor);
            for (Entry<Object, Object> entry : latestValueOccurrenceMap.entrySet()) {
                final Message.Builder entryBuilder = entryMessage.newBuilderForType();
                if (keyIsEnum) {
                    entryBuilder.setField(keyDescriptor, keyDescriptor.getEnumType().findValueByNumber((Integer) entry.getKey()));
                } else {
                    entryBuilder.setField(keyDescriptor, entry.getKey());
                }
                entryBuilder.setField(valueDescriptor, entry.getValue());
                mapHolder.addRepeatedField(mapFieldDescriptor, entryBuilder.build());
            }
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not add entry to map!", ex);
        }
    }

    /**
     * Method can be used within protobuf 2 to already have some kind of map support. The map is actually a repeated field of an entry with a key and a value field.
     * To simplify the map access this method can be used to resolve an value via its related {@code key}.
     *
     * @param key                the key to identify the value within the map.
     * @param mapFieldDescriptor the descriptor which refers the map field of the {@code mapHolder}.
     * @param mapHolder          the message builder providing the map field.
     *
     * @return the resolved entry related to the given {@code key}
     *
     * @throws CouldNotPerformException is thrown if the entry could not be resolved.
     */
    public static Object getMapEntry(final Object key, FieldDescriptor mapFieldDescriptor, MessageOrBuilder mapHolder) throws CouldNotPerformException {
        try {

            if (mapHolder.getRepeatedFieldCount(mapFieldDescriptor) == 0) {
                throw new InvalidStateException("Referred map is empty!");
            }

            // generic lookup of field descriptors
            final Message entryExample = (Message) mapHolder.getRepeatedField(mapFieldDescriptor, 0);

            final FieldDescriptor keyDescriptor = entryExample.getDescriptorForType().findFieldByName("key");
            final FieldDescriptor valueDescriptor = entryExample.getDescriptorForType().findFieldByName("value");

            if (keyDescriptor == null) {
                throw new NotAvailableException("Field[KEY] does not exist for type " + entryExample.getClass().getName());
            }

            if (valueDescriptor == null) {
                throw new NotAvailableException("Field[VALUE] does not exist for type " + entryExample.getClass().getName());
            }

            final boolean keyIsEnum = keyDescriptor.getJavaType() == JavaType.ENUM;

            // lookup key in map representation
            for (int i = 0; i < mapHolder.getRepeatedFieldCount(mapFieldDescriptor); i++) {
                final Message entryMessage = (Message) mapHolder.getRepeatedField(mapFieldDescriptor, i);

                if (keyIsEnum) {
                    if (((EnumValueDescriptor) entryMessage.getField(keyDescriptor)).getNumber() == ((ProtocolMessageEnum) key).getNumber()) {
                        return entryMessage.getField(valueDescriptor);
                    }
                } else {
                    if (entryMessage.getField(keyDescriptor) == key) {
                        return entryMessage.getField(valueDescriptor);
                    }
                }
            }
            throw new NotAvailableException("Entry for Key[" + key + "]");
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not lookup value for given Key[" + key + "]!", ex);
        }
    }
}
