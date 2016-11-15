package eu.cuteapps.camerahttp.myutils;

import android.content.Context;

import android.util.TypedValue;

public class ViewUtils {

  public static int getPixelsFromDps(int dps, Context context) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        dps, context.getResources().getDisplayMetrics());
  }
}