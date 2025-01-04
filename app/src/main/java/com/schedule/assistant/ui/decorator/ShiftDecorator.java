package com.schedule.assistant.ui.decorator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.style.LineBackgroundSpan;
import androidx.annotation.NonNull;
import com.schedule.assistant.data.entity.ShiftType;

public class ShiftDecorator implements LineBackgroundSpan {
    private final int color;
    private final ShiftType shiftType;
    private static final float CIRCLE_RADIUS = 4.5f;
    private static final float PADDING = 2f;

    public ShiftDecorator(int color, ShiftType shiftType) {
        this.color = color;
        this.shiftType = shiftType;
    }

    @Override
    public void drawBackground(@NonNull Canvas canvas, @NonNull Paint paint,
                             int left, int right, int top, int baseline, int bottom,
                             @NonNull CharSequence text, int start, int end, int lineNum) {
        int oldColor = paint.getColor();
        paint.setColor(color);

        float radius = CIRCLE_RADIUS * paint.getTextSize() / 15f;
        float padding = PADDING * paint.getTextSize() / 15f;

        // 计算标记的位置
        float circleX = (left + right) / 2f;
        float circleY = bottom + radius + padding;

        // 根据班次类型绘制不同的标记
        switch (shiftType) {
            case DAY_SHIFT:
                // 白班：实心圆
                canvas.drawCircle(circleX, circleY, radius, paint);
                break;
            case NIGHT_SHIFT:
                // 夜班：空心圆
                Paint.Style oldStyle = paint.getStyle();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2f);
                canvas.drawCircle(circleX, circleY, radius, paint);
                paint.setStyle(oldStyle);
                break;
            case REST:
                // 休息：小方块
                float halfSize = radius * 0.8f;
                canvas.drawRect(circleX - halfSize, circleY - halfSize,
                              circleX + halfSize, circleY + halfSize, paint);
                break;
        }

        paint.setColor(oldColor);
    }
} 