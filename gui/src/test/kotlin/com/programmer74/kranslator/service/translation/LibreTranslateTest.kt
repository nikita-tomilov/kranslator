package com.programmer74.kranslator.service.translation

import com.programmer74.kranslator.translate.TranslatorLanguage
import com.programmer74.kranslator.translate.TranslatorRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LibreTranslateTest {

  private val translator = LibreTranslate()

  @Test
  fun hello() {
    //given/when
    val answer = translator.translate(
        TranslatorRequest(
            "hello", TranslatorLanguage.EN_US, TranslatorLanguage.DE))
    //then
    assertThat(answer.lowercase()).isEqualTo("hallo")
  }
}
