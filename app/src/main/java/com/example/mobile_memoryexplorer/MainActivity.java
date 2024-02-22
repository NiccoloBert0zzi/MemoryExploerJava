package com.example.mobile_memoryexplorer;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mobile_memoryexplorer.databinding.ActivityMainBinding;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
  private LocationRequest locationRequest;
  double latitude,longitude;
  @SuppressLint("MissingPermission")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    com.example.mobile_memoryexplorer.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_layout);
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
        R.id.navigation_home, R.id.navigation_addmemory, R.id.navigation_profile, R.id.navigation_statistics)
        .build();
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    NavigationUI.setupWithNavController(binding.navView, navController);

    locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(3000)
        .setMaxUpdateDelayMillis(100)
        .build();
    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {
      public void run() {
        //get position evry 5 seconds
        getCurrentLocation();
        handler.postDelayed(this, 5000);
        float[] results = new float[2];
        Location.distanceBetween(latitude, longitude, 44.0491517, 12.5673628, results);
        //distanza minore di 5km
        if(results[0]<5000){
          System.out.println("Distanza minore di 5km" + results[0]);
        }
      }
    };runnable.run();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 1) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        if (isGPSEnabled()) {

          getCurrentLocation();

        } else {
          turnOnGPS();
        }
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 2) {
      if (resultCode == Activity.RESULT_OK) {

        getCurrentLocation();
      }
    }
  }

  private void getCurrentLocation() {
    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

      if (isGPSEnabled()) {

        LocationServices.getFusedLocationProviderClient(MainActivity.this)
            .requestLocationUpdates(locationRequest, new LocationCallback() {
              @Override
              public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LocationServices.getFusedLocationProviderClient(MainActivity.this)
                    .removeLocationUpdates(this);

                if (locationResult.getLocations().size() > 0) {

                  int index = locationResult.getLocations().size() - 1;
                  latitude = locationResult.getLocations().get(index).getLatitude();
                  longitude = locationResult.getLocations().get(index).getLongitude();
                }
              }
            }, Looper.getMainLooper());

      } else {
        turnOnGPS();
      }

    } else {
      requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }
  }

  private void turnOnGPS() {
    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest);
    builder.setAlwaysShow(true);

    Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
        .checkLocationSettings(builder.build());

    result.addOnCompleteListener(task -> {
    });

  }

  private boolean isGPSEnabled() {
    LocationManager locationManager;
    boolean isEnabled;

    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    return isEnabled;

  }

}