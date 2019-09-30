package org.openbase.jul.processing.json;

/*
 * #%L
 * JUL Processing JSon
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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.processing.FileProcessor;

import java.io.File;
import java.io.IOException;

/**
 * @param <DT>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JSonObjectFileProcessor<DT extends Object> implements FileProcessor<DT> {

    private final ObjectMapper mapper;
    private final JsonFactory jsonFactory;
    private final Class<DT> dataTypeClass;

    public JSonObjectFileProcessor(final Class<DT> dataTypeClass) {
        this.dataTypeClass = dataTypeClass;
        this.jsonFactory = new JsonFactory();
        this.jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false); // disable auto-close of the outputStream
        this.mapper = new ObjectMapper(jsonFactory);
        this.mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator());
        this.mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, JsonTypeInfo.As.WRAPPER_ARRAY);
        this.mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);  // paranoidly repeat ourselves
        this.mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public File serialize(final DT data, final File file) throws CouldNotPerformException {
        try {
            JsonGenerator generator = jsonFactory.createGenerator(file, JsonEncoding.UTF8);
            generator.setPrettyPrinter(new DefaultPrettyPrinter());
            mapper.writeValue(generator, data);
            return file;
        } catch (IOException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not serialize " + data.getClass().getSimpleName() + " into " + file + "!", ex);
        }
    }

    @Override
    public DT deserialize(File file, DT message) throws CouldNotPerformException {
        return deserialize(file);
    }

    @Override
    public DT deserialize(File file) throws CouldNotPerformException {
        return deserialize(file, dataTypeClass);
    }

    public <T extends Object> T deserialize(final File file, final Class<T> clazz) throws CouldNotPerformException {
        try {
            JsonParser parser = jsonFactory.createParser(file);
            return mapper.readValue(parser, clazz);
        } catch (IOException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not deserialize " + clazz.getSimpleName() + " from " + file + "!", ex);
        }
    }
}
