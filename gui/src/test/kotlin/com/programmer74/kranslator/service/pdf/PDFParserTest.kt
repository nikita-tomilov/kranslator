package com.programmer74.kranslator.service.pdf

import com.lowagie.text.*
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.ColumnText
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfWriter
import com.programmer74.kranslator.service.graphics.ImageUtils
import com.programmer74.kranslator.service.graphics.SimpleBoundary
import com.programmer74.kranslator.service.pdf.PDFParser.extractLines
import com.programmer74.kranslator.service.pdf.PDFParser.extractParagraphs
import mu.KLogging
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO
import javax.swing.text.StyleConstants.FontFamily
import kotlin.math.abs
import kotlin.math.min

class PDFParserTest {

  @Test
  @Disabled
  fun dummy() {
    val ud = System.getProperty("user.dir")
    val file = File("$ud/src/test/resources/testpdf-saveaspdf.pdf")
    //val file = File("$ud/src/test/resources/testpdf-printedscanned.pdf")
    val para = PDFParser.extractParagraphs(file)
    val i = 1
  }

  @Test
  @Disabled
  fun linesCoordinatesDemo() {
    val ud = System.getProperty("user.dir")
    val file = File("$ud/src/test/resources/testpdf-saveaspdf.pdf")
    //val file = File("$ud/src/test/resources/testpdf-printedscanned.pdf")
    val lines = extractLines(file)
    val pagesPNG = PDFConverter.convertPDFToImages(file)

    val reprintedPagesPNG = pagesPNG.mapIndexed { index, pagePNG ->
      val page = index + 1
      val linesForPage = lines.filter { it.page == page }
      logger.warn { "for page $page got ${linesForPage.size} lines" }

      val originalImage = ImageIO.read(pagePNG)
      val reprintedPagePNG = File("$page-lines.PNG")
      val aw = originalImage.width // * 1.1
      val ah = originalImage.height // * 1.1
      val imprintedImage =
          ImageUtils.imprintBoundariesToImage(
              originalImage,
              linesForPage.map {
                SimpleBoundary(
                    (it.bounds.llx * aw).toInt(),
                    (it.bounds.lly * ah).toInt(),
                    (it.bounds.urx * aw).toInt(),
                    (it.bounds.ury * ah).toInt())
              })
      ImageIO.write(imprintedImage, "PNG", reprintedPagePNG)
      reprintedPagePNG
    }

    reprintedPagesPNG.forEach { logger.warn { it.absolutePath } }
  }

  @Test
  @Disabled
  fun paragraphCoordinatesDemo() {
    val ud = System.getProperty("user.dir")
    val file = File("$ud/src/test/resources/testpdf-saveaspdf.pdf")
    //val file = File("$ud/src/test/resources/testpdf-printedscanned.pdf")
    val paragraphs = extractParagraphs(file)
    val pagesPNG = PDFConverter.convertPDFToImages(file)

    val reprintedPagesPNG = pagesPNG.mapIndexed { index, pagePNG ->
      val page = index + 1
      val paragraphsForPage = paragraphs.filter { it.page == page }
      logger.warn { "for page $page got ${paragraphsForPage.size} paragraphs" }

      val originalImage = ImageIO.read(pagePNG)
      val reprintedPagePNG = File("$page-para.PNG")
      val aw = originalImage.width // * 1.1
      val ah = originalImage.height // * 1.1
      val imprintedImage =
          ImageUtils.imprintBoundariesToImage(
              originalImage,
              paragraphsForPage.map {
                SimpleBoundary(
                    (it.bounds.llx * aw).toInt(),
                    (it.bounds.lly * ah).toInt(),
                    (it.bounds.urx * aw).toInt(),
                    (it.bounds.ury * ah).toInt())
              })
      ImageIO.write(imprintedImage, "PNG", reprintedPagePNG)
      reprintedPagePNG
    }

    reprintedPagesPNG.forEach { logger.warn { it.absolutePath } }
  }

  @Test
  @Disabled
  fun columnTextDemo() {
    val ud = System.getProperty("user.dir")
    val file = File("$ud/src/test/resources/testpdf-saveaspdf.pdf")
    //val file = File("$ud/src/test/resources/testpdf-printedscanned.pdf")
    val paragraphs = PDFParser.extractParagraphs(file)
    val paragraphsPerPage = paragraphs.groupBy { it.page }.toMap()

    val target = File(file.absolutePath + "-2.pdf")
    val document = Document()
    val outputStream = FileOutputStream(target)

    val writer = PdfWriter.getInstance(document, outputStream)
    document.open()
    writer.isPageEmpty = false
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

        val longestLine = it.lines.maxByOrNull { l -> l.bounds.width() }!!
        var fontSizeCurrent = 6.0f
        while (fontSizeCurrent < 72.0f) {
          val font = Font(Font.HELVETICA, fontSizeCurrent)
          val chunk = Chunk(longestLine.line, font)
          if (chunk.widthPoint >= w) break
          fontSizeCurrent += 1.0f
        }
        fontSizeCurrent -= 1.0f
        fontSizeCurrent = min(fontSizeCurrent, (it.bounds.height() * ah))

        logger.debug { "estimated font size: $fontSizeCurrent for height ${it.bounds.height() * ah}" }
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

    logger.warn { target.absolutePath }
  }

  companion object : KLogging()
}