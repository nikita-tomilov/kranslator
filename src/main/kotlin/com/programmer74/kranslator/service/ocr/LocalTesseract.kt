package com.programmer74.kranslator.service.ocr

import com.programmer74.kranslator.service.PDFConverter
import net.sourceforge.tess4j.ITessAPI
import net.sourceforge.tess4j.Tesseract
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

// git clone https://github.com/tesseract-ocr/tessdata /opt/tessdata
// sudo apt install libtesseract*
class LocalTesseract(
  private val dataPath: String = "/opt/tessdata",
  private val dpi: Int = PDFConverter.DPI
) : CharacterRecognizer {

  override fun recognize(imageFile: File, language: OCRLanguage): List<TextBlock> {
    val instance = Tesseract()
    instance.setDatapath(File(dataPath).path)
    instance.setLanguage(language.threeLetterCode)
    instance.setTessVariable("user_defined_dpi", dpi.toString())
    return parsePage(instance, imageFile)
  }

    private fun parsePage(instance: Tesseract, image: File): List<TextBlock> {
        val bi: BufferedImage = ImageIO.read(image)
        val level: Int = ITessAPI.TessPageIteratorLevel.RIL_PARA
        val result: List<Rectangle> = instance.getSegmentedRegions(bi, level)
        val ans = ArrayList<TextBlock>()
        for (i in result.indices) {
            val rect = result[i]
            val text = instance.doOCR(image, rect)
            //ans.add(BoxWithText(text.replace("\n", ""), rect))
            ans.add(TextBlock(text, rect))
        }
        return ans
    }
}
