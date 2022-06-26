package com.programmer74.kranslator

import com.programmer74.kranslator.util.FontFixStyle
import com.programmer74.kranslator.view.MainView
import tornadofx.App
import tornadofx.reloadStylesheetsOnFocus

class LaunchableApp : App(MainView::class, FontFixStyle::class) {

  init {
    reloadStylesheetsOnFocus()
  }

  companion object {
    @JvmStatic
    fun start(args: Array<String>) {
      launch(LaunchableApp::class.java)
    }
  }
}

//https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing/52654791#52654791
class Application {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      LaunchableApp.start(args)
    }
  }
}