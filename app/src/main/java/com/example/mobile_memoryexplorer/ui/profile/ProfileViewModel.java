package com.example.mobile_memoryexplorer.ui.profile;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobile_memoryexplorer.Database.AppDatabase;
import com.example.mobile_memoryexplorer.Database.Profile;
import com.example.mobile_memoryexplorer.MySharedData;
import com.google.firebase.database.DatabaseReference;

import java.util.List;
import java.util.Objects;

public class ProfileViewModel extends ViewModel {

  private Profile profile;
  private final MutableLiveData<List<Profile>> users;
  String email;
  MySharedData mySharedData;
  public ProfileViewModel() {
    users = new MutableLiveData<>();
  }

  public void loadDb(Context context) {
    AppDatabase db = AppDatabase.getInstance(context);
    users.setValue(db.profileDao().getAll());
    mySharedData = new MySharedData(context);
    email = MySharedData.getEmail();
    for (Profile user : users.getValue()) {
      if (Objects.equals(user.getEmail(), email)) {
        profile = user;
      }
    }
  }

  public MutableLiveData<String> getName() {
    MutableLiveData<String> name = new MutableLiveData<>();
    name.setValue("Nome: "+profile.getName());
    return name;
  }
  public MutableLiveData<String> getimageURI() {
    MutableLiveData<String> name = new MutableLiveData<>();
    name.setValue(profile.getImageUri());
    return name;
  }
}