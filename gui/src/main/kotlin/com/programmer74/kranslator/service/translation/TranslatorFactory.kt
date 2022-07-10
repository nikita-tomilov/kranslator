package com.programmer74.kranslator.service.translation

import com.programmer74.kranslator.translate.Translator
import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import mu.KLogging

object TranslatorFactory : KLogging() {

  fun getInstance(): Translator {
    val deeplKey = System.getenv("DEEPL_API_KEY")
    val instance = if (deeplKey != null) {
      logger.warn { "Found Deepl api key, using Deepl as translator backend" }
      DeeplTranslate(key = deeplKey).also { logger.warn { it.usage() } }
    } else {
      logger.warn { "Using Libretranslate via DE server as translator backend" }
      LibreTranslate()
    }

    return object : Translator {
      override fun availableLanguages(): Set<TranslatorLanguage> {
        return instance.availableLanguages()
      }

      override fun translate(
        request: TranslatorRequest,
        lineAvailable: (String) -> Unit
      ): List<String> {
        if (request.source == request.target) return request.texts
        return instance.translate(request, lineAvailable)
      }
    }
  }
}