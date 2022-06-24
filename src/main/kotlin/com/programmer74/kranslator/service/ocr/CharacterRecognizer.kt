package com.programmer74.kranslator.service.ocr

import java.awt.Rectangle
import java.io.File

interface CharacterRecognizer {
    fun recognize(imageFile: File, language: OCRLanguage): List<TextBlock>
}

data class TextBlock(
    val text: String,
    val block: Rectangle
)

enum class OCRLanguage(
    val threeLetterCode: String
) {
    EN("eng"), DE("deu"), RU("rus")
}
