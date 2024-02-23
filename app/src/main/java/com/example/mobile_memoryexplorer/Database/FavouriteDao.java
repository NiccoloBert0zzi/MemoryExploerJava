package com.example.mobile_memoryexplorer.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FavouriteDao {
  @Query("SELECT * FROM Favourite")
  List<Favourite> getAll();
  @Query("SELECT * FROM Favourite WHERE email = :email")
  List<Favourite> getUserMemories(String email);

  @Query("SELECT * FROM Favourite WHERE email = :email AND memory_id = :memoryId")
  Favourite checkMemories(String email, String memoryId);
  @Insert
  void insert(Favourite favourite);

  @Update
  void updateTask(Favourite favourite);

  @Delete
  void deleteTask(Favourite favourite);
}
