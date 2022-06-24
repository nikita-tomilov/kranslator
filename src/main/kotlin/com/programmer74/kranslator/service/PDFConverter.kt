package com.programmer74.kranslator.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.tools.imageio.ImageIOUtil
import java.io.File

object PDFConverter {

    fun convertPDFToImages(pdf: File): List<File> {
        val ans = ArrayList<File>()
        PDDocument.load(pdf).use { document ->
            val pdfRenderer = PDFRenderer(document)
            for (page in 0 until document.numberOfPages) {
                val bim = pdfRenderer.renderImageWithDPI(page, DPI * 1.0f, ImageType.RGB)
                val target = File(pdf.absolutePath + "-${page + 1}.png")
                ImageIOUtil.writeImage(bim, target.absolutePath, DPI)
                ans.add(target)
            }
        }
        return ans
    }

    public const val DPI: Int = 600
}
