package com.programmer74.kranslator.service.pdf

import com.programmer74.kranslator.service.graphics.ImageUtils
import com.programmer74.kranslator.service.graphics.SimpleBoundary
import com.programmer74.kranslator.service.pdf.PDFParser.extractLines
import com.programmer74.kranslator.service.pdf.PDFParser.extractParagraphs
import com.programmer74.kranslator.util.TextUtils
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
  fun linesCoordinatesDemo() {
    val ud = System.getProperty("user.dir")
    val file = File("$ud/src/test/resources/testpdf-saveaspdf.pdf")
    //val file = File("$ud/src/test/resources/testpdf-printedscanned.pdf")
    val lines = extractLines(file)
    val pagesPNG = PDFToImageConverter.convertPDFToImages(file)

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
    val paragraphs = extractParagraphs(file)
    val pagesPNG = PDFToImageConverter.convertPDFToImages(file)

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
  fun paragraphSmartSplitByLines() {
    val ud = System.getProperty("user.dir")
    val file = File("$ud/src/test/resources/testpdf-saveaspdf.pdf")
    val paragraphs = extractParagraphs(file)[4]
    val originalTextLines = paragraphs.originalLines.map { it.line }
    val translatedText = originalTextLines.joinToString(" ")

    val translatedTextSplitted = TextUtils.splitTranslatedParagraphToLines(translatedText, originalTextLines)
    val i = 1
  }

  @Test
  fun pdfBuildWorks() {
    val ud = System.getProperty("user.dir")
    val file = File("$ud/src/test/resources/testpdf-saveaspdf.pdf")
    val paragraphs = extractParagraphs(file).mapIndexed { index, it -> it.copy(page = index + 1) }
    PDFCreator.createViaText(File(file.absolutePath + "-2.pdf"), paragraphs)
  }

  @Test
  @Disabled
  fun columnTextDemo() {
    val ud = System.getProperty("user.dir")
    val file = File("$ud/src/test/resources/testpdf-saveaspdf.pdf")
    val paragraphs = extractParagraphs(file)

    val target = File(file.absolutePath + "-2.pdf")
    PDFCreator.createViaText(target, paragraphs)

    logger.warn { target.absolutePath }
  }

  @Test
  @Disabled
  fun directPNGDemo() {
    val ud = System.getProperty("user.dir")
    val file = File("$ud/src/test/resources/testpdf-saveaspdf.pdf")
    val paragraphs = extractParagraphs(file)

    val target = File(file.absolutePath + "-2.pdf")
    PDFCreator.createViaPNG(target, paragraphs)

    logger.warn { target.absolutePath }
  }

  companion object : KLogging()
}