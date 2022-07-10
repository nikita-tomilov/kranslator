package com.programmer74.kranslator.service.pdf

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class MappedBounds(
  val llx: Float,
  val lly: Float,
  val urx: Float,
  val ury: Float
) {
  override fun toString(): String {
    return "$llx;$lly - $urx;$ury"
  }

  fun width() = abs(llx - urx)
  fun height() = abs(lly - ury)

  companion object {
    fun <T> from(
      collection: List<T>,
      llxFunc: (T) -> Float,
      llyFunc: (T) -> Float,
      urxFunc: (T) -> Float,
      uryFunc: (T) -> Float
    ): MappedBounds {

      if (collection.size == 1) {
        val e = collection.single()
        return MappedBounds(llxFunc(e), llyFunc(e), urxFunc(e), uryFunc(e))
      }
      //TODO: optimize
      val minByX = collection.minByOrNull { llxFunc(it) }!!
      val minByY = collection.minByOrNull { llyFunc(it) }!!
      val maxByX = collection.maxByOrNull { urxFunc(it) }!!
      val maxByY = collection.maxByOrNull { uryFunc(it) }!!

      val llx = llxFunc(minByX)
      val lly = llyFunc(minByY)
      val urx = urxFunc(maxByX)
      val ury = uryFunc(maxByY)

      return MappedBounds(llx, lly, urx, ury)
    }
  }
}