package com.programmer74.kranslator.service.pdf

import com.programmer74.kranslator.ocr.TextBlock
import com.programmer74.kranslator.ocr.TextBlockRectangle
import com.programmer74.kranslator.service.graphics.ImageUtils
import mu.KLogging
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
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
      val aw = originalImage.width// * 1.1
      val ah = originalImage.height// * 1.1
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

  companion object : KLogging()
}