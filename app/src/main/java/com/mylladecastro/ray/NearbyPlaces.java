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
import java.util.Arrays;
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
    private boolean continue_tss;
    private static NearbyPlaces instance;
    TextToVoice tts;
    private int distance;
    private boolean addMarkers;
    private ArrayList recentPlaces = new ArrayList<>();

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
            Log.d(TAG, "NearbyPlaces latitude: " + currentLocationLatitude);
            this.currentLocationLongitude = (double) params[2];
            Log.d(TAG, "NearbyPlaces longitude: " + currentLocationLongitude);
            this.addMarkers = (boolean) params[3];
            Log.d(TAG, "NearbyPlaces addMarker: " + addMarkers);


            if (addMarkers == true) {
                //mapsActivity.setSetMarkers(false);
                this.continue_tss = true;
                // Getting url
                String url = getUrl(currentLocationLatitude, currentLocationLongitude);
                // Downloading url data (json)
                DownloadUrl downloadUrl = new DownloadUrl();
                googlePlacesData = downloadUrl.readUrl(url);
                Log.d(TAG, "googlePlacesData: " + googlePlacesData);
            }

        } catch (Exception e) {
            Log.d(TAG, "doInBackground: exception");
            e.printStackTrace();
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result){
        if (this.addMarkers == true) {
            Log.d(TAG, "onPostExecute Entered");
            DataParser dataParser = new DataParser();
            this.nearbyPlacesList = dataParser.parse(result);

            Log.d(TAG, "onPostExecute Exit");

            fillNearbyPlacesList(this.nearbyPlacesList);
            //nearbyPlaceName();
        }
    }



    public void nearbyPlaceName(LatLng currentLocation) {
        this.currentLocationLatitude = currentLocation.latitude;
        this.currentLocationLongitude = currentLocation.longitude;
        int userDistance;

        for (int i = 0; i < this.nearbyPlacesList.size(); i++) {
            //
            HashMap<String, String> googlePlace = this.nearbyPlacesList.get(i);

            Double poi_lat = Double.valueOf(googlePlace.get("lat"));
            Double poi_lng = Double.valueOf(googlePlace.get("lng"));
            String type = googlePlace.get("types");
            Log.d(TAG, "nearbyPlaceName Type " + type);
            // Calculating distance between the 2 points
            userDistance = calculationByDistance(poi_lat, poi_lng);
            Log.d(TAG, "nearbyPlaceName continue_tss: " + this.continue_tss);

            if (userDistance <= 7 && this.continue_tss == true && this.currentGooglePlace != googlePlace) {
                Log.d(TAG, "nearbyPlaceName continue_tss: " + this.continue_tss + "; distance: " + userDistance);
                textToSpeechHandler(googlePlace);
            }
        }
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
            // Add markers to the map
            addNearbyPlaceMarker(type, poi_lat, poi_lng);
        }
    }

    private void addNearbyPlaceMarker(String type, Double poi_lat, Double poi_lng) {
        Log.d(TAG, "Adding marker... " + type);
        switch (type) {
            case "restaurant":
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_blue)));
                break;
            case "location":
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_green)));
                break;
            case "store":
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_yellow)));
                break;
            case "route":
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_blue)));
                break;
            default:
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(poi_lat, poi_lng))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_ball)));
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
        //this.setCurrentGooglePlace(googlePlace);
        String placeName = googlePlace.get("place_name");
        Log.d(TAG, "tTS handler currentPlace: " + placeName);
        context = mapsActivity.getContext();

        if (context != null) {
            Log.d(TAG, "tTS handler recentPlaces list: " + this.recentPlaces);
            if (!this.recentPlaces.contains(googlePlace.get("place_name"))) {
                this.recentPlaces.add(googlePlace.get("place_name"));
                this.setCurrentGooglePlace(googlePlace);
                Log.d(TAG, "tTS handler creating tts obj");
                this.tts = new TextToVoice(context, placeName);
                this.continue_tss = true;
                //tts.setStopSpeaking();
                //Log.d(TAG, "places array: " + places.toString());
            } else {
                Log.d(TAG, "tTS handler: " + "CONTAIN");
            }
        }

    }

    public HashMap<String, String> getCurrentGooglePlace() {
        Log.d(TAG, "getCurrentGooglePlace: " + this.currentGooglePlace);
        return this.currentGooglePlace;
    }

    public void setCurrentGooglePlace(HashMap<String, String> currentGooglePlace) {
        this.currentGooglePlace = currentGooglePlace;
    }

    public void setContinue_tss(boolean continue_tss) {
        this.continue_tss = continue_tss;
        Log.d(TAG, "setContinue_tss: " + continue_tss);
    }

    public void ttsCurrentLocation () {
        HashMap<String, String> place = this.getCurrentGooglePlace();
        context = mapsActivity.getContext();
        String text = null;

        String vinicity = place.get("vicinity");
        text = "Your current address is " + vinicity;
        Log.d(TAG, "Current address: " + vinicity);
        this.tts = new TextToVoice(context, text);
        //keep telling places around
        this.continue_tss = true;
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
                text = "This is a " + type + " and it is open now";
                break;
            case "false":
                text = "This is a " + type + " and it is closed now";
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

    public double getCurrentLocationLatitude() {
        return currentLocationLatitude;
    }

    public double getCurrentLocationLongitude() {
        return currentLocationLongitude;
    }
}
