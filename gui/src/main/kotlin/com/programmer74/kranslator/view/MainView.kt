package com.programmer74.kranslator.view

import com.programmer74.kranslator.ocr.TextBlock
import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import javafx.beans.property.*
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Orientation
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent.KEY_PRESSED
import tornadofx.*
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

class MainView : Fragment("Translator") {

  private lateinit var rootHeight: ReadOnlyDoubleProperty
  private val controller = MainController()
  private val languages = controller.languages()
  private val fromLanguage = SimpleObjectProperty(TranslatorLanguage.DE)
  private val toLanguage = SimpleObjectProperty(TranslatorLanguage.EN_US)
  private val originalText = SimpleStringProperty("")
  private val originalImage = AtomicReference<BufferedImage>(null)
  private val originalImageSwing = SimpleObjectProperty<Image>()
  private lateinit var originalImageViewPane: ScrollPane
  private val imageOCRResults = AtomicReference<List<TextBlock>>(null)
  private val resultText = SimpleStringProperty("")
  private val resultImageSwing = SimpleObjectProperty<Image>()
  private lateinit var resultImageViewPane: ScrollPane
  private val actionInProgress = SimpleBooleanProperty(false)

  private fun translateAction() {
    actionInProgress.set(true)

    if (imageOCRResults.get() != null) {
      val requests =
          imageOCRResults.get().map {
            TranslatorRequest(
                it.text,
                fromLanguage.get(),
                toLanguage.get())
          }
      controller.translate(requests) {
        resultText.set(it.joinToString("\n"))
        controller.imprintTranslateResponseToImage(
            originalImage.get(),
            imageOCRResults.get(),
            it) { img ->
          resultImageSwing.set(SwingFXUtils.toFXImage(img, null))
          resultImageViewPane.prefHeightProperty().unbind()
          resultImageViewPane.prefHeightProperty().bind(rootHeight)
          actionInProgress.set(false)
          imageOCRResults.set(null)
        }
      }
    }

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
        imageOCRResults.set(res)
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
      originalImageViewPane.prefHeightProperty().unbind()
      originalImageViewPane.prefHeightProperty().bind(rootHeight)
      imageOCRResults.set(null)
      originalText.set("")
      actionInProgress.set(false)
    }, { error ->
      originalText.set(error)
      actionInProgress.set(false)
      clearImageAction()
    })
  }

  private fun clearImageAction() {
    originalImageViewPane.prefHeightProperty().unbind()
    originalImageViewPane.prefHeightProperty().bind(SimpleDoubleProperty(0.0))
    resultImageViewPane.prefHeightProperty().unbind()
    resultImageViewPane.prefHeightProperty().bind(SimpleDoubleProperty(0.0))
  }

  override val root = form {
    prefWidth = 1280.0
    prefHeight = 720.0
    rootHeight = this.heightProperty()

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

    splitpane {
      vbox {
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
          prefHeightProperty().bind(rootHeight)
          minHeightProperty().bind(rootHeight.divide(2))
        }
        scrollpane {
          imageview {
            imageProperty().bind(originalImageSwing)
            isPreserveRatio = true
          }
          originalImageViewPane = this
        }
      }
      vbox {
        textarea(resultText) {
          disableProperty().bind(actionInProgress)
          isEditable = false
          prefHeightProperty().bind(rootHeight)
          minHeightProperty().bind(rootHeight.divide(2))
        }
        scrollpane {
          imageview {
            imageProperty().bind(resultImageSwing)
            isPreserveRatio = true
          }
          resultImageViewPane = this
        }
      }
      orientation = Orientation.HORIZONTAL
      setDividerPositions(0.5)
    }
    clearImageAction()
    primaryStage.setOnCloseRequest { exitProcess(0) }
  }
}