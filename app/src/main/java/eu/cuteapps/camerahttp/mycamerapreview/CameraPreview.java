package eu.cuteapps.camerahttp.mycamerapreview;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

  private static final String TAG = "CameraPreview";

  public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  private SurfaceHolder previewHolder;
  private Camera previewCamera;

  public CameraPreview(Context context, Camera camera) {
    super(context);
    previewCamera = camera;
    previewHolder = this.getHolder();
    previewHolder.addCallback(this);
    previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    try {
      previewCamera.setPreviewDisplay(holder);
      previewCamera.startPreview();
    } catch(Exception e) {
      Log.d(TAG, e.getMessage());
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {

  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    if(previewHolder.getSurface() == null) {
      return;
    }

    try {
      previewCamera.stopPreview();
    } catch(Exception e) {
      Log.w(TAG, e.getMessage());
    }

    try {
      previewCamera.setPreviewDisplay(previewHolder);
      previewCamera.startPreview();
    } catch(Exception e) {
      Log.d(TAG, e.getMessage());
    }
  }

}