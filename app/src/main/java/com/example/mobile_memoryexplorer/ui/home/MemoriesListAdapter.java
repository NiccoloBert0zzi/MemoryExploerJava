package com.example.mobile_memoryexplorer.ui.home;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.SpannableString;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobile_memoryexplorer.Database.AppDatabase;
import com.example.mobile_memoryexplorer.Database.Favourite;
import com.example.mobile_memoryexplorer.ui.addMemory.Memory;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.SingleMemory;

public class MemoriesListAdapter extends RecyclerView.Adapter<MemoriesListAdapter.MyViewHolder> {

  private final List<Memory> list;
  Context context;
  private final String email;
  private static Boolean isProfile = false;

  public MemoriesListAdapter(List<Memory> list, Context context, String email,Boolean isProfile) {
    this.list = list;
    this.context = context;
    this.email = email;
    this.isProfile = isProfile;
  }

  public static class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView title_tv,date_tv,my_memory_tv,my_favorite_tv;
    public ImageView image, favorite;

    public MyViewHolder(View view) {
      super(view);
      title_tv = view.findViewById(R.id.title);
      date_tv = view.findViewById(R.id.date);
      my_memory_tv = view.findViewById(R.id.my_memories);
      my_favorite_tv = view.findViewById(R.id.my_favourite);
      image = view.findViewById(R.id.imgVersionName);
      favorite = view.findViewById(R.id.favorite);

      if(isProfile){
        favorite.setVisibility(View.GONE);
      }
    }
  }

  @NonNull
  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
    return new MyViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(final MyViewHolder holder, final int position) {
    setUpMymemory(holder, position);
    holder.itemView.setOnClickListener(view -> {
          Intent singleMemory = new Intent(context, SingleMemory.class);
          Bundle b = new Bundle();
          b.putString("id", holder.image.getTag().toString()); //Your id
          singleMemory.putExtras(b); //Put your id to your next Intent
          context.startActivity(singleMemory);
        });
    holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.animation));
  }

  @Override
  public int getItemCount() {
    return list.size();
  }
  private void setUpMymemory(final MyViewHolder holder, final int position){
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
    holder.favorite.setTag(list.get(position).getId());
    Glide.with(context)
        .load(Uri.parse(list.get(position).getImage()))
        .into(holder.image);
    holder.favorite.setOnClickListener(view -> {
      AppDatabase appDb = AppDatabase.getInstance(context);
      Favourite favourite = new Favourite(email, holder.favorite.getTag().toString());
      // check if the memory is already in the favourite list
      if (appDb.favouriteUserMemoryDao().checkMemories(email, holder.favorite.getTag().toString()) != null) {
        appDb.favouriteUserMemoryDao().deleteTask(favourite);
        Toast.makeText(context, list.get(position).getTitle() + " eliminato dai preferiti!", Toast.LENGTH_SHORT).show();
      } else {
        appDb.favouriteUserMemoryDao().insert(favourite);
        Toast.makeText(context, list.get(position).getTitle() + " aggiunto ai preferiti!", Toast.LENGTH_SHORT).show();
      }
    });
  }
}
