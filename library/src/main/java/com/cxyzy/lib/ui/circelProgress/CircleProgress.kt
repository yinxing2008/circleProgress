package com.cxyzy.lib.ui.circelProgress

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.cxyzy.lib.ui.circleProgress.R
import kotlin.math.abs

class CircleProgress(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val ANTI_ALIAS = true
    private val DEFAULT_SIZE = 150
    private val DEFAULT_START_ANGLE = 270
    private val DEFAULT_SWEEP_ANGLE = 360
    private val DEFAULT_ANIM_TIME = 1500
    private val DEFAULT_MAX_VALUE = 100
    private val DEFAULT_VALUE = 50
    private val DEFAULT_HINT_SIZE = 25
    private val DEFAULT_UNIT_SIZE = 30
    private val DEFAULT_VALUE_SIZE = 40
    private val DEFAULT_TEXT_SIZE = 30
    private val DEFAULT_ARC_WIDTH = 15

    //默认大小
    private var mDefaultSize: Int = 0
    //是否开启抗锯齿
    var isAntiAlias: Boolean = false
    //绘制提示
    private lateinit var mHintPaint: TextPaint
    var hint: CharSequence? = null
    private var mHintColor: Int = 0
    private var mHintSize: Float = 0.toFloat()
    private var mHintOffset: Float = 0.toFloat()

    //绘制单位
    private lateinit var mUnitPaint: TextPaint
    var unit: CharSequence? = null
    private var mUnitColor: Int = 0
    private var mUnitSize: Float = 0.toFloat()
    private var mUnitOffset: Float = 0.toFloat()

    //绘制数值
    private lateinit var mValuePaint: TextPaint
    private var mValue: Float = 0.toFloat()
    private var mValueOffset: Float = 0.toFloat()
    private var mPrecision: Int = 0
    private lateinit var mPrecisionFormat: String
    private var mValueColor: Int = 0
    private var mValueSize: Float = 0.toFloat()

    //绘制最大值和最小值
    private lateinit var mMaxAndMinPaint: TextPaint
    var maxValue: Int = 0
    private var mMaxAndMinXOffset: Float = 0.toFloat()
    private var mMaxAndMinYOffset: Float = 0.toFloat()
    private var mMaxXLocate: Float = 0.toFloat()
    private var mMaxYLocate: Float = 0.toFloat()
    private var mMinXLocate: Float = 0.toFloat()
    private var mMinYLocate: Float = 0.toFloat()
    private var mMaxAndMinColor: Int = 0
    private var mMaxAndMinSize: Float = 0.toFloat()

    //绘制圆弧
    private lateinit var mArcPaint: Paint
    private var mArcWidth: Float = 0.toFloat()
    private var mStartAngle: Float = 0.toFloat()
    private var mSweepAngle: Float = 0.toFloat()
    private lateinit var mRectF: RectF
    //渐变的角度是360度，如果只显示270度，会缺失一段颜色
    private var mSweepGradient: SweepGradient? = null
    private var mGradientColors = intArrayOf(Color.GREEN, Color.YELLOW, Color.WHITE)
    //当前进度
    private var mPercent: Float = 0.toFloat()
    //动画时间
    var animTime: Long = 0
    //属性动画
    private lateinit var mAnimator: ValueAnimator

    //绘制背景圆弧
    private lateinit var mBgArcPaint: Paint
    private var mBgArcColor: Int = 0
    private var mBgArcWidth: Float = 0.toFloat()

    //圆心坐标，半径
    private lateinit var mCenterPoint: Point
    private var mRadius: Float = 0.toFloat()
    private var mTextOffsetPercentInRadius: Float = 0.toFloat()

    /**
     * 设置值
     *
     * @param value
     */
    var value: Float
        get() = mValue
        set(value) {
            var value = value
            if (value > maxValue) {
                value = maxValue.toFloat()
            }
            mValue = value

        }

    var precision: Int
        get() = mPrecision
        set(mPrecision) {
            this.mPrecision = mPrecision
            mPrecisionFormat = getPrecisionFormat(mPrecision)
        }

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {
        mDefaultSize = dipToPx(DEFAULT_SIZE.toFloat())
        mAnimator = ValueAnimator()
        mRectF = RectF()
        mCenterPoint = Point()
        initAttrs(attrs)
        initPaint()
        value = mValue
    }

    private fun initAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar)
        isAntiAlias =
            typedArray.getBoolean(R.styleable.CircleProgressBar_antiAlias, ANTI_ALIAS)
        hint = typedArray.getString(R.styleable.CircleProgressBar_hint)
        mHintColor = typedArray.getColor(R.styleable.CircleProgressBar_hintColor, Color.BLACK)
        mHintSize = typedArray.getDimension(
            R.styleable.CircleProgressBar_hintSize, DEFAULT_HINT_SIZE.toFloat()
        )

        mValue = typedArray.getFloat(
            R.styleable.CircleProgressBar_value, DEFAULT_VALUE.toFloat()
        )
        mPrecision = typedArray.getInt(R.styleable.CircleProgressBar_precision, 0)
        mPrecisionFormat = getPrecisionFormat(mPrecision)
        mValueColor = typedArray.getColor(R.styleable.CircleProgressBar_valueColor, Color.BLACK)
        mValueSize = typedArray.getDimension(
            R.styleable.CircleProgressBar_valueSize, DEFAULT_VALUE_SIZE.toFloat()
        )

        maxValue =
            typedArray.getInt(R.styleable.CircleProgressBar_maxValue, DEFAULT_MAX_VALUE)
        mMaxAndMinColor =
            typedArray.getColor(R.styleable.CircleProgressBar_maxAndMinValueColor, Color.GRAY)
        mMaxAndMinSize = typedArray.getDimension(
            R.styleable.CircleProgressBar_maxAndMinValueSize, DEFAULT_TEXT_SIZE.toFloat()
        )
        mMaxAndMinXOffset =
            typedArray.getFloat(R.styleable.CircleProgressBar_maxAndMinXOffsetPercent, 0.70f)
        mMaxAndMinYOffset =
            typedArray.getFloat(R.styleable.CircleProgressBar_maxAndMinYOffsetPercent, 0.90f)

        unit = typedArray.getString(R.styleable.CircleProgressBar_unit)
        mUnitColor = typedArray.getColor(R.styleable.CircleProgressBar_unitColor, Color.BLACK)
        mUnitSize = typedArray.getDimension(
            R.styleable.CircleProgressBar_unitSize, DEFAULT_UNIT_SIZE.toFloat()
        )

        mArcWidth = typedArray.getDimension(
            R.styleable.CircleProgressBar_arcWidth, DEFAULT_ARC_WIDTH.toFloat()
        )
        mStartAngle = typedArray.getFloat(
            R.styleable.CircleProgressBar_startAngle, DEFAULT_START_ANGLE.toFloat()
        )
        mSweepAngle = typedArray.getFloat(
            R.styleable.CircleProgressBar_sweepAngle, DEFAULT_SWEEP_ANGLE.toFloat()
        ) + 2

        mBgArcColor = typedArray.getColor(R.styleable.CircleProgressBar_bgArcColor, Color.WHITE)
        mBgArcWidth = typedArray.getDimension(
            R.styleable.CircleProgressBar_bgArcWidth, DEFAULT_ARC_WIDTH.toFloat()
        )
        mTextOffsetPercentInRadius =
            typedArray.getFloat(R.styleable.CircleProgressBar_textOffsetPercentInRadius, 0.43f)

        animTime =
            typedArray.getInt(R.styleable.CircleProgressBar_animTime, DEFAULT_ANIM_TIME)
                .toLong()

        val gradientArcColors = typedArray.getResourceId(R.styleable.CircleProgressBar_arcColors, 0)
        if (gradientArcColors != 0) {
            val gradientColors = resources.getIntArray(gradientArcColors)
            /**
             * 如果渐变色数组为0，则以单色读取;
             * 如果只有一种颜色，默认设置为两种相同颜色.
             */
            when {
                gradientColors.isEmpty() -> {
                    val color = ContextCompat.getColor(context, gradientArcColors)
                    mGradientColors = IntArray(2)
                    mGradientColors[0] = color
                    mGradientColors[1] = color
                }
                gradientColors.size == 1 -> {
                    mGradientColors = IntArray(2)
                    mGradientColors[0] = gradientColors[0]
                    mGradientColors[1] = gradientColors[0]
                }
                else -> mGradientColors = gradientColors
            }
        }

        typedArray.recycle()
    }


    private fun initPaint() {
        mHintPaint = TextPaint()
        //设置抗锯齿
        mHintPaint.isAntiAlias = isAntiAlias
        //绘制文字大小
        mHintPaint.textSize = mHintSize
        //设置画笔颜色
        mHintPaint.color = mHintColor
        //从中间向两边绘制，不用再次计算文字
        mHintPaint.textAlign = Paint.Align.CENTER

        mValuePaint = TextPaint()
        mValuePaint.isAntiAlias = isAntiAlias
        mValuePaint.textSize = mValueSize
        mValuePaint.color = mValueColor
        // 设置Typeface对象，即字体风格，包括粗体，斜体以及衬线体，非衬线体等
        mValuePaint.typeface = Typeface.DEFAULT_BOLD
        mValuePaint.textAlign = Paint.Align.CENTER

        mUnitPaint = TextPaint()
        mUnitPaint.isAntiAlias = isAntiAlias
        mUnitPaint.textSize = mUnitSize
        mUnitPaint.color = mUnitColor
        mUnitPaint.textAlign = Paint.Align.CENTER

        mArcPaint = Paint()
        mArcPaint.isAntiAlias = isAntiAlias
        //设置画笔样式，FILL,FILL_OR_STROKE,STROKE
        mArcPaint.style = Paint.Style.STROKE
        //设置画笔粗细
        mArcPaint.strokeWidth = mArcWidth
        //当画笔样式为STROKE 或 FILL_OR_STROKE ，设置笔刷图形样式
        mArcPaint.strokeCap = Paint.Cap.ROUND

        mMaxAndMinPaint = TextPaint()
        mMaxAndMinPaint.color = mMaxAndMinColor
        mMaxAndMinPaint.isAntiAlias = isAntiAlias
        mMaxAndMinPaint.textSize = mMaxAndMinSize
        mMaxAndMinPaint.textAlign = Paint.Align.CENTER

        mBgArcPaint = Paint()
        mBgArcPaint.isAntiAlias = isAntiAlias
        mBgArcPaint.color = mBgArcColor
        mBgArcPaint.style = Paint.Style.STROKE
        mBgArcPaint.strokeWidth = mBgArcWidth
        mBgArcPaint.strokeCap = Paint.Cap.ROUND
    }


    fun startAnimation() {
        mAnimator = ValueAnimator.ofFloat(0f, mValue / maxValue.toFloat())
        mAnimator.duration = (2000 + 20 * mValue).toLong()
        mAnimator.addUpdateListener { animation ->
            mPercent = animation.animatedValue as Float
            invalidate()
        }
        mAnimator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            internalMeasure(widthMeasureSpec, mDefaultSize),
            internalMeasure(heightMeasureSpec, mDefaultSize)
        )

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //求圆弧和圆弧背景的最大宽度
        val maxArcWidth = mArcWidth.coerceAtLeast(mBgArcWidth)
        //求最小值作为实际值
        val minSize =
            (w - paddingLeft - paddingRight - 2 * maxArcWidth.toInt()).coerceAtMost(w - paddingTop - paddingBottom - 2 * maxArcWidth.toInt())
        mRadius = (minSize / 2).toFloat()
        //获取圆的参数
        mCenterPoint.x = w / 2
        mCenterPoint.y = h / 2
        //绘制圆弧边界,画圆弧的大小
        mRectF.left =
            mCenterPoint.x.toFloat() - mRadius - maxArcWidth / 2//?不除以2的话会有一半圆弧跑出去，recf指定的是外边界圆弧的中点吧
        mRectF.top = mCenterPoint.y.toFloat() - mRadius - maxArcWidth / 2
        mRectF.right = mCenterPoint.x.toFloat() + mRadius + maxArcWidth / 2
        mRectF.bottom = mCenterPoint.y.toFloat() + mRadius + maxArcWidth / 2
        //计算文字的baseline
        //由于文字的baseline,descent,ascent等属性只与textSize和typeface 有关，所以此时可以直接计算
        //若value，hint,unit 由同一个画笔绘制或者需要动态设置文字的大小，则需要在每次更新后再次绘制
        mValueOffset = mCenterPoint.y + getBaselineOffsetFromY(mValuePaint) / 2
        mHintOffset = mCenterPoint.y - mRadius * mTextOffsetPercentInRadius
        mUnitOffset =
            mCenterPoint.y.toFloat() + mRadius * mTextOffsetPercentInRadius + getBaselineOffsetFromY(
                mUnitPaint
            )
        mMaxXLocate = mCenterPoint.x + mRadius * mMaxAndMinXOffset
        mMaxYLocate =
            mCenterPoint.y.toFloat() + mRadius * mMaxAndMinYOffset + getBaselineOffsetFromY(
                mMaxAndMinPaint
            )
        mMinXLocate = mCenterPoint.x - mRadius * mMaxAndMinXOffset
        mMinYLocate =
            mCenterPoint.y.toFloat() + mRadius * mMaxAndMinYOffset + getBaselineOffsetFromY(
                mMaxAndMinPaint
            )

        updateArcPaint()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawText(canvas)
        drawArc(canvas)
    }

    private fun drawText(canvas: Canvas) {
        //计算文字宽度，由于Paint已设置为设置为居中绘制，故此处不需要重新计算
        canvas.drawText(
            String.format(mPrecisionFormat, mValue),
            mCenterPoint.x.toFloat(),
            mValueOffset,
            mValuePaint
        )
        canvas.drawText(maxValue.toString() + "", mMaxXLocate, mMaxYLocate, mMaxAndMinPaint)
        canvas.drawText("0", mMinXLocate, mMinYLocate, mMaxAndMinPaint)
        if (hint != null) {
            canvas.drawText(
                hint.toString(),
                mCenterPoint.x.toFloat(),
                mHintOffset,
                mHintPaint
            )
        }
        if (unit != null) {
            canvas.drawText(
                unit.toString(),
                mCenterPoint.x.toFloat(),
                mUnitOffset,
                mUnitPaint
            )
        }

    }

    private fun drawArc(canvas: Canvas) {
        //绘制圆弧背景
        //从进度圆弧结束的地方重新开始绘制，优化性能
        canvas.save()
        val currentAngle = mSweepAngle * mPercent
        canvas.rotate(
            mStartAngle,
            mCenterPoint.x.toFloat(),
            mCenterPoint.y.toFloat()
        )//旋转,以stratAngle 为起点
        canvas.drawArc(
            mRectF,
            currentAngle,
            mSweepAngle - currentAngle,
            false,
            mBgArcPaint
        )//这里最好画个图理解一下
        //第二个参数：startAngel 起始角度，第三个参数：圆弧度数，
        //3点钟方向为0度，顺时针递增，startAngle超过取360 或小于0 与360 取余
        //useCenter 如果为true 时 ，绘制圆弧将圆心包含，通常用来绘制扇形
        canvas.drawArc(mRectF, 0f, currentAngle, false, mArcPaint)
        canvas.restore()
    }

    /**
     * 更新圆弧画笔
     */
    private fun updateArcPaint() {
        //设置渐变
        //        int[] mGradientColors = {Color.GREEN,Color.YELLOW,Color.RED};
        mSweepGradient = SweepGradient(
            mCenterPoint.x.toFloat(),
            mCenterPoint.y.toFloat(),
            mGradientColors,
            null
        )
        mArcPaint.shader = mSweepGradient
    }

    private fun getBaselineOffsetFromY(paint: Paint): Float {
        return measureTextHeight(paint) / 2
    }

    fun setmGradientColors(mGradientColors: IntArray) {
        this.mGradientColors = mGradientColors
        updateArcPaint()
    }

    fun getmGradientColors(): IntArray {
        return mGradientColors
    }


    /**
     * 测量 view
     * @param measureSpec
     * @param defaultSize View的默认大小
     * @return
     */
    private fun internalMeasure(measureSpec: Int, defaultSize: Int): Int {
        var result = defaultSize
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = result.coerceAtMost(specSize)
        }
        return result
    }

    /**
     * dp 转 px
     * @param context
     * @param dip
     * @return
     */
    private fun dipToPx(dip: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dip * density + 0.5f * if (dip >= 0) 1 else -1).toInt()
    }


    /**
     * 获取数值精度格式化字符串
     * @param precision
     * @return
     */
    private fun getPrecisionFormat(precision: Int): String {
        return "%." + precision + "f"
    }


    /**
     * 测量文字高度
     */
    private fun measureTextHeight(paint: Paint): Float {
        val fontMetrics = paint.fontMetrics
        return abs(fontMetrics.ascent) + fontMetrics.descent
    }

}
