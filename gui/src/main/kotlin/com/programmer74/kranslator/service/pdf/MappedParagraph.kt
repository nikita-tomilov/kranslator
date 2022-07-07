package com.programmer74.kranslator.service.pdf

import kotlin.math.abs

data class MappedParagraph(
  val page: Int,
  val paragraphIndex: Int,
  val text: String,
  val bounds: MappedBounds,
  val lines: List<MappedLine>
)

data class MappedLine(
  val page: Int,
  val paragraph: Int,
  val line: String,
  val bounds: MappedBounds
) {
  override fun toString(): String {
    return "$page - $paragraph | $bounds | $line"
  }
}