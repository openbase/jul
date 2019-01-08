package org.openbase.jul.extension.protobuf.processing;

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
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.MessageOrBuilder;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.iface.provider.LabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ProtoBufFieldProcessor {


    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoBufFieldProcessor.class);

    //TODO release: Create ProtoBufFieldProcessor and move some methods to there and to ProtoBufBuilderProcessor

    //TODO: write java doc for all methods
    public static Descriptors.FieldDescriptor getFieldDescriptor(final MessageOrBuilder builder, final int fieldNumber) {
        return builder.getDescriptorForType().findFieldByNumber(fieldNumber);
    }

    public static FieldDescriptor[] getFieldDescriptors(final Class<? extends Message> messageClass, final int... fieldNumbers) throws CouldNotPerformException {
        try {
            FieldDescriptor[] fieldDescriptors = new FieldDescriptor[fieldNumbers.length];
            Message defaultMessage = (Message) messageClass.getMethod("getDefaultInstance").invoke(null);
            for (int i = 0; i < fieldNumbers.length; i++) {
                fieldDescriptors[i] = getFieldDescriptor(defaultMessage, fieldNumbers[i]);
            }
            return fieldDescriptors;
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            throw new CouldNotPerformException("Could not detect field descriptors!", ex);
        }
    }

    public static FieldDescriptor getFieldDescriptor(final Class<? extends Message> messageClass, final int fieldNumber) throws CouldNotPerformException {
        try {
            return getFieldDescriptor((Message) messageClass.getMethod("getDefaultInstance").invoke(null), fieldNumber);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            throw new CouldNotPerformException("Could not detect field descriptor!", ex);
        }
    }

    public static Descriptors.FieldDescriptor getFieldDescriptor(final MessageOrBuilder msg, final String fieldName) throws NotAvailableException {
        final FieldDescriptor descriptor = msg.getDescriptorForType().findFieldByName(fieldName);
        if(descriptor == null) {
            throw new NotAvailableException("Field", fieldName);
        }
        return descriptor;
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

    /**
     * @param msg
     *
     * @return
     *
     * @throws CouldNotPerformException
     * @deprecated cannot be used in its current form because the label is now an openbase type and not a string
     */
    @Deprecated
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
            try{
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
            } catch (NotAvailableException ex) {
                ExceptionPrinter.printHistory("Could not init field!", ex, LOGGER, LogLevel.WARN);
            }
        }
        return builder;
    }

    public static void clearRequiredFields(final Message.Builder builder) {
        builder.findInitializationErrors().forEach((initError) -> clearRequiredField(builder, initError));
    }

    public static Message.Builder clearRequiredField(final Message.Builder builder, final String fieldPath) {
        Descriptors.FieldDescriptor fieldDescriptor = null;
        Message.Builder lastBuilder = builder;
        Message.Builder tmpBuilder = builder;

        final String[] fields = fieldPath.split("\\.");
        for (int i = 0; i < fields.length; ++i) {
            try {
                if (fields[i].endsWith("]")) {
                    // handle repeated field
                    final String fieldName = fields[i].split("\\[")[0];
                    // extract position
                    int number = Integer.parseInt(fields[i].split("\\[")[1].split("\\]")[0]);
                    // get field descriptor
                    fieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(tmpBuilder, fieldName);

                    // extract builder at the given position
                    final Message.Builder repeatedBuilder = ((Message) tmpBuilder.getRepeatedField(fieldDescriptor, number)).toBuilder();
                    // create field path to uninitialized field relative to extracted builder
                    final StringBuilder subPath = new StringBuilder(fields[i + 1]);
                    for (int j = i + 2; j < fields.length; ++j) {
                        subPath.append(".").append(fields[j]);
                    }
                    // clear required field in extracted builder and place it at its old position
                    tmpBuilder.setRepeatedField(fieldDescriptor, number, clearRequiredField(repeatedBuilder, subPath.toString()).buildPartial());
                    // return now cleared builder
                    return builder;
                } else {
                    // extract next field
                    final FieldDescriptor tmpFieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(tmpBuilder, fields[i]);
                    if (tmpFieldDescriptor.isRequired()) {
                        // if it is required clear the last optional field
                        if (i == 0) {
                            lastBuilder.clearField(tmpFieldDescriptor);
                        } else {
                            // clear last optional field and return
                            lastBuilder.clearField(fieldDescriptor);
                            return builder;
                        }
                        break;
                    } else {
                        fieldDescriptor = tmpFieldDescriptor;
                        lastBuilder = tmpBuilder;
                        tmpBuilder = tmpBuilder.getFieldBuilder(fieldDescriptor);
                    }
                }
            } catch (NotAvailableException ex) {
                ExceptionPrinter.printHistory("Could not clear field!", ex, LOGGER, LogLevel.WARN);
            }
        }
        return builder;
    }

    public enum BuilderInitializationStatus {

        ALL_REQUIRED_FIELDS_SET,
        NO_REQUIRED_FIELDS_SET,
        SOME_REQUIRED_FIELDS_SET
    }

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
    public static void putMapEntry(final Message entryMessage, FieldDescriptor mapFieldDescriptor, Message.Builder mapHolder) throws CouldNotPerformException {
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
     * @throws NotAvailableException is thrown if the entry could not be resolved.
     */
    public static Object getMapEntry(final Object key, FieldDescriptor mapFieldDescriptor, MessageOrBuilder mapHolder) throws NotAvailableException {
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

            // lookup key in map representation
            for (int i = 0; i < mapHolder.getRepeatedFieldCount(mapFieldDescriptor); i++) {
                final Message entryMessage = (Message) mapHolder.getRepeatedField(mapFieldDescriptor, i);

                if (entryMessage.getField(keyDescriptor).equals(key)) {
                    return entryMessage.getField(valueDescriptor);
                }
            }
            throw new InvalidStateException("Key is unknown!");
        } catch (Exception ex) {
            throw new NotAvailableException("Entry of Key", key, ex);
        }
    }

    /**
     * Extracts the repeated field values out of the given message or builder instance.
     * @param repeatedFieldName the name of the repeated field.
     * @param repeatedFieldProvider the message holding the repeated field.
     * @return a list of values of the repeated field.
     * @throws NotAvailableException is thrown if the repeated field is not available.
     */
    public static ArrayList getRepeatedFieldList(final String repeatedFieldName, MessageOrBuilder repeatedFieldProvider) throws NotAvailableException {
        return getRepeatedFieldList(ProtoBufFieldProcessor.getFieldDescriptor(repeatedFieldProvider, repeatedFieldName), repeatedFieldProvider);
    }

    /**
     * Extracts the repeated field values out of the given message or builder instance.
     * @param repeatedFieldDescriptor the descriptor of the repeated field.
     * @param repeatedFieldProvider the message holding the repeated field.
     * @return a list of values of the repeated field.
     */
    public static ArrayList getRepeatedFieldList(final FieldDescriptor repeatedFieldDescriptor, MessageOrBuilder repeatedFieldProvider) {
        final int repeatedFieldCount = repeatedFieldProvider.getRepeatedFieldCount(repeatedFieldDescriptor);
        final ArrayList list = new ArrayList(repeatedFieldCount);
        for (int i = 0; i < repeatedFieldCount; i++) {
            final Object repeatedField = repeatedFieldProvider.getRepeatedField(repeatedFieldDescriptor, i);
            list.add(repeatedField);
        }
        return list;
    }
}
