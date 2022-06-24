package com.programmer74.kranslator.service.translation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.programmer74.kranslator.api.feign.LibreTranslateFeignApi
import com.programmer74.kranslator.api.feign.LibreTranslateRequest
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder

class LibreTranslate(
    private val target: String = "https://libretranslate.com/translate"
) : Translator {

    private val om = ObjectMapper().registerKotlinModule()

    private val api = Feign.builder()
        .encoder(JacksonEncoder(om))
        .decoder(JacksonDecoder(om))
        .target(LibreTranslateFeignApi::class.java, target)

    override fun availableLanguages(): Set<TranslatorLanguage> {
        return setOf(TranslatorLanguage.DE, TranslatorLanguage.EN_US)
    }

    override fun translate(text: String, source: TranslatorLanguage, target: TranslatorLanguage): String {
        return api.translate(LibreTranslateRequest(text, source.twoLetterCode, target.twoLetterCode)).translatedText
    }
}
