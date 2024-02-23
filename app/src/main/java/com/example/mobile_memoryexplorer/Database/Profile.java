package com.example.mobile_memoryexplorer.Database;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "profile")
public class Profile implements Serializable {
  @PrimaryKey
  @NonNull
  private String email;
  @ColumnInfo(name = "name")
  private String name;
  @ColumnInfo(name = "imageUri")
  private String imageUri;

  public Profile(String email, String name, String imageUri) {
    this.email = email;
    this.name = name;
    this.imageUri = imageUri;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImageUri() {
    return imageUri;
  }

  public void setImageUri(String imageUri) {
    this.imageUri = imageUri;
  }
}