package com.example.mobile_memoryexplorer.Database;


import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity (tableName = "profile")
public class Profile implements Serializable {
@PrimaryKey
@NonNull
  private String email;
  @ColumnInfo(name = "name")
  private String name;
  @ColumnInfo(name = "surname")
  private String surname;
  @ColumnInfo(name = "address")
  private String address;
  @ColumnInfo(name = "birthdate")
  private String birthdate;
  @ColumnInfo(name = "imageUri")
  private String imageUri;

  public Profile(String email, String name, String surname, String address, String birthdate,String imageUri) {
    this.email = email;
    this.name = name;
    this.surname = surname;
    this.address = address;
    this.birthdate = birthdate;
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

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getBirthdate() {
    return birthdate;
  }

  public void setBirthdate(String birthdate) {
    this.birthdate = birthdate;
  }

  public String getImageUri() {
    return imageUri;
  }

  public void setImageUri(String imageUri) {
    this.imageUri = imageUri;
  }
}