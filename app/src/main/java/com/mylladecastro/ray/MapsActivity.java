//https://developer.android.com/guide/topics/ui/menus#options-menu

package com.mylladecastro.ray;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        TouchableWrapper.UpdateMapAfterUserInterection {

    public static boolean mMapIsTouched;
    double latitude;
    double longitude;
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
    private static final float DEFAULT_ZOOM = 24f;
    public static final int REQUEST_LOCATION_CODE = 99;
    private FusedLocationProviderClient mFusedLocationClient;
    String type;
    String markers;
    private List<HashMap<String, String>> nearbyPlaces;
    TextToSpeech t1;
    private Context context;
    private static MapsActivity instance;
    private LatLng initialLocation;
    public View mOriginalContentView;
    public TouchableWrapper mTouchView;
    private GestureDetectorCompat mDetector;
    NearbyPlaces getNearbyPlacesData;
    private boolean setMarkers;

    public void setSetMarkers(boolean setMarkers) {
        this.setMarkers = setMarkers;
    }




    // Implement the interface method
    public void onUpdateMapAfterUserInterection() {
        Log.d(TAG, "HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }


    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int distance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        instance = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }


        /////////////////
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
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

        Log.d(TAG, "onMapReady: applying map view!");
        map = googleMap;
        this.setMarkers = true;


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();

            //////////////
            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);
            map.setMyLocationEnabled(false);

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

    public LatLng getFirstLocation() {
        return this.initialLocation;
    }

    public void setFirstLocation(LatLng currentLocation) {
        this.initialLocation = currentLocation;
        Log.d(TAG, "setFirstLocation: " + this.initialLocation);
    }


    @Override
    public void onLocationChanged(Location location) {
        // Once this method is triggered...
        Log.d(TAG, "onLocationChanged: updating location...");
        lastLocation = location;

        //Setting current location
        // If the marker is already set, remove it to replace by the current location one
        if(currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        LatLng latlgn = new LatLng(location.getLatitude(), location.getLongitude());


        MarkerOptions markerOptions = new MarkerOptions();
        // Add marker to received location
        markerOptions.position(latlgn);

        // Add red round icon to marker
        Log.d(TAG, "Adding current location icon at " + latlgn);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_current_location));

        currentLocationMarker = map.addMarker(markerOptions);
        // Moving camera to current location
        moveCamera(latlgn);

        markersHandler(latlgn);



        if(client == null){
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }


    }

    private void markersHandler(LatLng currentLocation) {
        Log.d(TAG, "markersHandler: starting... " + currentLocation.latitude);

        //this.setMarkers = false;

        boolean addMarkers;

        int distance = calculationByDistance(currentLocation.latitude, currentLocation.longitude);
        this.setDistance(distance);

        if (distance >= 500 || this.setMarkers == true) {
            this.setMarkers = false;
            addMarkers = true;
        } else {
            addMarkers = false;
        }

        Log.d(TAG, "addMarkers: " + addMarkers);
        Log.d(TAG, "setMarkers: " + this.setMarkers);
        getNearbyPlacesMarkers(currentLocation, addMarkers);



    }

    private void getNearbyPlacesMarkers(LatLng currentLocation, boolean addMarkers) {

        Log.d(TAG, "getNearbyPlacesMarkers: distance is bigger than 50 meters or initial location is null. Updating markers... ");

        Log.d(TAG, "getNearbyPlacesMarkers: almost getting url.");
        //String url = getUrl(latitude, longitude, restaurant);
        Object[] DataTransfer = new Object[4];
        DataTransfer[0] = map;
        Log.d(TAG, "getNearbyPlacesMarkers map: " + map.toString());
        DataTransfer[1] = currentLocation.latitude;
        Log.d(TAG, "getNearbyPlacesMarkers: latitude " + currentLocation.latitude);
        DataTransfer[2] = currentLocation.longitude;
        DataTransfer[3] = addMarkers;
        Log.d(TAG, "getNearbyPlacesMarkers: addMarkers " + addMarkers);
        getNearbyPlacesData = getNearbyPlacesData.getInstance();
        Log.d(TAG, "getNearbyPlacesMarkers: instance " + getNearbyPlacesData);

        if (addMarkers == true) {
            getNearbyPlacesData.execute(DataTransfer);
        } else {
            getNearbyPlacesData.nearbyPlaceName(currentLocation);
        }



        //this.setMarkers = true;
        //else if (distance < 10 && distance >1) {
        //    Context context = getContext();
        //    UserJourney uj = new UserJourney(context);
        //    Log.d(TAG, "getNearbyPlacesMarkers: vibrate");
        //    uj.vibrate(distance);
        //}

    }

    public int calculationByDistance(double currentLatitude, double currentLongitude) {
        float[] distance = new float[2];

        LatLng initialLocation = this.getFirstLocation();
        Log.d(TAG, "calculationByDistance: initialLocation before " + initialLocation);
        Log.d(TAG, "calculationByDistance: current location " + currentLatitude);

        if (initialLocation == null) {
            LatLng firstLocation = new LatLng(currentLatitude, currentLongitude);
            Log.d(TAG, "calculationByDistance: firstLocation: " + firstLocation);
            this.setFirstLocation(firstLocation);
            initialLocation = this.getFirstLocation();
        }
        Log.d(TAG, "calculationByDistance: initialLocation after " + initialLocation);

        Location.distanceBetween( initialLocation.latitude, initialLocation.longitude,
                currentLatitude, currentLongitude, distance);



        int approximate_distance = (int) distance[0];

        Log.d(TAG, "Distance in meters: " + approximate_distance);

        return approximate_distance;
    }


    private void moveCamera(LatLng latlgn) {
        Log.d(TAG, "moving the camera to: lat " + latlgn.latitude + " and log " + latlgn.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlgn, DEFAULT_ZOOM));

    }




    public List<HashMap<String, String>> getNearbyPlaces() {
        return nearbyPlaces;
    }

    public void setNearbyPlaces(List<HashMap<String, String>> nearbyPlaces) {
        this.nearbyPlaces = nearbyPlaces;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public void setContext(Context context){
        this.context = context;
    }



}




