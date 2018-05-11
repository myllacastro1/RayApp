package com.mylladecastro.ray;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by mylladecastro on 04/04/2018.
 */


@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class NearbyPlaces extends AsyncTask<Object, String, String> {
    private static final String TAG = NearbyPlaces.class.getSimpleName();
    String googlePlacesData;
    GoogleMap mMap;
    String url;
    protected List<HashMap<String, String>> nearbyPlacesList;
    private double currentLocationLatitude;
    private double currentLocationLongitude;
    private int PROXIMITY_RADIUS = 50;
    private Context context;
    MapsActivity mapsActivity;
    TextToSpeech t1;
    List<String> places = new ArrayList<>();

    @Override
    protected String doInBackground(Object... params) {
        try {
            Log.d(TAG, "doInBackground started");
            this.mMap = (GoogleMap) params[0];
            Log.d(TAG, "NearbyPlaces map: " + mMap.toString());
            this.currentLocationLatitude = (double) params[1];
            this.currentLocationLongitude = (double) params[2];
            // Getting url
            String url = getUrl(currentLocationLatitude, currentLocationLongitude);
            // Downloading url data (json)
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url);
            Log.d(TAG, "googlePlacesData: " + googlePlacesData);

        } catch (Exception e) {
            Log.d(TAG, "doInBackground: exception");
            e.printStackTrace();
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result){
        Log.d(TAG, "onPostExecute Entered");
        DataParser dataParser = new DataParser();
        this.nearbyPlacesList =  dataParser.parse(result);

        Log.d(TAG, "onPostExecute Exit");
        fillNearbyPlacesList(this.nearbyPlacesList);
    }

    private void fillNearbyPlacesList(List<HashMap<String, String>> nearbyPlacesList) {

        Log.d(TAG, "fillNearbyPlacesList: before loop. " + this.nearbyPlacesList.toString());

        // before loop:
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            //
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);

            Double poi_lat = Double.valueOf(googlePlace.get("lat"));
            Double poi_lng = Double.valueOf(googlePlace.get("lng"));
            String type = googlePlace.get("types");
            // Creating PoI location object
            //LatLng poi_location = new LatLng(poi_lat, poi_lng);
            // Creating current location obj
            //LatLng current_location = new LatLng(this.currentLocationLatitude, this.currentLocationLongitude);
            // Calculating distance between the 2 points
            int distance = calculationByDistance(poi_lat, poi_lng);

            if (distance <= 10) {
                textToSpeechHandler(googlePlace);
            }
            // Add markers to the map
            addNearbyPlaceMarker(type, poi_lat, poi_lng);
        }
    }

    private void addNearbyPlaceMarker(String type, Double poi_lat, Double poi_lng) {
        Log.d(TAG, "Adding marker... " + type);
        switch (type) {
            case "park":
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                break;
            case "restaurant":
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                break;
            case "school":
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                break;
            case "taxi_stand":
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                break;
        }

    }

    public int calculationByDistance(double poi_lat, double poi_lng) {
        float[] distance = new float[2];

        Location.distanceBetween(this.currentLocationLatitude, this.currentLocationLongitude, poi_lat, poi_lng, distance);

        int approximate_distance = (int) distance[0];

        Log.d(TAG, "Distance in meters: " + approximate_distance);

        return approximate_distance;
    }

    private String getUrl(Double latitude, Double longitude) {
        Log.d(TAG, "getUrl lat/lng: " + latitude + ", " + longitude);
        String nearbyPlace = "school,restaurant";
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=");
        googlePlacesUrl.append("&key=" + "AIzaSyDRJGcOuLrHdGBPdHssSMaLAJQ4AkjuQck");

        Log.d(TAG, "getUrl " + googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    private void textToSpeechHandler(HashMap<String, String> googlePlace) {

        String placeName = googlePlace.get("place_name");
        String vicinity = googlePlace.get("vicinity");
        String type = googlePlace.get("types");
        Log.d(TAG, "TEXT TO SPEECH handler");
        Context context = mapsActivity.getContext();

        if (context != null && !places.contains(placeName)) {
            places.add(placeName);
            TextToVoice tts = new TextToVoice(vicinity, context);
            //tts.setStopSpeaking();
            Log.d(TAG, "places array: " + places.toString());
        }

    }





}
