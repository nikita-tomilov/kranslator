package com.programmer74.kranslator.view

import com.programmer74.kranslator.ocr.CharacterRecognizer
import com.programmer74.kranslator.ocr.OCRLanguage
import com.programmer74.kranslator.ocr.RIL_PARA
import com.programmer74.kranslator.ocr.TextBlock
import com.programmer74.kranslator.service.graphics.ImageUtils
import com.programmer74.kranslator.service.ocr.RemoteTesseractKtor
import com.programmer74.kranslator.service.translation.LibreTranslate
import com.programmer74.kranslator.translate.Translator
import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import java.awt.image.BufferedImage
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
      val results = ocrInstance.recognize(tmp, 0, language.toOCRLanguage(), RIL_PARA)
      Platform.runLater {
        callback(results.blocks)
      }
    }
    //    ex.submit {
    //      val tmp = Files.createTempFile("img", ".png").toFile()
    //      ImageIO.write(originalImage.get(), "png", tmp)
    //      val result = ocrInstance.recognize(tmp, 0, fromLanguage.get().toOCRLanguage(), RIL_PARA)
    //      val adjustedImage = originalImage.get()
    //      val g = originalImage.get().createGraphics()
    //      g.color = Color.RED
    //      result.blocks.forEach {
    //        val block = it.block
    //        g.drawRect(block.x, block.y, block.w, block.h)
    //      }
    //      val text = result.blocks.joinToString("\n") { it.text }
    //      Platform.runLater {
    //        originalText.set(text)
    //        originalImageSwing.set(SwingFXUtils.toFXImage(adjustedImage, null))
    //        actionInProgress.set(false)
    //      }
    //    }
  }

  private fun TranslatorLanguage.toOCRLanguage(): OCRLanguage {
    return when (this) {
      TranslatorLanguage.EN_GB -> OCRLanguage.EN
      TranslatorLanguage.EN_US -> OCRLanguage.EN
      TranslatorLanguage.DE -> OCRLanguage.DE
      TranslatorLanguage.RU -> OCRLanguage.RU
    }
  }
}