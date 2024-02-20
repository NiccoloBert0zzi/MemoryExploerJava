package com.example.mobile_memoryexplorer;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobile_memoryexplorer.Auth.Login;

public class MemoriesListAdapter extends RecyclerView.Adapter<MemoriesListAdapter.MyViewHolder> {

  private final List<Memory> list;
  Context context;


  public MemoriesListAdapter(List<Memory> list, Context context) {
    this.list = list;
    this.context = context;

  }

  public static class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView title_tv;
    public TextView date_tv;
    public ImageView image;


    public MyViewHolder(View view) {
      super(view);

      title_tv = view.findViewById(R.id.title);
      date_tv = view.findViewById(R.id.date);
      image = view.findViewById(R.id.imgVersionName);
    }
  }

  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
    return new MyViewHolder(itemView);

  }

  @Override
  public void onBindViewHolder(final MyViewHolder holder, final int position) {
    Calendar calendar = Calendar.getInstance();
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN);
      Date date = sdf.parse(list.get(position).getDate());
      calendar.setTime(date);
      int year = calendar.get(Calendar.YEAR);
      holder.date_tv.setText(String.valueOf(year));
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    holder.title_tv.setText(list.get(position).getTitle());
    holder.image.setTag(list.get(position).getId());
    Glide.with(context)
        .load(Uri.parse(list.get(position).getImage()))
        .into(holder.image);

    holder.itemView.setOnClickListener(view -> {
          Intent singleMemory = new Intent(context, SingleMemory.class);
          Bundle b = new Bundle();
          b.putString("id", holder.image.getTag().toString()); //Your id
          singleMemory.putExtras(b); //Put your id to your next Intent
          context.startActivity(singleMemory);
        }
    );
  }

  @Override
  public int getItemCount() {
    return list.size();
  }
}
