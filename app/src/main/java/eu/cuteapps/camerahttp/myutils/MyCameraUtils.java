package eu.cuteapps.camerahttp.myutils;

import android.hardware.Camera;
import android.util.Log;

public class MyCameraUtils {

    public static Camera getCameraInstance() {
        Camera camera = null;
        try { 
        	camera = Camera.open();
        }
        catch (Exception e) {}
        return camera;
    }
    
    public static void closeTheCamera(Camera camera) {
    	if(camera != null) {
    		camera.release();
    		camera = null;
    	}
    }
    
    
}
