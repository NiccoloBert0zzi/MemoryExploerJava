package com.example.mobile_memoryexplorer;

import static java.security.AccessController.getContext;

import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class ResponsiveDimension {
  Integer width;

  public ResponsiveDimension(WindowManager wd) {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    wd.getDefaultDisplay().getMetrics(displayMetrics);
    int width = displayMetrics.widthPixels;
    this.width = width;
  }

  public int getResponsiveCollum() {
    if (width >= 1200) {
      return 3;
    } else if (width >= 800) {
      return 2;
    } else {
      return 1;
    }
  }
}
