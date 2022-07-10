package com.programmer74.kranslator.service.translation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.programmer74.kranslator.api.feign.LibreTranslateFeignApi
import com.programmer74.kranslator.api.feign.LibreTranslateRequest
import com.programmer74.kranslator.translate.Translator
import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder

class LibreTranslate(
  private val target: String = "https://libretranslate.de",
  private val characterLimit: Int = 240,
  private val msPauseBetweenApiCalls: Long = 1000L
) : Translator {

  private val om = ObjectMapper().registerKotlinModule()

  private val api = Feign.builder()
      .encoder(JacksonEncoder(om))
      .decoder(JacksonDecoder(om))
      .target(LibreTranslateFeignApi::class.java, target)

  override fun availableLanguages(): Set<TranslatorLanguage> {
    return setOf(TranslatorLanguage.DE, TranslatorLanguage.EN_US, TranslatorLanguage.RU)
  }

  override fun translate(
    request: TranslatorRequest,
    lineAvailable: (String) -> Unit
  ): List<String> {
    return request.texts.map { translate(it, request.source, request.target, lineAvailable) }
  }

  private fun translate(
    text: String,
    source: TranslatorLanguage,
    target: TranslatorLanguage,
    lineAvailable: (String) -> Unit
  ): String {
    if (text.length < characterLimit) {
      return invokeTranslate(text, source, target)
    }
    return text.split(" ")
        .chunkedBy(characterLimit) { length + 1 }.joinToString(" ") {
          val lineOfWordsWithinLimit = it.joinToString(" ")
          val res = invokeTranslate(lineOfWordsWithinLimit, source, target)
          lineAvailable(res)
          res
        }
  }

  private fun invokeTranslate(
    text: String,
    source: TranslatorLanguage,
    target: TranslatorLanguage
  ): String {
    val response = api.translate(
        LibreTranslateRequest(
            text,
            source.twoLetterCode,
            target.twoLetterCode)).translatedText
    Thread.sleep(msPauseBetweenApiCalls)
    return response
  }

  inline fun <T> Iterable<T>.chunkedBy(maxSize: Int, size: T.() -> Int): List<List<T>> {
    val result = mutableListOf<List<T>>()
    var sublist = mutableListOf<T>()
    var sublistSize = 0L
    for (item in this) {
      val itemSize = item.size()
      if (sublistSize + itemSize > maxSize && sublist.isNotEmpty()) {
        result += sublist
        sublist = mutableListOf()
        sublistSize = 0
      }
      sublist.add(item)
      sublistSize += itemSize
    }
    if (sublist.isNotEmpty())
      result += sublist

    return result
  }
}
