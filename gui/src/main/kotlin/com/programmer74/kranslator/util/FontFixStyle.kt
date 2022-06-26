package com.programmer74.kranslator.util

import mu.KLogging
import tornadofx.Stylesheet

class FontFixStyle : Stylesheet() {

  init {
    star {
      if (shallApplyFontFix()) {
        logger.warn { "Fixing JavaFX font issue on MacOS" }
        fontFamily = "Arial"
      }
    }
  }

  companion object : KLogging() {
    fun shallApplyFontFix(): Boolean {
      return System.getProperty("os.name").contains("mac", true)
    }
  }
}