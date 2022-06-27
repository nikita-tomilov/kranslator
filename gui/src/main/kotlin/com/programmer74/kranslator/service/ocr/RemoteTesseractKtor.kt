package com.programmer74.kranslator.service.ocr

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.programmer74.kranslator.api.feign.RemoteTesseractKtorFeignApi
import com.programmer74.kranslator.ocr.CharacterRecognizer
import com.programmer74.kranslator.ocr.OCRLanguage
import com.programmer74.kranslator.ocr.TextBlocks
import feign.Feign
import feign.form.FormEncoder
import feign.jackson.JacksonDecoder
import java.io.File

class RemoteTesseractKtor(
  private val target: String = "https://api2.nt-services.tk"
) : CharacterRecognizer {

  private val om = ObjectMapper().registerKotlinModule()

  private val api = Feign.builder()
      .encoder(FormEncoder())
      .decoder(JacksonDecoder(om))
      .requestInterceptor {
        it.header("Accept", "application/json")
        it.header("X-MAGIC", "TSX5w24M9bxjPHu6Tf9VrKr5")
      }
      .target(RemoteTesseractKtorFeignApi::class.java, target)

  override fun recognize(
    imageFile: File,
    dpi: Int,
    language: OCRLanguage,
    pil: Int
  ): TextBlocks {
    return api.translate(imageFile, dpi, language.name, pil)
  }
}