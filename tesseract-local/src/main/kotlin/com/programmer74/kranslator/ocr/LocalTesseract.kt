package com.programmer74.kranslator.ocr

import net.sourceforge.tess4j.Tesseract
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

// git clone https://github.com/tesseract-ocr/tessdata /opt/tessdata
// sudo apt install libtesseract*
class LocalTesseract(
  private val dataPath: String = "/opt/tessdata"
) : CharacterRecognizer {

  override fun recognize(
    imageFile: File,
    dpi: Int,
    language: OCRLanguage,
    pil: Int
  ): List<TextBlock> {
    val instance = Tesseract()
    instance.setDatapath(File(dataPath).path)
    instance.setLanguage(language.threeLetterCode)
    instance.setTessVariable("user_defined_dpi", dpi.toString())
    return parsePage(instance, imageFile, pil)
  }

  private fun parsePage(instance: Tesseract, image: File, pil: Int): List<TextBlock> {
    val bi: BufferedImage = ImageIO.read(image)
    val result: List<Rectangle> = instance.getSegmentedRegions(bi, pil)
    val ans = ArrayList<TextBlock>()
    for (i in result.indices) {
      val rect = result[i]
      val text = instance.doOCR(image, rect)
      ans.add(TextBlock(text, TextBlockRectangle(rect.x, rect.y, rect.width, rect.height)))
    }
    return ans
  }
}
