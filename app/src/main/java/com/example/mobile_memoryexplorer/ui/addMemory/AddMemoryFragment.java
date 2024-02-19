package com.example.mobile_memoryexplorer.ui.addMemory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_memoryexplorer.databinding.FragmentAddMemoryBinding;
import com.example.mobile_memoryexplorer.ui.dashboard.DashboardViewModel;

public class AddMemoryFragment extends Fragment {

  private FragmentAddMemoryBinding binding;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    DashboardViewModel dashboardViewModel =
        new ViewModelProvider(this).get(DashboardViewModel.class);

    binding = FragmentAddMemoryBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    final TextView textView = binding.textView;
    dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}