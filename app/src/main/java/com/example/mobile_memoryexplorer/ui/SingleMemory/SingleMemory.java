package com.example.mobile_memoryexplorer.ui.SingleMemory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mobile_memoryexplorer.Database.AppDatabase;
import com.example.mobile_memoryexplorer.Database.Favourite;
import com.example.mobile_memoryexplorer.MyMarker;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.databinding.ActivitySingleMemoryBinding;
import com.example.mobile_memoryexplorer.ui.addMemory.Memory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class SingleMemory extends AppCompatActivity {
  private DatabaseReference dbRef;
  private ActivitySingleMemoryBinding binding;
  String email;
  MySharedData mySharedData;
  Memory m;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivitySingleMemoryBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_layout);

    mySharedData = new MySharedData(this);
    email = MySharedData.getEmail();

    Bundle b = getIntent().getExtras();
    String id = b.getString("id");
    dbRef = FirebaseDatabase.getInstance().getReference("memories/" + id);
    loadInfo();

    binding.favorite.setOnClickListener(v -> {
      AppDatabase appDb = AppDatabase.getInstance(this);
      Favourite favourite = new Favourite(email, binding.favorite.getTag().toString());
      // check if the memory is already in the favourite list
      if (appDb.favouriteUserMemoryDao().checkMemories(email, binding.favorite.getTag().toString()) != null) {
        appDb.favouriteUserMemoryDao().deleteTask(favourite);
        Toast.makeText(this, m.getTitle() + " eliminato dai preferiti!", Toast.LENGTH_SHORT).show();
      } else {
        appDb.favouriteUserMemoryDao().insert(favourite);
        Toast.makeText(this, m.getTitle() + " aggiunto ai preferiti!", Toast.LENGTH_SHORT).show();
      }
    });

  }

  private void loadInfo() {
    dbRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        m = snapshot.getValue(Memory.class);
        if (m != null) {
          binding.title.setText(m.getTitle());
          binding.description.setText(m.getDescription());
          binding.favorite.setTag(m.getId());
          if (m.getCreator().equals(email)) {
            binding.favorite.setVisibility(View.GONE);
          }
          String placeholder = m.getDate();
          binding.creatorDate.setText(placeholder);
          Glide.with(binding.getRoot().getContext())
              .load(Uri.parse(m.getImage()))
              .into(binding.memoryImage);

        }
        try {
          openMap(Double.parseDouble(m.getLatitude()), Double.parseDouble(m.getLongitude()));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Toast.makeText(SingleMemory.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void openMap(Double lat, Double lon) throws IOException {
    Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));

    MapView mapView = binding.map;
    mapView.setTileSource(TileSourceFactory.MAPNIK);
    mapView.setClickable(true);
    mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
    mapView.setMultiTouchControls(true);
    GeoPoint currentLocation = new GeoPoint(lat, lon);
    IMapController mapController = mapView.getController();
    mapController.setZoom(10.0);
    mapController.setCenter(currentLocation);

    Marker startMarker = new Marker(mapView);
    startMarker.setIcon(new BitmapDrawable(getResources(), new MyMarker(this).getSmallMarker()));
    if(setLocation(lat, lon) != null) {
      startMarker.setTitle(setLocation(lat, lon).getCountryName());
      startMarker.setSnippet(setLocation(lat, lon).getLocality());
    }
    startMarker.setPosition(currentLocation);
    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    mapView.getOverlays().add(startMarker);
  }
  private Address setLocation(Double lat, Double lon) throws IOException {
    Geocoder gcd = new Geocoder(this, Locale.getDefault());
    List<Address> addresses;
    addresses = gcd.getFromLocation(lat, lon, 1);
    if (addresses.size() > 0) {
      return addresses.get(0);
    }
    return null;
  }

}