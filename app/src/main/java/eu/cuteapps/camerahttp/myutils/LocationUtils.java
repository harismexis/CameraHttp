package eu.cuteapps.camerahttp.myutils;

import android.location.Location;

public class LocationUtils {

  public static String getStringLatitude(Location location) {
    if(location != null) {
      return String.valueOf(location.getLatitude());
    }
    return "unknown";
  }

  public static String getStringLongitude(Location location) {
    if(location != null) {
      return String.valueOf(location.getLongitude());
    }
    return "unknown";
  }
}
