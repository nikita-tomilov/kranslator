package com.programmer74.kranslator.ktor

import com.programmer74.kranslator.ocr.LocalTesseract
import com.programmer74.kranslator.ocr.OCRLanguage
import java.io.File

object TesseractWrapper {
  private val INSTANCE = LocalTesseract()

  fun doOCR(
    file: File,
    languageParam: String,
    dpiParam: String,
    pilParam: String
  ): TesseractResult {
    val language = OCRLanguage.valueOf(languageParam.uppercase())
    val dpi = dpiParam.toInt()
    val pil = pilParam.toInt()
    val result = INSTANCE.recognize(file, dpi, language, pil)
    return TesseractResult(result.map {
      TesseractResultBlock(
          it.text,
          it.block.x,
          it.block.y,
          it.block.w,
          it.block.h)
    })
  }
}