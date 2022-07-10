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

  override fun translate(request: TranslatorRequest, lineAvailable: (String) -> Unit): List<String> {
    return invokeTranslate(request.texts, request.source, request.target, lineAvailable)
  }

  fun usage() = api.usage(key)

  private fun invokeTranslate(
    texts: List<String>,
    source: TranslatorLanguage,
    target: TranslatorLanguage,
    lineAvailable: (String) -> Unit
  ): List<String> {
    val body = texts.joinToString("&") {
      val clearText = it.trim().replace("&", "\\&")
      "text=$clearText"
    }
    val response = api.translate(
        key,
        source.twoLetterCode.uppercase(),
        target.twoLetterCode.uppercase(),
        body)
    response.translations.forEach { lineAvailable(it.text) }
    return response.translations.map { it.text }
  }
}
