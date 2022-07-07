package com.programmer74.kranslator.service.pdf

import mu.KLogging
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.io.Writer

object PDFParser : KLogging() {

  fun extractParagraphs(file: File): List<MappedParagraph> {
    val lines = extractLines(file)
    return lines.groupBy { it.paragraph }.map { e ->
      val paragraphIndex = e.key
      val linesForParagraph = e.value
      val page = linesForParagraph.first().page
      val paragraphText =
          linesForParagraph.filter { it.line.isNotBlank() }.joinToString(" ") { it.line.trim() }
      val firstLine = linesForParagraph.first()
      val lastLine = linesForParagraph.last()
      val widestLine = linesForParagraph.maxByOrNull { it.bounds.urx }!!
      val paragraphBounds = MappedBounds(
          lastLine.bounds.llx,
          lastLine.bounds.lly,
          widestLine.bounds.urx,
          firstLine.bounds.ury)
      MappedParagraph(page, paragraphIndex, paragraphText, paragraphBounds, linesForParagraph)
    }
  }

  fun extractLines(file: File): List<MappedLine> {
    var document: PDDocument? = null
    try {
      document = PDDocument.load(file)
      val result = ArrayList<MappedLine>()
      val stripper: PDFTextStripper = PDFStripper { result.add(it) }
      stripper.sortByPosition = true
      stripper.startPage = 0
      stripper.endPage = document.numberOfPages
      val dummy: Writer = OutputStreamWriter(ByteArrayOutputStream())
      stripper.writeText(document, dummy)
      return result
    } finally {
      document?.close()
    }
  }
}