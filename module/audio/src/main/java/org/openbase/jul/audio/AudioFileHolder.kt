package org.openbase.jul.audio

import java.io.File

/**
 *
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
interface AudioFileHolder : AudioSource {
    val file: File
}
