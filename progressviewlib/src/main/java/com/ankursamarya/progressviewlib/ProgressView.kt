package com.ankursamarya.progressviewlib

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat


class ProgressView : View {
    companion object {
        const val ANIMATION_DURATION = 600L
        const val CIRCLE_WIDTH = 20f
    }

    private var circleWidth = CIRCLE_WIDTH
    private val thumbRadius = circleWidth.times(1.5f)
    private val outerMargin = circleWidth.times(2)
    private var thumnbAngle = 0f;

    private val circlePaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = circleWidth
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.circle_color);
    }

    private val thumbPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.tumnb_color);
        isAntiAlias = true
        strokeWidth = thumbRadius
    }

    private val progressPaint = Paint(circlePaint).apply {
        color = ContextCompat.getColor(context, R.color.progress_color);
    }

    private val progressBound = RectF()
    private val progressPath = Path()
    private var progress: Int = 0
    private var currentProgress: Float = 0f

    private val progressAnimator = ValueAnimator.ofFloat().apply {
        duration = ANIMATION_DURATION
        addUpdateListener {
            thumnbAngle = it.animatedValue.toString().toFloat()
            currentProgress = angleToProgress(thumnbAngle)
            invalidate()
        }
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        //enable the view saving
        isSaveEnabled = true
        setAttrValue(context, attrs, defStyleAttr, defStyleRes)
    }

    /**
     * set attribute from xml
     * */
    private fun setAttrValue(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        val typedValue =
            context?.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.ProgressView,
                defStyleAttr,
                defStyleRes
            );
        typedValue?.let {

            val circleColor = it.getColor(R.styleable.ProgressView_circleColor, -1)
            if (circleColor > -1) {
                circlePaint.color = circleColor
            }

            val thumbColor = it.getColor(R.styleable.ProgressView_thumbColor, -1)
            if (thumbColor > -1) {
                thumbPaint.color = thumbColor
            }

            val progressColor = it.getColor(R.styleable.ProgressView_progressColor, -1)
            if (progressColor > -1) {
                progressPaint.color = progressColor
            }
        }
    }

    /*
    * restore the progress
    * */
    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        setProgress(savedState.progress)
    }

    /**
     * set progress and start animation from current progress
     * */
    fun setProgress(progress: Int) {
        this.progress = progress

        if (progressAnimator.isRunning) {
            progressAnimator.cancel()
        }
        progressAnimator.setFloatValues(
            progressToAngle(currentProgress),
            progressToAngle(progress.toFloat())
        )
        progressAnimator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val heightSize = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom

        val defaultWidthSize = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val defaultHeightSize = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)

        val width: Int
        val height: Int

        if (widthSize < heightSize) {
            width = defaultWidthSize

            height = when (heightMode) {
                MeasureSpec.EXACTLY -> defaultHeightSize
                MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> widthSize + paddingTop + paddingBottom
                else -> widthSize + paddingTop + paddingBottom
            }
        } else {
            width = when (widthMode) {
                MeasureSpec.EXACTLY -> defaultWidthSize
                MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> heightSize + paddingLeft + paddingRight
                else -> widthSize + paddingLeft + paddingRight
            }
            height = defaultHeightSize
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        val effectiveWidth = width - paddingLeft - paddingRight
        val effectiveHeight = height - paddingTop - paddingBottom

        val radius = (Math.min(effectiveHeight, effectiveWidth) - 2 * outerMargin).div(2f)

        val centerX = radius + paddingLeft + outerMargin
        val centerY = radius + paddingTop + outerMargin

        canvas?.drawCircle(centerX, centerY, radius, circlePaint)

        drawProgressArc(canvas, radius)

        drawThumb(canvas, radius, centerX, centerY)
    }

    /*
    * save the progress
    * */
    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val myState = SavedState(superState)
        myState.progress = this.progress
        return myState
    }

    /**
     * draws progress arc
     * */
    private fun drawProgressArc(canvas: Canvas?, radius: Float) {
        if (progressBound.isEmpty) {
            progressBound.left = (paddingLeft + outerMargin)
            progressBound.top = (paddingTop + outerMargin)
            progressBound.right = (paddingLeft + outerMargin) + radius.times(2)
            progressBound.bottom = (paddingTop + outerMargin) + radius.times(2)
        }
        progressPath.reset()
        progressPath.addArc(progressBound, 270f, thumnbAngle)
        canvas?.drawPath(progressPath, progressPaint)
    }

    /**
     * draws thumb
     * */
    private fun drawThumb(canvas: Canvas?, radius: Float, centerX: Float, centerY: Float) {
        val xy = getThumbXY(90 - thumnbAngle, radius, centerX, centerY)
        canvas?.drawCircle(xy[0], xy[1], thumbRadius, thumbPaint)
    }

    /**
     * @return will return thumb x and Y on canvas
     * @param angle angle with x axis
     * @param radius radius of progress circle
     * @param cX center of progress circle
     * @param cY center of progress circle
     */
    private fun getThumbXY(angle: Float, radius: Float, cX: Float, cY: Float): Array<Float> {
        val y = cY - radius * Math.sin(Math.toRadians(angle.toDouble()))
        val x = cX + radius * Math.cos(Math.toRadians(angle.toDouble()))
        return arrayOf<Float>(x.toFloat(), y.toFloat())
    }

    /**
     * @param progress 0 to 100
     * @return angle 0 to 360
     * */
    private fun progressToAngle(progress: Float): Float {
        return progress.times(360).div(100f)
    }

    /**
     * @param angle 0 to 360
     * @return 0 to 100
     * */
    private fun angleToProgress(angle: Float): Float {
        return angle.times(100).div(360f)
    }

    /*
    * used for saving the sate
    * */
    private class SavedState : BaseSavedState {
        var progress = 0

        internal constructor(superState: Parcelable?) : super(superState) {}
        private constructor(input: Parcel) : super(input) {
            progress = input.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(progress)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState?> =
                object : Parcelable.Creator<SavedState?> {
                    override fun createFromParcel(input: Parcel): SavedState? {
                        return SavedState(input)
                    }

                    override fun newArray(size: Int): Array<SavedState?> {
                        return arrayOfNulls<SavedState>(size)
                    }
                }
        }
    }
}