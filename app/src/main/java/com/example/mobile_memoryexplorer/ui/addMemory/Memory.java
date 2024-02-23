package com.example.mobile_memoryexplorer.ui.addMemory;

public class Memory {
  public Memory(String id, String creator, String title, String description, String date, String latitude, String longitude, String image) {
    this.title = title;
    this.description = description;
    this.date = date;
    this.latitude = latitude;
    this.longitude = longitude;
    this.image = image;
    this.creator = creator;
    this.id = id;
  }

  public Memory() {
  }

  public String getId() {
    return id;
  }

  public String getCreator() {
    return creator;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getDate() {
    return date;
  }

  public String getLatitude() {
    return latitude;
  }

  public String getLongitude() {
    return longitude;
  }

  public String getImage() {
    return image;
  }

  private String id;
  private String creator;
  private String title;
  private String description;
  private String date;
  private String latitude;
  private String longitude;
  private String image;

  @Override
  public String toString() {
    return "Memory{" +
        "title='" + title + '\'' +
        ", description='" + description + '\'' +
        ", date='" + date + '\'' +
        ", latitude='" + latitude + '\'' +
        ", longitude='" + longitude + '\'' +
        ", image='" + image + '\'' +
        ", creator='" + creator + '\'' +
        ", id='" + id + '\'' +
        '}';
  }
}
