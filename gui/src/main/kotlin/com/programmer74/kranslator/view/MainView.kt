package com.programmer74.kranslator.view

import tornadofx.*

class MainView : Fragment("Translator") {

  override val root = form {

    prefWidth = 1280.0
    prefHeight = 720.0
    val heightProp = this.heightProperty()

    tabpane {
      tab("Text") {
        isClosable = false
        label("TBD")
      }
      tab("Image") {
        isClosable = false
        label("TBD")
      }
      prefHeightProperty().bind(heightProp)
    }
  }
}