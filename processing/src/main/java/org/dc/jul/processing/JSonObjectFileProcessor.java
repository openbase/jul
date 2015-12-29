/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.processing;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.dc.jul.exception.CouldNotPerformException;
import java.io.File;

/**
 *
 * @author mpohling
 * @param <DT>
 */
public class JSonObjectFileProcessor<DT extends Object> implements FileProcessor<DT>  {

    private final ObjectMapper mapper;
    private final JsonFactory jsonFactory;
    private final Class<DT> dataTypeClass;

    public JSonObjectFileProcessor(final Class<DT> dataTypeClass) {
        this.dataTypeClass = dataTypeClass;
        this.jsonFactory = new JsonFactory();
        this.jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false); // disable auto-close of the outputStream
        this.mapper = new ObjectMapper(jsonFactory);
        this.mapper.enableDefaultTyping(); // default to using DefaultTyping.OBJECT_AND_NON_CONCRETE
        this.mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not serialize " + clazz.getSimpleName() + " into " + file + "!", ex);
        }
    }
}
