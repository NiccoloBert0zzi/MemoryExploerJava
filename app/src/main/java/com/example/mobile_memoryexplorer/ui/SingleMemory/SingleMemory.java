package com.example.mobile_memoryexplorer.ui.SingleMemory;

import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Objects;

public class SingleMemory extends AppCompatActivity {
  private DatabaseReference dbRef;
  private ActivitySingleMemoryBinding binding;
  String email;
  MySharedData mySharedData;
  Memory m;
  AppDatabase appDb;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivitySingleMemoryBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setupActionBar();

    appDb = AppDatabase.getInstance(this);
    mySharedData = new MySharedData(this);
    email = MySharedData.getEmail();

    Bundle b = getIntent().getExtras();
    if (b != null) {
      String id = b.getString("id");
      dbRef = FirebaseDatabase.getInstance().getReference("memories/" + id);
      loadInfo();

      binding.favorite.setOnClickListener(v -> toggleFavorite());
    } else {
      Toast.makeText(this, "Memory ID not provided", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private void setupFavourite() {
    if (appDb.favouriteUserMemoryDao().checkMemories(email, binding.favorite.getTag().toString()) != null) {
      binding.favorite.setImageResource(R.drawable.ic_baseline_favorite_full);
    } else {
      binding.favorite.setImageResource(R.drawable.ic_baseline_favorite_empty);
    }
  }

  private void setupActionBar() {
    Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_layout);
  }

  private void loadInfo() {
    dbRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (binding == null) return;
        m = snapshot.getValue(Memory.class);
        if (m != null) {
          updateUI();
        }
        try {
          assert m != null;
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

  private void updateUI() {
    binding.title.setText(m.getTitle());
    binding.description.setText(m.getDescription());
    binding.favorite.setTag(m.getId());
    if (m.getCreator().equals(email)) {
      binding.favorite.setVisibility(View.GONE);
    }
    setupFavourite();
    binding.creatorDate.setText(m.getDate());
    Glide.with(binding.getRoot().getContext())
        .load(Uri.parse(m.getImage()))
        .into(binding.memoryImage);
  }

  private void toggleFavorite() {
    Favourite favourite = new Favourite(email, binding.favorite.getTag().toString());
    // check if the memory is already in the favourite list
    if (appDb.favouriteUserMemoryDao().checkMemories(email, binding.favorite.getTag().toString()) != null) {
      appDb.favouriteUserMemoryDao().deleteTask(favourite);
      binding.favorite.setImageResource(R.drawable.ic_baseline_favorite_empty);
      showToast(m.getTitle() + " eliminato dai preferiti!");
    } else {
      binding.favorite.setImageResource(R.drawable.ic_baseline_favorite_full);
      appDb.favouriteUserMemoryDao().insert(favourite);
      showToast(m.getTitle() + " aggiunto ai preferiti!");
    }
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
    if (setLocation(lat, lon) != null) {
      startMarker.setTitle(Objects.requireNonNull(setLocation(lat, lon)).getCountryName());
      startMarker.setSnippet(Objects.requireNonNull(setLocation(lat, lon)).getLocality());
    }
    startMarker.setPosition(currentLocation);
    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    mapView.getOverlays().add(startMarker);
  }

  private Address setLocation(Double lat, Double lon) throws IOException {
    Geocoder gcd = new Geocoder(this, Locale.getDefault());
    List<Address> addresses;
    addresses = gcd.getFromLocation(lat, lon, 1);
    assert addresses != null;
    if (addresses.size() > 0) {
      return addresses.get(0);
    }
    return null;
  }

  private void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
}
