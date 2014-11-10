package comx.detian.clockpuzzle;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Detian on 11/2/2014.
 */
public class ClockGestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onSingleTapUp(MotionEvent event){
        System.out.println(event.getX() + " " + event.getY());
        return true;
    }
}
