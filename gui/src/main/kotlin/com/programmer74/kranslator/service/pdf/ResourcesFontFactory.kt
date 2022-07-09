package com.programmer74.kranslator.service.pdf

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object ResourcesFontFactory {

  private val extractedFonts = HashMap<String, File>()

  @Synchronized
  fun getFontPath(fontName: String): File {
    return extractedFonts.getOrPut(fontName) { extract("/pdffonts/$fontName.ttf") }
  }

  private fun extract(resourceName: String): File {
    val extension = resourceName.split(".").last()
    val resourceStream =
        this::class.java.getResourceAsStream(resourceName) ?: error("cannot find $resourceName")
    val target = Files.createTempFile("font", ".$extension")
    Files.copy(resourceStream, target, StandardCopyOption.REPLACE_EXISTING)
    return target.toFile()
  }
}