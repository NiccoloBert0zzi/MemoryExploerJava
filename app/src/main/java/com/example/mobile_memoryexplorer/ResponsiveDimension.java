package com.example.mobile_memoryexplorer;

import android.util.DisplayMetrics;
import android.view.WindowManager;


public class ResponsiveDimension {
  Integer width;

  public ResponsiveDimension(WindowManager wd) {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    wd.getDefaultDisplay().getMetrics(displayMetrics);
    this.width = displayMetrics.widthPixels;

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
  public int getResposniveDimension(){
    if (width >= 1200) {
      return 500;
    } else if (width >= 800) {
      return 400;
    } else {
      return 300;
    }
  }
}
