package com.programmer74.kranslator.view

import com.programmer74.kranslator.translate.Translator
import com.programmer74.kranslator.translate.TranslatorLanguage
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS
import mu.KLogging
import tornadofx.*
import java.util.concurrent.Executors

class PlainTextTranslateView : Fragment("test") {

  val translatorInstance: Translator by param()

  private val languages = translatorInstance.availableLanguages().toList()
  private val fromLanguage = SimpleObjectProperty(TranslatorLanguage.EN_US)
  private val toLanguage = SimpleObjectProperty(TranslatorLanguage.DE)
  private val originalText = SimpleStringProperty("hello")
  private val resultText = SimpleStringProperty("hallo")
  private val translationInProgress = SimpleBooleanProperty(false)

  private val ex = Executors.newSingleThreadExecutor()

  private fun translate() {
    translationInProgress.set(true)
    ex.submit {
      val it = translatorInstance.translate(originalText.get(), fromLanguage.get(), toLanguage.get())
      Platform.runLater {
        resultText.set(it)
        translationInProgress.set(false)
      }
    }
  }

  override val root = form {
    val rootHeight = this.heightProperty()
    vbox {
      hbox {
        label("From")
        combobox(property = fromLanguage, values = languages) {
          disableProperty().bind(translationInProgress)
        }
        label("To")
        combobox(property = toLanguage, values = languages) {
          disableProperty().bind(translationInProgress)
        }
        button("Translate") {
          action {
            translate()
          }
        }
        progressbar(INDETERMINATE_PROGRESS) {
          visibleProperty().bind(translationInProgress)
        }
      }
      splitpane {
        textarea(originalText) {
          disableProperty().bind(translationInProgress)
        }
        textarea(resultText) {
          disableProperty().bind(translationInProgress)
        }
        orientation = Orientation.HORIZONTAL
        setDividerPositions(0.5)
        prefHeightProperty().bind(rootHeight)
      }
      prefHeightProperty().bind(rootHeight)
    }
  }

  companion object : KLogging()
}