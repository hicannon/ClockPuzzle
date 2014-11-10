package comx.detian.clockpuzzle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


/**
 * TODO: document your custom view class.
 */
public class ClockView extends View implements Runnable {
    private Paint paint;
    private GestureDetector gestureDetector;

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        gestureDetector = new GestureDetector(context, new ClockGestureListener());
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClockView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        float cWidth = canvas.getWidth();
        float cHeight = canvas.getHeight();

        float radius =  Math.min(cHeight, cWidth)/2-10f;

        canvas.drawCircle(cWidth / 2, cHeight /2 , radius, paint);

        long currentTimeMillis = System.currentTimeMillis();
        double degree = ((currentTimeMillis % 60000) / 60000.0) * 2 * Math.PI - (0.25 * Math.PI);

        canvas.drawLine(cWidth/2, cHeight/2, (float) Math.cos(degree) * radius + cWidth/2, (float) Math.sin(degree) * radius + cHeight/2, paint);

        postDelayed(this,33); //around 30fps
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void run() {
        invalidate();
    }
}
