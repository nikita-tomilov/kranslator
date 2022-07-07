package com.programmer74.kranslator.service.pdf

import com.lowagie.text.*
import com.lowagie.text.pdf.ColumnText
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfWriter
import com.programmer74.kranslator.ocr.TextBlockRectangle
import com.programmer74.kranslator.service.graphics.ImageUtils
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.math.min

object PDFCreator {

  fun createViaColumnText(target: File, paragraphs: List<MappedParagraph>) {
    val paragraphsPerPage = paragraphs.groupBy { it.page }.toMap()
    val document = Document()
    val outputStream = FileOutputStream(target)

    val writer = PdfWriter.getInstance(document, outputStream)
    document.open()
    //TODO: fix multiple pages
    //writer.isPageEmpty = false
    val cb: PdfContentByte = writer.directContent

    paragraphsPerPage.forEach { (page, paragraphsPerPage) ->
      writer.newPage()
      cb.beginText()
      val aw = document.pageSize.width
      val ah = document.pageSize.height
      paragraphsPerPage.forEach {
        val p = Phrase(it.text + "\n")
        val x = it.bounds.llx * aw
        val y = ah - it.bounds.lly * ah
        val w = it.bounds.width() * aw
        val h = it.bounds.height() * ah

        cb.saveState()

        val ct = ColumnText(cb)
        cb.setLineWidth(0f)
        cb.rectangle(x, y, w, h)
        cb.stroke()

        val avgLineLength = it.text.length / it.lines.size
        //val roughlyMaxLineLength = avgLineLength * 1.2
        val longestLine =  it.lines.maxByOrNull { l -> l.bounds.width() }!!.line
        //val longestLine = it.text.chunked(roughlyMaxLineLength.toInt()).first()
        var fontSizeCurrent = 6.0f
        while (fontSizeCurrent < 72.0f) {
          val font = Font(Font.HELVETICA, fontSizeCurrent)
          val chunk = Chunk(longestLine, font)
          if (chunk.widthPoint >= w) break
          fontSizeCurrent += 1.0f
        }
        fontSizeCurrent -= 1.0f
        fontSizeCurrent = min(fontSizeCurrent, (it.bounds.height() * ah))

        //logger.debug { "estimated font size: $fontSizeCurrent for height ${it.bounds.height() * ah}" }
        val fontSize = fontSizeCurrent
        val leading = if (it.lines.size > 1) fontSize * 1.2f else fontSize

        val urx = x + w
        val ury = y + h

        p.font.size = fontSize
        ct.setSimpleColumn(p, x, y, urx, ury, leading, Element.ALIGN_LEFT)
        ct.go()
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
      g.fillRect(0,0, a4w, a4h)
      val pagePNG = Files.createTempFile("img", ".PNG").toFile()
      val translatedTexts = paragraphsForPage.map { it.text }
      val textBlocks = paragraphsForPage.map { TextBlockRectangle(
          (it.bounds.llx * img.width).toInt(),
          (it.bounds.ury * img.height).toInt(),
          (it.bounds.width() * img.width).toInt(),
          (it.bounds.height() * img.height).toInt()
      ) }
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
}