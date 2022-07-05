package com.programmer74.kranslator.service.pdf

import mu.KLogging
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import java.util.concurrent.atomic.AtomicInteger

class PDFStripper(
  private val callback: (MappedLine) -> Unit
) : PDFTextStripper() {

  private val pageCounter = AtomicInteger(0)

  private val paragraphCounter = AtomicInteger(0)

  override fun writePageStart() {
    logger.warn { "Page start" }
    pageCounter.incrementAndGet()
  }

  override fun writePageEnd() {
    logger.warn { "Page end" }
  }

  override fun writeParagraphStart() {
    logger.warn { "Paragraph start" }
    paragraphCounter.incrementAndGet()
  }

  override fun writeParagraphSeparator() {
    logger.warn { "Paragraph separator" }
    paragraphCounter.incrementAndGet()
  }

  override fun writeParagraphEnd() {
    logger.warn { "Paragraph end" }
    paragraphCounter.incrementAndGet()
  }

  override fun writeString(text: String, textPositions: MutableList<TextPosition>) {
    val bounds = MappedBounds.from(
        textPositions,
        { it.x / it.pageWidth },
        { (it.y - it.height) / it.pageHeight },
        { it.width / it.pageWidth },
        { it.height / it.pageHeight })
    callback(MappedLine(pageCounter.get(), paragraphCounter.get(), text, bounds))
  }

  companion object : KLogging()
}