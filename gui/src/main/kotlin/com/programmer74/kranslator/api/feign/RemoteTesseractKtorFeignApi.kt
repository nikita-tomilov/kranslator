package com.programmer74.kranslator.api.feign

import com.programmer74.kranslator.ocr.TextBlocks
import feign.Headers
import feign.Param
import feign.RequestLine
import java.io.File

interface RemoteTesseractKtorFeignApi {

  @RequestLine("POST /image/{dpi}/{lang}/{pil}/ocr")
  @Headers("Content-Type: multipart/form-data")
  fun translate(
    @Param("file") file: File,
    @Param("dpi") dpi: Int,
    @Param("lang") language: String,
    @Param("pil") pil: Int
  ): TextBlocks
}