package com.example.mobile_memoryexplorer.ui.home;

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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobile_memoryexplorer.Database.AppDatabase;
import com.example.mobile_memoryexplorer.Database.Favourite;
import com.example.mobile_memoryexplorer.ResponsiveDimension;
import com.example.mobile_memoryexplorer.ui.addMemory.Memory;
import com.example.mobile_memoryexplorer.R;
import com.example.mobile_memoryexplorer.ui.SingleMemory.SingleMemory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MemoriesListAdapter extends RecyclerView.Adapter<MemoriesListAdapter.MyViewHolder> {

  private final List<Memory> list;
  private final Context context;
  private final String email;
  private static Boolean isProfile = false;
  private final ResponsiveDimension responsiveDimension;

  public MemoriesListAdapter(List<Memory> list, Context context, String email, Boolean isProfile, ResponsiveDimension responsiveDimension) {
    this.list = list;
    this.context = context;
    this.email = email;
    MemoriesListAdapter.isProfile = isProfile;
    this.responsiveDimension = responsiveDimension;
  }

  public static class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView title_tv, date_tv, my_memory_tv, my_favorite_tv;
    public ImageView image, favorite;

    public MyViewHolder(View view) {
      super(view);
      title_tv = view.findViewById(R.id.title);
      date_tv = view.findViewById(R.id.date);
      my_memory_tv = view.findViewById(R.id.my_memories);
      my_favorite_tv = view.findViewById(R.id.my_favourite);
      image = view.findViewById(R.id.imgVersionName);
      favorite = view.findViewById(R.id.favorite);

      if (isProfile) {
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
  public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
    setUpMemory(holder, position);
    setClickListener(holder);
  }

  private void setClickListener(final MyViewHolder holder) {
    holder.itemView.setOnClickListener(view -> {
      Intent singleMemory = new Intent(context, SingleMemory.class);
      Bundle b = new Bundle();
      b.putString("id", holder.image.getTag().toString());
      singleMemory.putExtras(b);
      context.startActivity(singleMemory);
    });
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  private void setUpMemory(final MyViewHolder holder, final int position) {
    Calendar calendar = Calendar.getInstance();
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN);
      Date date = sdf.parse(list.get(position).getDate());
      if (date != null) {
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        holder.date_tv.setText(String.valueOf(year));
      }
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }

    setImageViewDimensions(holder.image);
    holder.title_tv.setText(list.get(position).getTitle());
    holder.image.setTag(list.get(position).getId());
    holder.favorite.setTag(list.get(position).getId());

    loadImageWithGlide(holder.image, list.get(position).getImage());
    setFavoriteClickListener(holder, position);
  }

  private void setImageViewDimensions(ImageView imageView) {
    imageView.getLayoutParams().height = responsiveDimension.getResposniveDimension();
    imageView.getLayoutParams().width = responsiveDimension.getResposniveDimension();
  }

  private void loadImageWithGlide(ImageView imageView, String imageUrl) {
    Glide.with(context)
        .load(Uri.parse(imageUrl))
        .into(imageView);
  }

  private void setFavoriteClickListener(final MyViewHolder holder, final int position) {
    holder.favorite.setOnClickListener(view -> {
      AppDatabase appDb = AppDatabase.getInstance(context);
      Favourite favourite = new Favourite(email, holder.favorite.getTag().toString());

      if (appDb.favouriteUserMemoryDao().checkMemories(email, holder.favorite.getTag().toString()) != null) {
        appDb.favouriteUserMemoryDao().deleteTask(favourite);
        showToast(list.get(position).getTitle() + " eliminato dai preferiti!");
      } else {
        appDb.favouriteUserMemoryDao().insert(favourite);
        showToast(list.get(position).getTitle() + " aggiunto ai preferiti!");
      }
    });
  }

  private void showToast(String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }
}
