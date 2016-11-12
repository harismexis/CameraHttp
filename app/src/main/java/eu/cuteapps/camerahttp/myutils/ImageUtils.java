package eu.cuteapps.camerahttp.myutils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.TypedValue;

public class ImageUtils {

  // Encodes an image to a String
  public static String encodeImageToString(String path) {
    Bitmap bitmap = BitmapFactory.decodeFile(path);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
    byte[] ba = baos.toByteArray();
    String imageString = null;
    try {
      imageString = Base64.encodeToString(ba, Base64.DEFAULT);
    } catch(Exception e) {
      return null;
    }
    return imageString;
  }

  // Encodes a bitmap to a String
  public static String encodeBitmapToString(Bitmap bitmap) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
    byte[] ba = baos.toByteArray();
    try {
      return Base64.encodeToString(ba, Base64.DEFAULT);
    } catch(Exception e) {
      return null;
    }
  }

  // Converts the dps to pixels, based on density scale
//  public static int getPixels(float scale, int dps) {
//    int pixels = (int) (dps * scale + 0.5f);
//    return pixels;
//  }

  public static int getPixelsFromDps(int dps, Context context) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        dps, context.getResources().getDisplayMetrics());
  }

  public static Bitmap loadPrescaledBitmap(String filename) throws IOException {
    // Facebook image size
    final int IMAGE_MAX_SIZE = 630;

    File file = null;
    FileInputStream fis;

    BitmapFactory.Options opts;
    int resizeScale;
    Bitmap bmp;

    file = new File(filename);

    // This bit determines only the width/height of the bitmap without loading the contents
    opts = new BitmapFactory.Options();
    opts.inJustDecodeBounds = true;
    fis = new FileInputStream(file);
    BitmapFactory.decodeStream(fis, null, opts);
    fis.close();

    // Find the correct scale value. It should be a power of 2
    resizeScale = 1;

    if(opts.outHeight > IMAGE_MAX_SIZE || opts.outWidth > IMAGE_MAX_SIZE) {
      resizeScale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(opts.outHeight, opts.outWidth)) / Math.log(0.5)));
    }

    // Load pre-scaled bitmap
    opts = new BitmapFactory.Options();
    opts.inSampleSize = resizeScale;
    fis = new FileInputStream(file);
    bmp = BitmapFactory.decodeStream(fis, null, opts);

    fis.close();

    return bmp;
  }

}