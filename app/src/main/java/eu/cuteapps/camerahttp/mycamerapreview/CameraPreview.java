package eu.cuteapps.camerahttp.mycamerapreview;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	
		public CameraPreview(Context context, AttributeSet attrs, int defStyle) { 
			super(context, attrs, defStyle);
		}

		private SurfaceHolder previewHolder;
	    private Camera previewCamera;

	    public CameraPreview(Context context, Camera camera) {
	        super(context);
	        previewCamera = camera;
	        
	        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
	        previewHolder = this.getHolder();
	        previewHolder.addCallback(this);
	        
	        // deprecated setting, but required on Android versions prior to 3.0
	        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }

	    @Override
	    public void surfaceCreated(SurfaceHolder holder) {
	    	
	        // The Surface has been created, now tell the camera where to draw the preview.
	        try {
	            previewCamera.setPreviewDisplay(holder);
	            previewCamera.startPreview();
	        } 
//	        catch (IOException e) {
//	            // Log.d(TAG, "Error setting camera preview: " + e.getMessage());
//	        }
	        
	        catch (Exception e) {
	        	// Log.d(TAG, "Error setting camera preview: " + e.getMessage());
	        }
   
	    }

	    @Override
	    public void surfaceDestroyed(SurfaceHolder holder) {
	        // empty. Take care of releasing the Camera preview in your activity.
	    }

	    @Override
	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	        
	    	// If your preview can change or rotate, take care of those events here. Make sure to stop the preview before resizing or reformatting it.
	        
	    	if (previewHolder.getSurface() == null){
	          // preview surface does not exist
	          return;
	        }
	        
	        // stop preview before making changes
	    	
	        try {
	            previewCamera.stopPreview();
	            
	            // this.setBestPreviewSize(w, h); // update preview size
	        } 
	        catch (Exception e) {
	          // ignore: tried to stop a non-existent preview
	        }
	        
	        // set preview size and make any resize, rotate or reformatting changes here - start preview with new settings
	        
	        try {
	        	
	        	previewCamera.setPreviewDisplay(this.previewHolder);
	            
	            previewCamera.startPreview();
	        }
	        catch (Exception e){
	            //Log.d(TAG, "Error starting camera preview: " + e.getMessage());
	        }
	 }
	    
	private void setBestPreviewSize(int w, int h) {
		Camera.Parameters p = this.previewCamera.getParameters();
		Camera.Size myBestSize = this.getOptimalPreviewSize(p, w, h);
		if (myBestSize != null) {
			p.setPreviewSize(myBestSize.width, myBestSize.height);
			this.previewCamera.setParameters(p);
		}
	}
	    
	private Camera.Size getBestPreviewSize(Camera.Parameters params, int w, int h) {
		Camera.Size result = null;
		for (Camera.Size size : params.getSupportedPreviewSizes()) {
			if (size.width <= w && size.height <= h) {
				if (null == result)
					result = size;
				else {
					int resultDelta = w - result.width + h - result.height;
					int newDelta = w - size.width + h - size.height;
					if (newDelta < resultDelta)
						result = size;
				}
			}
		}
		return result;
	}
	    
	private Camera.Size getOptimalPreviewSize(Camera.Parameters params, int w, int h) {

		List<Camera.Size> sizes = params.getSupportedPreviewSizes();
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) h / w;

		if (sizes == null)
			return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = h;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}
	    
}