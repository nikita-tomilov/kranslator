package com.programmer74.kranslator.view

import com.programmer74.kranslator.ocr.CharacterRecognizer
import com.programmer74.kranslator.service.ocr.RemoteTesseractKtor
import com.programmer74.kranslator.service.translation.LibreTranslate
import com.programmer74.kranslator.translate.Translator
import tornadofx.Fragment
import tornadofx.form
import tornadofx.tab
import tornadofx.tabpane
import kotlin.system.exitProcess

class MainView : Fragment("Translator") {

  private val translatorInstance: Translator = LibreTranslate()
  private val ocrInstance: CharacterRecognizer = RemoteTesseractKtor()

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
        val imageFromClipboardTranslateView = find<ImageFromClipboardTranslateView>(
            mapOf(
                ImageFromClipboardTranslateView::translatorInstance to translatorInstance,
                ImageFromClipboardTranslateView::ocrInstance to ocrInstance)
        )
        add(imageFromClipboardTranslateView)
      }
      prefHeightProperty().bind(heightProp)
    }

    primaryStage.setOnCloseRequest { exitProcess(0) }
  }
}