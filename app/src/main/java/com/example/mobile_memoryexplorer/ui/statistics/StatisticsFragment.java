package com.example.mobile_memoryexplorer.ui.statistics;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mobile_memoryexplorer.ui.addMemory.Memory;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.databinding.FragmentStatisticsBinding;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
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

public class StatisticsFragment extends Fragment {

  private FragmentStatisticsBinding binding;
  private DatabaseReference dbRef;
  private final List<Memory> list = new ArrayList<>();
  String email;
  MySharedData mySharedData;
  Map<String, Integer> locations = new HashMap<>();
  List<String> filter = new ArrayList<>();
  ArrayAdapter<String> adapterItem;
  String filter_chosen;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentStatisticsBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    dbRef = FirebaseDatabase.getInstance().getReference("memories");
    assert getContext() != null;
    mySharedData = new MySharedData(getContext());
    email = MySharedData.getEmail();
    filter.add("Mondo");
    prepareItemData();

    binding.autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
      locations.clear();
      filter_chosen = parent.getItemAtPosition(position).toString();
      downloadData();
    });
    locations.clear();
    filter_chosen = "Mondo";
    downloadData();
    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  // Carica i dati dal database e ottiene le memorie dell'utente
  public void prepareItemData() {
    dbRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (binding == null) return;
        list.clear();
        for (DataSnapshot memorySnapshot : snapshot.getChildren()) {
          Memory m = memorySnapshot.getValue(Memory.class);
          if (m.getCreator().equals(email))
            list.add(m);
        }
        if (list.isEmpty()) {
          Toast.makeText(getContext(), "No memories found", Toast.LENGTH_SHORT).show();
        } else {
          downloadData();
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Toast.makeText(getContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
      }
    });
  }

  // Scarica le posizioni e le mette in una mappa con la chiave città e il valore numero di volte visitata
  private void downloadData() {
    for (Memory m : list) {
      String key = getlocation(m.getLatitude(), m.getLongitude());
      if (key == null) {
        continue;
      }
      if (!filter_chosen.isEmpty() && !filter.contains(key) && filter_chosen.equals("Mondo")) {
        filter.add(key);
      }
      if (!filter_chosen.isEmpty()) {
        locations.put(key, locations.getOrDefault(key, 0) + 1);
      }
      setupBarChart();
    }
    assert getContext() != null;
    adapterItem = new ArrayAdapter<>(getContext(), R.layout.filter_list_item, filter);
    binding.autoCompleteTextView.setAdapter(adapterItem);
  }

  // Ritorna la città in base alla latitudine e longitudine
  private String getlocation(String lat, String lon) {
    assert getContext() != null;
    Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
    try {
      List<Address> addresses = gcd.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lon), 1);
      if (!addresses.isEmpty()) {
        Address address = addresses.get(0);
        if (filter_chosen != null && !filter_chosen.isEmpty()) {
          if (filter_chosen.equals("Mondo")) {
            return address.getCountryName();
          } else if (address.getCountryName().equals(filter_chosen)) {
            return address.getAdminArea();
          }
        } else {
          return address.getCountryName();
        }
      }
    } catch (IOException | NumberFormatException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  private void setupBarChart() {
    binding.barChart.clear();
    ArrayList<PieEntry> yValues = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : locations.entrySet()) {
      yValues.add(new PieEntry(entry.getValue(), entry.getKey()));
    }
    PieDataSet dataSet = new PieDataSet(yValues, "Locations");
    dataSet.setSliceSpace(3f);
    dataSet.setSelectionShift(5f);
    dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

    PieData data = new PieData(dataSet);
    data.setValueTextSize(15f);
    binding.barChart.setData(data);

    binding.barChart.getDescription().setEnabled(false);
    binding.barChart.setDrawHoleEnabled(true);
    binding.barChart.setHoleColor(0);
    binding.barChart.getLegend().setEnabled(false);
    binding.barChart.animateY(1000);
    binding.barChart.invalidate();

    binding.barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
      @SuppressLint("SetTextI18n")
      @Override
      public void onValueSelected(Entry e, Highlight h) {
        PieEntry pe = (PieEntry) e;
        String number = (int) e.getY() == 1 ? (int) e.getY() + " volta" : (int) e.getY() + " volte";
        binding.info.setText("Hai visitato " + pe.getLabel() + " " + number);
      }

      @Override
      public void onNothingSelected() {

      }
    });
  }
}
