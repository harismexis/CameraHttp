package eu.cuteapps.camerahttp.myutils;

import android.hardware.Camera;

import java.util.List;

public class CameraUtils {

  public static boolean cameraSupportsFlashMode(Camera camera, String mode) {
    final List<String> flashModes = camera.getParameters().getSupportedFlashModes();
    if(flashModes == null || flashModes.size() == 0) {
      return false;
    }
    for(int i = 0; i < flashModes.size(); i++) {
      if(flashModes.get(i).equals(mode)) {
        return true;
      }
    }
    return false;
  }

}
