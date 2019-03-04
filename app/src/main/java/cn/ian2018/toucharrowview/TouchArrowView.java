package cn.ian2018.toucharrowview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: pixloop 接收路径view
 * Author:chenshuai
 * E-mail:chenshuai@amberweather.com
 * Date:2019/3/1
 */
public class TouchArrowView extends View {

    // 路径和箭头的画笔
    private Paint mPaint;
    // 点的画笔
    private Paint mPointPaint;

    // 当前坐标
    private float currentX, currentY;
    // 上次坐标
    private float lastX, lastY;

    // 当前各点的坐标
    private List<PointF> mPoints;
    // 当前路径
    private Path mPath;
    // 所有路径集合
    private List<PathPoint> allPathList = new ArrayList<>();
    private List<PathPoint> showPathList = new ArrayList<>();

    // 是否画出箭头
    private boolean isDrawArrowLine = false;

    // 线段的最小长度
    private float minLengthOfLine = 100;

    // 箭头的高度
    private double arrowHeight = 26;
    // 箭头底边一半
    private double arrowBottomHalfLength = 12;

    // 是否正在绘制  防止这时候点击按钮
    private boolean isDrawing = false;

    // 回调是否可以前进撤销
    private OnArrowViewEventListener listener;

    // 当前路径的下标
    private int currentIndex;

    public TouchArrowView(Context context) {
        super(context);
        init(context, null);
    }

    public TouchArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TouchArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int pathColor = Color.BLACK;
        int pointColor = Color.RED;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TouchArrowView);
            minLengthOfLine = typedArray.getDimension(R.styleable.TouchArrowView_line_min_length, 100);
            arrowHeight = typedArray.getDimension(R.styleable.TouchArrowView_arrow_height, 26);
            arrowBottomHalfLength = typedArray.getDimension(R.styleable.TouchArrowView_arrow_bottom_half_length, 12);
            pathColor = typedArray.getColor(R.styleable.TouchArrowView_path_color, Color.BLACK);
            pointColor = typedArray.getColor(R.styleable.TouchArrowView_point_color, Color.RED);

            typedArray.recycle();
        }
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(pathColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);

        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(pointColor);
    }

    public void setMinLengthOfLine(float minLengthOfLine) {
        this.minLengthOfLine = minLengthOfLine;
    }

    public void setArrowHeight(double arrowHeight) {
        this.arrowHeight = arrowHeight;
    }

    public void setArrowBottomHalfLength(double arrowBottomHalfLength) {
        this.arrowBottomHalfLength = arrowBottomHalfLength;
    }

    public void setPathColor(int color) {
        mPaint.setColor(color);
    }

    public void setPointColor(int color) {
        mPointPaint.setColor(color);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.currentX = event.getX();
        this.currentY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 判断all和show是否相等 如果不相等 清空all 并将show的内容赋值给all
                if (allPathList.size() != showPathList.size()) {
                    allPathList.clear();
                    allPathList.addAll(showPathList);
                }
                if (listener != null) {
                    listener.canGoBackGoForward(true, false);
                }
                isDrawing = true;
                lastX = currentX;
                lastY = currentY;
                // 每次按下时 新建一个路径和点集合
                mPath = new Path();
                mPath.moveTo(currentX, currentY);
                mPoints = new ArrayList<>();
                mPoints.add(new PointF(currentX, currentY));
                break;
            case MotionEvent.ACTION_MOVE:
                double distance = Math.sqrt(Math.pow(currentX - lastX, 2) + Math.pow(currentY - lastY, 2));
                // 如果大于线段的最小长度，才将当前点添加到路径中，并更新上次点坐标
                if (distance > minLengthOfLine) {
                    isDrawArrowLine = false;
                    mPath.lineTo(currentX, currentY);
                    mPoints.add(new PointF(currentX, currentY));
                    lastX = currentX;
                    lastY = currentY;
                } else {
                    // 当长度未确定时 才画箭头
                    isDrawArrowLine = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                isDrawArrowLine = false;
                // 抬起时 将最后一个点添加到路径中
                mPath.lineTo(currentX, currentY);
                mPoints.add(new PointF(currentX, currentY));
                PathPoint pathPoint = new PathPoint(mPath, mPoints);
                allPathList.add(pathPoint);
                showPathList.add(pathPoint);
                currentIndex = showPathList.size() - 1;
                isDrawing = false;
                if (listener != null) {
                    listener.onTouchUp(showPathList);
                }
                // 将当前路径和点清空
                mPath = null;
                mPoints = null;
                break;
        }
        invalidate();
        return true;
    }

    // 前进
    public void goForward() {
        if (currentIndex + 1 >= allPathList.size()) return;
        showPathList.add(allPathList.get(currentIndex + 1));
        currentIndex++;
        invalidate();
        if (listener != null) {
            if (currentIndex + 1 >= allPathList.size()) {
                listener.canGoBackGoForward(true, false);
            } else {
                listener.canGoBackGoForward(true, true);
            }
        }
    }

    // 撤销
    public void goBack() {
        if (currentIndex < 0) return;
        showPathList.remove(currentIndex);
        currentIndex--;
        invalidate();
        if (listener != null) {
            if (currentIndex < 0) {
                listener.canGoBackGoForward(false, true);
            } else {
                listener.canGoBackGoForward(true, true);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 实时绘制当前的箭头
        if (isDrawArrowLine) {
            drawAL(lastX, lastY, currentX, currentY, canvas, mPaint);
        }

        // 恢复历史路径
        if (showPathList.size() > 0) {
            for (PathPoint pathPoint : showPathList) {
                Path path = pathPoint.path;
                List<PointF> points = pathPoint.points;
                // 绘制路径
                canvas.drawPath(path, mPaint);
                // 绘制点和箭头
                for (int i = 0; i < points.size(); i++) {
                    // 画出每段的箭头
                    if (i + 1 < points.size()) {
                        drawAL(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y, canvas, mPaint);
                    }
                    // 最后一个点不画出
                    if (i != points.size() - 1) {
                        canvas.drawCircle(points.get(i).x, points.get(i).y, 8, mPointPaint);
                    }
                }
            }
        }

        if (mPath != null && mPoints != null) {
            // 绘制当前路径
            canvas.drawPath(mPath, mPaint);
            // 绘制当前点和箭头
            for (int i = 0; i < mPoints.size(); i++) {
                if (i + 1 < mPoints.size()) {
                    drawAL(mPoints.get(i).x, mPoints.get(i).y, mPoints.get(i + 1).x, mPoints.get(i + 1).y, canvas, mPaint);
                }
                canvas.drawCircle(mPoints.get(i).x, mPoints.get(i).y, 8, mPointPaint);
            }
        }
    }

    // 画箭头
    private void drawAL(float sx, float sy, float ex, float ey, Canvas canvas, Paint paint) {
        double H = arrowHeight; // 箭头高度
        double L = arrowBottomHalfLength; // 底边的一半
        double awrad = Math.atan(L / H); // 箭头角度
        double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, arraow_len);
        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, arraow_len);
        float x3 = (float) (ex - arrXY_1[0]); // (x3,y3)是第一端点
        float y3 = (float) (ey - arrXY_1[1]);
        float x4 = (float) (ex - arrXY_2[0]); // (x4,y4)是第二端点
        float y4 = (float) (ey - arrXY_2[1]);
        // 画线
        canvas.drawLine(sx, sy, ex, ey, paint);
//        Path triangle = new Path();
//        triangle.moveTo(ex, ey);
//        triangle.lineTo(x3, y3);
//        triangle.lineTo(x4, y4);
//        triangle.close();
//        canvas.drawPath(triangle, paint);
        canvas.drawLine(x3, y3, ex, ey, paint);
        canvas.drawLine(x4, y4, ex, ey, paint);
    }

    // 计算
    private double[] rotateVec(float px, float py, double ang, double newLen) {
        double mathstr[] = new double[2];
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、新长度
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        double d = Math.sqrt(vx * vx + vy * vy);
        vx = vx / d * newLen;
        vy = vy / d * newLen;
        mathstr[0] = vx;
        mathstr[1] = vy;
        return mathstr;
    }

    // 获取当前显示的坐标点
    public List<PathPoint> getShowPathList() {
        return showPathList;
    }

    // 返回是否正在绘制
    public boolean isDrawing() {
        return isDrawing;
    }

    public void setOnCanGoBackGoForwardListener(OnArrowViewEventListener listener) {
        this.listener = listener;
    }

    // 前进撤销按钮是否可用回调接口
    interface OnArrowViewEventListener {
        void onTouchUp(List<PathPoint> pointList);

        void canGoBackGoForward(boolean canGoBack, boolean canGoForward);
    }
}
