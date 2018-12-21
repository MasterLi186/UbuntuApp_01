package com.example.lfl.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class CircleCountDownView extends View {
    private CountDownListener countDownListener;

    private int width;
    private int height;
    private int padding;
    private int borderWidth;
    // 根据动画执行进度计算出来的插值，用来控制动画效果，建议取值范围为0到1
    private float currentAnimationInterpolation;
    private boolean showProgress;
    private float totalTimeProgress;
    private int processColorStart;
    private int processColorEnd;
    private int processBlurMaskRadius;

    private int initialCountDownValue;
    private int currentCountDownValue;

    private Paint circleBorderPaint;
    private Paint circleProcessPaint;
    private RectF circleProgressRectF;

    private Paint circleImgPaint;
    private Matrix circleImgMatrix;
    private Bitmap circleImgBitmap;
    private int circleImgRadius;
    private AnimationInterpolator animationInterpolator;
    private BitmapShader circleImgBitmapShader;
    private float circleImgTranslationX;
    private float circleImgTranslationY;
    private Paint valueTextPaint;

    private ValueAnimator countDownAnimator;

    public CircleCountDownView(Context context) {
        this(context, null);
    }

    public CircleCountDownView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleCountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        circleImgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleImgPaint.setStyle(Paint.Style.FILL);
        circleImgMatrix = new Matrix();
        valueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircleCountDownView);
        // 控制外层进度条的边距
        padding = typedArray.getDimensionPixelSize(R.styleable.CircleCountDownView_padding, DisplayUtil.dp2px(5));
        // 进度条边线宽度
        borderWidth = typedArray.getDimensionPixelSize(R.styleable.CircleCountDownView_circleBorderWidth, 0);
        if (borderWidth > 0) {
            circleBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circleBorderPaint.setStyle(Paint.Style.STROKE);
            circleBorderPaint.setStrokeWidth(borderWidth);
            circleBorderPaint.setColor(typedArray.getColor(R.styleable.CircleCountDownView_circleBorderColor, Color.WHITE));

            showProgress = typedArray.getBoolean(R.styleable.CircleCountDownView_showProgress, false);
            if (showProgress) {
                circleProcessPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                circleProcessPaint.setStyle(Paint.Style.STROKE);
                circleProcessPaint.setStrokeWidth(borderWidth);
                // 进度条渐变色值
                processColorStart = typedArray.getColor(R.styleable.CircleCountDownView_processColorStart, Color.parseColor("#00ffff"));
                processColorEnd = typedArray.getColor(R.styleable.CircleCountDownView_processColorEnd, Color.parseColor("#35adc6"));
                // 进度条高斯模糊半径
                processBlurMaskRadius = typedArray.getDimensionPixelSize(R.styleable.CircleCountDownView_processBlurMaskRadius, DisplayUtil.dp2px(5));
            }
        }


        int circleImgSrc = typedArray.getResourceId(R.styleable.CircleCountDownView_circleImgSrc, R.mipmap.ic_radar);
        // 图片剪裁成正方形
        circleImgBitmap = ImageUtil.cropSquareBitmap(BitmapFactory.decodeResource(getResources(), circleImgSrc));

        valueTextPaint.setColor(typedArray.getColor(R.styleable.CircleCountDownView_valueTextColor, Color.WHITE));
        valueTextPaint.setTextSize(typedArray.getDimensionPixelSize(R.styleable.CircleCountDownView_valueTextSize, DisplayUtil.dp2px(13)));

        typedArray.recycle();

        // 初始化属性动画，周期为1秒
        countDownAnimator = ValueAnimator.ofFloat(0, 1).setDuration(1000);
        countDownAnimator.setInterpolator(new LinearInterpolator());
        countDownAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (countDownListener != null) {
                    // 监听剩余时间
                    long restTime = (long) ((currentCountDownValue - animation.getAnimatedFraction()) * 1000);
                    countDownListener.restTime(restTime);
                }
                // 整体倒计时进度
                totalTimeProgress = (initialCountDownValue - currentCountDownValue + animation.getAnimatedFraction()) / initialCountDownValue;
                if (animationInterpolator != null) {
                    currentAnimationInterpolation = animationInterpolator.getInterpolation(animation.getAnimatedFraction());
                } else {
                    currentAnimationInterpolation = animation.getAnimatedFraction();
                    currentAnimationInterpolation *= currentAnimationInterpolation;
                }
                invalidate();
            }
        });
        countDownAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                currentCountDownValue--;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (countDownListener != null) {
                    countDownListener.onCountDownFinish();
                }
            }
        });
    }

    // 设置倒计时初始时间
    public void setStartCountValue(int initialCountDownValue) {
        this.initialCountDownValue = initialCountDownValue;
        this.currentCountDownValue = initialCountDownValue;
        // 设置重复执行次数，共执行initialCountDownValue次，恰好为倒计时总数
        countDownAnimator.setRepeatCount(currentCountDownValue - 1);
        invalidate();
    }

    public void setAnimationInterpolator(AnimationInterpolator animationInterpolator) {
        if (!countDownAnimator.isRunning()) {
            this.animationInterpolator = animationInterpolator;
        }
    }

    // 重置
    public void reset() {
        countDownAnimator.cancel();
        lastAnimationInterpolation = 0;
        totalTimeProgress = 0;
        currentAnimationInterpolation = 0;
        currentCountDownValue = initialCountDownValue;
        circleImgMatrix.setTranslate(circleImgTranslationX, circleImgTranslationY);
        circleImgMatrix.postRotate(0, width / 2, height / 2);
        invalidate();
    }

    public void restart() {
        reset();
        startCountDown();
    }

    public void pause() {
        countDownAnimator.pause();
    }

    public void setCountDownListener(CountDownListener countDownListener) {
        this.countDownListener = countDownListener;
    }

    //  启动倒计时
    public void startCountDown() {
        if (countDownAnimator.isPaused()) {
            countDownAnimator.resume();
            return;
        }
        if (currentCountDownValue > 0) {
            countDownAnimator.start();
        } else if (countDownListener != null) {
            countDownListener.onCountDownFinish();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        if (width > 0 && height > 0) {
            doCalculate();
        }
    }

    private void doCalculate() {
        circleImgMatrix.reset();
        // 圆形图片绘制区域半径
        circleImgRadius = (Math.min(width, height) - 2 * borderWidth - 2 * padding) / 2;
        float actualCircleImgBitmapWH = circleImgBitmap.getWidth();
        float circleDrawingScale = circleImgRadius * 2 / actualCircleImgBitmapWH;
        // bitmap缩放处理
        Matrix matrix = new Matrix();
        matrix.setScale(circleDrawingScale, circleDrawingScale, actualCircleImgBitmapWH / 2, actualCircleImgBitmapWH / 2);
        circleImgBitmap = Bitmap.createBitmap(circleImgBitmap, 0, 0, circleImgBitmap.getWidth(), circleImgBitmap.getHeight(), matrix, true);
        // 绘制圆形图片使用
        circleImgBitmapShader = new BitmapShader(circleImgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        // 平移至中心
        circleImgTranslationX = (width - circleImgRadius * 2) / 2;
        circleImgTranslationY = (height - circleImgRadius * 2) / 2;
        circleImgMatrix.setTranslate(circleImgTranslationX, circleImgTranslationY);

        if (borderWidth > 0) {
            // 外层进度条宽度（注意：需要减掉画笔宽度）
            float circleProgressWH = Math.min(width, height) - borderWidth - 2 * padding;
            float left = (width > height ? (width - height) / 2 : 0) + borderWidth / 2 + padding;
            float top = (height > width ? (height - width) / 2 : 0) + borderWidth / 2 + padding;
            float right = left + circleProgressWH;
            float bottom = top + circleProgressWH;
            circleProgressRectF = new RectF(left, top, right, bottom);
            if (showProgress) {
                // 进度条渐变及边缘高斯模糊处理
                circleProcessPaint.setShader(new LinearGradient(left, top, left + circleImgRadius * 2, top + circleImgRadius * 2, processColorStart, processColorEnd, Shader.TileMode.MIRROR));
                circleProcessPaint.setMaskFilter(new BlurMaskFilter(processBlurMaskRadius, BlurMaskFilter.Blur.SOLID)); // 设置进度条阴影效果
            }
        }
    }

    private float lastAnimationInterpolation;

    @Override
    protected void onDraw(Canvas canvas) {
        if (width == 0 || height == 0) {
            return;
        }
        int centerX = width / 2;
        int centerY = height / 2;
        if (borderWidth > 0) {
            // 绘制外层圆环
            canvas.drawCircle(centerX, centerY, Math.min(width, height) / 2 - borderWidth / 2 - padding, circleBorderPaint);
            if (showProgress) {
                // 绘制整体进度
                canvas.drawArc(circleProgressRectF, 0, 360 * totalTimeProgress, false, circleProcessPaint);
            }

        }
        // 设置图片旋转角度增量
        circleImgMatrix.postRotate((currentAnimationInterpolation - lastAnimationInterpolation) * 360, centerX, centerY);
        circleImgBitmapShader.setLocalMatrix(circleImgMatrix);
        circleImgPaint.setShader(circleImgBitmapShader);
        canvas.drawCircle(centerX, centerY, circleImgRadius, circleImgPaint);
        lastAnimationInterpolation = currentAnimationInterpolation;

        // 绘制倒计时时间
        // current
        String currentTimePoint = currentCountDownValue + "s";
        float textWidth = valueTextPaint.measureText(currentTimePoint);
        float x = centerX - textWidth / 2;
        Paint.FontMetrics fontMetrics = valueTextPaint.getFontMetrics();
        // 文字绘制基准线(圆形区域正中央)
        float verticalBaseline = (height - fontMetrics.bottom - fontMetrics.top) / 2;
        // 随动画执行进度而更新的y轴位置
        float y = verticalBaseline - currentAnimationInterpolation * (Math.min(width, height) / 2);
        valueTextPaint.setAlpha((int) (255 - currentAnimationInterpolation * 255));
        canvas.drawText(currentTimePoint, x, y, valueTextPaint);

        // next
        String nextTimePoint = (currentCountDownValue - 1) + "s";
        textWidth = valueTextPaint.measureText(nextTimePoint);
        x = centerX - textWidth / 2;
        y = y + (Math.min(width, height)) / 2;
        valueTextPaint.setAlpha((int) (currentAnimationInterpolation * 255));
        canvas.drawText(nextTimePoint, x, y, valueTextPaint);
    }

    public interface CountDownListener {
        /**
         * 倒计时结束
         */
        void onCountDownFinish();

        /**
         * 倒计时剩余时间
         *
         * @param restTime 剩余时间，单位毫秒
         */
        void restTime(long restTime);
    }

    public interface AnimationInterpolator {
        /**
         * @param inputFraction 动画执行时间因子，取值范围0到1
         */
        float getInterpolation(float inputFraction);
    }
}
