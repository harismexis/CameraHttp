package eu.cuteapps.camerahttp.myutils;

import android.location.Location;

public class LocationUtils {

  public static final int LOCATION_UPDATE_INTERVAL = 10000;
  public static final int LOCATION_UPDATE_FASTEST_INTERVAL = 5000;

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
