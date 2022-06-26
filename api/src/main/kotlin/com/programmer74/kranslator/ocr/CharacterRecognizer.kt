package com.programmer74.kranslator.ocr

import java.io.File

interface CharacterRecognizer {
  fun recognize(imageFile: File, language: OCRLanguage): List<TextBlock>
}

data class TextBlock(
  val text: String,
  val block: TextBlockRectangle
)

data class TextBlockRectangle(
  val x: Int,
  val y: Int,
  val w: Int,
  val h: Int
)

enum class OCRLanguage(
  val threeLetterCode: String
) {
  EN("eng"), DE("deu"), RU("rus")
}
