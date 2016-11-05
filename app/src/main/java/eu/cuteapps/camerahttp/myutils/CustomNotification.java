package eu.cuteapps.camerahttp.myutils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class CustomNotification {
	
	private int iconId;
	private CharSequence tickerText;
	private Context context;
	private CharSequence contentTitle;
	private CharSequence contentText;
	private Intent notificationIntent;
	private int ID;
	
	public CustomNotification(int iconId,  CharSequence tickerText, 
			Context context, CharSequence contentTitle, 
			CharSequence contentText, Intent notificationIntent, int ID) {		
		this.iconId = iconId;
		this.tickerText = tickerText;
		this.context = context;
		this.contentTitle = contentTitle;
		this.contentText = contentText;
		this.notificationIntent = notificationIntent;
		this.ID = ID;
	}
	
//	public void sendStatusNotification() {
//		NotificationManager notificationManager = 
//				(NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
//		long when = System.currentTimeMillis();
//		Notification notification = new Notification(this.iconId, this.tickerText, when);
//		PendingIntent contentIntent = PendingIntent.getActivity(this.context, 0, this.notificationIntent, 0);
//		notification.setLatestEventInfo(this.context, this.contentTitle, this.contentText, contentIntent);
//		notificationManager.notify(this.ID, notification);
//	}

}
