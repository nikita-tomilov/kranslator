package com.programmer74.kranslator.service.pdf

data class MappedParagraph(
  val page: Int,
  val paragraphIndex: Int,
  val originalText: String,
  val translatedText: String,
  val bounds: MappedBounds,
  val originalLines: List<MappedLine>
) {
  constructor(
    page: Int,
    paragraphIndex: Int,
    text: String,
    bounds: MappedBounds,
    lines: List<MappedLine>
  ) : this(page, paragraphIndex, text, text, bounds, lines)
}

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