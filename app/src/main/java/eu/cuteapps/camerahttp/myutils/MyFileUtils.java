package eu.cuteapps.camerahttp.myutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class MyFileUtils {
	
	public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_AUDIO = 3;
	
//    // Creates a File for saving an image
//    public static File getOutputMediaFile(String folderName) {
//    	File mediaStorageDir = new File(
//        		Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), 
//        		folderName);
//        
//        if (! mediaStorageDir.exists()) {
//            if (! mediaStorageDir.mkdirs()) {
//                Log.d("MyCameraApp", "failed to create directory");
//                return null;
//            }
//        }
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
//        return mediaFile;
//    }

    // Creates a file Uri for saving an image or video
    public static Uri getOutputMediaFileUri(int type, String folderName){
          return Uri.fromFile(getOutputMediaFile(type, folderName));
    }
    
    // Creates a File for saving image / video / audio
    public static File getOutputMediaFile(int type, String folderName) {
    	
        /*	To be safe, you should check that the SDCard is mounted using 
    		Environment.getExternalStorageState() before doing this. */
        File mediaStorageDir = new File(
        		Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName);
        
        if ( !mediaStorageDir.exists() ) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(folderName, "failed to create directory");
                return null;
            }
        }
                
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
        } 
        else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4");
        }
        else if(type == MEDIA_TYPE_AUDIO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "AUD_"+ timeStamp + ".3gp");
        } 
        else {
            return null;
        }
        return mediaFile;
    }
    
    public static boolean fileIsImage(String fileName) {
    	if(fileName == null) {
    		return false;
    	}
    	if(fileName.endsWith(".jpg") || fileName.endsWith(".JPG") || 
    		fileName.endsWith(".png") || fileName.endsWith(".PNG")) {
    		return true;
    	}
    	return false;
    }
    
    public static boolean fileIsVideo(String fileName) {
    	if(fileName == null) {
    		return false;
    	}
    	if(fileName.endsWith(".mp4") || fileName.endsWith(".MP4")) {
    		return true;
    	}
    	return false;
    }
    
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }
        return sb.toString();
    }
    
    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        fin.close(); // Make sure you close all streams.        
        return ret;
    }
}