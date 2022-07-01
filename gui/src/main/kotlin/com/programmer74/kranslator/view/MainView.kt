package com.programmer74.kranslator.view

import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Orientation
import javafx.scene.control.ProgressIndicator
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent.KEY_PRESSED
import tornadofx.*
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

class MainView : Fragment("Translator") {

  private val controller = MainController()
  private val languages = controller.languages()
  private val fromLanguage = SimpleObjectProperty(TranslatorLanguage.DE)
  private val toLanguage = SimpleObjectProperty(TranslatorLanguage.EN_US)
  private val originalText = SimpleStringProperty("")
  private val originalImage = AtomicReference<BufferedImage>(null)
  private val originalImageSwing = SimpleObjectProperty<Image>()
  private val imageOCRPerformed = AtomicBoolean(false)
  private val resultText = SimpleStringProperty("")
  private val actionInProgress = SimpleBooleanProperty(false)

  private fun translateAction() {
    actionInProgress.set(true)

    if (originalText.get().isNotEmpty()) {
      val requests =
          listOf(
              TranslatorRequest(
                  originalText.get(),
                  fromLanguage.get(),
                  toLanguage.get()))
      controller.translate(requests) {
        actionInProgress.set(false)
        resultText.set(it.joinToString("\n"))
      }
      return
    }

    if (originalImage.get() != null) {
      actionInProgress.set(true)
      controller.ocr(originalImage.get(), fromLanguage.get()) { res ->
        originalText.set(res.joinToString("\n") { it.text })
        imageOCRPerformed.set(true)
        translateAction()
      }
      return
    }

    resultText.set("Nothing to translate")
    actionInProgress.set(false)
  }

  private fun insertImageAction() {
    originalText.set("")
    resultText.set("")
    actionInProgress.set(true)
    controller.pasteImageFromClipboard({ clipboardImage ->
      originalImageSwing.set(SwingFXUtils.toFXImage(clipboardImage, null))
      originalImage.set(clipboardImage)
      imageOCRPerformed.set(false)
      originalText.set("")
      actionInProgress.set(false)
    }, { error ->
      originalText.set(error)
      actionInProgress.set(false)
    })
  }

  override val root = form {
    prefWidth = 1280.0
    prefHeight = 720.0
    val rootHeight = this.heightProperty()

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
          translateAction()
        }
      }
      progressbar(ProgressIndicator.INDETERMINATE_PROGRESS) {
        visibleProperty().bind(actionInProgress)
      }
    }

    splitpane(orientation = Orientation.VERTICAL) {
      splitpane {
        textarea(originalText) {
          disableProperty().bind(actionInProgress)
          addEventHandler(KEY_PRESSED) { event ->
            if (event.isControlDown && event.code === KeyCode.V) {
              if (clipboard.hasImage()) {
                event.consume()
                insertImageAction()
              }
            }
          }
        }
        textarea(resultText) {
          disableProperty().bind(actionInProgress)
          isEditable = false
        }
        orientation = Orientation.HORIZONTAL
        setDividerPositions(0.5)
        prefHeightProperty().bind(rootHeight)
      }
      splitpane {
        hbox {
          imageview { imageProperty().bind(originalImageSwing) }
        }
        prefHeightProperty().bind(rootHeight)
      }
      prefHeightProperty().bind(rootHeight)
    }

    primaryStage.setOnCloseRequest { exitProcess(0) }
  }
}