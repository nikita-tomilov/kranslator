package com.programmer74.kranslator.service.pdf

import kotlin.math.abs

data class MappedBounds(
  val x: Float,
  val y: Float,
  val w: Float,
  val h: Float
) {
  override fun toString(): String {
    return "$x;$y - $w;$h"
  }

  companion object {
    fun <T> from(
      collection: List<T>,
      xFunc: (T) -> Float,
      yFunc: (T) -> Float,
      wFunc: (T) -> Float,
      hFunc: (T) -> Float
    ): MappedBounds {

      if (collection.size == 1) {
        val e = collection.single()
        return MappedBounds(xFunc(e), yFunc(e), wFunc(e), hFunc(e))
      }
      //TODO: optimize
      val minByX = collection.minByOrNull { xFunc(it) }!!
      val minByY = collection.minByOrNull { yFunc(it) }!!
      val maxByX = collection.maxByOrNull { xFunc(it) + wFunc(it) }!!
      val maxByY = collection.maxByOrNull { yFunc(it) + hFunc(it) }!!

      val x = xFunc(minByX)
      val y = yFunc(minByY)
      val w = xFunc(maxByX) + wFunc(maxByX) - x
      val h = yFunc(maxByY) + hFunc(maxByY) - y

      return MappedBounds(x, y, w, h)
    }

    fun from(collection: List<MappedBounds>): MappedBounds {
      return from(collection, { it.x }, { it.y }, { it.w }, { it.h })
    }
  }
}