package com.programmer74.kranslator.translate

interface Translator {

  fun availableLanguages(): Set<TranslatorLanguage>

  fun translate(request: TranslatorRequest): List<String> {
    return translate(request) {}
  }
  fun translate(request: TranslatorRequest, lineAvailable: (String) -> Unit): List<String>
}

enum class TranslatorLanguage(
  val twoLetterCode: String
) {
  EN_US("en"), EN_GB("en"), DE("de"), RU("ru")
}

data class TranslatorRequest(
  val texts: List<String>,
  val source: TranslatorLanguage,
  val target: TranslatorLanguage
) {
  constructor(text: String, source: TranslatorLanguage, target: TranslatorLanguage) :
      this(listOf(text), source, target)
}
