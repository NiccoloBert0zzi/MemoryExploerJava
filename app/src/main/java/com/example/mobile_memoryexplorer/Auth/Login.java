package com.example.mobile_memoryexplorer.Auth;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.mobile_memoryexplorer.MainActivity;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
public class Login extends AppCompatActivity {

  private FirebaseAuth auth;
  private ActivityLoginBinding binding;
  MySharedData mySharedData;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityLoginBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    mySharedData = new MySharedData(this);
    if(mySharedData.getRemember().equals("true")){
      Intent homePage= new Intent(this, MainActivity.class);
      homePage.setFlags(homePage.getFlags()| Intent.FLAG_ACTIVITY_NO_HISTORY);
      startActivity(homePage);
    }
    auth = FirebaseAuth.getInstance();
    binding.login.setOnClickListener(v -> {
      String email = binding.email.getText().toString();
      String password = binding.passsword.getText().toString();
      auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
          // Sign in success, update UI with the signed-in user's information
          mySharedData.setSharedpreferences("email", email);
          Intent homePage= new Intent(this, MainActivity.class);
          homePage.setFlags(homePage.getFlags()| Intent.FLAG_ACTIVITY_NO_HISTORY);
          startActivity(homePage);
        } else {
          // If sign in fails, display a message to the user.
          Log.w(TAG, "signInWithEmail:failure", task.getException());
          Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
      });
    });

    binding.register.setOnClickListener(v -> startActivity(new Intent(this, Register.class)));
    binding.rememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (isChecked) {
        mySharedData.setSharedpreferences("remember", "true");
      } else {
        mySharedData.setSharedpreferences("remember", "false");
      }
    });

  }
}