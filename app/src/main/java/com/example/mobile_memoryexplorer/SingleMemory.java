package com.example.mobile_memoryexplorer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mobile_memoryexplorer.databinding.ActivitySingleMemoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SingleMemory extends AppCompatActivity {
  private DatabaseReference dbRef;
  private ActivitySingleMemoryBinding binding;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivitySingleMemoryBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_layout);

    Bundle b = getIntent().getExtras();
    String id = b.getString("id");
    dbRef = FirebaseDatabase.getInstance().getReference("memories/"+id);
    loadInfo();
  }

  private void loadInfo() {
    dbRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Memory m = snapshot.getValue(Memory.class);
        if (m != null) {
          binding.title.setText(m.getTitle());
          binding.description.setText(m.getDescription());
          Calendar calendar = Calendar.getInstance();
          try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN);
            Date date = sdf.parse(m.getDate());
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            binding.creatorDate.setText(m.getCreator()+"-"+String.valueOf(year));
          } catch (ParseException e) {
            throw new RuntimeException(e);
          }
          Glide.with(binding.getRoot().getContext())
              .load(Uri.parse(m.getImage()))
              .into(binding.memoryImage);
        }
      }
      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Toast.makeText(SingleMemory.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }
}