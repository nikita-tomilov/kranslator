package com.programmer74.kranslator.service

import com.lowagie.text.Document
import com.lowagie.text.RectangleReadOnly
import com.lowagie.text.pdf.PdfWriter
import com.programmer74.kranslator.service.ocr.LocalTesseract
import com.programmer74.kranslator.api.ocr.OCRLanguage
import com.programmer74.kranslator.service.translation.LibreTranslate
import com.programmer74.kranslator.api.translate.TranslatorLanguage
import mu.KLogging
import org.junit.jupiter.api.Test
import java.awt.*
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

class Experiments {

  @Test
  fun foo() {
    val source = File("/home/nt/pdfexp/test.pdf")
    val pagesPNG = PDFConverter.convertPDFToImages(source)
    val ocr = LocalTesseract()
    val translator = LibreTranslate()

    val translatedPagesPNG = pagesPNG.map { page ->
      val textBlocks = ocr.recognize(page, OCRLanguage.DE)
      val renderer = PageRenderer(page)
      val g = renderer.graphics

      textBlocks.forEach {
        val translated =
            translator.translate(it.text, TranslatorLanguage.DE, TranslatorLanguage.EN_US)
        renderTextBlock(translated, it.block, g)
      }

      val translatedPage = File(page.absolutePath + "-translated.png")
      renderer.render(translatedPage)
      Files.deleteIfExists(page.toPath())
      translatedPage
    }

    val target = File("/home/nt/pdfexp/test-translated.pdf")
    val document = Document(RectangleReadOnly(4960.0f, 7016.0f), 0.0f, 0.0f, 0.0f, 0.0f)
    val outputStream = FileOutputStream(target)

    PdfWriter.getInstance(document, outputStream)
    document.open()
    translatedPagesPNG.forEach { imageFile ->
      document.newPage()
      document.add(com.lowagie.text.Image.getInstance(imageFile.absolutePath))
      Files.delete(imageFile.toPath())
    }
    document.close()
  }

  private fun renderTextBlock(text: String, block: Rectangle, g: Graphics2D) {
    g.color = Color.WHITE
    g.fillRect(block.x, block.y, block.width, block.height)

    val origStroke = g.stroke
    g.color = Color.RED
    g.stroke = BasicStroke(3.0f)
    g.drawRect(block.x, block.y, block.width, block.height)

    g.color = Color.BLACK
    g.stroke = origStroke
    drawTextToFit(text, g, block)
  }

  //https://stackoverflow.com/questions/12485236/finding-maximum-font-size-to-fit-in-region-java
  private fun drawTextToFit(text: String, g: Graphics2D, maxRect: Rectangle) {
    val lines = text.split("\n")
    val linesCount = lines.count()

    val lineHeight = maxRect.height / linesCount
    val lineWidth = maxRect.width

    val longestLine = lines.maxByOrNull { it.length } ?: lines.first()

    g.font = Font("TimesRoman", Font.PLAIN, 20)
    g.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    )
    logger.info { "drawing text block of size ${text.length} to ${maxRect.x};${maxRect.y}" }

    val maxFontSize = getMaxFittingFontSize(g, g.font, longestLine, lineWidth, lineHeight)
    logger.info { "drawing text of size ${text.length} to ${maxRect.x};${maxRect.y}; computed size: $maxFontSize" }

    g.font = Font("TimesRoman", Font.PLAIN, maxFontSize)

    val x = maxRect.x
    var y = maxRect.y + g.fontMetrics.height
    lines.forEach { line ->
      g.drawString(line, x, y)
      y += g.fontMetrics.height
    }
  }

  companion object : KLogging()

  private fun getMaxFittingFontSize(
    g: Graphics,
    font: Font,
    string: String?,
    width: Int,
    height: Int
  ): Int {
    var minSize = 0
    var maxSize = 288
    var curSize = font.size
    while (maxSize - minSize > 2) {
      val fm = g.getFontMetrics(Font(font.name, font.style, curSize))
      val fontWidth = fm.stringWidth(string)
      val fontHeight = fm.leading + fm.maxAscent + fm.maxDescent
      if ((fontWidth > width) || (fontHeight > height)) {
        maxSize = curSize
        curSize = (maxSize + minSize) / 2
      } else {
        minSize = curSize
        curSize = (minSize + maxSize) / 2
      }
    }
    return curSize
  }
}
