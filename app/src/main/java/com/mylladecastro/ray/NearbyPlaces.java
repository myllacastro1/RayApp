package com.mylladecastro.ray;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    TextToVoice textToVoice;
    List<String> places = new ArrayList<>();
    private boolean continue_tss = true;
    private static NearbyPlaces instance;
    TextToVoice tts;
    private int distance;

    private HashMap<String, String> currentGooglePlace;

    public static NearbyPlaces getInstance() {
        if (instance == null) {
            instance = new NearbyPlaces();
            return instance;
        }
        return instance;
    }


    private NearbyPlaces() {

    }


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

    public int getDistance() {
        return distance;
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
            Log.d(TAG, "Type " + type);
            // Creating PoI location object
            //LatLng poi_location = new LatLng(poi_lat, poi_lng);
            // Creating current location obj
            //LatLng current_location = new LatLng(this.currentLocationLatitude, this.currentLocationLongitude);
            // Calculating distance between the 2 points
            this.distance = calculationByDistance(poi_lat, poi_lng);
            Log.d(TAG, "fillNearbyPlacesList continue_tss: " + this.continue_tss);

            if (distance <= 15 && this.continue_tss == true) {
                Log.d(TAG, "fillNearbyPlacesList continue_tss: " + this.continue_tss + "; distance: " + distance);
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
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                break;
            case "taxi_stand":
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                break;
            default:
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
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
        this.setCurrentGooglePlace(googlePlace);
        String placeName = googlePlace.get("place_name");
        Log.d(TAG, "tTS handler currentPlace: " + placeName);
        context = mapsActivity.getContext();

        if (context != null) {
            //&& !places.contains(placeName)
            //places.add(placeName);
            Log.d(TAG, "tTS handler creating tts obj");
            this.tts = new TextToVoice(context, placeName);
            //tts.setStopSpeaking();
            //Log.d(TAG, "places array: " + places.toString());
        }

    }

    public HashMap<String, String> getCurrentGooglePlace() {
        return currentGooglePlace;
    }

    public void setCurrentGooglePlace(HashMap<String, String> currentGooglePlace) {
        this.currentGooglePlace = currentGooglePlace;
    }

    public void setContinue_tss(boolean continue_tss) {
        this.continue_tss = continue_tss;
    }


    public void ttsKnowMore() {
        HashMap<String, String> place = this.getCurrentGooglePlace();
        context = mapsActivity.getContext();
        String text = null;


        String type = place.get("types");
        Log.d(TAG, "Open now: " + type);
        String open_now = place.get("open_now");
        Log.d(TAG, "Open now: " + open_now);

        switch (open_now) {
            case "true":
                text = "This is a " + type + " and is open now";
                break;
            case "false":
                text = "This is a " + type + " and is closed now";
                break;
            default:
                text = "This is a " + type;
                break;
        }

        Log.d(TAG, "Text: " + text);
        this.tts = new TextToVoice(context, text);
        //keep telling places around
        this.continue_tss = true;


    }
}
