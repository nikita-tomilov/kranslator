package com.programmer74.kranslator.service.pdf

import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.ColumnText
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfWriter
import com.programmer74.kranslator.ocr.TextBlock
import com.programmer74.kranslator.ocr.TextBlockRectangle
import com.programmer74.kranslator.service.graphics.ImageUtils
import mu.KLogging
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO

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
  fun dummy2() {
    val ud = System.getProperty("user.dir")
    val file = File("$ud/src/test/resources/testpdf-saveaspdf.pdf")
    //val file = File("$ud/src/test/resources/testpdf-printedscanned.pdf")
    val paragraphs = PDFParser.extractParagraphs(file)
    val pagesPNG = PDFConverter.convertPDFToImages(file)

    val reprintedPagesPNG = pagesPNG.mapIndexed { index, pagePNG ->
      val page = index + 1
      val paragraphsForPage = paragraphs.filter { it.page == page }
      logger.warn { "for page $page got ${paragraphsForPage.size} paragraphs" }

      val originalImage = ImageIO.read(pagePNG)
      val reprintedPagePNG = File("$page.PNG")
      val aw = originalImage.width // * 1.1
      val ah = originalImage.height // * 1.1
      val imprintedImage =
          ImageUtils.imprintTranslateResponseToImage(
              originalImage,
              paragraphsForPage.map {
                TextBlock(
                    "",
                    TextBlockRectangle(
                        (it.bounds.x * aw).toInt(),
                        (it.bounds.y * ah).toInt(),
                        (it.bounds.w * aw).toInt(),
                        (it.bounds.h * ah).toInt()))
              },
              paragraphsForPage.map { it.text })
      ImageIO.write(imprintedImage, "PNG", reprintedPagePNG)
      reprintedPagePNG
    }

    reprintedPagesPNG.forEach { logger.warn { it.absolutePath } }
  }

  @Test
  @Disabled
  fun dummy3() {
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

    val bf: BaseFont =
        BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)

    val coeff = 1.1

    paragraphsPerPage.forEach { (page, paragraphsPerPage) ->
      writer.newPage()

      //cb.beginText()
      cb.setFontAndSize(bf, 10.0f)

      val ct = ColumnText(cb)
      var i = 0

      val aw = document.pageSize.width
      val ah = document.pageSize.height
      val ew = (document.pageSize.width * coeff).toFloat()
      val eh = (document.pageSize.height * coeff).toFloat()

      paragraphsPerPage.forEach {
        i++
        val p = Phrase(it.text)
        ct.setSimpleColumn(
            p,
            it.bounds.x * aw,
            ah - (it.bounds.y + it.bounds.h) * eh,
            (it.bounds.x + it.bounds.w) * ew,
            ah - (it.bounds.y * ah),
            10.0f,
            Element.ALIGN_LEFT)
//        cb.showTextAligned(
//            PdfContentByte.ALIGN_LEFT,
//            it.text,
//            it.bounds.x * document.pageSize.width,
//            document.pageSize.height - it.bounds.y * document.pageSize.height,
//            0.0f)
        ct.go()
      }
      //cb.endText()
    }
    document.close()

    logger.warn { target.absolutePath }
  }

  companion object : KLogging()
}