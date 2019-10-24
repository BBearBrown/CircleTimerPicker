package com.daniel.timeclock.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.MotionEvent;

import com.daniel.timeclock.R;

import java.lang.Override;

/**
 * Created by Daniel on 2016/5/22.
 */
public class CircleTimerPicker extends View {
    private static final String TAG = CircleTimerPicker.class.getSimpleName();
    private float mBigCircleRadius;
    private float mSmallCircleRadius;
    private int mBigCirclePaintWidth = 1;
    private Paint mBigCirclePaint;
    private Paint mSmallCirclePaint;
    private Paint mTimeTextPaint;
    private final static int TYPE_INTERNALLY_TANGENT = 0;
    private final static int TYPE_EXTERNALLY_TANGENT = 1;
    private final static int TYPE_INTERSECT = 2;
    //the relation of two circles
    private int mTypeRelation = TYPE_INTERSECT;
    private int mHorizontalInset;
    private int mVerticalInset;
    private int mGravity;
    private float mBigCircleCenterX;
    private float mBigCircleCenterY;
    private int mTime;
    private PointF mTouchPointOne = new PointF();
    private PointF mTouchPointTwo = new PointF();
    private final static int AWARDS = 30;
    private int mPreValue;
    private int mRingCount;
    private float mSmallCircleCenterX;
    private float mSmallCircleCenterY;
    private float mCurrentFloatDegree = 0.0f;

    public CircleTimerPicker(Context context) {
        this(context, null);
    }

    public CircleTimerPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
                context.obtainStyledAttributes(
                        attrs, R.styleable.CircleTimerPickerStyle, 0, R.style.CircleTimerPickerDefStyle);
        mTypeRelation = a.getInt(R.styleable.CircleTimerPickerStyle_typeRelation, TYPE_INTERSECT);
        mBigCircleRadius = a.getDimension(R.styleable.CircleTimerPickerStyle_bigCircleRadius, 200.0f);
        mSmallCircleRadius = a.getDimension(R.styleable.CircleTimerPickerStyle_smallCircleRadius, 30.0f);
        mSmallCirclePaint = new Paint();
        mSmallCirclePaint.setStyle(Paint.Style.FILL);
        mSmallCirclePaint.setColor(a.getColor(R.styleable.CircleTimerPickerStyle_smallCircleColor, Color.WHITE));
        mBigCirclePaint = new Paint();
        mBigCirclePaintWidth = (int) a.getDimension(R.styleable.CircleTimerPickerStyle_bigCirclePaintWidth, 5.0f);
        mBigCirclePaint.setStrokeWidth(mBigCirclePaintWidth);
        mBigCirclePaint.setStyle(Paint.Style.STROKE);
        mBigCirclePaint.setColor(a.getColor(R.styleable.CircleTimerPickerStyle_bigCircleColor, Color.WHITE));
        mBigCirclePaint.setAntiAlias(true);

        mTimeTextPaint = new TextPaint();
        mTimeTextPaint.setTextSize(a.getDimension(R.styleable.CircleTimerPickerStyle_textSize, 30.0f));
        mTimeTextPaint.setColor(a.getColor(R.styleable.CircleTimerPickerStyle_textColor, Color.WHITE));

        mGravity = a.getInt(R.styleable.CircleTimerPickerStyle_android_gravity, Gravity.TOP);
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float newWaveCenterX = mHorizontalInset
                + getSuggestedWidth() / 2;
        float newWaveCenterY = mVerticalInset
                + getSuggestedHeight() / 2;

        mBigCircleCenterX = newWaveCenterX;
        mBigCircleCenterY = newWaveCenterY;
    }

    public int getTime() {
        return mTime;
    }

    public void setTime(int time) {
        mTime = time;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mBigCircleCenterX, mBigCircleCenterY, mBigCircleRadius, mBigCirclePaint);
        drawSmallCircle(canvas);
        drawTime(canvas);
    }

    private void drawTime(Canvas canvas) {
        int hour = mTime / 3600;
        int temp = mTime % 3600;
        int minute = temp / 60;
        temp = temp % 60;
        int second = temp;
        String timeText;
        if (hour / 10 > 0) {
            timeText = String.format("%02d:%02d:%02d", hour, minute, second);
        } else if (hour % 10 > 0) {
            timeText = String.format("%d:%02d:%02d", hour, minute, second);
        } else {
            timeText = String.format("%02d:%02d", minute, second);
        }

        Paint paint = mTimeTextPaint;
        Rect targetRect = new Rect((int) (mBigCircleCenterX - getSuggestedWidth() / 2 + 0.5),
                (int) (mBigCircleCenterY - getSuggestedHeight() / 2 + 0.5),
                (int) (mBigCircleCenterX + getSuggestedWidth() / 2 + 0.5),
                (int) (mBigCircleCenterY + getSuggestedHeight() / 2 + 0.5));
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        //center vertical
        int baseline = (targetRect.top + targetRect.bottom - fontMetrics.bottom - fontMetrics.top) / 2;
        //center horizontal
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(timeText, targetRect.centerX(), baseline, paint);
    }

    private void drawSmallCircle(Canvas canvas) {
        float radian;
        float transDegree = mCurrentFloatDegree;
        float degree = transDegree - 90;
        radian = (float) Math.toRadians(degree);

        PointF centerPoint = getSmallCircleCenter(radian);

        mSmallCircleCenterX = centerPoint.x;
        mSmallCircleCenterY = centerPoint.y;
        canvas.drawCircle(mSmallCircleCenterX, mSmallCircleCenterY, mSmallCircleRadius, mSmallCirclePaint);
    }

    private static float getTranslateX(float degree, float distance) {
        return Double.valueOf(distance * Math.cos(degree)).floatValue();
    }

    public static float getTranslateY(float degree, float distance) {
        return Double.valueOf(distance * Math.sin(degree)).floatValue();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                RectF rect = new RectF();
                rect.set(
                        mSmallCircleCenterX - mSmallCircleRadius,
                        mSmallCircleCenterY - mSmallCircleRadius,
                        mSmallCircleCenterX + mSmallCircleRadius,
                        mSmallCircleCenterY + mSmallCircleRadius);
                mTouchPointOne.x = pointX;
                mTouchPointOne.y = pointY;
                mTouchPointTwo.x = pointX;
                mTouchPointTwo.y = pointY;
                if (rect.contains(pointX, pointY)) {
                    return true;
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                mTouchPointOne.x = mTouchPointTwo.x;
                mTouchPointOne.y = mTouchPointTwo.y;
                mTouchPointTwo.x = pointX;
                mTouchPointTwo.y = pointY;
                handleMove();
                calcTimeByDegree();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                handleUp();
                break;
        }
        invalidate();
        return true;
    }

    private PointF getSmallCircleCenter(float angle) {
        PointF centerPoint = new PointF(mBigCircleCenterX, mBigCircleCenterY);
        switch (mTypeRelation) {
            case TYPE_INTERNALLY_TANGENT:
                centerPoint.x = mBigCircleCenterX + getTranslateX(angle, mBigCircleRadius - mBigCirclePaintWidth / 2 - mSmallCircleRadius);
                centerPoint.y = mBigCircleCenterY + getTranslateY(angle, mBigCircleRadius - mBigCirclePaintWidth / 2 - mSmallCircleRadius);
                break;
            case TYPE_EXTERNALLY_TANGENT:
                centerPoint.x = mBigCircleCenterX + getTranslateX(angle, mBigCircleRadius + mBigCirclePaintWidth / 2 + mSmallCircleRadius);
                centerPoint.y = mBigCircleCenterY + getTranslateY(angle, mBigCircleRadius + mBigCirclePaintWidth / 2 + mSmallCircleRadius);
                break;
            case TYPE_INTERSECT:
                centerPoint.x = mBigCircleCenterX + getTranslateX(angle, mBigCircleRadius + mBigCirclePaintWidth / 2);
                centerPoint.y = mBigCircleCenterY + getTranslateY(angle, mBigCircleRadius + mBigCirclePaintWidth / 2);
                break;
        }
        return centerPoint;
    }

    private void handleUp() {
        int degree = (int) mCurrentFloatDegree;
        int value;
        int remainder;
        float target;
        final int percent = 360 / AWARDS;
        value = degree / percent;
        remainder = degree % percent;
        target = (float) value * percent;

        if (value == 0 && remainder == 0) {
            //do nothing
        } else {
            ValueAnimator animation = ObjectAnimator.ofFloat(mCurrentFloatDegree, target);
            animation.setDuration(200);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float currentValue = (float) animation.getAnimatedValue();
                    mCurrentFloatDegree = currentValue;
                    invalidate();
                }
            });
            animation.start();
        }
    }

    private void handleMove() {
        float onePx = mTouchPointOne.x - mBigCircleCenterX;
        float onePy = mTouchPointOne.y - mBigCircleCenterY;
        float kOne = (float) Math.atan2(onePy, onePx);
        //(-90, 270]
        float oneFloatDegree = (float) (Math.toDegrees(Math.atan2(onePy, onePx) + Math.PI / 2));
        if (oneFloatDegree < 0) {
            oneFloatDegree = 360 + oneFloatDegree;
        }

        float twoPx = mTouchPointTwo.x - mBigCircleCenterX;
        float twoPy = mTouchPointTwo.y - mBigCircleCenterY;
        float kTwo = (float) Math.atan2(twoPy, twoPx);

        float twoFloatDegree = (float) (Math.toDegrees(Math.atan2(twoPy, twoPx) + Math.PI / 2));
        if (twoFloatDegree < 0) {
            twoFloatDegree = 360 + twoFloatDegree;
        }

        if (kTwo > kOne && twoFloatDegree < oneFloatDegree && twoFloatDegree < 90 && oneFloatDegree > 270) {
            twoFloatDegree = twoFloatDegree + 360;
        } else if (kTwo < kOne && oneFloatDegree < twoFloatDegree && twoFloatDegree > 270 && oneFloatDegree < 90) {
            oneFloatDegree = oneFloatDegree + 360;
        }
        Log.v(TAG, "handleMove twoFloatDegree=" + twoFloatDegree + ",oneFloatDegree=" + oneFloatDegree);
        Log.v(TAG, "handleMove kTwo=" + kTwo + ",kOne=" + kOne + ",mCurrentFloatDegree=" + mCurrentFloatDegree);
        float diffDegree = Math.abs(twoFloatDegree - oneFloatDegree);
        //clockwise
        if (kTwo > kOne) {
            mCurrentFloatDegree += diffDegree;//
        } else {//anti-clockwise
            mCurrentFloatDegree -= diffDegree;
        }

        if (mCurrentFloatDegree < 0) {
            mCurrentFloatDegree = 0;
        }
        Log.v(TAG, "handleMove diffDegree=" + diffDegree + ",mCurrentFloatDegree=" + mCurrentFloatDegree);
    }

    private void calcTimeByDegree() {
        int degree = (int) (mCurrentFloatDegree + 0.5);
        final int percent = 360 / AWARDS;
        int value = degree / percent;

        if (mPreValue == 29 && value == 0) {
            mRingCount++;
        } else if (mPreValue == 0 && value == 29) {
            mRingCount--;
        }
        mTime = mRingCount * AWARDS + value;
        //minute
        mTime = mTime * 60;
        mPreValue = value;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();

        int computedWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int computedHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        int scaledWidth = getSuggestedWidth();
        int scaledHeight = getSuggestedHeight();

        computeInsets(computedWidth - scaledWidth, computedHeight - scaledHeight);
        setMeasuredDimension(computedWidth, computedHeight);
    }

    private int getSuggestedWidth() {
        int return_width = 0;
        switch (mTypeRelation) {
            case TYPE_EXTERNALLY_TANGENT:
                return_width = (int) (mBigCircleRadius + mSmallCircleRadius + mBigCirclePaintWidth) * 2;
                break;
            case TYPE_INTERNALLY_TANGENT:
                return_width = (int) (mBigCircleRadius + mBigCirclePaintWidth) * 2;
                break;
            case TYPE_INTERSECT:
                return_width = (int) (mBigCircleRadius * 2 + mSmallCircleRadius + mBigCirclePaintWidth);
                break;
        }
        return return_width;
    }

    private int getSuggestedHeight() {
        int return_height = 0;
        switch (mTypeRelation) {
            case TYPE_EXTERNALLY_TANGENT:
                return_height = (int) (mBigCircleRadius + mSmallCircleRadius + mBigCirclePaintWidth) * 2;
                break;
            case TYPE_INTERNALLY_TANGENT:
                return_height = (int) (mBigCircleRadius + mBigCirclePaintWidth) * 2;
                break;
            case TYPE_INTERSECT:
                return_height = (int) (mBigCircleRadius * 2 + mSmallCircleRadius + mBigCirclePaintWidth);
                break;
        }
        return return_height;
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    private void computeInsets(int dx, int dy) {
        final int layoutDirection = getLayoutDirection();
        final int absoluteGravity = Gravity.getAbsoluteGravity(mGravity, layoutDirection);

        switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT:
                mHorizontalInset = 0;
                break;
            case Gravity.RIGHT:
                mHorizontalInset = dx;
                break;
            case Gravity.CENTER_HORIZONTAL:
            default:
                mHorizontalInset = dx / 2;
                break;
        }
        switch (absoluteGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:
                mVerticalInset = 0;
                break;
            case Gravity.BOTTOM:
                mVerticalInset = dy;
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                mVerticalInset = dy / 2;
                break;
        }
    }
}
