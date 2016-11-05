package eu.cuteapps.camerahttp.myutils;

import android.hardware.GeomagneticField;
import android.location.Location;

public class LocationParser {
	
	private Location location = null;
	
	public LocationParser(Location location) {
		this.location = location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public String getStringLatitude() {
		if(location != null) {
			return String.valueOf(location.getLatitude());
		}
		return null;
	}
	
	public String getStringLongitude() {
		if(location != null) {
			return String.valueOf(location.getLongitude());
		}
		return null;
	}
	
	public String getStringAltitude() {
		if(location != null) {
			return String.valueOf(location.getAltitude());
		}
		return null;
	}
	
	public String getStringAccuracy() {
		if(location != null && location.hasAccuracy()) {
			return String.valueOf(location.getAccuracy());
		}
		return null;
	}
	
//	public String getStringNorthDeclination() {
//		if(location != null) {
//			GeomagneticField geoField = new GeomagneticField(
//					Double.valueOf(this.getRealLatitude()).floatValue(),
//					Double.valueOf(this.getRealLongitude()).floatValue(),
//					Double.valueOf(this.getRealtAltitude()).floatValue(),
//					System.currentTimeMillis());
//		}
//	}
}