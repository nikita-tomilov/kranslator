package com.programmer74.kranslator.service.pdf

import mu.KLogging
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.max

class PDFStripper(
  private val maxDeltaX: Float = 0.1f,
  private val maxDeltaYCoeff: Float = 2.0f,
  private val callback: (MappedLine) -> Unit
) : PDFTextStripper() {

  private val pageCounter = AtomicInteger(0)

  private val paragraphCounter = AtomicInteger(0)

  private val prevLine = AtomicReference<MappedLine>(null)

  private val curLine = AtomicReference<MappedLine>(null)

  private val lineBelongsToPrevParagraph = AtomicBoolean(false)

  override fun writePageStart() {
    logger.warn { "Page start" }
    pageCounter.incrementAndGet()
    resetParagraph()
  }

  override fun writePageEnd() {
    logger.warn { "Page end" }
    resetParagraph()
  }

  override fun writeParagraphStart() {
    logger.warn { "Paragraph start" }
    resetParagraph()
  }

  override fun writeParagraphEnd() {
    logger.warn { "Paragraph end" }
    resetParagraph()
  }

  override fun writeParagraphSeparator() {
    val dx = dx(prevLine.get().bounds, curLine.get().bounds)
    val dy = dy(prevLine.get().bounds, curLine.get().bounds)
    val h1 = prevLine.get().bounds.height()
    val h2 = curLine.get().bounds.height()
    val h = max(h1, h2)
    logger.warn { "  Paragraph separator; dx $dx, dy $dy, h $h" }
    if ((dx <= maxDeltaX) && (dy <= h * maxDeltaYCoeff) && !(curLine.get().line.endsWith('.'))) {
      logger.warn { "  ignored" }
    } else {
      //logger.warn { "  Paragraph separator; dx $dx, dy $dy, h $h" }
      resetParagraph()
    }
  }

  private fun resetParagraph() {
    paragraphCounter.incrementAndGet()
    lineBelongsToPrevParagraph.set(false)
  }

  override fun writeString(text: String, textPositions: MutableList<TextPosition>) {
    val first = textPositions.firstOrNull() ?: return
    val pw = first.pageWidth
    val ph = first.pageHeight
        val bounds = MappedBounds.from(
            textPositions,
            { it.x / pw },
            { it.y / ph },
            { (it.x + it.width) / pw },
            { (it.y - it.height) / ph })
    val currentLine = MappedLine(pageCounter.get(), paragraphCounter.get(), text, bounds)
    callback(currentLine)
    logger.warn { "    got line >>${text.lines().joinToString(" ") { it.trim() }}" }

    if (curLine.get() == null) curLine.set(currentLine)
    prevLine.set(curLine.get())
    curLine.set(currentLine)
  }
  
  private fun dy(a: MappedBounds, b: MappedBounds): Float {
    val dist1 = dist(a.lly, b.lly)
    val dist2 = dist(a.lly, b.ury)
    val dist3 = dist(a.ury, b.lly)
    val dist4 = dist(a.ury, b.ury)
    return min(listOf(dist1, dist2, dist3, dist4))
  }

  private fun dx(a: MappedBounds, b: MappedBounds): Float {
    val dist1 = dist(a.llx, b.llx)
    val dist2 = dist(a.llx, b.urx)
    val dist3 = dist(a.urx, b.llx)
    val dist4 = dist(a.urx, b.urx)
    return min(listOf(dist1, dist2, dist3, dist4))
  }

  private fun dist(a: Float, b: Float) = abs(a - b)
  
  private fun min(a: List<Float>) = a.minOrNull()!!

  companion object : KLogging()
}