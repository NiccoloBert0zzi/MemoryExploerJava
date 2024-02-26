package com.example.mobile_memoryexplorer.ui.addMemory;

import static android.content.Context.MODE_PRIVATE;


import android.app.DatePickerDialog;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;

import com.example.mobile_memoryexplorer.MainActivity;
import com.example.mobile_memoryexplorer.MyMarker;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.databinding.FragmentAddMemoryBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddMemoryFragment extends Fragment {

  private FragmentAddMemoryBinding binding;
  private Uri imageURI;
  FirebaseDatabase database = FirebaseDatabase.getInstance();
  MySharedData mySharedData;
  String email;
  FirebaseStorage storage = FirebaseStorage.getInstance();
  Calendar calendar;
  Double lat, lon;
  Marker marker;
  Boolean isPublic = false;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {

    binding = FragmentAddMemoryBinding.inflate(inflater, container, false);
    View root = binding.getRoot();
    mySharedData = new MySharedData(this.getContext());
    email = MySharedData.getEmail();
    marker = new Marker(binding.map);
    //take uri from drawable image
    imageURI = getUriFromDrawable(R.drawable.lake);
    //setup Calendar picker
    setUpCalendar();

    try {
      lat = MainActivity.latitude;
      lon = MainActivity.longitude;
      loadMap(lat, lon);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    binding.addMemory.setOnClickListener(v -> {
      binding.progressBar.setVisibility(RelativeLayout.VISIBLE);
      getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
          WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
      DatabaseReference memoryRef = database.getReference("memories/");
      String id = memoryRef.push().getKey();
      StorageReference ref = storage.getReference("Images/" + email + "/" + "memoriesImage/" + id);
      DatabaseReference finalMemoryRef = memoryRef.child(id);
      ref.putFile(imageURI)
          .addOnSuccessListener(taskSnapshot -> storage.getReference("Images/" + email + "/" + "memoriesImage/" + id)
              .getDownloadUrl().addOnSuccessListener(uri -> {
                imageURI = uri;
                Memory mem = new Memory(id, email, binding.title.getText().toString(), binding.description.getText().toString(), binding.birthday.getText().toString(), lat.toString(), lon.toString(), imageURI.toString(), isPublic);
                finalMemoryRef.setValue(mem);
                Toast.makeText(this.getContext(), "Memory added", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
              }).addOnFailureListener(e ->{
                  Toast.makeText(this.getContext(), "Error to create memory", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
              }))
          .addOnFailureListener(e -> {
            Toast.makeText(this.getContext(), "Error to load image", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
          });
    });

    binding.memoryImage.setOnClickListener(v -> checkAndREquestForPermission());

    binding.isPublic.setOnCheckedChangeListener((buttonView, isChecked) ->
        isPublic = isChecked);
    return root;
  }

  private void setUpCalendar() {
    calendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
      calendar.set(Calendar.YEAR, year);
      calendar.set(Calendar.MONTH, monthOfYear);
      calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
      updateLabel();
    };

    binding.birthday.setOnClickListener(v -> new DatePickerDialog(this.getContext(), R.style.DatePicker, date, calendar
        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)).show());
  }

  private void updateLabel() {
    String myFormat = "dd-MM-yyyy";
    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ITALIAN);
    binding.birthday.setText(sdf.format(calendar.getTime()));
  }

  private void openGallery() {
    pickMedia.launch(new PickVisualMediaRequest.Builder()
        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
        .build());
  }

  private void checkAndREquestForPermission() {
    if (ContextCompat.checkSelfPermission(AddMemoryFragment.this.getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(AddMemoryFragment.this.getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
        Toast.makeText(this.getContext(), "Please accept for required permission", Toast.LENGTH_SHORT).show();
      } else {
        ActivityCompat.requestPermissions(AddMemoryFragment.this.getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
      }
    } else {
      openGallery();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void loadMap(Double lat, Double lon) throws IOException {
    Configuration.getInstance().load(this.getContext(), this.getContext().getSharedPreferences("osmdroid", MODE_PRIVATE));

    MapView mapView = binding.map;
    mapView.setTileSource(TileSourceFactory.MAPNIK);
    mapView.setClickable(true);
    mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
    mapView.setMultiTouchControls(true);
    //set center to current location
    GeoPoint currentLocation = new GeoPoint(lat, lon);
    IMapController mapController = mapView.getController();
    mapController.setZoom(10.0);
    mapController.setCenter(currentLocation);
    setMarker(mapView, currentLocation);
    mapView.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
      @Override
      public boolean singleTapConfirmedHelper(GeoPoint p) {
        try {
          binding.map.getOverlays().remove(marker);
          setMarker(binding.map, p);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        return true;
      }

      @Override
      public boolean longPressHelper(GeoPoint p) {
        Log.e("MapView", "long click");
        return false;
      }
    }));
  }

  private Address setLocation(Double lat, Double lon) throws IOException {
    Geocoder gcd = new Geocoder(this.getContext(), Locale.getDefault());
    List<Address> addresses;
    addresses = gcd.getFromLocation(lat, lon, 1);
    if (addresses.size() > 0) {
      return addresses.get(0);
    }
    return null;
  }

  private void setMarker(MapView mapView, GeoPoint currentLocation) throws IOException {
    //add marker to current/clicked location
    marker.setIcon(new BitmapDrawable(getResources(), new MyMarker(this.getContext()).getSmallMarker()));
    if (setLocation(currentLocation.getLatitude(), currentLocation.getLongitude()) != null) {
      marker.setTitle(setLocation(currentLocation.getLatitude(), currentLocation.getLongitude()).getCountryName());
      marker.setSnippet(setLocation(currentLocation.getLatitude(), currentLocation.getLongitude()).getLocality());
    }
    marker.setPosition(currentLocation);
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    mapView.getOverlays().add(marker);
    lat = currentLocation.getLatitude();
    lon = currentLocation.getLongitude();
  }

  private Uri getUriFromDrawable(int drawableId) {
    Resources resources = getResources();
    return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
        "://" + resources.getResourcePackageName(drawableId)
        + '/' + resources.getResourceTypeName(drawableId)
        + '/' + resources.getResourceEntryName(drawableId));
  }

  ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
      registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {

        if (uri != null) {
          Log.d("PhotoPicker", "Selected URI: " + uri);
          imageURI = uri;
          binding.memoryImage.setImageURI(uri);
        } else {
          Log.d("PhotoPicker", "No media selected");
        }
      });
}
