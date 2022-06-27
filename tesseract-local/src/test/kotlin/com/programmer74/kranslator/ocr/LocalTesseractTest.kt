package com.programmer74.kranslator.ocr

import mu.KLogging
import net.sourceforge.tess4j.ITessAPI
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.util.Files
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class LocalTesseractTest {

  @Test
  fun ocrWorks() {
    //given
    val img = testPicture()
    logger.info { img }
    //when
    val parsed = LocalTesseract().recognize(
        img,
        600,
        OCRLanguage.EN,
        ITessAPI.TessPageIteratorLevel.RIL_PARA)
    //then
    assertThat(parsed.single().text.trim()).isEqualTo("Hello")
  }

  private fun testPicture(): File {
    val file = File(Files.newTemporaryFile().absolutePath + ".png")
    val image = BufferedImage(200, 20, BufferedImage.TYPE_INT_RGB)
    val g: Graphics2D = image.createGraphics()
    g.color = Color.WHITE
    g.fillRect(0, 0, 200, 20)
    g.color = Color.BLACK
    g.drawString("Hello", 10, 15)
    ImageIO.write(image, "png", file)
    return file
  }

  companion object : KLogging()
}
