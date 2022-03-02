package org.openbase.jul.audio;

/*-
 * #%L
 * JUL Audio
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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

import static org.openbase.jul.audio.AudioPlayer.BUFSIZE;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class AudioDataImpl implements AudioData {

	private final byte[] data;
	private final AudioFormat format;
	private final long dataLenght;

	public AudioDataImpl(final byte[] data, final AudioFormat format, final long dataLenght) {
		this.data = data;
		this.format = format;
		this.dataLenght = dataLenght;
	}

	public AudioDataImpl(final File soundFile) throws CouldNotPerformException, UnsupportedAudioFileException, IOException {
		if (!soundFile.exists()) {
			throw new CouldNotPerformException("AudioFile is missing!");
		}

        try (AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile)) {
            this.dataLenght=  ais.getFrameLength();
            this.format = ais.getFormat();
            this.data = new byte[(int) ais.getFrameLength() * format.getFrameSize()];
            final byte[] buf = new byte[BUFSIZE];
            for (int i = 0; i < data.length; i += BUFSIZE) {
                int r = ais.read(buf, 0, BUFSIZE);
                if (i + r >= data.length) {
                    r = data.length - i;
                }
                System.arraycopy(buf, 0, data, i, r);
            }
        }
	}


	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public AudioFormat getFormat() {
		return format;
	}

	@Override
	public long getDataLenght() {
		return dataLenght;
	}
}
