package com.helloandroid.lockdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 图案解锁
 * Created by @换个ID上微博 on 2015/2/2.
 */
public class LockDemoView extends View {

    //9个圆点
    private Point[][] points = new Point[3][3];
    //圆点是否初始化
    private boolean isInit;
    //是否选择
    private boolean isSelected;
    //是否绘制结束
    private boolean isFinished;
    //是否九宫格中的点
    private boolean isPoint;
    private float offsetsX, offsetsY;
    private float movingX, movingY;

    //选中点的最小个数
    private static final int POINT_SIZE = 5;

    //矩阵
    private Matrix matrix = new Matrix();

    //画笔
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap pointNormal, pointPressed, pointError, linePressed, lineError;
    private float bitmapR;

    //按下的圆点集合
    private List<Point> pointList = new ArrayList<>();

    //监听器
    public OnPatterChangeListener onPatterChangeListener;

    public LockDemoView(Context context) {
        super(context);
    }

    public LockDemoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public LockDemoView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInit) {
            //如果圆点没有初始化，则初始化
            initPoints();
        }
        //将圆点绘制到页面上
        pointToCanvas(canvas);

        //画线
        if (pointList.size() > 0) {
            Point a = pointList.get(0);

            //绘制九宫格中的坐标点
            for (int i = 0; i < pointList.size(); i++) {
                Point b = pointList.get(i);
                line2Canvas(canvas, a, b);
                a = b;
            }

            //绘制圆点之外的
            if (!isPoint) {
                line2Canvas(canvas, a, new Point(movingX, movingY));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isPoint = false;
        isFinished = false;
        movingX = event.getX();
        movingY = event.getY();

        Point point = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(onPatterChangeListener!=null){
                    onPatterChangeListener.onPatterStart(true);
                }
                resetPoint();
                point = checkSelectPoint();
                if (point != null) {
                    isSelected = true;
                    isPoint = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isSelected) {
                    point = checkSelectPoint();
                    if (point == null) {
                        isPoint = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isFinished = true;
                isSelected = false;
                break;
        }

        //选中重复检查
        if (!isFinished && isSelected && point != null) {
            if (crossPoint(point)) {
                isPoint = false;
            } else {//新点
                point.state = Point.STATE_PRESSED;
                pointList.add(point);
            }
        }

        //绘制结束
        if (isFinished) {
            if (pointList.size() == 1) {
                resetPoint();
            } else if (pointList.size() < 5 && pointList.size() >0) {
                errorPoint();
                if (onPatterChangeListener != null) {
                    onPatterChangeListener.onPatterChange(null);
                }
            } else {
                //绘制成功
                if (onPatterChangeListener != null) {
                    String password = "";
                    //取出密码
                    for (int i = 0; i < pointList.size(); i++) {
                        password = password + pointList.get(i).index;
                    }
                    onPatterChangeListener.onPatterChange(password);
                }
            }
        }

        //刷新View
        postInvalidate();
        return true;
    }

    /*
    * 设置绘制不成立
    * **/
    public void resetPoint() {

        for (int i = 0; i < pointList.size(); i++) {
            Point point = pointList.get(i);
            point.state = Point.STATE_NORMAL;
        }
        pointList.clear();
    }

    /*
    * 设置绘制错误
    * */
    public void errorPoint() {
        for (Point point : pointList) {
            point.state = Point.STATE_ERROR;
        }
    }

    /**
     * 检查是否选中
     */
    private Point checkSelectPoint() {
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                Point point = points[i][j];
                if (Point.with(point.x, point.y, bitmapR, movingX, movingY)) {
                    return point;
                }
            }
        }

        return null;
    }

    /**
     * 将圆点绘制到canvas上
     */
    private void pointToCanvas(Canvas canvas) {
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                Point point = points[i][j];
                if (point.state == Point.STATE_PRESSED) {
                    canvas.drawBitmap(pointPressed,
                            point.x - bitmapR, point.y - bitmapR,
                            paint);
                } else if (point.state == Point.STATE_ERROR) {
                    canvas.drawBitmap(pointError,
                            point.x - bitmapR, point.y - bitmapR,
                            paint);
                } else {
                    canvas.drawBitmap(pointNormal,
                            point.x - bitmapR, point.y - bitmapR,
                            paint);
                }
            }
        }
    }

    /**
     * 画线
     */
    private void line2Canvas(Canvas canvas, Point a, Point b) {

        //线的长度
        float lineLength = (float) Point.distance(a, b);
        //旋转画布
        float degrees = getDegrees(a, b);
        canvas.rotate(degrees, a.x, a.y);

        if (a.state == Point.STATE_PRESSED) {
            matrix.setScale(lineLength / linePressed.getWidth(), 1);
            //不需要减去长度，否则会出现问题
            //matrix.postTranslate(a.x - linePressed.getWidth() / 2, a.y - linePressed.getHeight() / 2);
            matrix.postTranslate(a.x, a.y);

            canvas.drawBitmap(linePressed, matrix, paint);
        } else {
            matrix.setScale(lineLength / lineError.getWidth(), 1);
            matrix.postTranslate(a.x, a.y);
            canvas.drawBitmap(lineError, matrix, paint);
        }

        //画布转回
        canvas.rotate(-degrees, a.x, a.y);
    }


    /**
     * 初始化圆点
     */
    private void initPoints() {
        //1.获取屏幕的宽度和高度
        float width = getWidth();
        float height = getHeight();

        //2.偏移量
        //横屏
        if (width > height) {
            offsetsX = (width - height) / 2;
            width = height;
        } else {//竖屏
            offsetsY = (height - width) / 2;
            height = width;
        }

        //3.图片资源
        pointNormal = BitmapFactory.decodeResource(getResources(), R.drawable.normal);
        pointPressed = BitmapFactory.decodeResource(getResources(), R.drawable.pressed);
        pointError = BitmapFactory.decodeResource(getResources(), R.drawable.error);
        linePressed = BitmapFactory.decodeResource(getResources(), R.drawable.line);
        lineError = BitmapFactory.decodeResource(getResources(), R.drawable.line_error);


        //根据想(x,y)初始化点
        points[0][0] = new Point(offsetsX + width / 4, offsetsY + width / 4);
        points[0][1] = new Point(offsetsX + width / 2, offsetsY + width / 4);
        points[0][2] = new Point(offsetsX + width - width / 4, offsetsY + width / 4);

        points[1][0] = new Point(offsetsX + width / 4, offsetsY + width / 2);
        points[1][1] = new Point(offsetsX + width / 2, offsetsY + width / 2);
        points[1][2] = new Point(offsetsX + width - width / 4, offsetsY + width / 2);

        points[2][0] = new Point(offsetsX + width / 4, offsetsY + width - width / 4);
        points[2][1] = new Point(offsetsX + width / 2, offsetsY + width - width / 4);
        points[2][2] = new Point(offsetsX + width - width / 4, offsetsY + width - width / 4);

        bitmapR = pointNormal.getWidth() / 2;

        //设置脚标
        int index = 1;
        for (Point[] points : this.points) {
            for (Point point : points) {
                point.index = index;
                index++;
            }
        }

        isInit = true;
    }

    /**
     * 判断是否交叉点
     */
    private boolean crossPoint(Point point) {

        if (pointList.contains(point)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取角度
     */
    public float getDegrees(Point a, Point b) {
        float ax = a.x;
        float ay = a.y;
        float bx = b.x;
        float by = b.y;
        float degrees = 0;
        if (bx == ax) {//y轴相等 90度或270
            if (by > ay) {//在y周的下边 90
                degrees = 90;
            } else if (by < ay) {
                degrees = 270;
            }
        } else if (by == ay) {
            if (bx > ax) {
                degrees = 0;
            } else if (bx < ax) {
                degrees = 180;
            }
        } else {
            if (bx > ax) {
                if (by > ay) {
                    degrees = 0;
                    degrees = degrees + switchDegrees(Math.abs(by - ay), Math.abs(bx - ax));
                } else if (by < ay) {
                    degrees = 360;
                    degrees = degrees - switchDegrees(Math.abs(by - ay), Math.abs(bx - ax));
                }
            } else if (bx < ax) {
                if (by > ay) {
                    degrees = 90;
                    degrees = degrees + switchDegrees(Math.abs(bx - ax), Math.abs(by - ay));
                } else if (by < ay) {
                    degrees = 270;
                    degrees = degrees - switchDegrees(Math.abs(bx - ax), Math.abs(by - ay));
                }
            }
        }

        return degrees;
    }

    private float switchDegrees(float x, float y) {
        //弧度转化为角度
        return (float) Math.toDegrees(Math.atan2(x, y));
    }

    /**
     * 自定义点类
     */
    public static class Point {
        //正常
        public static int STATE_NORMAL = 0;
        //选中
        public static int STATE_PRESSED = 1;
        //错误
        public static int STATE_ERROR = 2;

        public float x, y;
        public int index = 0;
        public int state = 0;

        public Point() {

        }

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        /**
         * 计算两点之间的距离
         */
        public static double distance(Point a, Point b) {
            return Math.sqrt(Math.abs(a.x - b.x) * Math.abs(a.x - b.x)
                    + Math.abs(a.y - b.y) * Math.abs(a.y - b.y));
        }

        /**
         * 点与线是否重合
         */
        public static boolean with(float pointX, float pointY, float r, float movingX, float movingY) {

            boolean result = false;
            //开方
            double R = Math.sqrt((pointX - movingX) * (pointX - movingX)
                    + (pointY - movingY) * (pointY - movingY));

            if ((float) R < r) {
                result = true;
            }

            return result;
        }
    }

    /**
     * 图案监听器
     */
    public static interface OnPatterChangeListener {
        void onPatterChange(String passwordStr);
        void onPatterStart(boolean isStart);
    }

    /**
     * 设置图案的监听器
     */
    public void setOnPatterChangeListener(OnPatterChangeListener onPatterChangeListener) {
        if (onPatterChangeListener != null)
            this.onPatterChangeListener = onPatterChangeListener;
    }


}


