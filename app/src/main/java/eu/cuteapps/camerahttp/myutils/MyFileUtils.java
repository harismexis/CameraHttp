package eu.cuteapps.camerahttp.myutils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class MyFileUtils {

  public static final int MEDIA_TYPE_IMAGE = 1;
  public static final int MEDIA_TYPE_VIDEO = 2;
  public static final int MEDIA_TYPE_AUDIO = 3;

  public static File getOutputMediaFile(int type, String folderName) {
    File mediaStorageDir = new File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName);
    if(!mediaStorageDir.exists()) {
      if(!mediaStorageDir.mkdirs()) {
        Log.d(folderName, "failed to create directory");
        return null;
      }
    }
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File mediaFile;
    if(type == MEDIA_TYPE_IMAGE) {
      mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    } else if(type == MEDIA_TYPE_VIDEO) {
      mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
    } else if(type == MEDIA_TYPE_AUDIO) {
      mediaFile = new File(mediaStorageDir.getPath() + File.separator + "AUD_" + timeStamp + ".3gp");
    } else {
      return null;
    }
    return mediaFile;
  }

  public static boolean fileIsImage(String fileName) {
    return fileName != null && (fileName.endsWith(".jpg") || fileName.endsWith(".JPG") ||
        fileName.endsWith(".png") || fileName.endsWith(".PNG"));
  }

  public static boolean fileIsVideo(String fileName) {
    return fileName != null && (fileName.endsWith(".mp4") || fileName.endsWith(".MP4"));
  }
}