package com.bezier.womob.test;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * 贝赛尔曲线估值器
 */
public class BezierEvaluator implements TypeEvaluator<PointF> {

 private PointF pointF1;
 private PointF pointF2;

 //传递的是控制点1 和控制点2
 public BezierEvaluator(PointF pointF1,PointF pointF2) {
  this.pointF1 = pointF1;
  this. pointF2 = pointF2;
 }

 @Override
 public PointF evaluate(float v, PointF pointF, PointF t1) {
  //这里用的是三阶贝塞尔曲线
  /**
   * @param t  曲线长度比例
   * @param p0 起始点
   * @param p1 控制点1
   * @param p2 控制点2
   * @param p3 终止点
   * @return t对应的点
   */
  return BezierUtil.CalculateBezierPointForCubic(v, pointF, pointF1, pointF2,t1);
 }
}