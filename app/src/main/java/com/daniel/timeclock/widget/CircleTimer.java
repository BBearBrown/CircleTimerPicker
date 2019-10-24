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
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.daniel.timeclock.R;

/**
 * Created by Deniel on 2016/5/22.
 */
public class CircleTimer extends View {
    private static final String TAG = CircleTimer.class.getSimpleName();
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
    private float mDownPointX;
    private float mDownPointY;
    private float mSmallCircleAngle;
    private final static int AWARDS = 30;
    private int mPreValue;
    private int mRingCount;
    private float mSmallCircleCenterX;
    private float mSmallCircleCenterY;
    private int mPreDegree = 360;

    public CircleTimer(Context context) {
        this(context, null);
    }

    public CircleTimer(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
                context.obtainStyledAttributes(
                        attrs, R.styleable.CircleTimerPickerStyle, 0, R.style.CircleTimerPickerDefStyle);
        mTypeRelation = a.getInt(R.styleable.CircleTimerPickerStyle_typeRelation, TYPE_INTERSECT);
        mBigCircleRadius = a.getDimension(R.styleable.CircleTimerPickerStyle_bigCircleRadius, 200.0f);
        mSmallCircleRadius = a.getDimension(R.styleable.CircleTimerPickerStyle_smallCircleRadius,30.0f);
        mSmallCircleAngle = (float)Math.PI / 2;
        mSmallCirclePaint = new Paint();
        mSmallCirclePaint.setStyle(Paint.Style.FILL);
        mSmallCirclePaint.setColor(a.getColor(R.styleable.CircleTimerPickerStyle_smallCircleColor, Color.WHITE));
        mBigCirclePaint = new Paint();
        mBigCirclePaintWidth = (int)a.getDimension(R.styleable.CircleTimerPickerStyle_bigCirclePaintWidth,5.0f);
        mBigCirclePaint.setStrokeWidth(mBigCirclePaintWidth);
        mBigCirclePaint.setStyle(Paint.Style.STROKE);
        mBigCirclePaint.setColor(a.getColor(R.styleable.CircleTimerPickerStyle_bigCircleColor, Color.WHITE));
        mBigCirclePaint.setAntiAlias(true);

        mTimeTextPaint = new TextPaint();
        mTimeTextPaint.setTextSize(a.getDimension(R.styleable.CircleTimerPickerStyle_textSize,30.0f));
        mTimeTextPaint.setColor(a.getColor(R.styleable.CircleTimerPickerStyle_textColor, Color.WHITE));
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float newWaveCenterX = mHorizontalInset
                + getSuggestedWidth()/2;
        float newWaveCenterY = mVerticalInset
                + getSuggestedHeight()/2;

        mBigCircleCenterX = newWaveCenterX;
        mBigCircleCenterY = newWaveCenterY;
    }

    public int getTime(){
        return mTime;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawCircle(mBigCircleCenterX, mBigCircleCenterY, mBigCircleRadius, mBigCirclePaint);
        drawSmallCircle(canvas);
        drawTime(canvas);
    }

    private void drawTime(Canvas canvas){
        canvas.drawText("" + mTime, getWidth() / 2, getHeight() / 2, mTimeTextPaint);
    }

    private void drawSmallCircle(Canvas canvas){
        PointF centerPoint = getSmallCircleCenter(mSmallCircleAngle);

        mSmallCircleCenterX = centerPoint.x;
        mSmallCircleCenterY = centerPoint.y;
        canvas.drawCircle(mSmallCircleCenterX, mSmallCircleCenterY, mSmallCircleRadius, mSmallCirclePaint);
    }

    private static float getTranslateX(float degree, float distance){
        return Double.valueOf(distance * Math.cos(degree)).floatValue();
    }

    public static float getTranslateY(float degree, float distance){
        return Double.valueOf(-1 * distance * Math.sin(degree)).floatValue();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();
        RectF zeroRect = new RectF();
        PointF center = getSmallCircleCenter((float) (Math.PI / 2));
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                RectF rect = new RectF();
                rect.set(
                        mSmallCircleCenterX - mSmallCircleRadius,
                        mSmallCircleCenterY - mSmallCircleRadius,
                        mSmallCircleCenterX + mSmallCircleRadius,
                        mSmallCircleCenterY + mSmallCircleRadius);
                mDownPointX = pointX;
                mDownPointY = pointY;
                if (rect.contains(pointX,pointY)){
                     return true;
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                zeroRect.set(center.x - mSmallCircleRadius,
                        center.y - mSmallCircleRadius,
                        center.x + mSmallCircleRadius,
                        center.y + mSmallCircleRadius);
                Log.v(TAG, "ACTION_MOVE pointX="+pointX+",mBigCircleCenterX="+mBigCircleCenterX + ",mTime="+mTime);
//                if (mTime <= 0 && (pointX < center.x | pointX > center.x){
//                    Log.v(TAG, "ACTION_MOVE don't move");
//                    pointX = center.x;
//                    pointY = center.y;
//                }
                handleMove(pointX, pointY);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.v(TAG, "ACTION_UP pointX="+pointX+",mBigCircleCenterX="+mBigCircleCenterX + ",mTime="+mTime);
                zeroRect.set(center.x - mSmallCircleRadius,
                        center.y - mSmallCircleRadius,
                        center.x + mSmallCircleRadius,
                        center.y + mSmallCircleRadius);

//                if (mTime <= 0 && pointX < mBigCircleCenterX){
//                    Log.v(TAG, "ACTION_UP don't move");
//                    pointX = center.x;
//                    pointY = center.y;
//                }
                handleUp(pointX, pointY);
                mPreDegree = 360;
                break;
        }
        invalidate();
        return true;
    }

    private PointF getSmallCircleCenter(float angle){
        PointF centerPoint = new PointF(mBigCircleCenterX,mBigCircleCenterY);
        switch (mTypeRelation){
            case TYPE_INTERNALLY_TANGENT:
                centerPoint.x = mBigCircleCenterX + getTranslateX(angle, mBigCircleRadius - mBigCirclePaintWidth/2 - mSmallCircleRadius);
                centerPoint.y = mBigCircleCenterY + getTranslateY(angle, mBigCircleRadius - mBigCirclePaintWidth/2 - mSmallCircleRadius);
                break;
            case TYPE_EXTERNALLY_TANGENT:
                centerPoint.x = mBigCircleCenterX + getTranslateX(angle, mBigCircleRadius + mBigCirclePaintWidth/2 + mSmallCircleRadius);
                centerPoint.y = mBigCircleCenterY + getTranslateY(angle, mBigCircleRadius + mBigCirclePaintWidth/2 + mSmallCircleRadius);
                break;
            case TYPE_INTERSECT:
                centerPoint.x = mBigCircleCenterX + getTranslateX(angle, mBigCircleRadius + mBigCirclePaintWidth/2);
                centerPoint.y = mBigCircleCenterY + getTranslateY(angle, mBigCircleRadius + mBigCirclePaintWidth/2);
                break;
        }
        return centerPoint;
    }

    private void handleUp(float x, float y){
        float px = x - mBigCircleCenterX;
        float py = y - mBigCircleCenterY;
        int degree = (int) ((Math.toDegrees(Math.atan2(py, px) + Math.PI / 2)) + 0.5);
        Log.v(TAG,"handleUp degree="+degree);

        int value;
        int remainder;
        float target;
        final int percent = 360 / AWARDS;
        if (degree < 0){
            degree = 360 + degree;
            value = degree / percent;
            target = (float) Math.toRadians(90 + (360 - value * percent));
        } else {
            value = degree / percent;
            target = (float) Math.toRadians(90 - value * percent);
        }

        if (degree < mPreDegree && mTime <= 0){
            return;
        }

        remainder = degree % percent;

        if (value == 0 && remainder == 0){
            //do nothing
        } else {
            ValueAnimator animation = ObjectAnimator.ofFloat(mSmallCircleAngle, target);
            animation.setDuration(200);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float currentValue = (float)animation.getAnimatedValue();
                    mSmallCircleAngle = currentValue;
                    invalidate();
                }
            });
            animation.start();
        }
    }

    private void handleMove(float x, float y){
        float px = x - mBigCircleCenterX;
        float py = y - mBigCircleCenterY;
        int degree = (int) (Math.toDegrees(Math.atan2(py, px) + Math.PI / 2) + 0.5);
        Log.v(TAG,"handleMove degree="+degree);
        if (degree < 0){
            degree = 360 + degree;
        }

        if (degree < mPreDegree && mTime <= 0){
            return;
        }
        mPreDegree = degree;

        mSmallCircleAngle = (float)(Math.atan2(-py, px));

        final int percent = 360 / AWARDS;
        int value = degree / percent;

        if (mPreValue == 29 && value == 0){
            mRingCount ++;
        } else if (mPreValue == 0 && value == 29){
            mRingCount --;
        }
        mTime = mRingCount * AWARDS + value;
        Log.v(TAG,"handleMove mTime="+mTime + ",value="+value);
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
        switch(mTypeRelation){
            case TYPE_EXTERNALLY_TANGENT:
                return_width = (int)(mBigCircleRadius + mSmallCircleRadius + mBigCirclePaintWidth) * 2;
                break;
            case TYPE_INTERNALLY_TANGENT:
                return_width = (int)(mBigCircleRadius + mBigCirclePaintWidth) * 2;
                break;
            case TYPE_INTERSECT:
                return_width = (int)(mBigCircleRadius * 2 + mSmallCircleRadius + mBigCirclePaintWidth);
                break;
        }
        return return_width;
    }

    private int getSuggestedHeight() {
        int return_height = 0;
        switch(mTypeRelation){
            case TYPE_EXTERNALLY_TANGENT:
                return_height = (int)(mBigCircleRadius + mSmallCircleRadius) * 2;
                break;
            case TYPE_INTERNALLY_TANGENT:
                return_height = (int)mBigCircleRadius * 2;
                break;
            case TYPE_INTERSECT:
                return_height = (int)(mBigCircleRadius * 2 + mSmallCircleRadius);
                break;
        }
        return return_height;
    }

    private int resolveMeasured(int measureSpec, int desired){
        int result = 0;
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
