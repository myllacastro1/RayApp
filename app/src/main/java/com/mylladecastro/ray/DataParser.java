package com.mylladecastro.ray;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mylladecastro on 05/04/2018.
 */

public class DataParser {
    private static final String TAG = DataParser.class.getSimpleName();


    public List<HashMap<String, String>> parse(String jsonFile) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            Log.d(TAG, "parse");

            jsonObject = new JSONObject((String) jsonFile);
            jsonArray = jsonObject.getJSONArray("results");

        } catch (JSONException e) {
            Log.d(TAG, "Parse: exception.");
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int placesCount = jsonArray.length();
        Log.d(TAG, "print array lenght: " + jsonArray.length());
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap = null;
        Log.d(TAG, "getPlaces: collecting places");

        for (int i = 0; i < placesCount; i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);
                Log.d(TAG, "getPlaces: place added.");

            } catch (JSONException e){
                Log.d(TAG, "getPlaces: exception.");
                e.printStackTrace();
            }
        }
        
        return placesList;

    }

    private HashMap<String,String> getPlace(JSONObject googlePlaceJson) {
        HashMap<String, String> googlePlaceMap = new HashMap<String, String>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String open_now = "";

        Log.d(TAG, "getPlace: collecting places");

        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");
            }
            // Checking if opening hours is available. If yes, then insert it into the HashMap
            if (googlePlaceJson.has("opening_hours")) {
                open_now = String.valueOf(googlePlaceJson.getJSONObject("opening_hours").getBoolean("open_now"));
                googlePlaceMap.put("open_now", open_now);
            }
            // Get list of types for that PoI
            JSONArray types = googlePlaceJson.getJSONArray("types");
            // Get lat and lgn for that PoI
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("types", types.getString(0));
            Log.d(TAG, "Putting Places");
        } catch (JSONException e) {
            Log.d(TAG, "Error");
            e.printStackTrace();
        }
        return googlePlaceMap;
    }
}
