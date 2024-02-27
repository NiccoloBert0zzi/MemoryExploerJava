package com.example.mobile_memoryexplorer.Auth;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.mobile_memoryexplorer.Database.AppDatabase;
import com.example.mobile_memoryexplorer.Database.Profile;
import com.example.mobile_memoryexplorer.MainActivity;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
  private static final int REQUEST_CODE_NOTIFICATION = 101010;

  private FirebaseAuth auth;
  private ActivityLoginBinding binding;
  MySharedData mySharedData;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityLoginBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_layout);

    mySharedData = new MySharedData(this);
    if (MySharedData.getThemePreferences()) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
    if (MySharedData.getRemember().equals("true")) {
      Intent homePage = new Intent(this, MainActivity.class);
      startActivity(homePage);
    }
    auth = FirebaseAuth.getInstance();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
      }
    }

    binding.login.setOnClickListener(v ->
    {
      binding.progressBar.setVisibility(RelativeLayout.VISIBLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
          WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

      String email = binding.email.getText().toString();
      String password = binding.passsword.getText().toString();
      auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
        if (task.isSuccessful()) {

          AppDatabase appDb = AppDatabase.getInstance(Login.this);
          if (appDb.profileDao().getProfile(email) == null) {
            Profile profile = new Profile(email, task.getResult().getUser().getDisplayName(), task.getResult().getUser().getPhotoUrl().toString());
            appDb.profileDao().insert(profile);
          }
          // Sign in success, update UI with the signed-in user's information
          mySharedData.setSharedpreferences("email", email);
          binding.progressBar.setVisibility(View.GONE);
          getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
          Intent homePage = new Intent(this, MainActivity.class);
          homePage.setFlags(homePage.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
          startActivity(homePage);
        } else {
          // If sign in fails, display a message to the user.
          Log.w(TAG, "signInWithEmail:failure", task.getException());
          binding.progressBar.setVisibility(View.GONE);
          getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
          Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
      });
    });

    binding.register.setOnClickListener(v ->
        startActivity(new Intent(this, Register.class)));
    binding.rememberMe.setOnCheckedChangeListener((buttonView, isChecked) ->
    {
      if (isChecked) {
        mySharedData.setSharedpreferences("remember", "true");
      } else {
        mySharedData.setSharedpreferences("remember", "false");
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (MySharedData.getRemember().equals("true")) {
      Intent homePage = new Intent(this, MainActivity.class);
      startActivity(homePage);
    }
  }
}