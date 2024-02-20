package org.openbase.jul.audio

import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.printer.ExceptionPrinter
import org.openbase.jul.exception.printer.LogLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import javax.sound.sampled.*
import kotlin.concurrent.withLock

/**
 *
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
class AudioPlayer @JvmOverloads constructor(soundChannels: Int = 10) {
    private val executorService: ExecutorService = Executors.newFixedThreadPool(soundChannels) { runnable: Runnable ->
        Executors.defaultThreadFactory().newThread(runnable)
            .apply {
                isDaemon = true
                priority = 9
            }
    }

    @JvmOverloads
    fun playAudio(source: AudioSource, wait: Boolean = false): Boolean {
        try {
            if (wait) {
                play(source)
                return true
            }
            executorService.execute {
                try {
                    play(source)
                } catch (ex: IOException) {
                    ExceptionPrinter.printHistory(
                        CouldNotPerformException("Could not play clip!", ex),
                        logger,
                        LogLevel.WARN
                    )
                } catch (ex: UnsupportedAudioFileException) {
                    ExceptionPrinter.printHistory(
                        CouldNotPerformException("Could not play clip!", ex),
                        logger,
                        LogLevel.WARN
                    )
                } catch (ex: LineUnavailableException) {
                    ExceptionPrinter.printHistory(
                        CouldNotPerformException("Could not play clip!", ex),
                        logger,
                        LogLevel.WARN
                    )
                }
            }
        } catch (ex: IOException) {
            ExceptionPrinter.printHistory(CouldNotPerformException("Could not play clip!", ex), logger, LogLevel.WARN)
            return false
        } catch (ex: UnsupportedAudioFileException) {
            ExceptionPrinter.printHistory(CouldNotPerformException("Could not play clip!", ex), logger, LogLevel.WARN)
            return false
        } catch (ex: LineUnavailableException) {
            ExceptionPrinter.printHistory(CouldNotPerformException("Could not play clip!", ex), logger, LogLevel.WARN)
            return false
        }
        return true
    }

    companion object {
        const val BUFSIZE: Int = 512
        private val logger: Logger = LoggerFactory.getLogger(AudioPlayer::class.java)

        val lock = ReentrantLock()
        val condition: Condition = lock.newCondition()

        @Throws(
            IOException::class,
            UnsupportedAudioFileException::class,
            LineUnavailableException::class,
            InterruptedException::class
        )
        private fun play(source: AudioSource) = when (source) {
            is AudioData -> play(source)
            is AudioFileHolder -> play(source.file)
            else -> {
                logger.warn("Unknown audio source! Skip clip ...")
            }
        }

        @Throws(
            IOException::class,
            UnsupportedAudioFileException::class,
            LineUnavailableException::class,
            InterruptedException::class
        )
        private fun play(audioData: AudioData) = AudioInputStream(
            ByteArrayInputStream(audioData.data),
            audioData.format,
            audioData.dataLength
        ).also { play(it) }

        @Throws(
            IOException::class,
            UnsupportedAudioFileException::class,
            LineUnavailableException::class,
            InterruptedException::class
        )
        private fun play(clipFile: File?) = play(AudioSystem.getAudioInputStream(clipFile))

        @Throws(
            IOException::class,
            UnsupportedAudioFileException::class,
            LineUnavailableException::class,
            InterruptedException::class
        )
        private fun play(audioInputStream: AudioInputStream) {
            class AudioListener : LineListener {
                private var done = false

                override fun update(event: LineEvent) {
                    lock.withLock {
                        val eventType = event.type
                        if (eventType === LineEvent.Type.STOP || eventType === LineEvent.Type.CLOSE) {
                            done = true
                            condition.signalAll()
                        }
                    }
                }

                @Throws(InterruptedException::class)
                fun waitUntilDone() = lock.withLock {
                    while (!done) {
                        condition.await()
                    }
                }
            }

            AudioListener().also { listener ->
                audioInputStream.use {
                    val clip = AudioSystem.getClip()
                    clip.addLineListener(listener)
                    clip.open(audioInputStream)
                    clip.use {
                        clip.start()
                        listener.waitUntilDone()
                    }
                }
            }
        }
    }
}
