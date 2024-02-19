package com.example.mobile_memoryexplorer.ui.addMemory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddMemoryViewModel extends ViewModel {

  private final MutableLiveData<String> mText;

  public AddMemoryViewModel() {
    mText = new MutableLiveData<>();
    mText.setValue("This is dashboard fragment");
  }

  public LiveData<String> getText() {
    return mText;
  }
}
