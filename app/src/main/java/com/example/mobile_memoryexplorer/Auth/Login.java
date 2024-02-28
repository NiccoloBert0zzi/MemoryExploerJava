package com.example.mobile_memoryexplorer.Auth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.biometric.BiometricPrompt;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.concurrent.Executor;

public class Login extends AppCompatActivity {
  private static final int REQUEST_CODE_NOTIFICATION = 101010;

  private FirebaseAuth auth;
  private ActivityLoginBinding binding;
  private MySharedData mySharedData;
  private BiometricPrompt biometricPrompt;
  private BiometricPrompt.PromptInfo promptInfo;

  @RequiresApi(api = Build.VERSION_CODES.R)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityLoginBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setupActionBar();

    mySharedData = new MySharedData(this);
    setupTheme();

    auth = FirebaseAuth.getInstance();
    requestNotificationPermission();
    setupTextWatcher();
    setupFingerprintListener();
    setupLoginButton();
    setupRegisterButton();
    setupRememberMeCheckbox();
  }

  private void setupActionBar() {
    Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_layout);
  }

  private void setupTheme() {
    if (MySharedData.getThemePreferences()) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
  }

  private void requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
      }
    }
  }

  private void setupTextWatcher() {
    binding.email.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        binding.fingerprint.setVisibility(isEmailValid(s) ? View.VISIBLE : View.GONE);
      }
    });
  }

  @RequiresApi(api = Build.VERSION_CODES.R)
  private void setupFingerprintListener() {
    binding.fingerprint.setOnClickListener(v -> {
      setupBiometricPrompt();
      biometricPrompt.authenticate(promptInfo);
    });
  }

  private void setupLoginButton() {
    binding.login.setOnClickListener(v -> {
      String email = Objects.requireNonNull(binding.email.getText()).toString();
      String password = Objects.requireNonNull(binding.passsword.getText()).toString();
      if (email.isEmpty() || password.isEmpty()) {
        Toast.makeText(this, "Inserisci email e password", Toast.LENGTH_SHORT).show();
      } else {
        binding.progressBar.setVisibility(RelativeLayout.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> handleAuthenticationResult(task, email));
      }
    });
  }

  private void setupRegisterButton() {
    binding.register.setOnClickListener(v -> startActivity(new Intent(this, Register.class)));
  }

  private void setupRememberMeCheckbox() {
    binding.rememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> mySharedData.setSharedpreferences("remember", String.valueOf(isChecked)));
  }

  @RequiresApi(api = Build.VERSION_CODES.R)
  private void setupBiometricPrompt() {
    Executor executor = ContextCompat.getMainExecutor(this);
    biometricPrompt = new BiometricPrompt(Login.this, executor, new BiometricPrompt.AuthenticationCallback() {
      @Override
      public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        fingerprintLogIn(Objects.requireNonNull(binding.email.getText()).toString());
      }
    });

    promptInfo = new BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric login for my app")
        .setSubtitle("Log in using your biometric credential")
        .setNegativeButtonText("Use account password")
        .build();
  }

  private void handleAuthenticationResult(@NonNull Task<AuthResult> task, String email) {
    if (task.isSuccessful()) {
      AppDatabase appDb = AppDatabase.getInstance(Login.this);
      if (appDb.profileDao().getProfile(email) == null) {
        Profile profile = new Profile(email, Objects.requireNonNull(task.getResult().getUser()).getDisplayName(), Objects.requireNonNull(task.getResult().getUser().getPhotoUrl()).toString());
        appDb.profileDao().insert(profile);
      }
      startHome(email);
    } else {
      handleAuthenticationFailure();
    }
  }

  private void handleAuthenticationFailure() {
    binding.progressBar.setVisibility(View.GONE);
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
  }

  private void startHome(String email) {
    mySharedData.setSharedpreferences("email", email);
    binding.progressBar.setVisibility(View.GONE);
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    Intent homePage = new Intent(this, MainActivity.class);
    homePage.setFlags(homePage.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
    startActivity(homePage);
  }

  private void fingerprintLogIn(String email) {
    binding.progressBar.setVisibility(RelativeLayout.VISIBLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    AppDatabase appDb = AppDatabase.getInstance(Login.this);
    if (appDb.profileDao().getProfile(email) != null) {
      startHome(email);
    } else {
      Toast.makeText(this, "Devi accedere almeno una volta con la tua mail!", Toast.LENGTH_SHORT).show();
      binding.progressBar.setVisibility(View.GONE);
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (MySharedData.getRemember().equals("true")) {
      Intent homePage = new Intent(this, MainActivity.class);
      startActivity(homePage);
    }
  }

  boolean isEmailValid(CharSequence email) {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
  }
}
