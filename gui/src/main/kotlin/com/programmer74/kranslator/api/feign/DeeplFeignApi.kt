package com.programmer74.kranslator.api.feign

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import feign.Body
import feign.Param
import feign.RequestLine

interface DeeplFeignApi {

  @RequestLine("POST /translate")
  @Body("auth_key={key}&source_lang={sourceLang}&target_lang={targetLang}&{body}")
  fun translate(
    @Param("key") key: String,
    @Param("sourceLang") sourceLang: String,
    @Param("targetLang") targetLang: String,
    @Param("body") body: String
  ): DeeplTranslationsResponse

  @RequestLine("POST /usage")
  @Body("auth_key={key}")
  fun usage(@Param("key") key: String): DeeplUsageResponse
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeeplTranslationsResponse(
  val translations: List<DeeplTranslation>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeeplTranslation(
  val text: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeeplUsageResponse(
  @JsonProperty("character_count")
  val characterCount: Long,
  @JsonProperty("character_limit")
  val characterLimit: Long
)