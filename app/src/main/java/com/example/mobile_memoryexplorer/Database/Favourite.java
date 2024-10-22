package com.example.mobile_memoryexplorer.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "favourite", primaryKeys = {"email", "memory_id"})
public class Favourite implements Serializable {
    @NonNull
    private String email;
    @NonNull
    @ColumnInfo(name = "memory_id")
    private String memoryId;

    public Favourite(String email, String memoryId) {
        this.email = email;
        this.memoryId = memoryId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
    }
}