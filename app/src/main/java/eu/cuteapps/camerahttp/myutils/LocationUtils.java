package eu.cuteapps.camerahttp.myutils;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

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
	
	public static String getStringAltitude(Location location) {
		if(location != null) {
			return String.valueOf(location.getAltitude());
		}
		return "unknown";
	}
	
	public static String getStringAccuracy(Location location) {
		if(location != null && location.hasAccuracy()) {
			return String.valueOf(location.getAccuracy());
		}
		return "unknown";
	}
	
	public static String getLocationProvider(LocationManager locationManager, String provider) {
			if(provider == null) {
				Criteria criteria = new Criteria();
				// criteria.setAccuracy(Criteria.ACCURACY_COARSE);
				// criteria.setBearingRequired(true);
				return locationManager.getBestProvider(criteria, true);
			}
			if(provider.equals(LocationManager.GPS_PROVIDER)) {
				return LocationManager.GPS_PROVIDER;
			}
			if(provider.equals(LocationManager.NETWORK_PROVIDER)) {
				return LocationManager.NETWORK_PROVIDER;
			}
		return null;
	}

	public static double calcBearingFromTwoPoints(double lat1, double lon1, 
    		double lat2, double lon2) {
		
		double rLat1 = Math.toRadians(lat1);
		double rLon1 = Math.toRadians(lon1);
		double rLat2 = Math.toRadians(lat2);
		double rLon2 = Math.toRadians(lon2);
		
    	double dLon = rLon2 - rLon1;
    	double y = Math.sin(dLon) * Math.cos(rLat2);	
    	double x = Math.cos(rLat1) * Math.sin(rLat2) -
    	        Math.sin(rLat1) * Math.cos(rLat2) * Math.cos(dLon);
    	
    	double brRads = Math.atan2(y, x);
    	double brDeg = Math.toDegrees(brRads);
    	// double normBrDeg = (brDeg + 360) % 360;
    	// double finalBrDeg = (brDeg + 180) % 360;
    	return brDeg;
    }
	
	public static boolean isAnyLocationProviderEnabled(Activity context) {
		LocationManager locationManager = 
				(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean isNetworkProviderEnabled = 
				locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		boolean isGPSProviderEnabled = 
				locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return (isNetworkProviderEnabled || isGPSProviderEnabled);
	}
	
	public static boolean isGPSProviderEnabled(Context context) {
		LocationManager locationManager = 
		(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	public static boolean isNetworkProviderEnabled(Context context) {
		LocationManager locationManager = 
		(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	public static boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}
	
	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
}
