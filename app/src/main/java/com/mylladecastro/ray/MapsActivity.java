package com.mylladecastro.ray;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final LatLng OCONNELL = new LatLng(53.3469168, -6.2589656);
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 19f;


    //vars
    private boolean LocationPermissionGranted = false;
    private GoogleMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Checking if Google Maps service is available
        isServiceOK();
        //Checking if location permissions are available and initializing map
        getLocationPermission();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: applying map view!");
        map = googleMap;
        boolean success = map.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }

        if (LocationPermissionGranted){
            getDeviceLocation();
        }

        // Set a preference for minimum and maximum zoom.
        //googleMap.setMinZoomPreference(19.0f);
        //googleMap.setMaxZoomPreference(21.0f);
    }

    private void getDeviceLocation(){
        Log.d(TAG, "Getting device's current location!");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if (LocationPermissionGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: foundLocation!");
                            Location currentLocation = (Location) task.getResult();

                            LatLng m = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }

        //mFusedLocationProviderClient = LocationSer
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round)));


    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map!");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocationPermission: fine location was granted!");

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationPermissionGranted = true;
                Log.d(TAG, "getLocationPermission: coarse location was granted!");
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            LocationPermissionGranted = false;
                            return;
                        }
                    }
                    LocationPermissionGranted = true;
                    //initialize our map
                    Log.d(TAG, "onRequestPermission: coarse location was granted!".concat(grantResults.toString()));
                    initMap();
                }
            }
        }
    }


    public boolean isServiceOK() {
        Log.d(TAG, "isServiceOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and user can make Map requests
            Log.d(TAG, "isServiceOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred
            Log.d(TAG, "isServiceOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


}
