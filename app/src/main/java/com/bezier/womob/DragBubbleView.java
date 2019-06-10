package com.bezier.womob;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PointFEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;


/**
 * QQ气泡效果
 */
public class DragBubbleView extends View {

    /**
     * 气泡默认状态静止
     */
    private final int BUBBLE_STATE_DEFAULT = 0;

    /**
     * 气泡相连
     */
    private final int BUBBLE_STATE_CONNECT = 1;
    /**
     * 气泡分离
     */
    private final int BUBBLE_STATE_APART = 2;
    /**
     * 气泡消失
     */
    private final int BUBBLE_STATE_DISMISS = 3;

    /**
     * 气泡半径
     */
    private float mBubbleRadius;

    /**
     * 气泡颜色
     */
    private int mBubbleColor;

    /**
     * 气泡消息文字
     */
    private String mTextStr;

    /**
     * 气泡消息文字颜色
     */
    private int mTextColor;

    /**
     * 气泡消息文字大小
     */
    private float mTextSize;

    /**
     * 不动气泡的半径
     */
    private float mBubFixedRadius;

    /**
     * 可动气泡的半径
     */
    private float mBubMovableRadius;

    /**
     * 不动气泡的圆心
     */
    private PointF mBubFixedCenter;

    /**
     * 可动气泡的圆心
     */
    private PointF mBubMovableCenter;

    /**
     * 气泡的画笔
     */
    private Paint mBubPaint;

    /**
     * 贝塞尔曲线
     */
    private Path mBazierPath;

    private Paint mTextPaint;
    //文本绘制区域
    private Rect mTextRect;

    private Paint mBurstPaint;
    //爆炸绘制区域
    private Rect mBurstRect;

    /**
     * 气泡状态连接标志
     */
    private int mBubbleState = BUBBLE_STATE_DEFAULT;

    /**
     * 两气泡圆心距离
     */
    private float mDist;

    /**
     * 气泡相连状态最大圆心距离
     */
    private float mMaxDist;

    /**
     * 手指触摸偏移量
     */
    private final float MOVE_OFFSET;

    /**
     * 气泡爆炸bitmap数组
     */
    private Bitmap[] mBurstBitmapArray;

    /**
     * 是否在执行气泡爆炸动画
     */
    private boolean mIsBurstAnimStart = false;

    /**
     * 当前气泡爆炸图片index
     */
    private int mCurDrawableIndex;

    /**
     * 气泡爆炸的图片id数组
     */
    private int[] mBurstDrawablesArray = {R.mipmap.burst_1, R.mipmap.burst_2,
            R.mipmap.burst_3, R.mipmap.burst_4, R.mipmap.burst_5};


    public DragBubbleView(Context context) {
        this(context, null);
    }

    public DragBubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragBubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DragBubbleView, defStyleAttr, 0);
        mBubbleRadius = array.getDimension(R.styleable.DragBubbleView_bubble_radius, mBubbleRadius);
        mBubbleColor = array.getColor(R.styleable.DragBubbleView_bubble_color, Color.RED);
        mTextStr = array.getString(R.styleable.DragBubbleView_bubble_text);
        mTextSize = array.getDimension(R.styleable.DragBubbleView_bubble_textSize, mTextSize);
        mTextColor = array.getColor(R.styleable.DragBubbleView_bubble_textColor, Color.WHITE);
        array.recycle();


        //两个圆半径大小一致
        mBubFixedRadius = mBubbleRadius;
        mBubMovableRadius = mBubFixedRadius;
        mMaxDist = 8 * mBubbleRadius;

        MOVE_OFFSET = mMaxDist / 4;



        //抗锯齿
        mBubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBubPaint.setColor(mBubbleColor);
        mBubPaint.setStyle(Paint.Style.FILL);
        mBazierPath = new Path();
        //文本画笔
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextRect = new Rect();

        //爆炸画笔
        mBurstPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBurstPaint.setFilterBitmap(true);
        mBurstRect = new Rect();
        mBurstBitmapArray = new Bitmap[mBurstDrawablesArray.length];
        for (int i = 0; i < mBurstDrawablesArray.length; i++) {
            //将气泡爆炸的drawable转为bitmap
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mBurstDrawablesArray[i]);
            mBurstBitmapArray[i] = bitmap;
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //不动气泡的圆心
        if (mBubFixedCenter == null) {
            mBubFixedCenter = new PointF(w / 2, h / 2);

        } else {
            mBubFixedCenter.set(w / 2, h / 2);
        }
        //可动气泡的圆心
        if (mBubMovableCenter == null) {
            mBubMovableCenter = new PointF(w / 2, h / 2);

        } else {
            mBubMovableCenter.set(w / 2, h / 2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBubbleState == BUBBLE_STATE_CONNECT) {
            //绘制不动气泡
            canvas.drawCircle(mBubFixedCenter.x, mBubFixedCenter.y, mBubFixedRadius, mBubPaint);
            //绘制贝塞尔曲线
            int iAnchorX = (int) ((mBubFixedCenter.x + mBubMovableCenter.x) / 2);
            int iAnchorY = (int) ((mBubFixedCenter.y + mBubMovableCenter.y) / 2);

            float sinTheta = (mBubMovableCenter.y - mBubFixedCenter.y) / mDist;
            float cosTheta = (mBubMovableCenter.x - mBubFixedCenter.x) / mDist;

            //B
            float iBubMovableStartX = mBubMovableCenter.x + sinTheta * mBubMovableRadius;
            float iBubMovableStartY = mBubMovableCenter.y - cosTheta * mBubMovableRadius;

            //A
            float iBubFixedEndX = mBubFixedCenter.x + mBubFixedRadius * sinTheta;
            float iBubFixedEndY = mBubFixedCenter.y - mBubFixedRadius * cosTheta;

            //D
            float iBubFixedStartX = mBubFixedCenter.x - mBubFixedRadius * sinTheta;
            float iBubFixedStartY = mBubFixedCenter.y + mBubFixedRadius * cosTheta;

            //C
            float iBubMovableEndX = mBubMovableCenter.x - mBubMovableRadius * sinTheta;
            float iBubMovableEndY = mBubMovableCenter.y + mBubMovableRadius * cosTheta;



            mBazierPath.reset();
            mBazierPath.moveTo(iBubFixedStartX, iBubFixedStartY);
            mBazierPath.quadTo(iAnchorX, iAnchorY, iBubMovableEndX, iBubMovableEndY);
            //移动到B点
            mBazierPath.lineTo(iBubMovableStartX, iBubMovableStartY);
            mBazierPath.quadTo(iAnchorX, iAnchorY, iBubFixedEndX, iBubFixedEndY);
            mBazierPath.close();
            canvas.drawPath(mBazierPath, mBubPaint);
        }

        if (mBubbleState != BUBBLE_STATE_DISMISS) {
            //绘制一个小球加消息数
            canvas.drawCircle(mBubMovableCenter.x, mBubMovableCenter.y, mBubMovableRadius, mBubPaint);
            mTextPaint.getTextBounds(mTextStr, 0, mTextStr.length(), mTextRect);
            canvas.drawText(mTextStr, mBubMovableCenter.x - mTextRect.width() / 2, mBubMovableCenter.y + mTextRect.height() / 2, mTextPaint);

        }

        if (mBubbleState == BUBBLE_STATE_DISMISS && mCurDrawableIndex < mBurstBitmapArray.length) {
            mBurstRect.set(
                    (int) (mBubMovableCenter.x - mBubMovableRadius),
                    (int) (mBubMovableCenter.y - mBubMovableRadius),
                    (int) (mBubMovableCenter.x + mBubMovableRadius),
                    (int) (mBubMovableCenter.y + mBubMovableRadius)

            );
            canvas.drawBitmap(mBurstBitmapArray[mCurDrawableIndex], null, mBurstRect, mBubPaint);
        }
    }


    //1、静止状态  气泡加消息数
    //2、连接状态  气泡加消息数，贝塞尔曲线，本身位置上的气泡 ，大小可变化
    //3、分离状态  气泡加消息数
    //4、消失状态，爆炸效果


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mBubbleState != BUBBLE_STATE_DISMISS) {
                    mDist = (float) Math.hypot(event.getX() - mBubFixedCenter.x, event.getY() - mBubFixedCenter.y);
                    if (mDist < mBubbleRadius + MOVE_OFFSET) {//加上MOVE_OFFSET是为了方便拖拽
                        mBubbleState = BUBBLE_STATE_CONNECT;
                    } else {
                        mBubbleState = BUBBLE_STATE_DEFAULT;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mBubbleState != BUBBLE_STATE_DEFAULT) {
                    mDist = (float) Math.hypot(event.getX() - mBubFixedCenter.x, event.getY() - mBubFixedCenter.y);
                    mBubMovableCenter.x = event.getX();
                    mBubMovableCenter.y = event.getY();
                    if (mBubbleState == BUBBLE_STATE_CONNECT) {
                        if (mDist < mMaxDist - MOVE_OFFSET) {//当拖拽的距离在指定范围内，那么调整不动气泡的半径
                            mBubFixedRadius = mBubbleRadius - mDist / 8;
                        } else {
                            mBubbleState = BUBBLE_STATE_APART;//当拖拽的距离超过指定范围，那么改成分离状态
                        }
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:

                if (mBubbleState == BUBBLE_STATE_CONNECT) {
                    //橡皮筋动画效果
                    startBubbleRestAnim();
                } else if (mBubbleState == BUBBLE_STATE_APART) {
                    if (mDist < 2 * mBubbleRadius) {
                        startBubbleRestAnim();
                    } else {
                        //爆炸效果
                        startBubbleBurstAnim();
                    }

                }
                break;
        }
        return true;
    }

    private void startBubbleBurstAnim() {
        mBubbleState = BUBBLE_STATE_DISMISS;
        ValueAnimator animator = ValueAnimator.ofInt(0, mBurstBitmapArray.length);
        animator.setDuration(500);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurDrawableIndex = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();

    }

    private void startBubbleRestAnim() {
        ValueAnimator animator = ValueAnimator.ofObject(new PointFEvaluator(),
                new PointF(mBubMovableCenter.x, mBubMovableCenter.y),
                new PointF(mBubFixedCenter.x, mBubFixedCenter.y));
        animator.setDuration(200);
        animator.setInterpolator(new OvershootInterpolator(5f));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBubMovableCenter = (PointF) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBubbleState = BUBBLE_STATE_DEFAULT;
            }
        });
        animator.start();
    }
}
