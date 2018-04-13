package com.mylladecastro.ray;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
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

public class NearbyPlaces extends AsyncTask<Object, String, String> {
    private static final String TAG = NearbyPlaces.class.getSimpleName();
    String googlePlacesData;
    GoogleMap mMap;
    String url;


    @Override
    protected String doInBackground(Object... params) {
        try {
            Log.d(TAG, "doInBackground started");
            this.mMap = (GoogleMap) params[0];
            Log.d(TAG, "NearbyPlaces map: " + mMap.toString());

            url = (String) params[1];
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url);
            Log.d(TAG, googlePlacesData.toString());
        } catch (Exception e) {
            Log.d(TAG, "doInBackground: exception");
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result){
        Log.d(TAG, "onPostExecute Entered");
        List<HashMap<String, String>> nearbyPlacesList = null;
        DataParser dataParser = new DataParser();
        nearbyPlacesList =  dataParser.parse(result);
        fillNearbyPlacesList(nearbyPlacesList);
        Log.d(TAG, "onPostExecute Exit");
    }



    private void fillNearbyPlacesList(List<HashMap<String, String>> nearbyPlacesList) {
        // before loop:
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));

            String lg = String.format("Value of a: %.2f", lat);
            Log.d(TAG, "ShowNearbyPlaces: inside for loop" + lg);

            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");

            Log.d(TAG, placeName);
            Log.d(TAG, vicinity);

            mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        //Log.d(TAG, String.valueOf(nearbyPlaces.size()));
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }

    }

}
