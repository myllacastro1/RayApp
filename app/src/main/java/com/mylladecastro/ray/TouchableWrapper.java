package com.mylladecastro.ray;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.mylladecastro.ray.MapsActivity;

public  class TouchableWrapper extends FrameLayout implements
        GestureDetector.OnGestureListener{

    private long lastTouched = 0;
    private static final long SCROLL_TIME = 200L; // 200 Milliseconds, but you can adjust that to your liking
    private UpdateMapAfterUserInterection updateMapAfterUserInterection;
    private static final String DEBUG_TAG = TouchableWrapper.class.getSimpleName();
    private GestureDetectorCompat mDetector;

    //MapsActivity mapsActivity;

    public TouchableWrapper(Context context) {
        super(context);
        mDetector = new GestureDetectorCompat(context,this);
        // Force the host activity to implement the UpdateMapAfterUserInterection Interface
        try {
            updateMapAfterUserInterection = (MapsActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement UpdateMapAfterUserInterection");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(DEBUG_TAG,"Action was DOWN");
                //return true;
                break;
            case MotionEvent.ACTION_UP:
                Log.d(DEBUG_TAG,"Action was UP");
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(DEBUG_TAG,"onDown: " + event.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
        Log.d(DEBUG_TAG,"onShowPress: " + motionEvent.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.d(DEBUG_TAG,"onSingleTapUp: " + motionEvent.toString());
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        Log.d(DEBUG_TAG,"onLongPress: " + motionEvent.toString());
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        Log.d(DEBUG_TAG,"onFling: " + motionEvent.toString());
        return false;
    }

    // Map Activity must implement this interface
    public interface UpdateMapAfterUserInterection {
        public void onUpdateMapAfterUserInterection();
    }
}