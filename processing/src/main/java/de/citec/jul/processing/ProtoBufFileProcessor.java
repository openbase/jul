/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.processing;

import com.google.protobuf.GeneratedMessage;
import com.googlecode.protobuf.format.JsonFormat;
import de.citec.jul.exception.CouldNotPerformException;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author mpohling
 * @param <M>
 */
public class ProtoBufFileProcessor<M extends GeneratedMessage> implements FileProcessor<M> {

    public ProtoBufFileProcessor() {
    }

    @Override
    public M deserialize(final File file, final M message) throws CouldNotPerformException {
        try {
            JsonFormat.merge(FileUtils.readFileToString(file), message.newBuilderForType());
            return message;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not deserialize " + file + " into " + message + "!", ex);
        }
    }

    @Override
    public File serialize(final M message, final File file) throws CouldNotPerformException {
        try {
            FileUtils.writeStringToFile(file, JsonFormat.printToString(message));
            return file;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not serialize " + message + " into " + file + "!", ex);
        }
    }
}
