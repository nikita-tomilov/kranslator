package com.programmer74.kranslator.view

import com.programmer74.kranslator.service.translation.LibreTranslate
import com.programmer74.kranslator.translate.Translator
import tornadofx.*
import kotlin.system.exitProcess

class MainView : Fragment("Translator") {

  private val translatorInstance: Translator = LibreTranslate()

  override val root = form {

    prefWidth = 1280.0
    prefHeight = 720.0
    val heightProp = this.heightProperty()

    tabpane {
      tab("Text") {
        isClosable = false
        val plainTextTranslateView = find<PlainTextTranslateView>(
            mapOf(PlainTextTranslateView::translatorInstance to translatorInstance)
        )
        add(plainTextTranslateView)
      }
      tab("Image") {
        isClosable = false
        label("TBD")
      }
      prefHeightProperty().bind(heightProp)
    }

    primaryStage.setOnCloseRequest { exitProcess(0) }
  }
}