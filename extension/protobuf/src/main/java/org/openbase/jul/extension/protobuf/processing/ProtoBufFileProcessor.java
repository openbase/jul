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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.googlecode.protobuf.format.JsonFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.processing.FileProcessor;

/**
 * Helper class to serialize and deserialize protobuf objects into json files and vise versa.
 *
 * Example Code:
 * <pre>
 * {@code
 *     ProtoBufFileProcessor<UserConfig, UserConfig, UserConfig.Builder> processor = new ProtoBufFileProcessor<>(UserConfig.newBuilder());
 *     processor.serialize(UserConfig.getDefaultInstance().newBuilderForType().setFirstName("Pink").build(), new File("/tmp/myuser.txt"));
 *     System.out.println("Username: "+processor.deserialize(new File("/tmp/myuser.txt")).getFirstName());
 * }
 * </pre>
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <DT> datatype
 * @param <M> message
 * @param <MB> message builder
 */
public class ProtoBufFileProcessor<DT, M extends AbstractMessage, MB extends M.Builder<MB>> implements FileProcessor<DT> {

    private static final String UTF_8 = "UTF-8";
    private final JsonParser parser;
    private final Gson gson;
    private final JsonFormat jsonFormat;
    private final TypeToMessageTransformer<DT, M, MB> transformer;

    /**
     * Constructor to create a new {@code ProtoBufFileProcessor}
     *
     * @param messageBuilder a builder which can be used to build the message after deserialization.
     */
    public ProtoBufFileProcessor(final MB messageBuilder) {
        this(new SimpleMessageTransformer(messageBuilder));
    }

    public ProtoBufFileProcessor(final TypeToMessageTransformer<DT, M, MB> transformer) {
        this.transformer = transformer;
        this.parser = new JsonParser();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.jsonFormat = new JsonFormat();
    }

    @Override
    public DT deserialize(final File file, final DT data) throws CouldNotPerformException {
        try {
            jsonFormat.merge(new FileInputStream(file), Charset.forName(UTF_8), transformer.transform(data).newBuilderForType());
            return data;
        } catch (IOException ex) {
            throw new CouldNotPerformException("Could not deserialize " + file + " into " + data + "!", ex);
        }
    }

    @Override
    public File serialize(final DT data, final File file) throws CouldNotPerformException {
        try {
            String jsonString = jsonFormat.printToString(transformer.transform(data));

            // format
            JsonElement el = parser.parse(jsonString);
            jsonString = gson.toJson(el);

            //write
            FileUtils.writeStringToFile(file, jsonString, UTF_8);
            return file;
        } catch (IOException | JsonIOException | JsonSyntaxException ex) {
            throw new CouldNotPerformException("Could not serialize " + transformer + " into " + file + "!", ex);
        }
    }

    @Override
    public DT deserialize(File file) throws CouldNotPerformException {
        MB builder = transformer.newBuilderForType();
        try {
            jsonFormat.merge(new FileInputStream(file), Charset.forName(UTF_8), builder);
            return transformer.transform((M) builder.build());
        } catch (IOException | CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not deserialize " + file + " into " + builder + "!", ex);
        }
    }

    public interface TypeToMessageTransformer<T, M extends AbstractMessage, MB extends Builder> {

        Message transform(T type);

        T transform(M message) throws CouldNotTransformException;

        MB newBuilderForType() throws CouldNotPerformException;
    }

    public static class SimpleMessageTransformer<M extends AbstractMessage, MB extends M.Builder> implements TypeToMessageTransformer<M, M, MB> {

        private final MB builder;

        public SimpleMessageTransformer(final MB builder) {
            this.builder = builder;
        }

        @Override
        public MB newBuilderForType() throws CouldNotPerformException {
            return (MB) builder.clone();
        }

        @Override
        public M transform(final M message) {
            return message;
        }
    }
}
