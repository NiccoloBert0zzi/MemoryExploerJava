package com.example.mobile_memoryexplorer.ui.addMemory;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;

import com.example.mobile_memoryexplorer.MainActivity;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.databinding.FragmentAddMemoryBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddMemoryFragment extends Fragment {

  private FragmentAddMemoryBinding binding;
  private Uri imageURI;
  FirebaseDatabase database = FirebaseDatabase.getInstance();
  MySharedData mySharedData;
  String email;
  FirebaseStorage storage = FirebaseStorage.getInstance();

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {

    binding = FragmentAddMemoryBinding.inflate(inflater, container, false);
    View root = binding.getRoot();
    mySharedData = new MySharedData(getContext());
    email = MySharedData.getEmail();
    //todo defalut image
    binding.addMemory.setOnClickListener(v -> {
      binding.progressBar.setVisibility(RelativeLayout.VISIBLE);
      DatabaseReference memoryRef = database.getReference("memories/");
      String id = memoryRef.push().getKey();
      StorageReference ref = storage.getReference("Images/" + email + "/" + "memoriesImage/" + id);
      DatabaseReference finalMemoryRef = memoryRef.child(id);
      ref.putFile(imageURI)
          .addOnSuccessListener(taskSnapshot -> storage.getReference("Images/" + email + "/" + "memoriesImage/" + id)
              .getDownloadUrl().addOnSuccessListener(uri -> {
                imageURI = uri;
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN);
                //todo add location
                Memory mem = new Memory(id, email, binding.title.getText().toString(), binding.description.getText().toString(), sdf.format(new Date()), "", "", imageURI.toString());
                finalMemoryRef.setValue(mem);
                Toast.makeText(getContext(), "Memory added", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
              }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error to create memory", Toast.LENGTH_SHORT).show()))
          .addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error to load image", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
          });
    });

    binding.memoryImage.setOnClickListener(v -> checkAndREquestForPermission());
    return root;
  }

  private Uri createURi() {
    File imageFile = new File(getContext().getFilesDir(), "camera_photo.jpg");
    return FileProvider.getUriForFile(
        this.getContext(),
        "com.example.camerapermission.fileprovider",
        imageFile);
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
        Toast.makeText(getContext(), "Please accept for required permission", Toast.LENGTH_SHORT).show();
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
