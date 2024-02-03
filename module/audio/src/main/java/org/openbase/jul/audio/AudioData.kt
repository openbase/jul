package org.openbase.jul.audio

import javax.sound.sampled.AudioFormat

/**
 *
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
interface AudioData : AudioSource {
    val data: ByteArray

    val format: AudioFormat

    val dataLength: Long
}
