package comx.detian.clocklibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Vibrator;
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
    private static final float NUM_PADDING = 5f;
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

    private long gameStartTime;
    private int retries;
    private int clockSize = 6;

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(100);
        paint.setColor(Color.WHITE);
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
        System.out.println("setting data");
        data = d;
        numCenters = null;

        updateBounds(getWidth(), getHeight());

        retries = 0;
        gameStartTime = System.currentTimeMillis();
        invalidate();
        ((Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
    }

    private void updateBounds(int cWidth, int cHeight){
        if (numCenters==null){
            numCenters = new PointF[data.getSize()];
        }

        separationDegree = (360.0 / data.getSize()) * (Math.PI / 180.0);
        clockRadius = Math.min(cWidth, cHeight)/2-10f;
        numberRadius = clockRadius / 5.0;

        //High school Geometry FTW
        double temp =  Math.sqrt(2 * (clockRadius * clockRadius) - 2 * (clockRadius * clockRadius) * Math.cos(separationDegree))/2.0;
        numberRadius = (float) Math.min(numberRadius, Math.sqrt(2 * Math.pow(clockRadius - temp, 2) - 2 * Math.pow(clockRadius - temp, 2) * Math.cos(separationDegree)) / 2.0);

        setTextSizeForCircle(paint, (float) (numberRadius * 2), data.getSize() + "");

        float centerX = cWidth / 2;
        float centerY = cHeight / 2;

        float currDegree = 0;

        for(int i=0; i<data.getSize(); i++){
            float numCenterX = (float) (centerX + Math.cos(currDegree) * (clockRadius - numberRadius - NUM_PADDING));
            float numCenterY = (float) (centerY + Math.sin(currDegree) * (clockRadius - numberRadius - NUM_PADDING));

            numCenters[i] = new PointF(numCenterX, numCenterY);
            currDegree += separationDegree;
        }
    }

    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint
     *            the Paint to set the text size for
     * @param desiredDiameter
     *            the desired diameter
     * @param text
     *            the text that should be that width
     */
    private static void setTextSizeForCircle(Paint paint, float desiredDiameter, String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        float diagonal = (float) (Math.sqrt(Math.pow(bounds.width(), 2) + Math.pow(bounds.height(), 2)));

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredDiameter / diagonal;

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
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

        canvas.drawColor(Color.BLACK);

        float cWidth = canvas.getWidth();
        float cHeight = canvas.getHeight();

        float centerX = cWidth / 2;
        float centerY = cHeight / 2;

        paint.setColor(Color.RED);
        canvas.drawCircle(centerX, centerY, clockRadius, paint);

        paint.setColor(Color.BLUE);
        canvas.drawCircle(centerX, centerY, (float) (clockRadius - 2 * numberRadius - 3 * NUM_PADDING), paint);
        /*long currentTimeMillis = System.currentTimeMillis();
        double degree = ((currentTimeMillis % 60000) / 60000.0) * 2 * Math.PI - (0.25 * Math.PI);

        canvas.drawLine(centerX, centerY, (float) Math.cos(degree) * clockRadius + centerX, (float) Math.sin(degree) * clockRadius + centerY, paint);*/

        for(int i=0; i<data.getSize(); i++){
            float numCenterX = numCenters[i].x;
            float numCenterY = numCenters[i].y;

            if (data.isMarked(i)){
                //paint.setStrikeThruText(true);
                paint.setColor(Color.GRAY);
            }else{
                //paint.setStrikeThruText(false);
                paint.setColor(Color.WHITE);
            }
            canvas.drawCircle(numCenterX, numCenterY, (float) numberRadius, paint);

            canvas.drawText(data.get(i)+"", numCenterX - (paint.measureText(data.get(i)+"")/2f), numCenterY - ((paint.ascent()+paint.descent())/2f), paint);

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

                paint.setColor(Color.RED);
                double degreeDistanceCW = percentage * ((handsReseting ? data.getCurrentIndex() : data.getHandCW()) - oldCWHand) * separationDegree;
                double degreeDistanceCCW = percentage * ((handsReseting ? data.getCurrentIndex() : data.getHandCCW()) - oldCCWHand) * separationDegree;

                canvas.drawLine(centerX, centerY, (float) (centerX + Math.cos(separationDegree * oldCCWHand + degreeDistanceCCW) * (clockRadius - numberRadius)), (float) (centerY + Math.sin(degreeDistanceCCW + separationDegree * oldCCWHand) * (clockRadius - numberRadius)), paint);
                canvas.drawLine(centerX, centerY, (float) (centerX + Math.cos(separationDegree * oldCWHand + degreeDistanceCW) * (clockRadius - numberRadius)), (float) (centerY + Math.sin(degreeDistanceCW + separationDegree * oldCWHand) * (clockRadius - numberRadius)), paint);
            }else {
                if (i == data.getHandCCW() || i == data.getHandCW()) {
                    //paint.setColor(Color.WHITE);
                    canvas.drawLine(centerX, centerY, numCenterX, numCenterY, paint);
                }
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        //return super.onTouchEvent(event);
        return true;
    }

    @Override
    public void run() {
        postInvalidate();
        if (handsMoving || handsReseting)
            postDelayed(this,33); //around 30fps
    }

    private class InternalGestureListener extends GestureDetector.SimpleOnGestureListener{
        ClockView cv;

        public InternalGestureListener(ClockView cv){
            this.cv = cv;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e){
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e){
            Toast.makeText(getContext(), "Retries: "+ (++retries), Toast.LENGTH_SHORT).show();
            data.clearMarks();
            invalidate();
            ((Vibrator) cv.getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(200);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
            float diffY = e2.getY() - e1.getY();
            if (diffY > Math.max(200, cv.getHeight()/2.0)){
                clockSize++;
                setData(new ClockPuzzle(clockSize));
            }else if (diffY < -1 * Math.max(200, cv.getHeight()/2.0)){
                if (clockSize > 4){
                    clockSize--;
                    setData(new ClockPuzzle(clockSize));
                }
            }

            if (e1.getX() - e2.getX() > Math.max(200, cv.getWidth() / 2.0)){
                setData(new ClockPuzzle(clockSize));
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event){
            int index = cv.getNumTouched(event.getX(), event.getY());

            int tempOldCCWHand = data.getHandCCW();
            int tempOldCWHand = data.getHandCW();

            if (cv.data.setMarked(index)) {
                if (data.hasWon()) {
                    Toast.makeText(getContext(), "Won!!!\nRetries: "+ retries+"\nTime: "+(System.currentTimeMillis()-gameStartTime)/1000.0+ "sec", Toast.LENGTH_SHORT).show();
                    setData(new ClockPuzzle(clockSize++));
                }else if (data.hasLost()) {
                    Toast.makeText(getContext(), "Lost :(\nRetries: "+ retries++, Toast.LENGTH_SHORT).show();
                    data.clearMarks();
                    oldCWHand = -1;
                    ((Vibrator) cv.getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(200);
                }else{
                    handsReseting = true;
                    startMoveTime = System.currentTimeMillis();
                }

                if (oldCWHand==-1){
                    oldCWHand = index;
                    oldCCWHand = index;
                }else{
                    oldCCWHand = tempOldCCWHand;
                    oldCWHand = tempOldCWHand;
                }

                if (oldCWHand==index && oldCCWHand==index){
                    handsReseting = false;
                    handsMoving = true;
                }

                cv.post(cv);
            }

            return true;
        }
    }
}
