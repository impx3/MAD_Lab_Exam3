package com.example.puprescue

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Bundle
import android.os.Build
import android.util.DisplayMetrics
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import java.util.ArrayList
import java.util.Random
import java.util.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
//GameView(context: Context, attrs: AttributeSet?) : View(context, attrs)
class GameView(context: Context) : View(context) {

    private val ground: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ground)
    private val shibaInu: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.shiba_inu)
    private val rectGround: Rect
    private val UPDATE_MILLIS: Long = 30
    private val textPaint: Paint = Paint()
    private val healthPaint: Paint = Paint()
    private val TEXT_SIZE: Float = 120f
    private var points: Int = 0
    private var life: Int = 3
    private val dWidth: Int
    private val dHeight: Int
    private val random: Random = Random()
    private var shibaInuX: Float
    private val shibaInuY: Float
    private var oldShibaInuX: Float = 0f
    private val stones: MutableList<Stone> = mutableListOf()
    private val snacks: MutableList<Snack> = mutableListOf()
    private val stoneWidth = 150
    private val stoneHeight = 176

    private var backgroundDay: Bitmap? = null
    private var backgroundNight: Bitmap? = null
    private var stoneBitmap: Bitmap? = null
    private var snackBitmap: Bitmap? = null
    private var hurtBitmap: Bitmap? = null
    private var isDaytime = true
    private var isShibaInuHurt = false
    private val handler = Handler()
    val typeface = ResourcesCompat.getFont(context, R.font.copyduck)
//    val typeface = Typeface.createFromAsset(context.assets, "@font")

    init {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        dWidth = displayMetrics.widthPixels
        dHeight = displayMetrics.heightPixels
        rectGround = Rect(0, dHeight - ground.height, dWidth, dHeight)
        textPaint.color = Color.rgb(255, 165, 0)
        textPaint.textSize = TEXT_SIZE
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = typeface

        healthPaint.color = Color.GREEN
        shibaInuX = (dWidth / 2 - shibaInu.width / 2).toFloat()
        shibaInuY = (dHeight - ground.height - shibaInu.height).toFloat()

        backgroundDay = BitmapFactory.decodeResource(resources, R.drawable.day_bg1)
        backgroundNight = BitmapFactory.decodeResource(resources, R.drawable.night_bg)
        stoneBitmap = BitmapFactory.decodeResource(resources, R.drawable.stone)
        hurtBitmap = BitmapFactory.decodeResource(resources, R.drawable.hurt)
        snackBitmap = BitmapFactory.decodeResource(resources, R.drawable.snack)

        backgroundDay?.let {
            backgroundDay = Bitmap.createScaledBitmap(it, dWidth, dHeight, true)
        }
        backgroundNight?.let {
            backgroundNight = Bitmap.createScaledBitmap(it, dWidth, dHeight, true)
        }
        stoneBitmap?.let {
            stoneBitmap = Bitmap.createScaledBitmap(it, stoneWidth, stoneHeight, true)
        }
        hurtBitmap?.let {
            hurtBitmap = Bitmap.createScaledBitmap(it, stoneWidth, stoneHeight, true)
        }
        snackBitmap?.let {
            snackBitmap = Bitmap.createScaledBitmap(it, stoneWidth, stoneHeight, true)
        }
    }

    fun startGame() {
        startBackgroundSwitch()
        startSnackDropping()
        startStoneDropping()
    }

    fun stopGame() {
        stopBackgroundSwitch()
        stopSnackDropping()
        stopStoneDropping()
        handler.removeCallbacks(runnable)
        val intent = Intent(context, GameOver::class.java)
        intent.putExtra("points", points)
        context.startActivity(intent)
        (context as Activity).finish()
//        return
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw the appropriate background based on time
        if (isDaytime) {
            backgroundDay?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        } else {
            backgroundNight?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        }

        canvas.drawBitmap(ground, null, rectGround, null)
        canvas.drawBitmap(shibaInu, shibaInuX, shibaInuY, null)
        
        // Draw snacks falling from above
        snacks.forEach { it.draw(canvas) }

        // Draw stones falling from above
        stones.forEach { it.draw(canvas) }

        // Draw hurt sign above the ShibaInu if it is hurt
        if (isShibaInuHurt) {
            hurtBitmap?.let {
                canvas.drawBitmap(it, shibaInuX, shibaInuY - it.height, null)
            }
        }

        when (life) {
            2 -> healthPaint.color = Color.YELLOW
            1 -> healthPaint.color = Color.RED
        }
        canvas.drawRect(dWidth - 200f, 30f, dWidth - 200 + 60 * life.toFloat(), 80f, healthPaint)
        canvas.drawText("$points", 20f, TEXT_SIZE, textPaint)
        handler.postDelayed(runnable, UPDATE_MILLIS)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        if (touchY >= shibaInuY) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldShibaInuX = event.x
                }
                MotionEvent.ACTION_MOVE -> {
                    val shift = oldShibaInuX - touchX
                    val newShibaInuX = shibaInuX - shift
                    shibaInuX = when {
                        newShibaInuX <= 0 -> 0f
                        newShibaInuX >= dWidth - shibaInu.width -> (dWidth - shibaInu.width).toFloat()
                        else -> newShibaInuX
                    }
                }
            }
        }
        return true
    }

    private val runnable = Runnable { invalidate() }

    //background change
    private val backgroundSwitchRunnable = object : Runnable {
        override fun run() {
            // Switch background and schedule the next switch
            isDaytime = !isDaytime
            invalidate()
            handler.postDelayed(this, 30000) // Switch every 30 seconds
        }
    }

    private val snackDropRunnable = object : Runnable {
        override fun run() {
            // Create a new snack and add it to the list
            val snack = Snack(context, R.drawable.snack, dWidth, dHeight)
            snacks.add(snack)
            invalidate()
            isShibaInuHurt = false
            if (snacks.any { it.y + it.bitmap.height >= shibaInuY && it.x + it.bitmap.width >= shibaInuX && it.x <= shibaInuX + shibaInu.width.toFloat() }) {
                points += 10
            }
            handler.postDelayed(this, 10000) // Drop a new snack every 10 seconds
        }
    }

    private val stoneDropRunnable = object : Runnable {
        override fun run() {
            // Create a new stone and add it to the list

            val stone = Stone(context, R.drawable.stone, dWidth, dHeight)
            stones.add(stone)
            invalidate()

            // Check if any stone hits the ShibaInu
            if (stones.any { it.y + it.bitmap.height >= shibaInuY && it.x + it.bitmap.width >= shibaInuX && it.x <= shibaInuX + shibaInu.width.toFloat() }) {
                // If a stone hits the ShibaInu, set isShibaInuHurt to true
                isShibaInuHurt = true
                life--
                handler.postDelayed({ resetShibaInuHurt() }, 1000)
            }
            if (life == 0) {
                stopGame()
//                val intent = Intent(context, GameOver::class.java)
//                intent.putExtra("points", points)
//                context.startActivity(intent)
//                (context as Activity).finish()
//                return
            }
            stones.removeIf { stone ->
                stone.y + stone.bitmap.height >= rectGround.top  // Check if stone hits ground image
            }
            points += 10
            handler.postDelayed(this, 10000) // Drop a new stone every 10 seconds
        }
    }

    fun startBackgroundSwitch() {
        // Start the background switching process
        handler.post(backgroundSwitchRunnable)
    }

    fun stopBackgroundSwitch() {
        // Stop the background switching process
        handler.removeCallbacks(backgroundSwitchRunnable)
    }

    fun startStoneDropping() {
        // Start dropping stones
        handler.post(stoneDropRunnable)
    }

    fun stopStoneDropping() {
        // Stop dropping stones
        handler.removeCallbacks(stoneDropRunnable)
    }

    fun startSnackDropping() {
        handler.post(snackDropRunnable)
    }

    fun stopSnackDropping() {
        handler.removeCallbacks(snackDropRunnable)
    }

    fun resetShibaInuHurt() {
        // Reset the hurt flag
        isShibaInuHurt = false
    }

}