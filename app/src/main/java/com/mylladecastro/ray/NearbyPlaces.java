package com.mylladecastro.ray;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mylladecastro on 04/04/2018.
 */


public class NearbyPlaces extends AsyncTask<Object, String, String> {
    private static final String TAG = NearbyPlaces.class.getSimpleName();
    String googlePlacesData;
    GoogleMap mMap;
    String url;
    protected List<HashMap<String, String>> nearbyPlacesList;
    private Location currentLocation;
    private int PROXIMITY_RADIUS = 500;


    @Override
    protected String doInBackground(Object... params) {
        try {
            Log.d(TAG, "doInBackground started");
            this.mMap = (GoogleMap) params[0];
            Log.d(TAG, "NearbyPlaces map: " + mMap.toString());
            this.currentLocation = (Location) params[1];
            // Getting url
            String url = getUrl(currentLocation.getLatitude(), currentLocation.getLongitude());
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
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);

            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            String type = googlePlace.get("types");

            Log.d(TAG, placeName);
            Log.d(TAG, vicinity);
            Log.d(TAG, type);

            Double poi_lat = Double.valueOf(googlePlace.get("lat"));
            Double poi_lng = Double.valueOf(googlePlace.get("lng"));
            LatLng poi_location = new LatLng(poi_lat, poi_lng);


            LatLng current_location = new LatLng(this.currentLocation.getLatitude(), this.currentLocation.getLongitude());

            double distance = calculationByDistance(current_location, poi_location);

            addNearbyPlaceMarker(poi_lat, poi_lng);


        }

    }

    private void addNearbyPlaceMarker(Double poi_lat, Double poi_lng) {
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(poi_lat, poi_lng))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

    }

    public double calculationByDistance(LatLng startP, LatLng endP) {
        float[] distance = new float[2];

        Location.distanceBetween( startP.latitude, startP.longitude,
                endP.latitude, endP.longitude, distance);

        Log.d(TAG, "Distance in meters: " + String.valueOf(distance[0]));

        return distance[0];
    }

    private String getUrl(double latitude, double longitude) {
        String nearbyPlace = "school";
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&key=" + "AIzaSyDRJGcOuLrHdGBPdHssSMaLAJQ4AkjuQck");

        Log.d(TAG, "getUrl " + googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

}
