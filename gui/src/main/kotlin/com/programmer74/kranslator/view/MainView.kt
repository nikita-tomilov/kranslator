package com.programmer74.kranslator.view

import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Orientation
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.ToggleGroup
import javafx.scene.image.Image
import tornadofx.*
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

class MainView : Fragment("Translator") {
  enum class Mode {
    TEXT, IMAGE, PDF
  }

  private val controller = MainController()
  private val languages = controller.languages()
  private val fromLanguage = SimpleObjectProperty(TranslatorLanguage.DE)
  private val toLanguage = SimpleObjectProperty(TranslatorLanguage.EN_US)
  private val originalText = SimpleStringProperty("hello")
  private val originalImage = AtomicReference<BufferedImage>(null)
  private val originalImageSwing = SimpleObjectProperty<Image>()
  private val imageOCRPerformed = AtomicBoolean(false)
  private val resultText = SimpleStringProperty("hallo")
  private val actionInProgress = SimpleBooleanProperty(false)
  private val textBoxesVisible = SimpleBooleanProperty(true)
  private val imageBoxesVisible = SimpleBooleanProperty(false)
  private val mode = SimpleObjectProperty(Mode.TEXT)

  private fun translateAction(requests: List<TranslatorRequest> = emptyList()) {
    if (mode.get() == Mode.PDF) return
    actionInProgress.set(true)

    if (requests.isNotEmpty()) {
      controller.translate(requests) {
        actionInProgress.set(false)
        resultText.set(it.joinToString("\n"))
      }
      return
    }

    if ((mode.get() == Mode.IMAGE) && (originalImage.get() != null)) {
      if (!imageOCRPerformed.get()) {
        log.warning { "OCR not performed" }
        actionInProgress.set(true)
        controller.ocr(originalImage.get(), fromLanguage.get()) { res ->
          originalText.set(res.joinToString("\n") { it.text })
          imageOCRPerformed.set(true)
          translateAction(res.map {
            TranslatorRequest(it.text, fromLanguage.get(), toLanguage.get())
          })
        }
        return
      } else {
        log.warning { "OCR already performed, skipping..." }
      }
    }

    translateAction(
        listOf(
            TranslatorRequest(
                originalText.get(),
                fromLanguage.get(),
                toLanguage.get())))
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

  private fun modeChanged(newMode: Mode) {
    mode.set(newMode)
    log.warning { "mode changed to $newMode" }
    when (newMode) {
      Mode.TEXT -> {
        textBoxesVisible.set(true)
        imageBoxesVisible.set(false)
      }
      Mode.IMAGE -> {
        textBoxesVisible.set(true)
        imageBoxesVisible.set(true)
      }
      Mode.PDF -> {
        textBoxesVisible.set(false)
        imageBoxesVisible.set(false)
      }
    }
  }

  override val root = form {
    prefWidth = 1280.0
    prefHeight = 720.0
    val rootHeight = this.heightProperty()
    val toggleGroup = ToggleGroup()

    hbox {
      Mode.values().forEach { m ->
        val toggle = togglebutton(m.name, toggleGroup)
        toggle.setOnAction { modeChanged(m) }
        this.add(toggle)
      }
    }

    hbox {
      label("From")
      combobox(property = fromLanguage, values = languages) {
        disableProperty().bind(actionInProgress)
      }
      label("To")
      combobox(property = toLanguage, values = languages) {
        disableProperty().bind(actionInProgress)
      }
      button("Insert image") {
        action {
          insertImageAction()
        }
        visibleProperty().bind(imageBoxesVisible)
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
        }
        textarea(resultText) {
          disableProperty().bind(actionInProgress)
          isEditable = false
        }
        orientation = Orientation.HORIZONTAL
        setDividerPositions(0.5)
        prefHeightProperty().bind(rootHeight)
        visibleProperty().bind(textBoxesVisible)
      }
      splitpane {
        hbox {
          imageview { imageProperty().bind(originalImageSwing) }
        }
        prefHeightProperty().bind(rootHeight)
        visibleProperty().bind(imageBoxesVisible)
      }
      prefHeightProperty().bind(rootHeight)
    }

    primaryStage.setOnCloseRequest { exitProcess(0) }
  }
}