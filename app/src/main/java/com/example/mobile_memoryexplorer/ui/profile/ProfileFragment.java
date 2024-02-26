package com.example.mobile_memoryexplorer.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.mobile_memoryexplorer.Auth.Login;
import com.example.mobile_memoryexplorer.Database.AppDatabase;
import com.example.mobile_memoryexplorer.Database.Favourite;
import com.example.mobile_memoryexplorer.ResponsiveDimension;
import com.example.mobile_memoryexplorer.ui.home.MemoriesListAdapter;
import com.example.mobile_memoryexplorer.ui.addMemory.Memory;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.databinding.FragmentProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ProfileFragment extends Fragment {

  private FragmentProfileBinding binding;
  private DatabaseReference dbRef;
  private final List<Memory> list = new ArrayList<>();
  String email;
  MySharedData mySharedData;
  private static SpannableString m_memory_span, m_favorite_span;
  static Boolean holderState = false;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    ProfileViewModel profileViewModel =
        new ViewModelProvider(this).get(ProfileViewModel.class);
    profileViewModel.loadDb(this.getContext());

    mySharedData = new MySharedData(getContext());
    email = MySharedData.getEmail();
    binding = FragmentProfileBinding.inflate(inflater, container, false);

    m_memory_span = new SpannableString(binding.myMemories.getText());
    m_memory_span.setSpan(new UnderlineSpan(), 0, m_memory_span.length(), 0);
    m_favorite_span = new SpannableString(binding.myFavourite.getText());
    m_favorite_span.setSpan(new UnderlineSpan(), 0, m_favorite_span.length(), 0);
    binding.myMemories.setText(m_memory_span);

    View root = binding.getRoot();

    if (MySharedData.getThemePreferences()) {
      binding.switchTheme.setChecked(true);
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
    binding.switchTheme.setOnClickListener(v -> {
      if (!MySharedData.getThemePreferences()) {
        mySharedData.setThemePreferences(true);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
      } else {
        mySharedData.setThemePreferences(false);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
      }
    });

    binding.myFavourite.setOnClickListener(view -> {
      binding.myFavourite.setText(m_favorite_span);
      binding.myMemories.setText("My memories");
      holderState = true;
      prepareItemData();
    });
    binding.myMemories.setOnClickListener(view -> {
      binding.myMemories.setText(m_memory_span);
      binding.myFavourite.setText("My favourite");
      holderState = false;
      prepareItemData();
    });

    binding.progressBar.setVisibility(RelativeLayout.VISIBLE);
    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    dbRef = FirebaseDatabase.getInstance().getReference("memories");
    prepareItemData();
    //Set view
    profileViewModel.getName().observe(getViewLifecycleOwner(), binding.name::setText);
    profileViewModel.getimageURI().observe(getViewLifecycleOwner(), s -> {
      if (s != null) {
        Glide.with(binding.getRoot().getContext())
            .load(Uri.parse(s))
            .into(binding.profileImage);
      } else {
        binding.profileImage.setImageResource(R.drawable.ic_baseline_account_circle_24);
      }
      binding.progressBar.setVisibility(View.GONE);
      getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    });
    //logout
    binding.buttonLogout.setOnClickListener(v -> {
      MySharedData mySharedData = new MySharedData(this.getContext());
      mySharedData.setSharedpreferences("remember", "false");
      //start login activity
      Intent loginpage = new Intent(this.getContext(), Login.class);
      loginpage.setFlags(loginpage.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
      startActivity(loginpage);
    });
    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  public void prepareItemData() {
    List<String> fav_id = new ArrayList<>();
    if (holderState) {
      AppDatabase appDb = AppDatabase.getInstance(getContext());
      List<Favourite> favourites = appDb.favouriteUserMemoryDao().getUserMemories(email);
      fav_id = favourites.stream().map(Favourite::getMemoryId).collect(Collectors.toList());
    }
    List<String> finalFav_id = fav_id;
    dbRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(binding == null) return;
        list.clear();
        for (DataSnapshot memorySnapshot : snapshot.getChildren()) {
          Memory m = memorySnapshot.getValue(Memory.class);
          if (holderState) {
            //se il memory è tra i preferiti
            if (finalFav_id.contains(m.getId()))
              list.add(m);
          } else if (m.getCreator().equals(email) && !holderState)
            list.add(m);
        }
        if (binding != null) {
          if (list.isEmpty()) {
            Toast.makeText(getContext(), "No memories found", Toast.LENGTH_SHORT).show();
          }
          //set GridLayoutManager in recyclerView and show items in grid with two columns
          binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), new ResponsiveDimension(getActivity().getWindowManager()).getResponsiveCollum()));
          //set adapter ItemAdapter in recyclerView
          binding.recyclerView.setAdapter(new MemoriesListAdapter(list, getContext(), email,true));
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