package com.mylladecastro.ray;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.mylladecastro.ray.MapsActivity;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public  class TouchableWrapper extends FrameLayout implements
        OnTouchListener{

    private long lastTouched = 0;
    private static final long SCROLL_TIME = 200L; // 200 Milliseconds, but you can adjust that to your liking
    private UpdateMapAfterUserInterection updateMapAfterUserInterection;
    private static final String DEBUG_TAG = TouchableWrapper.class.getSimpleName();
    private GestureDetectorCompat mDetector;
    private NearbyPlaces nearbyPlaces;


    private static final String logTag = "SwipeDetector";
    private static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;
    private Action mSwipeDetected = Action.None;
    UserJourney userJourney;




    public TouchableWrapper(Context context) {
        super(context);


        // Force the host activity to implement the UpdateMapAfterUserInterection Interface
        try {
            updateMapAfterUserInterection = (MapsActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement UpdateMapAfterUserInterection");
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }


    public static enum Action {
        LR, // Left to Right
        RL, // Right to Left
        TB, // Top to bottom
        BT, // Bottom to Top
        None, // when no action was detected
        Click
    }

    public boolean swipeDetected() {
        return mSwipeDetected != Action.None;
    }

    public Action getAction() {
        return mSwipeDetected;
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //Log.d(DEBUG_TAG, ev.toString());
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                mSwipeDetected = Action.None;
                // Log.i(logTag, "Click On List" );
                return true; // allow other events like Click to be processed
            case MotionEvent.ACTION_UP:
                upX = ev.getX();
                upY = ev.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                Log.d(DEBUG_TAG, "deltaX: " + deltaX);
                Log.d(DEBUG_TAG, "deltaY: " + deltaY);

                // left or right
                if (deltaX < -300 && deltaY > -50 && deltaY < 200) {
                    Log.d(DEBUG_TAG, "Swipe right");
                    mSwipeDetected = Action.LR;
                    nearbyPlaces = nearbyPlaces.getInstance();
                    nearbyPlaces.setContinue_tss(false);
                    nearbyPlaces.ttsKnowMore();
                    return false;
                } else if (deltaX > 300 && deltaY > -200 && deltaY < 0) {
                    Log.d(DEBUG_TAG, "Swipe left");
                        mSwipeDetected = Action.RL;
                        return false;
                } else if (deltaY > 400 && deltaX > -100 && deltaX < 0) {
                        Log.d(DEBUG_TAG, "Swipe UP");
                        mSwipeDetected = Action.BT;
                        return false;
                } else if (deltaY > -600 && deltaX > 0) {
                        Log.d(DEBUG_TAG, "Swipe down");
                        mSwipeDetected = Action.TB;
                        return false;
                } else {
                    Log.d(DEBUG_TAG, "Moviment not recognized");
                }


        }
        return super.dispatchTouchEvent(ev);
    }

    public void touchHandler(float deltaX, float deltaY) {


    }




    // Map Activity must implement this interface
    public interface UpdateMapAfterUserInterection {
        public void onUpdateMapAfterUserInterection();
    }


}