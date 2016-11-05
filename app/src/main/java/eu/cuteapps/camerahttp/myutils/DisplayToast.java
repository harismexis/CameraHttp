package eu.cuteapps.camerahttp.myutils;

import android.content.Context;
import android.widget.Toast;

// Private class for displaying toasts in onHandleIntent()
public class DisplayToast implements Runnable {
	
	private Context context = null;
	private String toastMessage = null;
	private int duration;

	public DisplayToast(Context context, String text, int duration) {
		this.context = context;
		this.toastMessage = text;
		this.duration = duration;
	}

	@Override
	public void run() {
		Toast.makeText(context, this.toastMessage, this.duration).show();
	}
	
}
