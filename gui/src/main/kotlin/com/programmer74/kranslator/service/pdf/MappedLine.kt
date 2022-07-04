package com.programmer74.kranslator.service.pdf

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