package com.example.mobile_memoryexplorer.Auth;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

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


public class Register extends AppCompatActivity {
  private FirebaseAuth auth;
  private ActivityRegisterBinding binding;
  private Uri imageURI;
  FirebaseStorage storage = FirebaseStorage.getInstance();
  MySharedData mySharedData;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityRegisterBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_layout);
    mySharedData = new MySharedData(this);

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

}
