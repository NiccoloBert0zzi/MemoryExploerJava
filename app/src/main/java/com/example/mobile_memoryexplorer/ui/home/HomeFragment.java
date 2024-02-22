package com.example.mobile_memoryexplorer.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.mobile_memoryexplorer.MemoriesListAdapter;
import com.example.mobile_memoryexplorer.Memory;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

  private FragmentHomeBinding binding;
  private final List<Memory> list = new ArrayList<>();
  String email;
  private DatabaseReference dbRef;
  MySharedData mySharedData;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    View root = binding.getRoot();
    mySharedData = new MySharedData(this.getContext());
    email = MySharedData.getEmail();
    dbRef = FirebaseDatabase.getInstance().getReference("memories");
    prepareItemData(this.getContext());
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
        list.clear();
        for (DataSnapshot memorySnapshot : snapshot.getChildren()) {
          Memory m = memorySnapshot.getValue(Memory.class);
          if (!m.getCreator().equals(email))
            list.add(m);
        }
        if (list.isEmpty()) {
          Toast.makeText(ctx, "No memories found", Toast.LENGTH_SHORT).show();
        } else {
          if (binding != null) {
            //set GridLayoutManager in recyclerView and show items in grid with two columns
            binding.recyclerView.setLayoutManager(new GridLayoutManager(ctx, 2));
            //set adapter ItemAdapter in recyclerView
            binding.recyclerView.setAdapter(new MemoriesListAdapter(list, ctx));
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