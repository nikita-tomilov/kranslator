package com.programmer74.kranslator.impl

import com.programmer74.kranslator.api.TranslatorLanguage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LibreTranslateTest {

    private val translator = LibreTranslate("http://localhost:5000")

    @Test
    fun hello() {
        //given/when
        val answer = translator.translate("hello", TranslatorLanguage.EN_US, TranslatorLanguage.DE)
        //then
        assertThat(answer).isEqualTo("hallo")
    }
}
