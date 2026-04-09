package org.openbase.jul.audio

import org.openbase.jul.exception.CouldNotPerformException
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

/**
 *
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
class AudioDataImpl : AudioData {
    override val data: ByteArray
    override val format: AudioFormat
    override val dataLength: Long

    constructor(data: ByteArray, format: AudioFormat, dataLength: Long) {
        this.data = data
        this.format = format
        this.dataLength = dataLength
    }

    constructor(soundFile: File) {
        if (!soundFile.exists()) {
            throw CouldNotPerformException("AudioFile is missing!")
        }

        AudioSystem.getAudioInputStream(soundFile).use { ais ->
            this.dataLength = ais.frameLength
            this.format = ais.format
            this.data = ByteArray(ais.frameLength.toInt() * format.frameSize)
            val buf = ByteArray(AudioPlayer.BUFSIZE)
            var i = 0
            while (i < data.size) {
                var r = ais.read(buf, 0, AudioPlayer.BUFSIZE)
                if (i + r >= data.size) {
                    r = data.size - i
                }
                System.arraycopy(buf, 0, data, i, r)
                i += AudioPlayer.BUFSIZE
            }
        }
    }
}
