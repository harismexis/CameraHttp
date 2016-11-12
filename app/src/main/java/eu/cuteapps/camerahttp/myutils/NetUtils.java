package eu.cuteapps.camerahttp.myutils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {

  public static String getResponseBody(HttpResponse response) {
    if(response == null) {
      return null;
    }
    HttpEntity entity = response.getEntity();
    String responseBody = "";
    try {
      responseBody = EntityUtils.toString(entity);
    } catch(ParseException | IOException e) {
      e.printStackTrace();
    }
    return responseBody;
  }

  public static String getResponseText(HttpResponse response) {
    if(response == null) {
      return "null_response";
    }
    String responseBody = NetUtils.getResponseBody(response);
    if(responseBody == null) {
      return "null_response_body";
    }
    return responseBody;
  }

  public static boolean isNetworkConnected(Activity context) {
    ConnectivityManager connMgr = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
  }

}