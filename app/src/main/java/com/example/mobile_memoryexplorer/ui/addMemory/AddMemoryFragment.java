package com.example.mobile_memoryexplorer.ui.addMemory;

import android.app.DatePickerDialog;

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
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;

import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.databinding.FragmentAddMemoryBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddMemoryFragment extends Fragment {

  private FragmentAddMemoryBinding binding;
  private Uri imageURI;
  FirebaseDatabase database = FirebaseDatabase.getInstance();
  MySharedData mySharedData;
  String email;
  FirebaseStorage storage = FirebaseStorage.getInstance();
  Calendar calendar;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {

    binding = FragmentAddMemoryBinding.inflate(inflater, container, false);
    View root = binding.getRoot();
    mySharedData = new MySharedData(getContext());
    email = MySharedData.getEmail();
    calendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
      calendar.set(Calendar.YEAR, year);
      calendar.set(Calendar.MONTH, monthOfYear);
      calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
      updateLabel();
    };
    binding.birthday.setOnClickListener(v -> new DatePickerDialog(getContext(),R.style.DatePicker, date, calendar
        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)).show());

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
                //todo add location
                Memory mem = new Memory(id, email, binding.title.getText().toString(), binding.description.getText().toString(), binding.birthday.getText().toString(), "", "", imageURI.toString());
                finalMemoryRef.setValue(mem);
                Toast.makeText(getContext(), "Memory added", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
