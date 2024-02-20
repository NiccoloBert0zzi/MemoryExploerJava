package com.example.mobile_memoryexplorer;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedData {

  public void setSharedpreferences(String key, String value) {
    SharedPreferences.Editor editor = sharedpreferences.edit();
    editor.putString(key, value);
    editor.apply();
  }

  private static SharedPreferences sharedpreferences;

  public MySharedData(Context context) {
    sharedpreferences = context.getSharedPreferences("log_info", Context.MODE_PRIVATE);
  }

  public static String getEmail() {
    return sharedpreferences.getString("email", "");
  }
  public static String getRemember() {
    return sharedpreferences.getString("remember", "");
  }
}
