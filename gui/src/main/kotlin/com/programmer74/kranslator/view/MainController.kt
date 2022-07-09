package com.programmer74.kranslator.view

import com.programmer74.kranslator.ocr.*
import com.programmer74.kranslator.service.graphics.ImageUtils
import com.programmer74.kranslator.service.ocr.RemoteTesseractKtor
import com.programmer74.kranslator.service.pdf.PDFCreator
import com.programmer74.kranslator.service.pdf.PDFParser
import com.programmer74.kranslator.service.translation.TranslatorFactory
import com.programmer74.kranslator.translate.Translator
import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import javafx.application.Platform
import mu.KLogging
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Executors
import javax.imageio.ImageIO

class MainController {

  private val translatorInstance: Translator = TranslatorFactory.getInstance()
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
    ocrBatchCallback: (List<String>) -> Unit,
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
    ocrBlocks: List<TextBlockRectangle>,
    translatedTexts: List<String>,
    callback: (BufferedImage) -> Unit
  ) {
    ex.submit {
      val printedImage =
          ImageUtils.imprintTranslateResponseToImage(image.deepCopy(), ocrBlocks, translatedTexts)
      Platform.runLater {
        callback(printedImage)
      }
    }
  }

  private fun translatePDF(
    pdf: File,
    fromLanguage: TranslatorLanguage,
    toLanguage: TranslatorLanguage,
    progressCallback: (String) -> Unit,
    ocrBatchCallback: (List<String>) -> Unit,
    translateBatchCallback: (List<String>) -> Unit,
    resultCallback: (File) -> Unit
  ) {
    log(progressCallback, "Attempting to translate file ${pdf.absolutePath}...")
    val paragraphs = PDFParser.extractParagraphs(pdf)
    Platform.runLater { ocrBatchCallback.invoke(paragraphs.map { it.originalText }) }

    val n = paragraphs.size
    val translatedParagraphs = paragraphs.mapIndexed { index, it ->

      log(progressCallback, "${index + 1}/$n Performing translation")
      val translatedText = translatorInstance.translate(
          TranslatorRequest(
              it.originalText,
              fromLanguage,
              toLanguage))
      Platform.runLater { translateBatchCallback.invoke(listOf(translatedText)) }

      it.copy(originalText = it.originalText, translatedText = translatedText)
    }

    log(progressCallback, "Attempting to reassemble the original PDF...")
    val target =
        File(pdf.absolutePath + "--${fromLanguage.twoLetterCode}-${toLanguage.twoLetterCode}.pdf")
    PDFCreator.createViaText(target, translatedParagraphs) // -- this sometimes cuts off the words; we need to fix it later.
    //PDFCreator.createViaPNG(target, translatedParagraphs) // -- this does not draw in multiple lines!
    Platform.runLater { resultCallback.invoke(target) }
  }

  private fun log(progressCallback: (String) -> Unit, s: String) {
    Platform.runLater { progressCallback(s) }
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