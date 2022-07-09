package com.programmer74.kranslator.service.pdf

import com.lowagie.text.*
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfWriter
import com.programmer74.kranslator.ocr.TextBlockRectangle
import com.programmer74.kranslator.service.graphics.ImageUtils
import com.programmer74.kranslator.service.pdf.PDFParser.logger
import com.programmer74.kranslator.util.TextUtils
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.math.min

object PDFCreator {

  fun createViaText(target: File, paragraphs: List<MappedParagraph>) {
    val paragraphsPerPage = paragraphs.groupBy { it.page }.toMap()
    val document = Document()
    val outputStream = FileOutputStream(target)
    val fontFile = ResourcesFontFactory.getFontPath("Roboto-Regular").absolutePath
    val font = BaseFont.createFont(fontFile, BaseFont.IDENTITY_H, true)

    val writer = PdfWriter.getInstance(document, outputStream)
    document.open()
    //TODO: fix multiple pages
    //writer.isPageEmpty = false
    val cb: PdfContentByte = writer.directContent

    paragraphsPerPage.forEach { (page, paragraphsPerPage) ->
      writer.newPage()
      cb.beginText()
      val aw = document.pageSize.width // in points! the same as in font size
      val ah = document.pageSize.height // in points! the same as in font size
      //1 point = 1/72 inch
      paragraphsPerPage.forEach {
        val x = it.bounds.llx * aw
        val y = ah - it.bounds.lly * ah
        val w = it.bounds.width() * aw
        val h = it.bounds.height() * ah

        cb.saveState()

        cb.setLineWidth(0f)
        cb.rectangle(x, y, w, h)
        cb.stroke()

        val translatedParagraphLines = TextUtils.splitTranslatedParagraphToLines(
            it.translatedText,
            it.originalLines.map { l -> l.line })
        cb.setFontAndSize(font, maxFontSize(font, translatedParagraphLines, it.bounds, aw, ah))

        //due to weird coordinates behavior (e.g. sometimes y is lly, sometimes y is ury)
        //this has to be like this, even if it may seem wrong
        val ury = y + h
        var cy = ury
        val dy = h / translatedParagraphLines.size
        translatedParagraphLines.forEach { translatedLine ->
          cy -= dy
          cb.showTextAligned(PdfContentByte.ALIGN_LEFT, translatedLine, x, cy, 0.0f)
        }

        cb.restoreState()
      }
      cb.endText()
    }
    document.close()
  }

  fun createViaPNG(target: File, paragraphs: List<MappedParagraph>) {
    val a4w = 2480
    val a4h = 3508
    val paragraphsPerPage = paragraphs.groupBy { it.page }.toMap()
    val document = Document(RectangleReadOnly(a4w * 1.0f, a4h * 1.0f), 0.0f, 0.0f, 0.0f, 0.0f)
    val outputStream = FileOutputStream(target)

    PdfWriter.getInstance(document, outputStream)
    document.open()

    val pagesPNG = paragraphsPerPage.map { e ->
      val paragraphsForPage = e.value
      val img = BufferedImage(a4w, a4h, TYPE_INT_RGB)
      val g = img.createGraphics()
      g.color = Color.WHITE
      g.fillRect(0, 0, a4w, a4h)
      val pagePNG = Files.createTempFile("img", ".PNG").toFile()
      val translatedTexts = paragraphsForPage.map { it.translatedText }
      val textBlocks = paragraphsForPage.map {
        TextBlockRectangle(
            (it.bounds.llx * img.width).toInt(),
            (it.bounds.ury * img.height).toInt(),
            (it.bounds.width() * img.width).toInt(),
            (it.bounds.height() * img.height).toInt()
        )
      }
      val imprintedImage =
          ImageUtils.imprintTranslateResponseToImage(img, textBlocks, translatedTexts)
      ImageIO.write(imprintedImage, "PNG", pagePNG)
      pagePNG
    }

    pagesPNG.forEach { imageFile ->
      document.newPage()
      document.add(Image.getInstance(imageFile.absolutePath))
      Files.delete(imageFile.toPath())
    }
    document.close()
  }

  private fun maxFontSize(bf: BaseFont, lines: List<String>, bounds: MappedBounds, aw: Float, ah: Float): Float {
    val w = bounds.width() * aw
    val longestLine = lines.maxByOrNull { l -> bf.getWidth(l) }
    var fontSizeCurrent = 6.0f
    while (fontSizeCurrent < 72.0f) {
      val lineWidth = bf.getWidthPointKerned(longestLine, fontSizeCurrent)
      if (lineWidth > w) break
      fontSizeCurrent += 1.0f
    }

    fontSizeCurrent -= 1.0f
    return fontSizeCurrent
  }
}