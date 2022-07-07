package com.programmer74.kranslator.service.pdf

import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.tools.imageio.ImageIOUtil
import java.io.File
import java.nio.file.Files

object PDFToImageConverter {

  fun convertPDFToImages(pdf: File, logCallback: (String) -> Unit = {}): List<File> {
    val ans = ArrayList<File>()
    val doc = PDDocument.load(pdf, MemoryUsageSetting.setupTempFileOnly())
    doc.use { document ->
      val pages = document.numberOfPages
      logCallback("Document has $pages pages")
      val pdfRenderer = PDFRenderer(document)
      for (page in 0 until pages) {
        logCallback("Converting page ${page + 1}/$pages to image")
        val bim = pdfRenderer.renderImageWithDPI(page, DPI * 1.0f, ImageType.RGB)
        val target = Files.createTempFile("${page + 1}", ".png").toFile()
        ImageIOUtil.writeImage(bim, target.absolutePath, DPI)
        ans.add(target)
      }
    }
    logCallback("Done creating ${ans.size} images")
    return ans
  }

  public const val DPI: Int = 600
}
