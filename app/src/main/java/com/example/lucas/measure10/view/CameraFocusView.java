package com.example.lucas.measure10.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

public class CameraFocusView extends View {

    private final static float DISTANCE = 0.33f, LINE_WIDTH = 10.0f;

    public CameraFocusView(Context context) {
        // TODO Auto-generated constructor stub
        super(context);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(drawRect(canvas));
    }

    public static Canvas drawRect(Canvas canvas) { // 畫對焦框
        float width = canvas.getWidth(),
                height = canvas.getHeight(),
                resultWidth = width * DISTANCE,
                resultHeight = height * DISTANCE,
                widthLineLength = width - (resultWidth * 2.0f),
                heightLineLength = height - (resultHeight * 2.0f);

        if (heightLineLength > widthLineLength) // 設定同等長寬判斷
            heightLineLength = widthLineLength;
        else
            widthLineLength = heightLineLength;

        float left = (width - widthLineLength) / 2,
                top = (height - heightLineLength) / 2,
                right = left + widthLineLength,
                bottom = top + heightLineLength;
        Paint paint = new Paint(); // 產生畫筆
        paint.setStrokeWidth(LINE_WIDTH); // 設定線寬
        paint.setStyle(Style.STROKE); // 設定樣式為粗體
        paint.setColor(Color.RED); // 設定顏色為紅色
        canvas.drawRect(left, top, right, bottom, paint); // 畫四邊形
        return canvas;
    }

}