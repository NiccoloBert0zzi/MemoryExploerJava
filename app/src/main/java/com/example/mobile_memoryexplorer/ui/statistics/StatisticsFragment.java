package com.example.mobile_memoryexplorer.ui.statistics;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mobile_memoryexplorer.Memory;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.databinding.FragmentStatisticsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

  private FragmentStatisticsBinding binding;
  private DatabaseReference dbRef;
  private final List<Memory> list = new ArrayList<>();
  String email;
  MySharedData mySharedData;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {

    binding = FragmentStatisticsBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    dbRef = FirebaseDatabase.getInstance().getReference("memories");
    mySharedData = new MySharedData(getContext());
    email = MySharedData.getEmail();
    prepareItemData();
    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  public void prepareItemData() {
    dbRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        list.clear();
        for (DataSnapshot memorySnapshot : snapshot.getChildren()) {
          Memory m = memorySnapshot.getValue(Memory.class);
          if (m.getCreator().equals(email))
            list.add(m);
        }
        if (list.isEmpty()) {
          Toast.makeText(getContext(), "No memories found", Toast.LENGTH_SHORT).show();
        } else {
          Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
          List<Address> addresses;
          try {
            addresses = gcd.getFromLocation(Double.parseDouble(list.get(1).getLatitude()), Double.parseDouble(list.get(1).getLongitude()), 1);
            if (addresses.size() > 0) {
              binding.textNotifications.setText(addresses.get(0).getLocality());
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        // calling on cancelled method when we receive
        // any error or we are not able to get the data.
        Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
      }
    });

  }
}