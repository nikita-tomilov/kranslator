package com.programmer74.kranslator.ktor

import kotlinx.serialization.Serializable

@Serializable
data class TesseractResult(
  val blocks: List<TesseractResultBlock>
)

@Serializable
data class TesseractResultBlock(
  val text: String,
  val x: Int,
  val y: Int,
  val w: Int,
  val h: Int
)

