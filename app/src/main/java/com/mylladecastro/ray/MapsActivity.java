package com.mylladecastro.ray;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Response;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    double latitude;
    double longitude;
    private int PROXIMITY_RADIUS = 500;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;


    //vars
    private boolean LocationPermissionGranted = false;
    private GoogleMap map;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    private static final float DEFAULT_ZOOM = 16f;
    public static final int REQUEST_LOCATION_CODE = 99;
    private FusedLocationProviderClient mFusedLocationClient;
    String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }


        /////////////////
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        type="hospital";
        /////////////////

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "checkLocationPermission: permission");

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            // If the client is null, create client
                            Log.d(TAG, "onRequestPermissionResult: client is null");

                            buildGoogleApiClient();
                        }
                        map.setMyLocationEnabled(true);
                        Log.d(TAG, "onRequestPermissionResult: enabling location");

                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    protected synchronized void buildGoogleApiClient() {

        Log.d(TAG, "buildGoogleApiClient: building client obj");

        // Creating Google API client
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Connecting client
        client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        map.animateCamera(CameraUpdateFactory.zoomBy(DEFAULT_ZOOM));
        // Location request object
        locationRequest = new LocationRequest();
        Log.d(TAG, "onConnected: requesting location");

        // Location request interval in milliseconds
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onConnected: permission is granted");

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
            // for ActivityCompat#requestPermissions for more details
        } else {
            Log.d(TAG, "onConnected: permission not granted");

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: applying map view!");
        map = googleMap;


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);


            //////////////
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.getUiSettings().setAllGesturesEnabled(true);

            /////////////

            buildGoogleApiClient();
            map.setMapStyle(new MapStyleOptions(getResources()
                    .getString(R.string.style_json)));

        }
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        // Once this method is triggered...
        Log.d(TAG, "onLocationChanged: updating location...");


        lastLocation = location;

        // If the marker is already set, remove it to replace by the current location one
        if(currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        LatLng latlgn = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        // Add marker to received location
        markerOptions.position(latlgn);

        // Add red round icon to marker
        Log.d(TAG, "Adding current location icon");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round));

        currentLocationMarker = map.addMarker(markerOptions);
        // Moving camera to current location
        moveCamera(latlgn);

        getNearbyPlacesMarkers(location);



        if(client == null){
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }


    }

    private void getNearbyPlacesMarkers(Location location) {
        List<MarkerOptions> nearbyPlaces;

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        String restaurant = "restaurant";
        Log.d(TAG, "onMapReady: almost getting url");
        String url = getUrl(latitude, longitude, restaurant);
        Object[] DataTransfer = new Object[2];
        DataTransfer[0] = map;
        Log.d(TAG, "MapsActivity map: " + map.toString());
        DataTransfer[1] = url;
        Log.d(TAG, DataTransfer[0].toString());
        Log.d(TAG,"getNearbyPlacesMarkers: " + url);
        NearbyPlaces getNearbyPlacesData = new NearbyPlaces();
        getNearbyPlacesData.execute(DataTransfer);
        //Log.d(TAG,"getNearbyPlacesMarkers: " + nearbyPlaces.size());
        //for (int i = 0; i < nearbyPlaces.size(); i++) {
        //    map.addMarker(nearbyPlaces.get(i));
        //}

    }

    private void moveCamera(LatLng latlgn) {
        Log.d(TAG, "moving the camera to: lat " + latlgn.latitude + " and log " + latlgn.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlgn, DEFAULT_ZOOM));

    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&key=" + "AIzaSyBX1iSgXWG6bEwqAdcEra-vsrYkndaak6A");

        Log.d(TAG, "getUrl " + googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }



}
