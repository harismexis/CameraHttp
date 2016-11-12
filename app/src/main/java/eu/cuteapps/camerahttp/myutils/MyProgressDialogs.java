package eu.cuteapps.camerahttp.myutils;

import android.app.ProgressDialog;
import android.content.Context;

public class MyProgressDialogs {
	
	public static ProgressDialog getCircleProgressDialog(Context context, String message) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setCancelable(true);
		progressDialog.setMessage(message);
		progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
		return progressDialog;
	}
}
