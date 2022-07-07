package com.programmer74.kranslator.service.graphics

import com.programmer74.kranslator.ocr.TextBlock
import com.programmer74.kranslator.ocr.TextBlockRectangle
import com.programmer74.kranslator.view.MainController
import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage

data class SimpleBoundary(
  val x1: Int,
  val y1: Int,
  val x2: Int,
  val y2: Int
)

object ImageUtils {

  fun pasteImageFromClipboard(): BufferedImage? {
    var result: BufferedImage?
    val width: Int
    val height: Int
    val g: Graphics
    result = null
    val img: Image? = pasteFromClipboard(DataFlavor.imageFlavor) as Image?
    if (img != null) {
      width = img.getWidth(null)
      height = img.getHeight(null)
      result = BufferedImage(
          width, height,
          BufferedImage.TYPE_INT_RGB)
      g = result.createGraphics()
      g.drawImage(img, 0, 0, null)
      g.dispose()
    }
    return result
  }

  fun imprintTranslateResponseToImage(
    target: BufferedImage,
    ocrBlocks: List<TextBlockRectangle>,
    translatedTexts: List<String>
  ): BufferedImage {
    val g = target.createGraphics()
    assert(ocrBlocks.size == translatedTexts.size)
    (ocrBlocks.indices).forEach { i ->
      val block = ocrBlocks[i]
      val text = translatedTexts[i]
      renderTextBlock(text, block, g)
    }
    return target
  }

  fun imprintBoundariesToImage(
    target: BufferedImage,
    boundaries: List<SimpleBoundary>
  ): BufferedImage {
    val hw = 10
    val w = hw * 2
    val g = target.createGraphics()
    boundaries.forEach {
      g.color = Color.RED
      g.fillRect(it.x1 - hw, it.y1 - hw, w, w)
      g.color = Color.GREEN
      g.fillRect(it.x2 - hw, it.y2 - hw, w, w)
    }
    return target
  }

  private fun renderTextBlock(text: String, block: TextBlockRectangle, g: Graphics2D) {
    g.color = Color.WHITE
    g.fillRect(block.x, block.y, block.w, block.h)

    val origStroke = g.stroke
    g.color = Color.RED
    g.stroke = BasicStroke(1.0f)
    g.drawRect(block.x, block.y, block.w, block.h)

    g.color = Color.BLACK
    g.stroke = origStroke
    drawTextToFit(text, g, block)
  }

  //https://stackoverflow.com/questions/12485236/finding-maximum-font-size-to-fit-in-region-java
  private fun drawTextToFit(text: String, g: Graphics2D, maxRect: TextBlockRectangle) {
    val lines = text.split("\n")
    val linesCount = lines.count()

    val lineHeight = maxRect.h / linesCount
    val lineWidth = maxRect.w

    val longestLine = lines.maxByOrNull { it.length } ?: lines.first()

    g.font = Font("TimesRoman", Font.PLAIN, 16)
    g.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    )
    MainController.logger.info { "drawing text block of size ${text.length} to ${maxRect.x};${maxRect.y}" }

    val maxFontSize = getMaxFittingFontSize(g, g.font, longestLine, lineWidth, lineHeight)
    MainController.logger.info { "drawing text of size ${text.length} to ${maxRect.x};${maxRect.y};${maxRect.w};${maxRect.h}; computed size: $maxFontSize" }

    g.font = Font("TimesRoman", Font.PLAIN, maxFontSize)

    val x = maxRect.x + 3
    var y = maxRect.y - 3 + g.fontMetrics.height
    lines.forEach { line ->
      g.drawString(line, x, y)
      y += g.fontMetrics.height
    }
  }

  fun getMaxFittingFontSize(
    g: Graphics,
    font: Font,
    string: String?,
    width: Int,
    height: Int
  ): Int {
    var minSize = 6
    var maxSize = 72
    var curSize = font.size
    while (maxSize - minSize > 2) {
      val fm = g.getFontMetrics(Font(font.name, font.style, curSize))
      val fontWidth = fm.stringWidth(string)
      val fontHeight = fm.leading + fm.maxAscent + fm.maxDescent
      if ((fontWidth > width) || (fontHeight > height)) {
        maxSize = curSize
        curSize = (maxSize + minSize) / 2
      } else {
        minSize = curSize
        curSize = (minSize + maxSize) / 2
      }
    }
    return curSize
  }

  private fun pasteFromClipboard(flavor: DataFlavor?): Any? {
    val clipboard: Clipboard
    var result: Any?
    val content: Transferable
    result = null
    try {
      clipboard = Toolkit.getDefaultToolkit().systemClipboard
      content = clipboard.getContents(null) ?: return null
      if (content.isDataFlavorSupported(flavor)) result = content.getTransferData(flavor)
    } catch (e: Exception) {
      result = null
    }
    return result
  }
}