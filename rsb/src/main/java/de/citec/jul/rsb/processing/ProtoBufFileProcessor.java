/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb.processing;

import com.google.protobuf.GeneratedMessage;
import com.googlecode.protobuf.format.JsonFormat;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.processing.FileProcessor;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author mpohling
 * @param <M>
 */
public class ProtoBufFileProcessor<M> implements FileProcessor<M> {

    private final TypeToMessageTransformer<M> transformer;
    
    public ProtoBufFileProcessor(final TypeToMessageTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public M deserialize(final File file, final M data) throws CouldNotPerformException {
        try {
            JsonFormat.merge(FileUtils.readFileToString(file), transformer.transform(data).newBuilderForType());
            return data;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not deserialize " + file + " into " + data + "!", ex);
        }
    }

    @Override
    public File serialize(final M data, final File file) throws CouldNotPerformException {
        try {
            FileUtils.writeStringToFile(file, JsonFormat.printToString(transformer.transform(data)));
            return file;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not serialize " + transformer + " into " + file + "!", ex);
        }
    }
    
    public interface TypeToMessageTransformer<T> {
        public GeneratedMessage transform(T type);
    }
}
