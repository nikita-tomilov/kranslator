package com.programmer74.kranslator.ocr

import net.coobird.thumbnailator.Thumbnails
import net.sourceforge.tess4j.Tesseract
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import java.io.File
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.imageio.ImageIO


// git clone https://github.com/tesseract-ocr/tessdata /opt/tessdata
// sudo apt install libtesseract*
class LocalTesseract(
  private val dataPath: String = "/opt/tessdata"
) : CharacterRecognizer {

  private val executor = Executors.newFixedThreadPool(4)

  override fun recognize(
    imageFile: File,
    dpi: Int,
    language: OCRLanguage,
    pil: Int
  ): TextBlocks {
    val instance = Tesseract()
    instance.setDatapath(File(dataPath).path)
    instance.setLanguage(language.threeLetterCode)
    instance.setTessVariable("user_defined_dpi", dpi.toString())
    val scaledImage = Thumbnails
      .of(imageFile)
      .scale(2.0)
      .asBufferedImage()
    val sharpenKernel = Kernel(
      3, 3, floatArrayOf(
        0f, -1f, 0f, -1f, 5f, -1f, 0f,
        -1f, 0f
      )
    )
    val sharpenOp: BufferedImageOp = ConvolveOp(sharpenKernel)
    val sharpenedImage = sharpenOp.filter(scaledImage, null)
    //ImageIO.write(sharpenedImage, "png", File(imageFile.absolutePath + "-2.png"))
    return TextBlocks(parsePage(dpi, language, sharpenedImage, pil))
  }

  private fun parsePage(dpi: Int, language: OCRLanguage, image: BufferedImage, pil: Int): List<TextBlock> {
    val segmentationInstance = buildInstance(dpi, language)
    val segmentedRegions: List<Rectangle> = segmentationInstance.getSegmentedRegions(image, pil)
    val maxW = image.width
    val maxH = image.height
    val tasks = (segmentedRegions.indices).map { i ->
      Callable {
        val rect = segmentedRegions[i]
        val subInstance = buildInstance(dpi, language)
        val text = subInstance.doOCR(image, enrichRectangle(rect, maxW, maxH, 3))
        TextBlock(text.trim(), TextBlockRectangle(rect.x, rect.y, rect.width, rect.height))
      }
    }
    val tasksResultFutures = executor.invokeAll(tasks)
    return tasksResultFutures.map { it.get() }
  }

  private fun buildInstance(dpi: Int,
                            language: OCRLanguage): Tesseract {
    val instance = Tesseract()
    instance.setDatapath(File(dataPath).path)
    instance.setLanguage(language.threeLetterCode)
    instance.setTessVariable("user_defined_dpi", dpi.toString())
    return instance
  }

  private fun enrichRectangle(original: Rectangle, maxW: Int, maxH: Int, delta: Int): Rectangle {
    val x = max(0, original.x - delta)
    val y = max(0, original.y - delta)
    val w = min(maxW, original.width + delta)
    val h = min(maxH, original.height + delta)
    return Rectangle(x, y, w, h)
  }
}
