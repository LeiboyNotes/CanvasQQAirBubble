package com.bezier.womob.test;

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
import android.view.animation.OvershootInterpolator;

import com.bezier.womob.R;


public class Rocket extends View implements View.OnClickListener{

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

        startX = 500;
        startY = 1800;
        mCurX = startX;
        mCurY= startY;
        controlOneX= 0;
        controlOneY = -700;
        controlTwoX = 1300;
        controlTwoY = -700;
        endX = -500;
        endY = -1600;
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
        Log.e("ondraw",startX+"");
//        path.moveTo(500,1200);
//        path.rQuadTo(200,-400,500,0);
//        canvas.drawCircle(startX, startY, 20, paint);
        //画布移到屏幕中心
//        canvas.translate((float) mWidth / 2, (float) mHeight / 2);
        path.moveTo(startX, startY);
//        mPath.cubicTo(500,100,600,1200,800,500);
        //参数表示相对位置，等同于上一行代码  0 500
        path.rCubicTo(controlOneX, controlOneY, controlTwoX, controlTwoY, endX, endY);
//        path.rCubicTo(0, -900, 800, -900, -500, -1400);
//        canvas.drawCircle(200,500,10,paint);
        canvas.drawPath(path, paint);
//        path.reset();
        bitmapW = mBitmap.getWidth();
        bitmapH = mBitmap.getHeight();
        drawRotateBitmap(canvas, paint, mBitmap, -90, mCurX - bitmapW / 2, mCurY - bitmapH / 2);
//        drawRotateBitmap(canvas, paint, mBitmap, -90, mCurX , mCurY );
//        canvas.drawPath(path, paint);
//        animationArrow();
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
        anim.setDuration(2000);
//        anim.setInterpolator(new OvershootInterpolator(5f));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                PointF curPoint = (PointF) valueAnimator.getAnimatedValue();
                mCurX = (int) curPoint.x;
                mCurY = (int) curPoint.y;
                Log.e("rocket  mCurX : ",mCurX+", mCurY :"+mCurY);
                invalidate();
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
