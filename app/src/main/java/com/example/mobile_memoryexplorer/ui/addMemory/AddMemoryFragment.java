package com.example.mobile_memoryexplorer.ui.addMemory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;

import com.example.mobile_memoryexplorer.MainActivity;
import com.example.mobile_memoryexplorer.Memory;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.databinding.FragmentAddMemoryBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    //TODO dialog on loading
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
                Memory mem = new Memory(id, email, binding.title.getText().toString(), binding.description.getText().toString(), sdf.format(new Date()), "","", imageURI.toString());
                finalMemoryRef.setValue(mem);
                Toast.makeText(getContext(), "Memory added", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
              }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error to create memory", Toast.LENGTH_SHORT).show()))
          .addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error to load image", Toast.LENGTH_SHORT).show();
            System.out.println(e.getMessage());
          });
    });

    binding.memoryImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
        .build()));

    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
      registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
          Log.d("PhotoPicker", "Selected URI: " + uri);
          imageURI = uri;
          binding.memoryImage.setImageURI(uri);
        } else {
          Log.d("PhotoPicker", "No media selected");
        }
      });
}
