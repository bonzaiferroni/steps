package ponder.steps.server.utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.IIOImage

fun writePngWithCompression(
    imageBytes: ByteArray,
    outputPath: String,
    quality: Float // 0.0 (fastest, least compressed) up to 1.0 (slowest, most compressed)
) {
    // Turn yer byte array into a BufferedImage
    val image: BufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))

    val writer = ImageIO.getImageWritersByFormatName("png").next()
    val ios = ImageIO.createImageOutputStream(File(outputPath))
    writer.output = ios

    val param = writer.defaultWriteParam
    if (param.canWriteCompressed()) {
        param.compressionMode = ImageWriteParam.MODE_EXPLICIT
        param.compressionQuality = quality.coerceIn(0f, 1f)
    }

    writer.write(null, IIOImage(image, null, null), param)
    ios.close()
    writer.dispose()
}

fun compressPngsInFolder(folderPath: String, quality: Float) {
    val folder = File(folderPath)
    println("compressing $folderPath")
    if (!folder.isDirectory) return

    folder.listFiles { f -> f.isFile && f.extension.equals("png", ignoreCase = true) }?.forEach { file ->
        println("compressing ${file.name}")
        val imageBytes = file.readBytes()
        val nameWithoutExt = file.nameWithoutExtension
        val outputPath = "${file.parent}${File.separator}$nameWithoutExt-min.png"
        writePngThumbnail(imageBytes, outputPath)
    }
}

fun writePngThumbnail(imageBytes: ByteArray, outputPath: String, newWidth: Int = 128) {
    val original: BufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
    val newHeight = (original.height * newWidth) / original.width
    val scaled: Image = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
    val thumbnail = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB).apply {
        createGraphics().drawImage(scaled, 0, 0, null)
    }
    ImageIO.write(thumbnail, "png", File(outputPath))
}