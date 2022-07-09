package com.programmer74.kranslator.service.translation

import com.programmer74.kranslator.translate.Translator
import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest

object TranslatorFactory {

  fun getInstance(): Translator {
    val instance = LibreTranslate()

    return object : Translator {
      override fun availableLanguages(): Set<TranslatorLanguage> {
        return instance.availableLanguages()
      }

      override fun translate(request: TranslatorRequest): String {
        if (request.source == request.target) return request.text
        return instance.translate(request)
      }
    }
  }
}