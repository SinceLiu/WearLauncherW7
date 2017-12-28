package com.readboy.wearlauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/8/3.
 */

public class MyTextView extends TextView{

    TextPaint mTextPaint;
    String TEXT;
    Paint mPaint = new Paint();

    public MyTextView(Context context) {
        this(context,null);
    }

    public MyTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextPaint = getPaint();
        TEXT = getText().toString();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        Log.d("Aige", "ascent：" + fontMetrics.ascent);
        Log.d("Aige", "top：" + fontMetrics.top);
        Log.d("Aige", "leading：" + fontMetrics.leading);
        Log.d("Aige", "descent：" + fontMetrics.descent);
        Log.d("Aige", "bottom：" + fontMetrics.bottom);
        // 计算Baseline绘制的起点X轴坐标 ，计算方式：画布宽度的一半 - 文字宽度的一半
        int baseX = (int) (canvas.getWidth() / 2 - mTextPaint.measureText(TEXT) / 2);

        // 计算Baseline绘制的Y坐标 ，计算方式：画布高度的一半 - 文字总高度的一半
        int baseY = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));

        // 居中画一个文字
        canvas.drawText(TEXT, baseX, baseY, mTextPaint);

        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(4);
        // 为了便于理解我们在画布中心处绘制一条中线
        canvas.drawLine(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2, mPaint);
    }
}
