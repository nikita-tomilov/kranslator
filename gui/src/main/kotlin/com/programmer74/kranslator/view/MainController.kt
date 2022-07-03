package com.programmer74.kranslator.view

import com.lowagie.text.Document
import com.lowagie.text.RectangleReadOnly
import com.lowagie.text.pdf.PdfWriter
import com.programmer74.kranslator.ocr.*
import com.programmer74.kranslator.service.graphics.ImageUtils
import com.programmer74.kranslator.service.ocr.RemoteTesseractKtor
import com.programmer74.kranslator.service.pdf.PDFConverter
import com.programmer74.kranslator.service.translation.LibreTranslate
import com.programmer74.kranslator.translate.Translator
import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import javafx.application.Platform
import mu.KLogging
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.concurrent.Executors
import javax.imageio.ImageIO

class MainController {

  private val translatorInstance: Translator = LibreTranslate()
  private val ocrInstance: CharacterRecognizer = RemoteTesseractKtor()
  private val ex = Executors.newSingleThreadExecutor()

  fun languages() = translatorInstance.availableLanguages().toList()

  fun translate(requests: List<TranslatorRequest>, callback: (List<String>) -> Unit) {
    ex.submit {
      val results = requests.map { translatorInstance.translate(it) }
      Platform.runLater {
        callback(results)
      }
    }
  }

  fun translate(
    pdf: File,
    fromLanguage: TranslatorLanguage,
    toLanguage: TranslatorLanguage,
    progressCallback: (String) -> Unit,
    ocrBatchCallback: (List<TextBlock>) -> Unit,
    translateBatchCallback: (List<String>) -> Unit,
    resultCallback: (File) -> Unit
  ) {
    ex.submit {
      translatePDF(
          pdf,
          fromLanguage,
          toLanguage,
          progressCallback,
          ocrBatchCallback,
          translateBatchCallback,
          resultCallback)
    }
  }

  fun pasteImageFromClipboard(success: (BufferedImage) -> Unit, error: (String) -> Unit) {
    ex.submit {
      try {
        val clipboardImage = ImageUtils.pasteImageFromClipboard()
        if (clipboardImage != null) {
          Platform.runLater {
            success(clipboardImage)
          }
        } else {
          Platform.runLater {
            error("Unable to get image from the clipboard")
          }
        }
      } catch (e: Exception) {
        Platform.runLater {
          error(e.message ?: e.toString())
        }
      }
    }
  }

  fun ocr(image: BufferedImage, language: TranslatorLanguage, callback: (List<TextBlock>) -> Unit) {
    ex.submit {
      val tmp = Files.createTempFile("img", ".png").toFile()
      ImageIO.write(image, "png", tmp)
      val results = ocr(tmp, language)
      Platform.runLater {
        callback(results)
      }
    }
  }

  private fun ocr(imageFile: File, language: TranslatorLanguage): List<TextBlock> {
    return ocrInstance.recognize(imageFile, 0, language.toOCRLanguage(), RIL_PARA).blocks
  }

  fun imprintTranslateResponseToImage(
    image: BufferedImage,
    ocrBlocks: List<TextBlock>,
    translatedTexts: List<String>,
    callback: (BufferedImage) -> Unit
  ) {
    ex.submit {
      val printedImage = imprintTranslateResponseToImage(image.deepCopy(), ocrBlocks, translatedTexts)
      Platform.runLater {
        callback(printedImage)
      }
    }
  }

  private fun imprintTranslateResponseToImage(
    target: BufferedImage,
    ocrBlocks: List<TextBlock>,
    translatedTexts: List<String>
  ): BufferedImage {
      val g = target.createGraphics()
      assert(ocrBlocks.size == translatedTexts.size)
      (ocrBlocks.indices).forEach { i ->
        val block = ocrBlocks[i].block
        val text = translatedTexts[i]
        renderTextBlock(text, block, g)
      }
    return target
  }

  private fun translatePDF(
    pdf: File,
    fromLanguage: TranslatorLanguage,
    toLanguage: TranslatorLanguage,
    progressCallback: (String) -> Unit,
    ocrBatchCallback: (List<TextBlock>) -> Unit,
    translateBatchCallback: (List<String>) -> Unit,
    resultCallback: (File) -> Unit
  ) {
    log(progressCallback, "Attempting to translate file ${pdf.absolutePath}...")
    val pagesPNG = PDFConverter.convertPDFToImages(pdf) { log(progressCallback, it) }
    val n = pagesPNG.size
    val translatedPagesPNG = pagesPNG.mapIndexed { index, pagePNG ->
      val page = index + 1

      log(progressCallback, "$page/$n Performing OCR")
      val textBlocks = ocr(pagePNG, fromLanguage)
      Platform.runLater { ocrBatchCallback.invoke(textBlocks) }

      log(progressCallback, "$page/$n Performing translation")
      val translatedTexts = textBlocks.map { textBlock ->
        val translatedText = translatorInstance.translate(TranslatorRequest(textBlock.text, fromLanguage, toLanguage))
        Platform.runLater { translateBatchCallback.invoke(listOf(translatedText)) }
        Thread.sleep(100)
        translatedText
      }

      log(progressCallback, "$page/$n Converting back to image...")
      val originalImage = ImageIO.read(pagePNG)
      val translatedPagePNG = File(pagePNG.absolutePath + "-t.png")
      val imprintedImage = imprintTranslateResponseToImage(originalImage, textBlocks, translatedTexts)
      ImageIO.write(imprintedImage, "PNG", translatedPagePNG)
      translatedPagePNG
    }

    log(progressCallback, "Attempting to reassemble the original PDF...")
    val target = File(pdf.absolutePath + "-translated.pdf")
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
    Platform.runLater { resultCallback.invoke(target) }
  }

  private fun log(progressCallback: (String) -> Unit, s: String) {
    Platform.runLater { progressCallback(s) }
  }

  private fun renderTextBlock(text: String, block: TextBlockRectangle, g: Graphics2D) {
    g.color = Color.WHITE
    g.fillRect(block.x, block.y, block.w, block.h)

    val origStroke = g.stroke
    g.color = Color.RED
    g.stroke = BasicStroke(1.0f)
    g.drawRect(block.x, block.y, block.w, block.h)

    g.color = Color.BLACK
    g.stroke = origStroke
    drawTextToFit(text, g, block)
  }

  //https://stackoverflow.com/questions/12485236/finding-maximum-font-size-to-fit-in-region-java
  private fun drawTextToFit(text: String, g: Graphics2D, maxRect: TextBlockRectangle) {
    val lines = text.split("\n")
    val linesCount = lines.count()

    val lineHeight = maxRect.h / linesCount
    val lineWidth = maxRect.w

    val longestLine = lines.maxByOrNull { it.length } ?: lines.first()

    g.font = Font("TimesRoman", Font.PLAIN, 16)
    g.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    )
    logger.info { "drawing text block of size ${text.length} to ${maxRect.x};${maxRect.y}" }

    val maxFontSize = getMaxFittingFontSize(g, g.font, longestLine, lineWidth, lineHeight)
    logger.info { "drawing text of size ${text.length} to ${maxRect.x};${maxRect.y};${maxRect.w};${maxRect.h}; computed size: $maxFontSize" }

    g.font = Font("TimesRoman", Font.PLAIN, maxFontSize)

    val x = maxRect.x + 3
    var y = maxRect.y - 3 + g.fontMetrics.height
    lines.forEach { line ->
      g.drawString(line, x, y)
      y += g.fontMetrics.height
    }
  }

  private fun getMaxFittingFontSize(
    g: Graphics,
    font: Font,
    string: String?,
    width: Int,
    height: Int
  ): Int {
    var minSize = 6
    var maxSize = 72
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

  companion object : KLogging()
}

private fun TranslatorLanguage.toOCRLanguage(): OCRLanguage {
  return when (this) {
    TranslatorLanguage.EN_GB -> OCRLanguage.EN
    TranslatorLanguage.EN_US -> OCRLanguage.EN
    TranslatorLanguage.DE -> OCRLanguage.DE
    TranslatorLanguage.RU -> OCRLanguage.RU
  }
}

private fun BufferedImage.deepCopy(): BufferedImage {
  val cm = this.colorModel
  val isAlphaPremultiplied = cm.isAlphaPremultiplied
  val raster = this.copyData(this.raster.createCompatibleWritableRaster())
  return BufferedImage(cm, raster, isAlphaPremultiplied, null)
}