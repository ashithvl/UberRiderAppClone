package com.forzo.uberriderappclone;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final int MY_PERMISSION_REQUEST_CODE = 7000;
    public static final int PLAY_SERVICE_REQUEST_CODE = 7000;
    public static final int UPDATE_INTERVAL = 5000;
    public static final int FASTEST_INTERVAL = 3000;
    public static final int DISPLACEMENT = 10;
    private static final String TAG = "WelcomeActivity";
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Location mLastLocation;
    private DatabaseReference ref;
    private GeoFire geoFire;
    private Marker mUserMarker;
    private boolean isMarkerRotating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ref = FirebaseDatabase.getInstance().getReference("Driver");
        geoFire = new GeoFire(ref);

        setupLocation();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }

    @Override
    public void onLocationChanged(Location location) {
        this.mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this::onLocationChanged);

        } else {
            // request permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
            }
        }

    }

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this::onLocationChanged);
        } else {
            // request permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        } else {
            // request permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setSmallestDisplacement(DISPLACEMENT)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int requestCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (requestCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(requestCode)) {
                GooglePlayServicesUtil.getErrorDialog(requestCode, this, PLAY_SERVICE_REQUEST_CODE).show();
            } else {
                Toast.makeText(this, R.string.this_device_is_not_supported, Toast.LENGTH_LONG).show();
                finish();
            }
        }
        return true;
    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (mLastLocation != null) {
                final double latitude = mLastLocation.getLatitude();
                final double longitude = mLastLocation.getLongitude();

                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        new GeoLocation(latitude, longitude), (key, error) -> {
                            if (mUserMarker != null) {
                                mUserMarker.remove();
                            }
                            mUserMarker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude, longitude))
                                    .title(getString(R.string.your_location)));

                            //animate camera
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),
                                    15.0f));
                            //draw animation rotate marker
                            rotateMarker(mUserMarker, -360, mMap);

                        });
            } else {
                Toast.makeText(this, "Cannot get your Location", Toast.LENGTH_LONG).show();
            }
        } else {
            // request permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
            }
        }

    }

    private void rotateMarker(Marker current, float toRotation, GoogleMap mMap) {
        isMarkerRotating = false;
        if (!isMarkerRotating) {
            final Handler handler = new Handler();
            long start = SystemClock.uptimeMillis();
            final float startRotation = current.getRotation();
            final long duration = 1000;

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    isMarkerRotating = true;
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);
                    float r = t * toRotation + (1 - t) * startRotation;
                    current.setRotation(-r > 180 ? r / 2 : r);
                    if (t < 1.0) {
                        handler.postDelayed(this, 16);
                    } else {
                        isMarkerRotating = false;
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted do nothing and carry on
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                            displayLocation();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "This app requires mLastLocation permissions to be granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


}
