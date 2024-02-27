package com.example.mobile_memoryexplorer;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mobile_memoryexplorer.databinding.ActivityMainBinding;
import com.example.mobile_memoryexplorer.ui.SingleMemory.SingleMemory;
import com.example.mobile_memoryexplorer.ui.addMemory.Memory;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private LocationRequest locationRequest;
  public static double latitude, longitude;
  String email;
  private DatabaseReference dbRef;
  MySharedData mySharedData;
  private final List<Memory> list = new ArrayList<>();
  Runnable runnable;
  private List<String> cacheNotifications;

  @SuppressLint("MissingPermission")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    com.example.mobile_memoryexplorer.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_layout);
    AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
        R.id.navigation_home, R.id.navigation_addmemory, R.id.navigation_profile, R.id.navigation_statistics)
        .build();
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    NavigationUI.setupWithNavController(binding.navView, navController);

    cacheNotifications = new ArrayList<>();
    mySharedData = new MySharedData(this);
    email = MySharedData.getEmail();
    dbRef = FirebaseDatabase.getInstance().getReference("memories");

    locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(3000)
        .setMaxUpdateDelayMillis(100)
        .build();
    getCurrentLocation();

    final Handler handler = new Handler();
    runnable = new Runnable() {
      public void run() {
        //get position every 5 seconds
        handler.postDelayed(this, 5000);
        float[] results = new float[1];
        getCurrentLocation();
        if (!list.isEmpty()) {
          for (Memory m : list) {
            Location.distanceBetween(latitude, longitude, Double.parseDouble(m.getLatitude()), Double.parseDouble(m.getLongitude()), results);
            //distanza minore di 5km
            if (results[0] < 5000) {
              System.out.println("Sei a " + results[0] + " da " + m.getTitle());
              if (!cacheNotifications.contains(m.getId())) {
                cacheNotifications.add(m.getId());
                sendNotification(m.getTitle(), m.getId());
              }
            }
          }
        }
      }
    };
    prepareItemData();
  }

  private void sendNotification(String title, String id) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notification")
        .setSmallIcon(R.drawable.logo)
        .setContentTitle("Sei vicino a " + title)
        .setContentText("Torna a visitarlo!")
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    Intent singleMemory = new Intent(this, SingleMemory.class);
    Bundle b = new Bundle();
    b.putString("id", id); //Your id
    singleMemory.putExtras(b);

    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, singleMemory, PendingIntent.FLAG_MUTABLE);
    builder.setContentIntent(pendingIntent);
    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      NotificationChannel channel = notificationManager.getNotificationChannel("notification");
      if (channel == null) {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        channel = new NotificationChannel("notification", "DESCRIZIONE", importance);
        channel.setLightColor(Color.GREEN);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);
      }
    }
    notificationManager.notify(0, builder.build());
  }

  public void prepareItemData() {
    dbRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        list.clear();
        for (DataSnapshot memorySnapshot : snapshot.getChildren()) {
          Memory m = memorySnapshot.getValue(Memory.class);
          if (m.getCreator().equals(email)) {
            list.add(m);
          }
        }
        runnable.run();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        // calling on cancelled method when we receive
        // any error or we are not able to get the data.
        Toast.makeText(MainActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
      }
    });

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    //TODO controllare che sia il permesso del gps entra anche con quello delle notifiche

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