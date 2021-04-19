package com.bezier.womob.test;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.bezier.womob.R;


public class Rocket extends View implements View.OnClickListener {

    private Path path = new Path();
    private Paint paint = new Paint();
    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;
    private int mCurX;
    private int mCurY;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private int bitmapW;
    private int bitmapH;
    private int controlOneX;
    private int controlOneY;
    private int controlTwoX;
    private int controlTwoY;
    private int preX;
    private int preY;

    public Rocket(Context context) {
        this(context, null);
    }

    public Rocket(Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public Rocket(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;//缩小4倍
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.arrow, options);
//        startX = 500;
//        startY = 1800;
//        mCurX = startX;
//        mCurY= startY;
//        controlOneX= 0;
//        controlOneY = -700;
//        controlTwoX = 1300;
//        controlTwoY = -700;
//        endX = -500;
//        endY = -1600;
        bitmapW = mBitmap.getWidth() / 2;
        bitmapH = mBitmap.getHeight() / 2;
        startX = 500 - bitmapW;
        startY = 1600 - bitmapH;
//        startY = 1800;
        mCurX = startX;
        mCurY = startY;
//        preX = 485 - bitmapW;
//        preY = 1593 - bitmapH;
        preX = startX;
        preY = startY + 1;
        controlOneX = 500;
        controlOneY = 900;
        controlTwoX = 1400;
        controlTwoY = 400;
        endX = 0;
        endY = 200;
        setOnClickListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("ondraw", startX + "");
//        path.rQuadTo(200,-400,500,0);
//        canvas.drawCircle(startX, startY, 20, paint);
        //画布移到屏幕中心
//        canvas.translate((float) mWidth / 2, (float) mHeight / 2);
        path.moveTo(startX, startY);
        path.cubicTo(controlOneX, controlOneY, controlTwoX, controlTwoY, endX, endY);
//        path.rCubicTo(0, -900, 800, -900, -500, -1400);
        canvas.drawPath(path, paint);
//        path.reset();

//        canvas.drawBitmap(mBitmap, mCurX , mCurY , paint);
        Log.e("mCurX - bitmapW / 2", bitmapW + "");
//        drawRotateBitmap(canvas, paint, mBitmap, -90, mCurX , mCurY );
//        canvas.drawPath(path, paint);
//        animationArrow();


        float angle = (float) Math.atan2((mCurY - preY), (mCurX - preX));
        Log.e("rocket  point", "preX :" + preX + "  preY :" + preY + ", mCurX :" + mCurX + " , mCurY" + mCurY);
        preX = mCurX;
        preY = mCurY;
        Log.e("angle", angle * 180 / Math.PI + "");
        drawRotateBitmap(canvas, paint, mBitmap, (float) (angle * 180 / Math.PI), mCurX, mCurY);
    }

    @Override
    public void onClick(View view) {
        animationArrow();
    }

    public void animationArrow() {
        BezierEvaluator evaluator = new BezierEvaluator(new PointF(controlOneX, controlOneY), new PointF(controlTwoX, controlTwoY));
        PointF startPoint = new PointF(startX, startY);
        PointF endPoint = new PointF(endX, endY);
        ValueAnimator anim = ValueAnimator.ofObject(evaluator, startPoint, endPoint);
        anim.setDuration(4000);
//        anim.setInterpolator(new OvershootInterpolator(5f));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                PointF curPoint = (PointF) valueAnimator.getAnimatedValue();
                mCurX = (int) curPoint.x - bitmapW / 2;
                mCurY = (int) curPoint.y - bitmapH / 2;
                Log.e("rocket  mCurX  ", mCurX + ", mCurY :" + mCurY);
                invalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCurX = startX;
                mCurY = startY;
                preX = startX;
                preY = startY + 1;
                invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }

    /**
     * 绘制自旋转位图
     *
     * @param canvas
     * @param paint
     * @param bitmap   位图对象
     * @param rotation 旋转度数
     * @param posX     在canvas的位置坐标
     * @param posY
     */
    void drawRotateBitmap(Canvas canvas, Paint paint, Bitmap bitmap,
                          float rotation, float posX, float posY) {
        Matrix matrix = new Matrix();
        int offsetX = bitmap.getWidth() / 2;
        int offsetY = bitmap.getHeight() / 2;
        matrix.postTranslate(-offsetX, -offsetY);
        matrix.postRotate(rotation);
        matrix.postTranslate(posX + offsetX, posY + offsetY);

        canvas.drawBitmap(bitmap, matrix, paint);
    }
}
