package com.programmer74.kranslator.api

interface Translator {

    fun availableLanguages(): Set<TranslatorLanguage>

    fun translate(text: String, source: TranslatorLanguage, target: TranslatorLanguage): String
}

enum class TranslatorLanguage(
    val twoLetterCode: String
) {
    EN_US("en"), EN_GB("en"), DE("de"), RU("ru")
}
