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

    // 1) Original PCM AIS
    val srcFormat = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        inputSampleRate,
        bitsPerSample,
        channels,
        frameSize,
        inputSampleRate,
        false
    )
    val srcAis = AudioInputStream(
        ByteArrayInputStream(pcm),
        srcFormat,
        (pcm.size / frameSize).toLong()
    )

    // 2) Down‐sample to 16 kHz
    val tgtSampleRate = 16000f
    val tgtFormat = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        tgtSampleRate,
        bitsPerSample,
        channels,
        frameSize,
        tgtSampleRate,
        false
    )
    val convAis = AudioSystem.getAudioInputStream(tgtFormat, srcAis)

    // 3) Drain into a byte[] so we know its length
    val buf = ByteArray(4096)
    val baTmp = ByteArrayOutputStream()
    var r = convAis.read(buf)
    while (r > 0) {
        baTmp.write(buf, 0, r)
        r = convAis.read(buf)
    }
    val audioBytes = baTmp.toByteArray()
    val frameCount = audioBytes.size / frameSize

    // 4) Wrap with known length and write WAV
    val finalAis = AudioInputStream(
        ByteArrayInputStream(audioBytes),
        tgtFormat,
        frameCount.toLong()
    )
    val out = ByteArrayOutputStream()
    AudioSystem.write(finalAis, AudioFileFormat.Type.WAVE, out)
    return out.toByteArray()
}