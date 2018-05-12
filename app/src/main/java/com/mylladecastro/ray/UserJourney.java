package com.mylladecastro.ray;

import android.content.Context;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class UserJourney {

    TextToVoice tts;
    MapsActivity mapsActivity;
    Context context;
    NearbyPlaces nearbyPlaces;
    int distance;
    private static final String TAG = UserJourney.class.getSimpleName();

    UserJourney(Context context) {
        this.context = context;
    }

    public void getDistance() {
        nearbyPlaces = nearbyPlaces.getInstance();
        this.distance = nearbyPlaces.getDistance();
        vibrate(distance);
    }

    public void vibrate(int distance) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            //tts.setStopSpeaking(true);
            //need to listen stop touch from the maps activity
            for (int i = 0; i < 10; i++) {
                //int distance = mapsActivity.getDistance();
                if (distance > 7) {
                    vibrator.vibrate(300); // for 500 ms
                } else if (distance > 3 && distance < 7) {
                    vibrator.vibrate(500); // for 500 ms
                } else if (distance < 3) {
                    vibrator.vibrate(1000); // for 500 ms
                }

            }
        }

    }


}
