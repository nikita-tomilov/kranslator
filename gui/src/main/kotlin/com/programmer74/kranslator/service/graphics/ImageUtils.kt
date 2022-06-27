package com.programmer74.kranslator.service.graphics

import java.awt.Graphics
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage

object ImageUtils {

  fun pasteImageFromClipboard(): BufferedImage? {
    var result: BufferedImage?
    val width: Int
    val height: Int
    val g: Graphics
    result = null
    val img: Image? = pasteFromClipboard(DataFlavor.imageFlavor) as Image?
    if (img != null) {
      width = img.getWidth(null)
      height = img.getHeight(null)
      result = BufferedImage(
          width, height,
          BufferedImage.TYPE_INT_RGB)
      g = result.createGraphics()
      g.drawImage(img, 0, 0, null)
      g.dispose()
    }
    return result
  }

  private fun pasteFromClipboard(flavor: DataFlavor?): Any? {
    val clipboard: Clipboard
    var result: Any?
    val content: Transferable
    result = null
    try {
      clipboard = Toolkit.getDefaultToolkit().systemClipboard
      content = clipboard.getContents(null) ?: return null
      if (content.isDataFlavorSupported(flavor)) result = content.getTransferData(flavor)
    } catch (e: Exception) {
      result = null
    }
    return result
  }
}