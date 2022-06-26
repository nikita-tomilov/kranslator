package com.programmer74.kranslator.service

import java.awt.Graphics2D
import java.io.File
import javax.imageio.ImageIO

class PageRenderer(
  private val source: File
) {

  private val image = ImageIO.read(source)

  val graphics: Graphics2D = image.createGraphics()

  fun render(target: File) {
    ImageIO.write(image, "png", target)
  }
}
