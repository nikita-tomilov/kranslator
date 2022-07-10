package com.programmer74.kranslator.service.translation

import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class DeeplTranslateTest {

  private val translator = DeeplTranslate(key = System.getenv("DEEPL_API_KEY"))

  @Test
  @Disabled
  fun translationWorks() {
    //given/when
    val answer = translator.translate(
        TranslatorRequest(
            "Hello World", TranslatorLanguage.EN_US, TranslatorLanguage.DE))
    //then
    assertThat(answer.lowercase()).isEqualTo("hallo welt")
  }

  @Test
  @Disabled
  fun usageAPI() {
    //given/when
    val usage = translator.usage()
    //then
    logger.warn { usage }
  }

  companion object : KLogging()
}
