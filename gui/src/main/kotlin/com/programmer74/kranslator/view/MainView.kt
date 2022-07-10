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
import mu.KLogging
import tornadofx.*
import java.awt.image.BufferedImage
import java.io.File
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
  private val logString = SimpleStringProperty("")
  private fun translateOCRedImageAction() {
    val request = TranslatorRequest(
        imageOCRResults.get().map { it.text },
        fromLanguage.get(),
        toLanguage.get())
    controller.translate(request) {
      resultText.set(it.joinToString("\n"))
      controller.imprintTranslateResponseToImage(
          originalImage.get(),
          imageOCRResults.get().map { o -> o.block },
          it) { img ->
        resultImageSwing.set(SwingFXUtils.toFXImage(img, null))
        resultImageViewPane.prefHeightProperty().unbind()
        resultImageViewPane.prefHeightProperty().bind(rootHeight)
        actionInProgress.set(false)
        imageOCRResults.set(null)
      }
    }
  }

  private fun translateTextAction() {
    val request = TranslatorRequest(originalText.get(), fromLanguage.get(), toLanguage.get())
    controller.translate(request) {
      actionInProgress.set(false)
      resultText.set(it.joinToString("\n"))
    }
  }

  private fun translatePDFAction(f: File) {
    originalText.set(f.absolutePath + "\n==========\n\n")
    resultText.set("")
    controller.translate(
        f,
        fromLanguage.get(),
        toLanguage.get(),
        { logString.set(it) },
        { ob -> originalText.set(originalText.get() + ob.joinToString("\n") + "\n") },
        { tb -> resultText.set(resultText.get() + tb.joinToString("\n") + "\n") },
        {
          logString.set("")
          actionInProgress.set(false)
          logger.warn { it }
          resultText.set(resultText.get() + "\n==========\nPlease see ${it.absolutePath}")
        }
    )
  }

  private fun ocrImageAction() {
    actionInProgress.set(true)
    controller.ocr(originalImage.get(), fromLanguage.get()) { res ->
      originalText.set(res.joinToString("\n") { it.text })
      imageOCRResults.set(res)
      translateAction()
    }
  }

  private fun translateAction() {
    actionInProgress.set(true)

    if (imageOCRResults.get() != null) {
      return translateOCRedImageAction()
    }

    if (isFile(originalText.get())) {
      return translatePDFAction(normalizeFile(originalText.get()))
    }

    if (originalText.get().isNotEmpty()) {
      return translateTextAction()
    }

    if (originalImage.get() != null) {
      return ocrImageAction()
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

  private fun isFile(s: String): Boolean {
    try {
      val f = File(s)
      if (f.exists() && (f.extension.lowercase() == "pdf")) {
        return true
      }
    } catch (e: Exception) {

    }
    if (s.startsWith("file://")) return isFile(s.replace("file://", ""))
    val lines = s.lines()
    if (lines.size > 1) return isFile(lines.first())
    return false
  }

  private fun normalizeFile(s: String): File {
    var path = s
    if (s.lines().size > 1) path = s.lines().first()
    if (path.startsWith("file://")) path = path.replaceFirst("file://", "")
    if (System.getProperty("os.name").lowercase().contains("windows")) {
      if (path.startsWith("/")) path = path.replaceFirst("/", "")
    }
    return File(path)
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
      label {
        visibleProperty().bind(actionInProgress)
        textProperty().bind(logString)
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
          wrapTextProperty().set(true)
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
          wrapTextProperty().set(true)
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

  companion object : KLogging()
}