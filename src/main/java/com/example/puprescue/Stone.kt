package com.example.puprescue

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import java.util.*

class Stone(
  private val context: Context,
  private val resId: Int,
  private val viewWidth: Int,
  private val screenHeight: Int,
) {
  var x: Float = 0f
  var y: Float = 0f
  private val random: Random = Random()
  val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, resId)

  init {
    resetPosition()
  }

  fun draw(canvas: Canvas) {
    // Draw the stone if bitmap is loaded successfully
    bitmap?.let {
      canvas.drawBitmap(it, x, y, null)
    }
    // Move the stone downwards
    y += 3 // the falling speed
      // Reset position if stone reaches the bottom
//    if (y >= screenHeight) {
//      points += 10
//      resetPosition()
//    }
  }

    fun resetPosition() {
      // Reset stone position to fall from above randomly
      x = random.nextInt(viewWidth - bitmap!!.width).toFloat()
      y = -bitmap.height.toFloat()
    }

}