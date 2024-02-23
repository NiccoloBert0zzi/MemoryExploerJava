package com.example.mobile_memoryexplorer.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProfileDao {
  @Query("SELECT * FROM Profile")
  List<Profile> getAll();
  @Query("SELECT * FROM Profile WHERE email = :email")
  Profile getProfile(String email);

  @Insert
  void insert(Profile student);

  @Update
  void updateTask(Profile student);

  @Delete
  void deleteTask(Profile student);
}
