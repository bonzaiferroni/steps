package ponder.steps.server.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.sound.sampled.*

fun pcmToWav(
    pcm: ByteArray,
    inputSampleRate: Float = 24000f,
    bitsPerSample: Int = 16,
    channels: Int = 1
): ByteArray {
    val bytesPerSample = bitsPerSample / 8
    val frameSize      = bytesPerSample * channels
    val frameCount     = pcm.size / frameSize

    // Original PCM stream
    val srcFormat = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        inputSampleRate,
        bitsPerSample,
        channels,
        frameSize,
        inputSampleRate,
        false
    )
    val pcmStream = AudioInputStream(
        ByteArrayInputStream(pcm),
        srcFormat,
        frameCount.toLong()
    )

    // Target 16 kHz WAV format
    val targetSampleRate = 16000f
    val tgtFormat = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        targetSampleRate,
        bitsPerSample,
        channels,
        frameSize,
        targetSampleRate,
        false
    )
    val converted = AudioSystem.getAudioInputStream(tgtFormat, pcmStream)

    // Write out as WAV
    val out = ByteArrayOutputStream()
    AudioSystem.write(converted, AudioFileFormat.Type.WAVE, out)
    return out.toByteArray()
}