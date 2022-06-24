package com.programmer74.kranslator.api.feign

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import feign.Headers
import feign.RequestLine

interface LibreTranslateFeignApi {

    @RequestLine("POST /translate")
    @Headers("Content-Type: application/json")
    fun translate(request: LibreTranslateRequest): LibreTranslateResponse
}

data class LibreTranslateRequest(
    val q: String,
    val source: String,
    val target: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LibreTranslateResponse(
    val translatedText: String
)
