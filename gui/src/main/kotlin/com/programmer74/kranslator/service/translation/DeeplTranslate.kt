package com.programmer74.kranslator.service.translation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.programmer74.kranslator.api.feign.DeeplFeignApi
import com.programmer74.kranslator.translate.Translator
import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import feign.Feign
import feign.jackson.JacksonDecoder

class DeeplTranslate(
  private val target: String = "https://api-free.deepl.com/v2",
  private val key: String
) : Translator {

  private val om = ObjectMapper().registerKotlinModule()

  private val api = Feign.builder()
      .decoder(JacksonDecoder(om))
      .target(DeeplFeignApi::class.java, target)

  override fun availableLanguages(): Set<TranslatorLanguage> {
    return setOf(TranslatorLanguage.DE, TranslatorLanguage.EN_US, TranslatorLanguage.RU)
  }

  override fun translate(request: TranslatorRequest): String {
    return invokeTranslate(request.text, request.source, request.target)
  }

  fun usage() = api.usage(key)

  private fun invokeTranslate(
    text: String,
    source: TranslatorLanguage,
    target: TranslatorLanguage
  ): String {
    val lines = text.lines().map {
      val clearString = it.trim().replace("&", "\\&")
      "text=$clearString"
    }
    val body = lines.joinToString("&")
    val response = api.translate(
        key,
        source.twoLetterCode.uppercase(),
        target.twoLetterCode.uppercase(),
        body)
    return response.translations.joinToString("\n") { it.text }
  }
}
