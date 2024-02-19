package com.example.mobile_memoryexplorer.Auth;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.mobile_memoryexplorer.MainActivity;
import com.example.mobile_memoryexplorer.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class Register extends AppCompatActivity {
  private FirebaseAuth auth;
  private ActivityRegisterBinding binding;
  private Uri imageURI;
  StorageReference storageReference;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityRegisterBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    auth = FirebaseAuth.getInstance();
    storageReference = FirebaseStorage.getInstance().getReference();
    binding.register.setOnClickListener(v -> {
      //load image on storage
      uploadImage(imageURI);
      String email = binding.email.getText().toString();
      String password = binding.passsword.getText().toString();
      auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
          // Sign in success, update UI
          Log.d(TAG, "createUserWithEmail:success");
          UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
              .setDisplayName(binding.name.getText().toString())
              .setPhotoUri(imageURI)
              .build();
          FirebaseUser user = auth.getCurrentUser();
          user.updateProfile(profileUpdates);
          Intent homePage = new Intent(this, MainActivity.class);
          homePage.putExtra("user", user);
          startActivity(homePage);
        } else {
          // If sign in fails, display a message to the user.
          Log.w(TAG, "createUserWithEmail:failure", task.getException());
          Toast.makeText(this, "Authentication failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
        }
      });
    });

    binding.login.setOnClickListener(v -> startActivity(new Intent(this, Login.class)));


    binding.profileImage.setOnClickListener(v -> {
      Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      someActivityResultLauncher.launch(intent);
    });
  }

  ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
          if (result.getResultCode() == Activity.RESULT_OK) {
            imageURI = result.getData().getData();
            binding.profileImage.setImageURI(result.getData().getData());
          }
        }
      });

  private void uploadImage(Uri file) {
    StorageReference ref = storageReference.child("Images/" + binding.email.getText().toString() + "/" + "profileImage");
    ref.putFile(file).addOnSuccessListener(taskSnapshot -> Toast.makeText(Register.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(Register.this, "Failed!" + e.getMessage(), Toast.LENGTH_SHORT).show());
  }

}
