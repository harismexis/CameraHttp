package eu.cuteapps.camerahttp.myutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class NetUtils {

	// Posts data to the server and returns the HttpResponse from the server
	public static HttpResponse executePostRequest(HttpPost httppost, ArrayList<NameValuePair> params) {
		HttpClient httpclient = new DefaultHttpClient();
			try {
				HttpEntity entity = new UrlEncodedFormEntity(params);
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				return response;
			} 
			catch (ClientProtocolException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			return null;
	}

	
	// Posts data to the server and returns the HttpResponse from the server
	public static HttpResponse executePostRequestToTheServer(ArrayList<NameValuePair> params, String serverUrl) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(serverUrl);
		try {
			HttpEntity entity = new UrlEncodedFormEntity(params);
			// httppost.addHeader(entity.getContentType());
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);
			return response;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Returns a String with the server response - returns null if the response is null
	public static String getResponseBody(HttpResponse response) {
		if (response == null) {
			return null;
		}
		HttpEntity entity = response.getEntity();
		String responseBody = "";
		try {
			responseBody = EntityUtils.toString(entity);
		} 
		catch (ParseException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return responseBody;
	}

	// Returns a String with the server response - returns a default String if the response is null
	public static String getResponseText(HttpResponse response) {
		if(response == null) {
			return "null_response";
		}
		String responseBody = NetUtils.getResponseBody(response);
		if (responseBody == null) {
			return "null_response_body";
		}
		return responseBody;
	}

	// Get All the response headers to a string
	public static String getAllResponseHeaders(HttpResponse response) {
		Header[] headers = response.getAllHeaders();
		String text = "";
		for (int i = 0; i < headers.length; i++) {
			Header header = headers[i];
			text += header.getName() + " , ";
			text += header.getValue();
			text += "\n";
		}
		return text;
	}

	// Get the response headers to a string
	public static String getResponseHeaders(HttpResponse response, String name) {
		Header[] headers = response.getHeaders(name);
		if (headers == null) {
			return "Got null headers!!!";
		}
		String text = "";
		for (int i = 0; i < headers.length; i++) {
			Header header = headers[i];
			text += header.getName() + " , ";
			text += header.getValue();
			text += "\n";
		}
		return text;
	}
	
	// Read Database Rows encoded in json and return them to a string
	public static String readDataEncodedInJson(HttpResponse response) {
			StringBuilder builder = new StringBuilder();
			try {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = 
						new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} 
			catch (ClientProtocolException e) {
				e.printStackTrace();
				return null;
			} 
			catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return builder.toString();
		}
	
	public static boolean isNetworkConnected(Activity context) {
		ConnectivityManager connMgr = 
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}
	
 	// Enables / disables the Wireless Internet Connection
// 	public static void setWirelessEnabled(Context context, boolean enabled) {
// 		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
// 		wifiManager.setWifiEnabled(enabled);
// 	}

}