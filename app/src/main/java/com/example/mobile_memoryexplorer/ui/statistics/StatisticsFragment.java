package com.example.mobile_memoryexplorer.ui.statistics;

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

import com.example.mobile_memoryexplorer.Memory;
import com.example.mobile_memoryexplorer.MySharedData;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.databinding.FragmentStatisticsBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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
    mySharedData = new MySharedData(getContext());
    email = MySharedData.getEmail();

    filter.add("Mondo");
    prepareItemData();

    binding.autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
      locations.clear();
      filter_chosen = parent.getItemAtPosition(position).toString();
      downloadData();
    });
    return root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  //scarica dati dal db e prende le memories dell'utente
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
          downloadData();
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

  //scarica le location e li mette in una mappa con la chiave citta e il valore numero di votle visitata
  private void downloadData() {
    for (Memory m : list) {
      String key = getlocation(m.getLatitude(), m.getLongitude());
      if (key == null)
        continue;
      if (!filter.contains(key) && filter_chosen == null) {
        filter.add(key);
      }
      if (filter_chosen != null && !filter_chosen.isEmpty()) {
        if (locations.containsKey(key)) {
          locations.put(key, locations.get(key) + 1);
        } else {
          locations.put(key, 1);
        }
      }
      setupBarChart();
    }
    adapterItem = new ArrayAdapter<>(getContext(), R.layout.filter_list_item, filter);
    binding.autoCompleteTextView.setAdapter(adapterItem);
  }

  //ritorna la citta in base alla latitudine e longitudine
  private String getlocation(String lat, String lon) {
    Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
    List<Address> addresses;
    try {
      addresses = gcd.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lon), 1);
      if (addresses.size() > 0) {
        if (filter_chosen != null && !filter_chosen.isEmpty()) {
          if (filter_chosen.equals("Mondo")) {
            return addresses.get(0).getCountryName();
          } else if (addresses.get(0).getCountryName().equals(filter_chosen)) {
            return addresses.get(0).getAdminArea();
          }
        } else {
          return addresses.get(0).getCountryName();
        }
      }
    } catch (IOException e) {
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
    binding.barChart.setData(data);

    binding.barChart.getDescription().setEnabled(false);
    binding.barChart.getLegend().setEnabled(false);
    binding.barChart.animateY(1000);
    binding.barChart.invalidate();
  }
}