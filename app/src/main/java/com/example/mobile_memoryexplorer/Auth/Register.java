package com.example.mobile_memoryexplorer.Auth;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mobile_memoryexplorer.Database.AppDatabase;
import com.example.mobile_memoryexplorer.MainActivity;
import com.example.mobile_memoryexplorer.Database.Profile;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;


public class Register extends AppCompatActivity {
  private FirebaseAuth auth;
  private ActivityRegisterBinding binding;
  private Uri imageURI;
  FirebaseStorage storage = FirebaseStorage.getInstance();
  MySharedData mySharedData;
  ActivityResultLauncher<Uri> takePicture;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityRegisterBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_layout);
    mySharedData = new MySharedData(this);

    imageURI = createUri();
    registerPictureProfile();

    auth = FirebaseAuth.getInstance();
    binding.register.setOnClickListener(v -> {

      binding.progressBar.setVisibility(RelativeLayout.VISIBLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
          WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

      String email = binding.email.getText().toString();
      String password = binding.passsword.getText().toString();
      //load image on storage
      StorageReference ref = storage.getReference("Images/" + email + "/" + "profileImage");
      ref.putFile(imageURI).addOnSuccessListener(taskSnapshot -> {
        Toast.makeText(Register.this, "Images added", Toast.LENGTH_SHORT).show();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            // Sign in success, update UI
            storage.getReference("Images/" + email + "/profileImage").getDownloadUrl().addOnSuccessListener(uri -> {
              imageURI = uri;
              //save in room
              AppDatabase appDb = AppDatabase.getInstance(Register.this);
              Profile profile = new Profile(email, binding.name.getText().toString(), imageURI.toString());
              appDb.profileDao().insert(profile);
              //save in firebase
              UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                  .setDisplayName(binding.name.getText().toString())
                  .setPhotoUri(imageURI)
                  .build();
              task.getResult().getUser().updateProfile(profileUpdates);
              //save in shared preferences
              mySharedData.setSharedpreferences("email", email);
              Log.d(TAG, "createUserWithEmail:success");
              //update UI
              binding.progressBar.setVisibility(View.GONE);
              getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
              Intent homePage = new Intent(Register.this, MainActivity.class);
              startActivity(homePage);
            });
          } else {
            binding.progressBar.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            // If sign in fails, display a message to the user.
            Log.w(TAG, "createUserWithEmail:failure", task.getException());
            Toast.makeText(Register.this, "Authentication failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
          }
        });
      }).addOnFailureListener(e -> {
        binding.progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        Toast.makeText(Register.this, "Images not added", Toast.LENGTH_SHORT).show();
      });
    });
    binding.login.setOnClickListener(v -> startActivity(new Intent(this, Login.class)));
    binding.profileImage.setOnClickListener(v -> checkCameraPermission());

  }

  private Uri createUri() {
    File file = new File(getFilesDir(), "profileImage.jpg");
    return FileProvider.getUriForFile(this, "com.example.mobile_memoryexplorer.fileprovider", file);
  }

  private void registerPictureProfile() {
    takePicture = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
      if (result) {
        imageURI = createUri();
        Glide.with(this)
            .load(imageURI)
            .into(binding.profileImage);
      }
    });
  }

  private void checkCameraPermission() {
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 101);
    } else {
      takePicture.launch(imageURI);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      takePicture.launch(imageURI);
    } else {
      Toast.makeText(this, "Permessi alla fotocamera negati!", Toast.LENGTH_SHORT).show();
    }
  }
}
