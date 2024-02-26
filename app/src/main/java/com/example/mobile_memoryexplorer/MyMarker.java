package com.example.mobile_memoryexplorer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MyMarker {
  Bitmap smallMarker;
  public MyMarker(Context context) {
    int height = 100;
    int width = 100;
    Bitmap b = BitmapFactory.decodeResource(context.getResources(),
        R.drawable.marker_icon);
    smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
  }
  public Bitmap getSmallMarker() {
    return smallMarker;
  }
}
