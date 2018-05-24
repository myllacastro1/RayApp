package com.mylladecastro.ray;

import android.content.Context;
import android.location.Location;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class UserJourney {

    TextToVoice tts;
    MapsActivity mapsActivity;
    Context context;
    NearbyPlaces nearbyPlaces;
    int distance;
    private static final String TAG = UserJourney.class.getSimpleName();
    private boolean stopVibration;
    private HashMap<String, String> selectedPlace;
    private int previousDistance;

    UserJourney(Context context) {
        this.context = context;
        this.stopVibration = true;
        this.nearbyPlaces = NearbyPlaces.getInstance();
        this.previousDistance = 0;
        Log.d(TAG, "UserJourney selectedPoI: " + selectedPlace);
    }


    public void startJourney() {
        this.selectedPlace = nearbyPlaces.getCurrentGooglePlace();
        int distance = calculationByDistance();

        if (this.stopVibration == false) {
            vibrate(distance);
        }
    }


    public void vibrate(int distance) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            //tts.setStopSpeaking(true);
            //need to listen stop touch from the maps activity
            for (int i = 0; i < 10; i++) {
                //int distance = mapsActivity.getDistance();
                if (distance > 5) {
                    vibrator.vibrate(100); // for 500 ms
                    Log.d(TAG, "User Journey vibration 300");
                } else if (distance > 3 && distance < 5) {
                    vibrator.vibrate(300); // for 500 ms
                    Log.d(TAG, "User Journey vibration 500");
                } else if (distance < 3) {
                    vibrator.vibrate(500); // for 500 ms
                    Log.d(TAG, "User Journey vibration 1000");
                }

            }
        }

    }


    public int calculationByDistance() {
        float[] distance = new float[2];


        Double poi_lat = Double.valueOf(this.selectedPlace.get("lat"));
        Double poi_lng = Double.valueOf(this.selectedPlace.get("lng"));

        Location.distanceBetween(this.nearbyPlaces.getCurrentLocationLatitude(), this.nearbyPlaces.getCurrentLocationLongitude(), poi_lat, poi_lng, distance);

        int approximate_distance = (int) distance[0];

        //Log.d(TAG, "User Journey Distance in meters: " + approximate_distance);

        return approximate_distance;
    }


    public void setStopVibration(boolean stopVibration) {
        this.stopVibration = stopVibration;
    }




}
