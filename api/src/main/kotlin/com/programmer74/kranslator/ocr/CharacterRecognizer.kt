package com.programmer74.kranslator.ocr

import java.io.File

interface CharacterRecognizer {
  fun recognize(imageFile: File, dpi: Int, language: OCRLanguage, pil: Int): TextBlocks
}

public const val RIL_BLOCK = 0
public const val RIL_PARA = 1
public const val RIL_TEXTLINE = 2
public const val RIL_WORD = 3
public const val RIL_SYMBOL = 4

data class TextBlocks(
  val blocks: List<TextBlock>
)

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
