package com.example.mobile_memoryexplorer;

public class Memory {
  public Memory(String id, String creator, String title, String description, String date, String location, String image) {
    this.title = title;
    this.description = description;
    this.date = date;
    this.location = location;
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

  public String getLocation() {
    return location;
  }

  public String getImage() {
    return image;
  }

  private String id;
  private String creator;
  private String title;
  private String description;
  private String date;
  private String location;
  private String image;

  @Override
  public String toString() {
    return "Memory{" +
        "title='" + title + '\'' +
        ", description='" + description + '\'' +
        ", date='" + date + '\'' +
        ", location='" + location + '\'' +
        ", image='" + image + '\'' +
        ", creator='" + creator + '\'' +
        ", id='" + id + '\'' +
        '}';
  }
}
