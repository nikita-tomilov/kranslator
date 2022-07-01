package com.programmer74.kranslator.translate

interface Translator {

  fun availableLanguages(): Set<TranslatorLanguage>

  fun translate(request: TranslatorRequest): String
}

enum class TranslatorLanguage(
  val twoLetterCode: String
) {
  EN_US("en"), EN_GB("en"), DE("de"), RU("ru")
}

data class TranslatorRequest(
  val text: String,
  val source: TranslatorLanguage,
  val target: TranslatorLanguage
)
