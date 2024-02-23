package com.example.mobile_memoryexplorer.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Profile.class, Favourite.class},exportSchema = false, version = 1)
public abstract class AppDatabase extends RoomDatabase {
  private  static final  String DB_NAME = "users_db";
  private static AppDatabase instance;
  public static  synchronized AppDatabase getInstance(Context context){
    if (instance == null){
      instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
              .allowMainThreadQueries()
              .build();
    }
    return instance;
  }
  public abstract ProfileDao profileDao();
  public abstract FavouriteDao favouriteUserMemoryDao();
}
