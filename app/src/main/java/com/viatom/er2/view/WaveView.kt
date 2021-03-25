package com.viatom.er2.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.viatom.er2.R


class WaveView : View {

    interface Ga{
        fun yes(x:Int,y:Int)
    }
    fun setG(g:Ga){
       ga=g
    }
    var ga:Ga?=null
    var canvas: Canvas? = null
    private val wavePaint = Paint()
    private val bgPaint = Paint()


    private val drawSize=500
    val data=IntArray(drawSize){
        0
    }
    var drawFra:Int=1

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    private fun init() {
        wavePaint.apply {
            color = getColor(R.color.wave_color)
            style = Paint.Style.FILL
            strokeWidth = 6.0f
        }

        bgPaint.apply {
            color=getColor(R.color.gray)
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
        }



    }

    var ixn=0;
    lateinit var backG: Bitmap
   lateinit var backC: Canvas
    lateinit var forC: Canvas
    lateinit var forG: Bitmap
    lateinit var w:Rect
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(ixn==0) {
            ixn= 1;
            backG = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            forG = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            w= Rect(0,0,width,height)
            backC = Canvas(backG)
            backC.drawARGB(255, 255, 255, 255)
            forC = Canvas(forG)
            val h1=35
            val h2=height/h1
            for(k in 0 until h1+2){
               backC.drawLine(0f,k*h2.toFloat(),width.toFloat(),k*h2.toFloat(),bgPaint)
            }

            val w1=60
            val w2=width/w1
            for(k in 0 until w1+2){
                backC.drawLine(k*w2.toFloat(),0f,k*w2.toFloat(),height.toFloat(),bgPaint)
            }


            drawFra=width/drawSize

        }
        forC.drawBitmap(backG,w, w,wavePaint)

        for((index,h) in data.withIndex()){
            if(index==data.size-1){
                break;
            }
            forC.drawLine(4*index.toFloat(),height/2- h.toFloat(),4*(index+1.toFloat()),height/2-data[index+1].toFloat(),wavePaint)
        }

        canvas.drawBitmap(forG, w, w,wavePaint)
    }


    private fun drawWave(canvas: Canvas) {
        canvas.drawColor(getColor(R.color.black))
    }


    private fun getColor(resource_id: Int): Int {
        return ContextCompat.getColor(context, resource_id)
    }
}