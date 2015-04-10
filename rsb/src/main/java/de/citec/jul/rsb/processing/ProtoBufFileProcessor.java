/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb.processing;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message.Builder;
import com.googlecode.protobuf.format.JsonFormat;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.CouldNotTransformException;
import de.citec.jul.processing.FileProcessor;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author mpohling
 * @param <DT> datatype
 * @param <M> message
 * @param <MB> message builder
 */
public class ProtoBufFileProcessor<DT, M extends GeneratedMessage, MB extends M.Builder<MB>> implements FileProcessor<DT> {

    private final TypeToMessageTransformer<DT, M, MB> transformer;
    
    public ProtoBufFileProcessor(final TypeToMessageTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public DT deserialize(final File file, final DT data) throws CouldNotPerformException {
        try {
            JsonFormat.merge(FileUtils.readFileToString(file), transformer.transform(data).newBuilderForType());
            return data;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not deserialize " + file + " into " + data + "!", ex);
        }
    }

    @Override
    public File serialize(final DT data, final File file) throws CouldNotPerformException {
        try {
            FileUtils.writeStringToFile(file, JsonFormat.printToString(transformer.transform(data)));
            return file;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not serialize " + transformer + " into " + file + "!", ex);
        }
    }

    @Override
    public DT deserialize(File file) throws CouldNotPerformException {
        MB builder = transformer.newBuilderForType();
        try {
            JsonFormat.merge(FileUtils.readFileToString(file), builder);
            return transformer.transform((M)builder.build());
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not deserialize " + file + " into " + builder + "!", ex);
        }
    }
    
    public static interface TypeToMessageTransformer<T, M extends GeneratedMessage, MB extends Builder> {
        public GeneratedMessage transform(T type);
        public T transform(M message) throws CouldNotTransformException;
        public MB newBuilderForType() throws CouldNotPerformException;
    }
}
