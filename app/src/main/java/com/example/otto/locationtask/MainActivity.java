package com.example.otto.locationtask;

import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient gac;
    Location loc;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    private static final int REQUEST_CHECK_SETTINGS=61124;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "";
    int MY_PERMISSION_ACCESS_COURSE_LOCATION=00;
    boolean mRequestingLocationUpdates;



    TextView mLatitude;
    TextView mLongitude;

    protected void createLocationRequest() {
        //LocationRequest
        Log.d("location request", "Normally created");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


       /* updateValuesFromBundle(savedInstanceState);*/

        gac = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLatitude = (TextView) findViewById(R.id.latitude);
        mLongitude = (TextView) findViewById(R.id.longitude);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d("success", "yes?");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("success", "nope? " + e);
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

        Log.d("location callback", "normally created");
        mLocationCallback = new LocationCallback() {
          public void onLocationResult(LocationResult locationResult) {
              Log.d("Location result", locationResult.toString());
              for (Location location : locationResult.getLocations()) {
                  
                  Double lat = location.getLatitude();
                  Double lon = location.getLongitude();
                  String la = "HERE????" + lat.toString();
                  String lo = lon.toString();
                  mLatitude.setText(la);
                  mLongitude.setText(lo);
              }
          }
        };

    }

    protected void onStart() {
        gac.connect();
        super.onStart();
        createLocationRequest();
    }

    protected void onResume() {
        super.onResume();
        Log.d("resume", "test " + mRequestingLocationUpdates);
        //if(mRequestingLocationUpdates) {
            startLocationUpdates();
        //}

    }

    protected void onStop() {
        gac.disconnect();
        super.onStop();
    }

    protected void startLocationUpdates() {
        Log.d("loc update", "maybe?");
        //mLatitude.setText("UPDATE????");
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSION_ACCESS_COURSE_LOCATION );
        }
        try{
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null);
            Log.d("loc update", "normally yes?");
        }catch (Exception e){
            Log.d("loc update", "nope?" + e);
        }

    }



    public void onConnected(Bundle connectionHint) {
        Log.d("loc connected", "maybe?");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        loc = LocationServices.FusedLocationApi.getLastLocation(gac);
        Log.d("loc connected", "normally yes?" + loc);
        if (loc != null) {
            mLatitude.setText("Lat: " + loc.getLatitude());
            mLongitude.setText("Lon: " + loc.getLongitude());

        }
    }
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onConnectionSuspended(int connectionSuspended) {

    }


}
