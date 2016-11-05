package eu.cuteapps.camerahttp.myutils;

import android.text.format.Time;

public class TimeUtils {

	public static String getDateTime() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		StringBuilder currentTime = new StringBuilder();
		currentTime.append(today.year + "-");
		currentTime.append((today.month+1) + "-");
		currentTime.append(today.monthDay + " ");
		currentTime.append(today.hour + ":");
		currentTime.append(today.minute + ":");
		currentTime.append(today.second + "");
		return currentTime.toString();
	}
	
	public static String getTimeDate() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		StringBuilder currentTime = new StringBuilder();
		currentTime.append(today.hour + ":");
		currentTime.append(today.minute + ":");
		currentTime.append(today.second + " ");
		currentTime.append(today.year + "-");
		currentTime.append((today.month+1) + "-");
		currentTime.append(today.monthDay);
		return currentTime.toString();
	}
	
	public static String getDate() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		StringBuilder sb = new StringBuilder();		
		sb.append(today.year + "-");
		sb.append((today.month+1) + "-");
		sb.append(today.monthDay);
		return sb.toString();
	}
	
	public static String getTime() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		StringBuilder sb = new StringBuilder();		
		sb.append(today.hour + ":");
		sb.append(today.minute + ":");
		sb.append(today.second);
		return sb.toString();
	}
	
//	public static String getCalendarDateTime() {
//		Calendar cal = Calendar.getInstance();
//		StringBuilder sb = new StringBuilder();
//		sb.append(cal.get(Calendar.HOUR) + ":");
//		sb.append(cal.get(Calendar.MINUTE) + ":");
//		sb.append(cal.get(Calendar.SECOND) + "-");
//		sb.append(cal.get(Calendar.DAY_OF_MONTH) + " / ");
//		sb.append(cal.get(Calendar.MONTH+1) + " / ");
//		sb.append(cal.get(Calendar.YEAR));
//		return sb.toString();
//	}
//	
//	public static String getCalendarDate() {
//		Calendar calendar = Calendar.getInstance();
//		StringBuilder sb = new StringBuilder();
//		sb.append(calendar.get(Calendar.DAY_OF_MONTH) + "/");
//		sb.append(calendar.get(Calendar.MONTH) + "/");
//		sb.append(calendar.get(Calendar.YEAR));
//		return sb.toString();
//	}
//	
//	public static String getCalendarTime() {
//		Calendar calendar = Calendar.getInstance();
//		StringBuilder sb = new StringBuilder();
//		sb.append(calendar.get(Calendar.HOUR) + ":");
//		sb.append(calendar.get(Calendar.MINUTE) + ":");
//		sb.append(calendar.get(Calendar.SECOND));
//		return sb.toString();
//	}
}
