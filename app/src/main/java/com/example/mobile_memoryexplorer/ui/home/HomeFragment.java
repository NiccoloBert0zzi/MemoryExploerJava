package com.example.mobile_memoryexplorer.ui.home;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.ResponsiveDimension;
import com.example.mobile_memoryexplorer.ui.addMemory.Memory;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

  private FragmentHomeBinding binding;
  private final List<Memory> list = new ArrayList<>();
  String email;
  private DatabaseReference dbRef;
  MySharedData mySharedData;
  Map<String, Integer> locations = new HashMap<>();
  List<String> filter = new ArrayList<>();
  ArrayAdapter<String> adapterItem;
  String filter_chosen = "Mondo";

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    if (this.getContext() != null) {
      //start progress bar
      mySharedData = new MySharedData(this.getContext());
      email = MySharedData.getEmail();
      dbRef = FirebaseDatabase.getInstance().getReference("memories");
      filter.clear();
      filter.add("Mondo");
      prepareItemData(this.getContext());

      binding.autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
        locations.clear();
        filter_chosen = parent.getItemAtPosition(position).toString();
        prepareItemData(this.getContext());
      });
    }
    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  public void prepareItemData(Context ctx) {
    dbRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (binding == null) return;
        binding.progressBar.setVisibility(RelativeLayout.VISIBLE);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        list.clear();
        for (DataSnapshot memorySnapshot : snapshot.getChildren()) {
          Memory m = memorySnapshot.getValue(Memory.class);
          if (!m.getCreator().equals(email) && m.isPublic()) {
            if (!filter_chosen.equals("Mondo")) {
              if (getLocation(m.getLatitude(), m.getLongitude()).equals(filter_chosen)) {
                list.add(m);
              }
            } else {
              list.add(m);
            }
          }
        }
        if (filter.size() == 1) downloadData();
        if (list.isEmpty()) {
          Toast.makeText(ctx, "No memories found", Toast.LENGTH_SHORT).show();
        } else {
          if (binding != null) {
            ResponsiveDimension responsiveDimension = new ResponsiveDimension(getActivity().getWindowManager());
            //set GridLayoutManager in recyclerView and show items in grid with two columns
            binding.recyclerView.setLayoutManager(new GridLayoutManager(ctx, responsiveDimension.getResponsiveCollum()));
            //set adapter ItemAdapter in recyclerView
            binding.recyclerView.setAdapter(new MemoriesListAdapter(list, ctx, email, false,responsiveDimension));
          }
        }
        assert binding != null;
        binding.progressBar.setVisibility(View.GONE);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        // calling on cancelled method when we receive
        // any error or we are not able to get the data.
        Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
      }
    });

  }

  private void downloadData() {
    for (Memory m : list) {
      String key = getLocation(m.getLatitude(), m.getLongitude());
      if (key == null)
        continue;
      if (!filter.contains(key)) {
        filter.add(key);
      }
      if (!filter_chosen.isEmpty()) {
        if (locations.containsKey(key)) {
          locations.put(key, locations.get(key) + 1);
        } else {
          locations.put(key, 1);
        }
      }
      prepareItemData(getContext());
    }
    if (this.getContext() == null) return;
    adapterItem = new ArrayAdapter<>(getContext(), R.layout.filter_list_item, filter);
    binding.autoCompleteTextView.setAdapter(adapterItem);
  }


  private String getLocation(String lat, String lon) {
    if (this.getContext() != null) {
      Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
      List<Address> addresses;
      try {
        addresses = gcd.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lon), 1);
        assert addresses != null;
        if (addresses.size() > 0) {
          return addresses.get(0).getCountryName();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }
}