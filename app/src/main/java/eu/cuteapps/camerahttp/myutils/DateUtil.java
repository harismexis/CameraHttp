package eu.cuteapps.camerahttp.myutils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class DateUtil {

  public static String getCurrentDateTime() {
    final DateTime now = DateTime.now();
    return DateTimeFormat.forPattern("dd/MM/yyyy").print(now) + ", " +
        DateTimeFormat.forPattern("HH:mm").print(now);
  }
}
