package org.openbase.jul.extension.protobuf.processing;

/*
 * #%L
 * JUL Extension Protobuf
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message.Builder;
import com.googlecode.protobuf.format.JsonFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.processing.FileProcessor;

/**
 *
 * @author mpohling
 * @param <DT> datatype
 * @param <M> message
 * @param <MB> message builder
 */
public class ProtoBufFileProcessor<DT, M extends GeneratedMessage, MB extends M.Builder<MB>> implements FileProcessor<DT> {

    private static final String UTF_8 = "UTF-8";
    private final JsonParser parser;
    private final Gson gson;
    private final JsonFormat jsonFormat;
    private final TypeToMessageTransformer<DT, M, MB> transformer;

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
            throw new CouldNotPerformException("Could not deserialize " + file + " inot " + data + "!", ex);
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

    public static interface TypeToMessageTransformer<T, M extends GeneratedMessage, MB extends Builder> {

        public GeneratedMessage transform(T type);

        public T transform(M message) throws CouldNotTransformException;

        public MB newBuilderForType() throws CouldNotPerformException;
    }
}
