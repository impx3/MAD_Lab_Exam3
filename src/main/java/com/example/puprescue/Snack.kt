package com.example.puprescue

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.Random
import android.graphics.Canvas
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import java.util.*

class Snack(
    private val context: Context,
    private val resId: Int,
    private val viewWidth: Int,
    private val screenHeight: Int,

) {
    var x: Float = 0f
    var y: Float = 0f
    var points: Int = 0
    val random: Random = Random()
    val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, resId)

  init {
//    resetPosition()
  }

  fun draw(canvas: Canvas) {
	if (bitmap != null) {
    		// Draw the snack if bitmap is loaded successfully
   		bitmap?.let {
      			canvas.drawBitmap(it, x, y, null)
    		}
	}
    // Move the snack downwards
    y += 3 // the falling speed
    // Reset position if snack reaches the bottom
    if (y >= screenHeight) {
      resetPosition()
    }
  }

  private fun resetPosition() {
    // Reset snack position to fall from above randomly
    x = random.nextInt(viewWidth - bitmap!!.width).toFloat()
    y = -bitmap.height.toFloat()
  }
}
