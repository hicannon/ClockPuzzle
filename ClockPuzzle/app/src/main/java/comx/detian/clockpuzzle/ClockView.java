package comx.detian.clockpuzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


/**
 * TODO: document your custom view class.
 */
public class ClockView extends View implements Runnable {
    private static final long HANDS_MOVE_TIME = 1000; //millis
    private static final long HANDS_RESET_TIME = 1000;
    private Paint paint;
    private ClockPuzzle data;

    private GestureDetectorCompat mDetector;

    private PointF[] numCenters = null;
    private float clockRadius;

    private boolean handsReseting = false, handsMoving = false;
    private int oldCWHand = -1;
    private int oldCCWHand = -1;
    private long startMoveTime;
    private double separationDegree;
    private double numberRadius;


    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(100);
        mDetector = new GestureDetectorCompat(context, new InternalGestureListener(this));

        handsMoving = false;

        //if (isInEditMode()){
            setData(new ClockPuzzle(8));
        //}

        this.run();
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClockView(Context context) {
        super(context);
    }

    public void setData(ClockPuzzle d){
        data = d;

        updateBounds(getWidth(), getHeight());
    }

    private void updateBounds(int cWidth, int cHeight){
        if (numCenters==null){
            numCenters = new PointF[data.getSize()];
        }

        float centerX = cWidth / 2;
        float centerY = cHeight / 2;

        float currDegree = 0;

        for(int i=0; i<data.getSize(); i++){
            float numCenterX = (float) (centerX + Math.cos(currDegree) * (clockRadius - numberRadius));
            float numCenterY = (float) (centerY + Math.sin(currDegree) * (clockRadius - numberRadius));

            numCenters[i] = new PointF(numCenterX, numCenterY);
            currDegree += separationDegree;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        separationDegree = (360.0 / data.getSize()) * (Math.PI / 180.0);
        clockRadius = Math.min(w, h)/2-10f;
        numberRadius = clockRadius / 5.0;
        updateBounds(w, h);
    }

    public int getNumTouched(float x, float y){
        for (int i=0; i<numCenters.length; i++){
            if (Math.sqrt(Math.pow(numCenters[i].x-x, 2) + Math.pow(numCenters[i].y-y, 2)) < numberRadius){
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        if (data==null){
            return;
        }

        float cWidth = canvas.getWidth();
        float cHeight = canvas.getHeight();

        float centerX = cWidth / 2;
        float centerY = cHeight / 2;
        canvas.drawCircle(centerX, centerY, clockRadius, paint);

        long currentTimeMillis = System.currentTimeMillis();
        double degree = ((currentTimeMillis % 60000) / 60000.0) * 2 * Math.PI - (0.25 * Math.PI);

        //canvas.drawLine(centerX, centerY, (float) Math.cos(degree) * clockRadius + centerX, (float) Math.sin(degree) * clockRadius + centerY, paint);

        for(int i=0; i<data.getSize(); i++){
            float numCenterX = numCenters[i].x;
            float numCenterY = numCenters[i].y;
            canvas.drawCircle(numCenterX, numCenterY, (float) numberRadius, paint);

            if (data.isMarked(i)){
                paint.setStrikeThruText(true);
            }else{
                paint.setStrikeThruText(false);
            }

            if (handsMoving || handsReseting){
                long elapsedTime = System.currentTimeMillis() - startMoveTime;
                double percentage = elapsedTime / (double) (handsMoving ? HANDS_MOVE_TIME : HANDS_RESET_TIME);

                if (percentage >= 1){
                    if (handsReseting){
                        handsReseting = false;
                        handsMoving = true;
                        oldCCWHand = data.getCurrentIndex();
                        oldCWHand = data.getCurrentIndex();
                        percentage = 0;
                        startMoveTime = System.currentTimeMillis();
                    }else if (handsMoving)
                        handsMoving = false;
                }

                double degreeDistanceCW = percentage * ((handsReseting ? data.getCurrentIndex() : data.getHandCW()) - oldCWHand) * separationDegree;
                double degreeDistanceCCW = percentage * ((handsReseting ? data.getCurrentIndex() : data.getHandCCW()) - oldCCWHand) * separationDegree;

                canvas.drawLine(centerX, centerY, (float) (centerX + Math.cos(separationDegree * oldCCWHand + degreeDistanceCCW) * (clockRadius - numberRadius)), (float) (centerY + Math.sin(degreeDistanceCCW + separationDegree * oldCCWHand) * (clockRadius - numberRadius)), paint);
                canvas.drawLine(centerX, centerY, (float) (centerX + Math.cos(separationDegree * oldCWHand + degreeDistanceCW) * (clockRadius - numberRadius)), (float) (centerY + Math.sin(degreeDistanceCW + separationDegree * oldCWHand) * (clockRadius - numberRadius)), paint);
            }else {
                if (i == data.getHandCCW() || i == data.getHandCW()) {
                    canvas.drawLine(centerX, centerY, numCenterX, numCenterY, paint);
                }
            }

            canvas.drawText(data.get(i)+"", numCenterX - (paint.measureText(data.get(i)+"")/2f), numCenterY - ((paint.ascent()+paint.descent())/2f), paint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void run() {
        postInvalidate();
        postDelayed(this,33); //around 30fps
    }

    private class InternalGestureListener extends GestureDetector.SimpleOnGestureListener{
        ClockView cv;

        public InternalGestureListener(ClockView cv){
            this.cv = cv;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            int index = cv.getNumTouched(event.getX(), event.getY());

            oldCCWHand = data.getHandCCW();
            oldCWHand = data.getHandCW();

            if (cv.data.setMarked(index)) {
                if (oldCWHand==-1){
                    oldCWHand = index;
                    oldCCWHand = index;
                }

                if (data.hasWon()) {
                    Toast.makeText(getContext(), "Won!!!", Toast.LENGTH_SHORT).show();
                    data.clearMarks();
                }else if (data.hasLost()) {
                    Toast.makeText(getContext(), "Lost :(", Toast.LENGTH_SHORT).show();
                    data.clearMarks();
                }else{
                    handsReseting = true;
                    startMoveTime = System.currentTimeMillis();
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event){
            int index = cv.getNumTouched(event.getX(), event.getY());
            System.out.println("abbbb");
            Toast.makeText(getContext(), index+"", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}
