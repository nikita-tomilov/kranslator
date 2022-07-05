package com.programmer74.kranslator.service.pdf

data class MappedParagraph(
  val page: Int,
  val paragraphIndex: Int,
  val text: String,
  val bounds: MappedBounds
)