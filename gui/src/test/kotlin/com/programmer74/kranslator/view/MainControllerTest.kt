package com.programmer74.kranslator.view

import com.programmer74.kranslator.service.pdf.PDFParser.logger
import com.programmer74.kranslator.translate.TranslatorLanguage
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class MainControllerTest {

  private val controller = MainController(true)

  @Test
  fun pdfParsingDemo() {
    val file = System.getenv("file")
    val result = AtomicReference<File>()
    val latch = CountDownLatch(1)
    controller.translate(File(file), TranslatorLanguage.DE, TranslatorLanguage.DE,
        { logger.warn { it } },
        { ob -> logger.warn { ob } },
        { /*tb -> logger.warn { tb }*/ },
        {
          result.set(it)
          latch.countDown()
        })
    latch.await()
    logger.warn { result.get() }
  }
}