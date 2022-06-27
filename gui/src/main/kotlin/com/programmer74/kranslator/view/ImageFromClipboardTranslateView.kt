package com.programmer74.kranslator.view

import com.programmer74.kranslator.ocr.CharacterRecognizer
import com.programmer74.kranslator.ocr.OCRLanguage
import com.programmer74.kranslator.ocr.RIL_PARA
import com.programmer74.kranslator.service.graphics.ImageUtils
import com.programmer74.kranslator.translate.Translator
import com.programmer74.kranslator.translate.TranslatorLanguage
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Orientation
import javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS
import javafx.scene.image.Image
import mu.KLogging
import tornadofx.*
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO

class ImageFromClipboardTranslateView : Fragment("test") {

  val translatorInstance: Translator by param()
  val ocrInstance: CharacterRecognizer by param()

  private val languages = translatorInstance.availableLanguages().toList()
  private val fromLanguage = SimpleObjectProperty(TranslatorLanguage.EN_US)
  private val toLanguage = SimpleObjectProperty(TranslatorLanguage.DE)
  private val originalImageSwing = SimpleObjectProperty<Image>()
  private val originalImage = AtomicReference<BufferedImage>(null)
  private val originalText = SimpleStringProperty("hello")
  private val resultText = SimpleStringProperty("hallo")
  private val actionInProgress = SimpleBooleanProperty(false)

  private val ex = Executors.newSingleThreadExecutor()

  private fun insertImage() {
    try {
      val clipboardImage = ImageUtils.pasteImageFromClipboard()
      if (clipboardImage != null) {
        originalImageSwing.set(SwingFXUtils.toFXImage(clipboardImage, null))
        originalImage.set(clipboardImage)
      } else {
        originalText.set("Unable to get image from the clipboard")
      }
    } catch (e: Exception) {
      originalText.set(e.message)
    }
  }

  private fun parseText() {
    if (originalImage.get() == null) return
    actionInProgress.set(true)
    ex.submit {
      val tmp = Files.createTempFile("img", ".png").toFile()
      ImageIO.write(originalImage.get(), "png", tmp)
      val result = ocrInstance.recognize(tmp, 0, fromLanguage.get().toOCRLanguage(), RIL_PARA)
      val adjustedImage = originalImage.get()
      val g = originalImage.get().createGraphics()
      g.color = Color.RED
      result.blocks.forEach {
        val block = it.block
        g.drawRect(block.x, block.y, block.w, block.h)
      }
      val text = result.blocks.joinToString("\n") { it.text }
      Platform.runLater {
        originalText.set(text)
        originalImageSwing.set(SwingFXUtils.toFXImage(adjustedImage, null))
        actionInProgress.set(false)
      }
    }
  }

  private fun translate() {
    actionInProgress.set(true)
    ex.submit {
      val it =
          translatorInstance.translate(originalText.get(), fromLanguage.get(), toLanguage.get())
      Platform.runLater {
        resultText.set(it)
        actionInProgress.set(false)
      }
    }
  }

  override val root = form {
    val rootHeight = this.heightProperty()
    vbox {
      hbox {
        label("From")
        combobox(property = fromLanguage, values = languages) {
          disableProperty().bind(actionInProgress)
        }
        label("To")
        combobox(property = toLanguage, values = languages) {
          disableProperty().bind(actionInProgress)
        }
        button("Translate") {
          action {
            translate()
          }
        }
        progressbar(INDETERMINATE_PROGRESS) {
          visibleProperty().bind(actionInProgress)
        }
      }
      splitpane {
        vbox {
          hbox {
            button("Insert image") {
              action {
                insertImage()
              }
            }
            button("Parse text") {
              action {
                parseText()
              }
            }
          }
          hbox {
            imageview { imageProperty().bind(originalImageSwing) }
            textarea(originalText) {
              disableProperty().bind(actionInProgress)
            }
            prefHeightProperty().bind(rootHeight)
          }
          prefHeightProperty().bind(rootHeight)
        }
        textarea(resultText) {
          disableProperty().bind(actionInProgress)
        }
        orientation = Orientation.HORIZONTAL
        setDividerPositions(0.5)
        prefHeightProperty().bind(rootHeight)
      }
      prefHeightProperty().bind(rootHeight)
    }
  }

  private fun TranslatorLanguage.toOCRLanguage(): OCRLanguage {
    return when(this) {
      TranslatorLanguage.EN_GB -> OCRLanguage.EN
      TranslatorLanguage.EN_US -> OCRLanguage.EN
      TranslatorLanguage.DE -> OCRLanguage.DE
      TranslatorLanguage.RU -> OCRLanguage.RU
    }
  }

  companion object : KLogging()
}