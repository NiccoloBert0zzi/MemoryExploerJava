package com.example.mobile_memoryexplorer.Auth;


import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.mobile_memoryexplorer.Database.AppDatabase;
import com.example.mobile_memoryexplorer.Database.Profile;
import com.example.mobile_memoryexplorer.MainActivity;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Objects;


public class Register extends AppCompatActivity {
  private static final int REQUEST_CAMERA_PERMISSION = 101;

  private FirebaseAuth auth;
  private ActivityRegisterBinding binding;
  private Uri imageURI;
  private final FirebaseStorage storage = FirebaseStorage.getInstance();
  private MySharedData mySharedData;
  private ActivityResultLauncher<Uri> takePicture;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityRegisterBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setupActionBar();

    mySharedData = new MySharedData(this);
    imageURI = createUri();
    registerPictureProfile();
    auth = FirebaseAuth.getInstance();

    imageURI= getUriFromDrawable(R.drawable.empty_user);
    binding.register.setOnClickListener(v -> {
      binding.progressBar.setVisibility(RelativeLayout.VISIBLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

      String email = Objects.requireNonNull(binding.email.getText()).toString();
      String password = Objects.requireNonNull(binding.passsword.getText()).toString();

      StorageReference ref = storage.getReference("Images/" + email + "/profileImage");
      ref.putFile(imageURI).addOnSuccessListener(taskSnapshot -> handleImageUploadSuccess(email, password)).addOnFailureListener(e -> handleImageUploadFailure());
    });

    binding.login.setOnClickListener(v -> startActivity(new Intent(this, Login.class)));
    binding.profileImage.setOnClickListener(v -> checkCameraPermission());
  }

  private void setupActionBar() {
    Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_layout);
  }

  private Uri createUri() {
    File file = new File(getFilesDir(), "profileImage.jpg");
    return FileProvider.getUriForFile(this, "com.example.mobile_memoryexplorer.fileprovider", file);
  }

  private void registerPictureProfile() {
    takePicture = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
      if (result) {
        imageURI = createUri();
        loadImageIntoProfile();
      }
    });
  }

  private void loadImageIntoProfile() {
    Glide.with(this)
        .load(imageURI)
        .into(binding.profileImage);
  }

  private void checkCameraPermission() {
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    } else {
      takePicture.launch(imageURI);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      takePicture.launch(imageURI);
    } else {
      Toast.makeText(this, "Permessi alla fotocamera negati!", Toast.LENGTH_SHORT).show();
    }
  }

  private void handleImageUploadSuccess(String email, String password) {
    Toast.makeText(Register.this, "Images added", Toast.LENGTH_SHORT).show();
    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        handleUserCreationSuccess(email);
      } else {
        handleUserCreationFailure(task);
      }
    });
  }

  private void handleImageUploadFailure() {
    binding.progressBar.setVisibility(View.GONE);
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    Toast.makeText(Register.this, "Images not added", Toast.LENGTH_SHORT).show();
  }

  private void handleUserCreationSuccess(String email) {
    storage.getReference("Images/" + email + "/profileImage").getDownloadUrl().addOnSuccessListener(uri -> {
      imageURI = uri;
      saveProfileData(email);
      updateUI();
    });
  }

  private void handleUserCreationFailure(@NonNull Task<AuthResult> task) {
    binding.progressBar.setVisibility(View.GONE);
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    Toast.makeText(Register.this, "Authentication failed. " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
  }

  private void saveProfileData(String email) {
    AppDatabase appDb = AppDatabase.getInstance(Register.this);
    Profile profile = new Profile(email, Objects.requireNonNull(binding.name.getText()).toString(), imageURI.toString());
    appDb.profileDao().insert(profile);

    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
        .setDisplayName(binding.name.getText().toString())
        .setPhotoUri(imageURI)
        .build();
    Objects.requireNonNull(auth.getCurrentUser()).updateProfile(profileUpdates);

    mySharedData.setSharedpreferences("email", email);
  }

  private void updateUI() {
    binding.progressBar.setVisibility(View.GONE);
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    Intent homePage = new Intent(Register.this, MainActivity.class);
    startActivity(homePage);
  }

  private Uri getUriFromDrawable(int drawableId) {
    Resources resources = getResources();
    return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
        "://" + resources.getResourcePackageName(drawableId)
        + '/' + resources.getResourceTypeName(drawableId)
        + '/' + resources.getResourceEntryName(drawableId));
  }
}
