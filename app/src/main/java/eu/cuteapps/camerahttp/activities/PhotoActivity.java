package eu.cuteapps.camerahttp.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.OutputFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Video.Thumbnails;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import eu.cuteapps.camerahttp.CameraPreview;
import eu.cuteapps.camerahttp.R;
import eu.cuteapps.camerahttp.constants.Prefs;
import eu.cuteapps.camerahttp.myadapters.CapturesAdapter;
import eu.cuteapps.camerahttp.mysqlite.Capture;
import eu.cuteapps.camerahttp.mysqlite.MySQLiteCapturesDataSource;
import eu.cuteapps.camerahttp.myutils.ImageUtils;
import eu.cuteapps.camerahttp.myutils.LocationUtils;
import eu.cuteapps.camerahttp.myutils.MyFileUtils;
import eu.cuteapps.camerahttp.myutils.MyProgressDialogs;
import eu.cuteapps.camerahttp.myutils.NetUtils;

public class PhotoActivity extends Activity implements ConnectionCallbacks,
    OnConnectionFailedListener, LocationListener {

  /* prefs flash */
  static final int VIDEO_CAMERA_FLASH_MODE_ON = 1;
  static final int VIDEO_CAMERA_FLASH_MODE_OFF = 0;

  private int videoCameraFlashMode = VIDEO_CAMERA_FLASH_MODE_OFF;

  static final String CAMERA_PREVIEW_BUSY_MSG = "Camera is busy !";

  public static final String ACTION_PERIODIC_CAPTURE = "mine.app.sendpictures.actionperiodiccapture";
  private static final String MEDIA_FOLDER_NAME = "NetPicsMediaStorage";
  private int periodicCaptureInterval;

  private SendCaptureViaHttpTask sendCaptureViaHttpTask;
  private ProgressDialog sendCaptureViaHttpProgressDialog;
  private HttpPost httpPost;

  private float density;
  private AudioManager audioManager;
  private LinearLayout leftControlsLayout;
  private LinearLayout activityLayout;
  private int delayAfterCapture = 1000;
  private boolean isShutterSoundEnabled = true;
  private File lastCapturedMediaFile;

  private Location mLastLocation;
  private GoogleApiClient mGoogleApiClient;

	/* -- camera parameters -- */

  /* flash */
  private boolean isFlashModeSupported = false;
  private boolean isFlashModeAUTOSupported = false;
  private boolean isFlashModeONSupported = false;
  private boolean isFlashModeOFFSupported = false;
  private boolean isFlashOn = false;

  /* zoom */
  private SeekBar zoomBar;
  private LinearLayout mZoomBarLayout;
  private TextView minZoomTextView;
  private TextView maxZoomTextView;
  private int defaultZoom;
  private int maxZoom;

  /* exposure compensation */
  private SeekBar brightnessBar;
  private LinearLayout mBrightnessBarLayout;
  private TextView minBrightnessTextView;
  private TextView maxBrightnessTextView;
  private int maxExposureCompensation;
  private int minExposureCompensation;
  private int defaultExposureCompensation;

  /* white balance */
  private ListView mListViewWhiteBalance;
  private List<String> mSupportedWhiteBalanceList;
  private String defaultWhiteBalance;

  /* color effects */
  private ListView mListViewColorEffects;
  private List<String> mSupportedColorEffectsList;
  private String defaultColorEffect;

  /* scene mode */
  private ListView mListViewSceneModes;
  private List<String> mSupportedSceneModesList;
  private String defaultSceneMode;

  /* picture size */
  private ListView mListViewPictureSizes;
  private List<Size> mSupportedPictureSizesList;
  private ArrayList<String> mPictureSizes;
  private Size defaultPictureSize;
  private Size selectedPictureSize;

  /* video size */
  private ListView mListViewVideoSizes;
  private List<Size> mSupportedVideoSizesList;
  private ArrayList<String> mVideoSizes;
  private Size defaultVideoSize;
  private Size selectedVideoSize;

  /* boolean vars to check which parameters are supported */
  private boolean isZoomSupported = false;
  private boolean isExposureCompensationSupported = false;
  private boolean isSceneModeSupported = false;
  private boolean isWhiteBalanceSupported = false;
  private boolean isColorEffectSupported = false;
  private boolean isPictureSizeSupported = false;
  private boolean isVideoSizeSupported = false;
  private boolean isFrontCameraSupported = false;

  private boolean isFacingBackCamera = true;

  private ImageButton btnZoom;
  private ImageButton btnReverse;

  /* audio capture */
  private ImageButton btnAudioCapture;
  private boolean isAudioRecording = false;

  /* camera / preview / take picture */
  private ImageButton buttonTakePicture;
  private Camera mCamera;
  private CameraPreview mPreview;
  private FrameLayout frameLayout;
  private boolean isCapturingPhoto = false;
  private ImageView mImageView;
  private CapturePhotoTask capturePhotoTask;
  private ProgressDialog cameraProgressDialog;
  private int thumbNailTargetWidth;
  private int thumbNailTargetHeight;

  /* periodic capture */
  private AlarmManager alarmManager;
  private Intent periodicCaptureIntent;
  private PendingIntent periodicCapturePendingIntent;
  private boolean isPeriodicCaptureOn = false;
  private int counterPeriodicCapture = 0;
  private boolean infinitePeriodicCapture = false;
  private ImageButton periodicCaptureButton;

  /* video capture */
  private ImageButton videoButton;
  private MediaRecorder mMediaRecorder;
  private boolean isVideoRecording = false;
  private boolean isVideoCameraMode = false;
  private ImageButton switchPhotoVideoBtn;

  /* ListView with captured places */
  private ListView mListViewCaptures;
  private MySQLiteCapturesDataSource datasource;
  private ArrayList<Capture> allCaptures;
  private Capture selectedPlace;
  private CapturesAdapter capturesAdapter;

  private boolean restoreCameraEffectsInOnResume = false;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_photo);

    PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

		/* Create a GoogleApiClient instance */
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();

    datasource = new MySQLiteCapturesDataSource(this);
    datasource.open();

    density = getResources().getDisplayMetrics().density;
    alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    activityLayout = (LinearLayout) this.findViewById(R.id.activity_photo_layout);
    leftControlsLayout = (LinearLayout) this.findViewById(R.id.activity_photo_left_controls);
    frameLayout = (FrameLayout) findViewById(R.id.activity_photo_preview);
		
		/* Remove all Views when screen is touched */
    frameLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        removeSettingsViews();
      }
    });
		
		/* Progress Dialog appearing on photo capture */
    cameraProgressDialog = MyProgressDialogs.getCircleProgressDialog(this, "Capturing photo...");
    cameraProgressDialog.setCancelable(false);
		
		/* Progress Dialog when data is sent via HTTP */
    sendCaptureViaHttpProgressDialog = MyProgressDialogs.getCircleProgressDialog(this,
        "Sending Capture via http...");
    sendCaptureViaHttpProgressDialog.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        if(httpPost != null) {
          httpPost.abort();
        }
        if(sendCaptureViaHttpTask != null &&
            sendCaptureViaHttpTask.getStatus() != AsyncTask.Status.FINISHED) {
          sendCaptureViaHttpTask.cancel(true);
        }
      }
    });
		
		/* ListView with places */
    mListViewCaptures = new ListView(this);
    allCaptures = datasource.getAllModels();
    capturesAdapter = new CapturesAdapter(this, allCaptures);
    mListViewCaptures.setAdapter(capturesAdapter);
    mListViewCaptures.setBackgroundColor(Color.parseColor("#91A9B9"));
    mListViewCaptures.setCacheColorHint(Color.parseColor("#91A9B9"));
    mListViewCaptures.setId(R.id.captures_list_view);
    registerForContextMenu(mListViewCaptures);

    btnZoom = (ImageButton) this.findViewById(R.id.activity_photo_button_zoom);
    btnReverse = (ImageButton) this.findViewById(R.id.activity_photo_button_reverse);
    btnAudioCapture = (ImageButton) this.findViewById(R.id.activity_photo_button_audio_capture);
		
		/* Button for switching camera between video and photo */
    switchPhotoVideoBtn = (ImageButton) this.findViewById(R.id.activity_photo_switch_video_photo_cam_btn);
    switchPhotoVideoBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
				/* Check if preview is busy */
        if(isPreviewBusy()) {
          Toast.makeText(PhotoActivity.this, CAMERA_PREVIEW_BUSY_MSG, Toast.LENGTH_SHORT).show();
          return;
        }

        try {
					
					/* Save Flash Mode of previews mode (photo - video) */
          saveFlashMode();
					
					/* Close camera and preview */
          closeCameraAndPreview();
					
					/* Set default values to camera settings views */
          resetCameraSettingsViews();
					
					/* Switch to photo camera mode */
          if(isVideoCameraMode) {
            switchPhotoVideoBtn.setImageResource(R.mipmap.switch_video_cam);
            periodicCaptureButton.setVisibility(View.VISIBLE);
            videoButton.setVisibility(View.GONE);
            buttonTakePicture.setVisibility(View.VISIBLE);
          }
					/* Switch to video camera mode */
          else {
            switchPhotoVideoBtn.setImageResource(R.mipmap.switch_photo_cam);
            periodicCaptureButton.setVisibility(View.GONE);
            videoButton.setVisibility(View.VISIBLE);
            buttonTakePicture.setVisibility(View.GONE);
          }
					
					/* Change Camera Mode (Photo / Video) */
          isVideoCameraMode = !isVideoCameraMode;
					
					/* Init camera (BACK / FRONT) */
          if(isFacingBackCamera) {
            mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
          } else {
            mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
          }
					
					/* Init Camera Preview */
          initCameraPreview();
					
					/* Update Video / Picture size */
          if(isVideoCameraMode) {
						
						/* Update Video Size ListView */
            final String selectedVideoSizeToString = String.valueOf(selectedVideoSize.width) +
                " x " + String.valueOf(selectedVideoSize.height);
            mListViewVideoSizes.setItemChecked(mVideoSizes.indexOf(selectedVideoSizeToString), true);
          } else {
						/* Update Camera Picture Size */
            mCamera.stopPreview();
            Parameters params = mCamera.getParameters();
            params.setPictureSize(selectedPictureSize.width, selectedPictureSize.height);
            mCamera.setParameters(params);
            mCamera.startPreview();
						
						/* Update Picture Size ListView */
            final String selectedPictureSizeToString = String.valueOf(selectedPictureSize.width) + " x " + String.valueOf(selectedPictureSize.height);
            mListViewPictureSizes.setItemChecked(mPictureSizes.indexOf(selectedPictureSizeToString), true);
          }
					
					/* Update Flash Mode */
          if(!isVideoCameraMode && isFlashModeSupported) {
            final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PhotoActivity.this);
            mCamera.stopPreview();
            Parameters params = mCamera.getParameters();
            params.setFlashMode(settings.getString(Prefs.PREF_PHOTO_CAMERA_FLASH_MODE,
                Parameters.FLASH_MODE_OFF));
            mCamera.setParameters(params);
            mCamera.startPreview();
          }

        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, "Error switching photo / video !",
              Toast.LENGTH_SHORT).show();
        }
      }
    });
		
		/* Button for taking photo */
    buttonTakePicture = (ImageButton) this.findViewById(R.id.activity_photo_button_capture);
		
		/* Periodic photo capture */
    periodicCaptureButton = (ImageButton) findViewById(R.id.activity_photo_button_periodic_capture);
    periodicCaptureIntent = new Intent(PhotoActivity.ACTION_PERIODIC_CAPTURE);
    periodicCapturePendingIntent = PendingIntent.getBroadcast(this, 1, periodicCaptureIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		/* Button for capturing video */
    videoButton = (ImageButton) findViewById(R.id.activity_photo_button_video);
    videoButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if(isVideoRecording) {
          stopVideoRecording();
        } else if(isPreviewBusy()) {
          Toast.makeText(PhotoActivity.this, CAMERA_PREVIEW_BUSY_MSG, Toast.LENGTH_SHORT).show();
        } else {
          removeSettingsViews();
          CamcorderProfile camcorderProfile = null;
          try {

            if(isFacingBackCamera && videoCameraFlashMode == VIDEO_CAMERA_FLASH_MODE_ON) {
              mCamera.stopPreview();
              Parameters params = mCamera.getParameters();
              params.setFlashMode(Parameters.FLASH_MODE_TORCH);
              mCamera.setParameters(params);
              mCamera.startPreview();
            }

            mMediaRecorder = new MediaRecorder();
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
	        	 	    
            /* Back camera */
            if(isFacingBackCamera) {
              mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
              mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
              camcorderProfile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);
              mMediaRecorder.setProfile(camcorderProfile);
            }
            /* Front Camera */
            else {
              mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
              mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
              camcorderProfile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_FRONT,
                  CamcorderProfile.QUALITY_480P);
              mMediaRecorder.setProfile(camcorderProfile);
//	        	 	    	mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//	        	 	    	mMediaRecorder.setAudioEncoder(AudioEncoder.DEFAULT);
//	        	 	    	mMediaRecorder.setVideoEncoder(VideoEncoder.DEFAULT);
              mMediaRecorder.setVideoFrameRate(10);
            }

            mMediaRecorder.setVideoSize(selectedVideoSize.width, selectedVideoSize.height);

            lastCapturedMediaFile = MyFileUtils.getOutputMediaFile(MyFileUtils.MEDIA_TYPE_VIDEO,
                PhotoActivity.MEDIA_FOLDER_NAME);
            mMediaRecorder.setOutputFile(lastCapturedMediaFile.toString());
	        	 	    
	        	 	    /* Add video to gallery */
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(lastCapturedMediaFile)));
	        	 	    
	        	 	    /* Start Video Recording */
            mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
          } catch(Exception e) {
            releaseMediaRecorder();
            Toast.makeText(PhotoActivity.this, "Error recording video !", Toast.LENGTH_SHORT).show();
            return;
          }

//	            	Toast.makeText(PhotoActivity.this, 
//        	 	    		String.valueOf(camcorderProfile.videoFrameWidth) + " x " + String.valueOf(camcorderProfile.videoFrameHeight), 
//        	 	    		Toast.LENGTH_SHORT).show();

          videoButton.setImageResource(R.mipmap.stop);
          isVideoRecording = true;
	        		
	         	    /* Update current capture */
          new UpdatePositionInfoAndCaptureTask().execute(Capture.TYPE_VIDEO);
        }
      }
    });
		
		/* Thumnail icon */
    mImageView = (ImageView) findViewById(R.id.activity_photo_camera_thumbnail);
    mImageView.setScaleType(ScaleType.FIT_XY);
    thumbNailTargetWidth = (int) this.getResources().getDimension(R.dimen.thumbnail_imageview_width);
    thumbNailTargetHeight = (int) this.getResources().getDimension(R.dimen.thumbnail_imageview_height);
    mImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if(lastCapturedMediaFile == null) {
          Toast.makeText(PhotoActivity.this, "No captured media file found !",
              Toast.LENGTH_SHORT).show();
          return;
        }
        if(isPreviewBusy()) {
          Toast.makeText(PhotoActivity.this, CAMERA_PREVIEW_BUSY_MSG, Toast.LENGTH_SHORT).show();
          return;
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        if(MyFileUtils.fileIsImage(lastCapturedMediaFile.getName())) {
          intent.setDataAndType(Uri.fromFile(lastCapturedMediaFile), "image/*");
        } else if(MyFileUtils.fileIsVideo(lastCapturedMediaFile.getName())) {
          intent.setDataAndType(Uri.fromFile(lastCapturedMediaFile), "video/*");
        } else if(lastCapturedMediaFile.getName().endsWith(".3gp")) {
          intent.setDataAndType(Uri.fromFile(lastCapturedMediaFile), "audio/*");
        } else {
          Toast.makeText(PhotoActivity.this, "Unknown file type !", Toast.LENGTH_SHORT).show();
          return;
        }
        if(intent.resolveActivity(PhotoActivity.this.getPackageManager()) == null) {
          Toast.makeText(PhotoActivity.this, "Unable to complete this action !",
              Toast.LENGTH_SHORT).show();
          return;
        }
        startActivity(intent);
        return;
      }
    });
		
		/* Create Views with camera settings */
    createCameraSettingsViews();
		
		/* Check if hardware supports front camera */
    Camera c = null;
    try {
      c = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
      if(c == null) {
        isFrontCameraSupported = false;
        btnReverse.setEnabled(false);
      } else {
        isFrontCameraSupported = true;
        btnReverse.setEnabled(true);
      }
    } catch(Exception e) {
      isFrontCameraSupported = false;
      btnReverse.setEnabled(false);
      Toast.makeText(this, "Error checking for FRONT camera!",
          Toast.LENGTH_LONG).show();
    } finally {
      if(c != null) {
        c.release();
      }
    }
		
		/* Get last used camera to read its parameters */
    if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("back_camera", true)) {
      c = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
    } else {
      c = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }
		
		/* Read parameters of last used camera (front / back) */
    readCameraSettingsAndSetUpSettingsViews(c);
		
		/* Release Camera */
    c.release();
		
		/* Check if camera was in video camera mode so as to update buttons */
    isVideoCameraMode = PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean("video_camera_mode", false);
    if(isVideoCameraMode) {
      switchPhotoVideoBtn.setImageResource(R.mipmap.switch_photo_cam);
      buttonTakePicture.setVisibility(View.GONE);
      periodicCaptureButton.setVisibility(View.GONE);
      videoButton.setVisibility(View.VISIBLE);
    }
  }

  /* Saves Flash Mode */
  private void saveFlashMode() {
    if(!isFlashModeSupported) {
      return;
    }
    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = settings.edit();
    if(isVideoCameraMode) {
      editor.putInt(Prefs.PREF_VIDEO_CAMERA_FLASH_MODE, videoCameraFlashMode);
    } else {
      editor.putString(Prefs.PREF_PHOTO_CAMERA_FLASH_MODE, mCamera.getParameters().getFlashMode());
    }
    editor.commit();
  }

  /* Restores Flash Mode */
  private void restoreFlashMode() {
    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    videoCameraFlashMode = settings.getInt(Prefs.PREF_VIDEO_CAMERA_FLASH_MODE,
        VIDEO_CAMERA_FLASH_MODE_OFF);
    if(!isVideoCameraMode && isFlashModeSupported) {
      Parameters params = mCamera.getParameters();
      params.setFlashMode(settings.getString(Prefs.PREF_PHOTO_CAMERA_FLASH_MODE,
          Parameters.FLASH_MODE_OFF));
      mCamera.setParameters(params);
    }
  }

  /* Creates the Views for camera settings */
  private void createCameraSettingsViews() {

    final LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);
		
		/* -- Zoom Bar -- */
    mZoomBarLayout = (LinearLayout) layoutInflater.inflate(R.layout.zoom_layout, null, false);
    mZoomBarLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {

      }
    });
    zoomBar = (SeekBar) mZoomBarLayout.findViewById(R.id.the_zoom_bar);
    minZoomTextView = (TextView) mZoomBarLayout.findViewById(R.id.min_zoom_text_view);
    maxZoomTextView = (TextView) mZoomBarLayout.findViewById(R.id.max_zoom_text_view);
    minZoomTextView.setText(String.valueOf(0));
    zoomBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      int progressChanged = 0;

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progressChanged = progress;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        mCamera.stopPreview();
        Parameters p = mCamera.getParameters();
        p.setZoom(progressChanged);
        mCamera.setParameters(p);
        mCamera.startPreview();
      }
    });
		
		/* -- Brightness Bar -- */
    mBrightnessBarLayout = (LinearLayout) layoutInflater.inflate(R.layout.brightness_bar_layout,
        null, false);
    mBrightnessBarLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {

      }
    });
    brightnessBar = (SeekBar) mBrightnessBarLayout.findViewById(R.id.the_brightness_bar);
    minBrightnessTextView = (TextView) mBrightnessBarLayout.findViewById(R.id.min_bar_text_view);
    maxBrightnessTextView = (TextView) mBrightnessBarLayout.findViewById(R.id.max_bar_text_view);
    brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      int progressChanged = 0;

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progressChanged = progress;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        int index = progressChanged - Math.abs(minExposureCompensation);
        mCamera.stopPreview();
        Parameters p = mCamera.getParameters();
        p.setExposureCompensation(index);
        mCamera.setParameters(p);
        mCamera.startPreview();
      }
    });
		
		/* -- ListView for Scene Modes -- */
    mListViewSceneModes = new ListView(this);
    mSupportedSceneModesList = new ArrayList<>();
    final ArrayAdapter<String> sceneModesAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_single_choice, mSupportedSceneModesList);
    mListViewSceneModes.setAdapter(sceneModesAdapter);
    mListViewSceneModes.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    mListViewSceneModes.setBackgroundColor(Color.GRAY);
    mListViewSceneModes.setCacheColorHint(Color.GRAY);
    mListViewSceneModes.setSelector(getResources().getDrawable(R.drawable.listselector));
    mListViewSceneModes.setId(R.id.scene_modes_list_view);
    mListViewSceneModes.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
        String selectedSceneMode = mSupportedSceneModesList.get(position);
        try {
          String userMessage = "";
          mCamera.stopPreview();
          Parameters p = mCamera.getParameters();
					
					/* 1. Set color effect and white balance to default */
          String previousWhiteBalance = "";
          if(isWhiteBalanceSupported) {
            previousWhiteBalance = p.getWhiteBalance();
            // p.setWhiteBalance(defaultWhiteBalance);
            // mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.
            // indexOf(defaultWhiteBalance), true);
          }

          String previousColorEffect = "";
          if(isColorEffectSupported) {
            previousColorEffect = p.getColorEffect();
            p.setColorEffect(defaultColorEffect);
            if(!previousColorEffect.equals(defaultColorEffect)) {
              mListViewColorEffects.setItemChecked(mSupportedColorEffectsList
                  .indexOf(defaultColorEffect), true);
              userMessage += "Color Effect changed from " + previousColorEffect +
                  " to " + defaultColorEffect + "\n\n";
            }
          }

          mCamera.setParameters(p);
					
					/* 2. Set scene mode */
          Parameters params = mCamera.getParameters();
          params.setSceneMode(selectedSceneMode);

          mCamera.setParameters(params);
          mCamera.startPreview();
					
					/* 3. Update listViews ( check if white balance changed to update list view ) */
          if(isWhiteBalanceSupported) {
            final String newWhiteBalance = mCamera.getParameters().getWhiteBalance();
            if(!previousWhiteBalance.equals(newWhiteBalance)) {
              mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.indexOf(newWhiteBalance),
                  true);
              userMessage += "White Balance changed from " + previousWhiteBalance +
                  " to " + newWhiteBalance;
            }
          }
					
					/* 4. Notify user for affected parameters */
          if(!userMessage.equals("")) {
            Toast.makeText(PhotoActivity.this, userMessage, Toast.LENGTH_LONG).show();
          }
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, "Error setting scene mode!", Toast.LENGTH_SHORT).show();
        }
      }
    });
		
		/* -- ListView White Balance -- */
    mListViewWhiteBalance = new ListView(this);
    mSupportedWhiteBalanceList = new ArrayList<>();
    final ArrayAdapter<String> whiteBalanceAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_single_choice, mSupportedWhiteBalanceList);
    mListViewWhiteBalance.setAdapter(whiteBalanceAdapter);
    mListViewWhiteBalance.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    mListViewWhiteBalance.setBackgroundColor(Color.GRAY);
    mListViewWhiteBalance.setCacheColorHint(Color.GRAY);
    mListViewWhiteBalance.setSelector(getResources().getDrawable(R.drawable.listselector));
    mListViewWhiteBalance.setId(R.id.white_balance_list_view);
    mListViewWhiteBalance.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
        String selectedWhiteBalance = mSupportedWhiteBalanceList.get(position);
        try {
          String userMessage = "";
          mCamera.stopPreview();
          Parameters p = mCamera.getParameters();

          // 1. Set scene mode to default
          if(isSceneModeSupported) {
            final String prevSceneMode = p.getSceneMode();
            if(!prevSceneMode.equals(defaultSceneMode)) {
              p.setSceneMode(defaultSceneMode);
              mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(defaultSceneMode),
                  true);
              userMessage = "Scene Mode changed from " + prevSceneMode +
                  " to " + defaultSceneMode + "\n\n";
            }
          }

          // 2. Set color effect to default
          if(isColorEffectSupported) {
            String previousColorEffect = p.getColorEffect();
            if(!previousColorEffect.equals(defaultColorEffect)) {
              p.setColorEffect(defaultColorEffect);
              mListViewColorEffects.setItemChecked(mSupportedColorEffectsList.
                  indexOf(defaultColorEffect), true);
              userMessage += "Color effect changed from " + previousColorEffect +
                  " to " + defaultColorEffect;
            }
          }

          mCamera.setParameters(p);

          // 3. Now set selected white balance
          Parameters params = mCamera.getParameters();
          params.setWhiteBalance(selectedWhiteBalance);

          mCamera.setParameters(params);
          mCamera.startPreview();

          // 4. Alert user for affected parameters
          if(!userMessage.equals("")) {
            Toast.makeText(PhotoActivity.this, userMessage, Toast.LENGTH_LONG).show();
          }
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, "Error setting White Balance!",
              Toast.LENGTH_SHORT).show();
        }
      }
    });
		
		/* -- ListView Color Effects -- */
    mListViewColorEffects = new ListView(this);
    mSupportedColorEffectsList = new ArrayList<>();
    final ArrayAdapter<String> colorEffectsAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_single_choice, mSupportedColorEffectsList);
    mListViewColorEffects.setAdapter(colorEffectsAdapter);
    mListViewColorEffects.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    mListViewColorEffects.setBackgroundColor(Color.GRAY);
    mListViewColorEffects.setCacheColorHint(Color.GRAY);
    mListViewColorEffects.setSelector(getResources().getDrawable(R.drawable.listselector));
    mListViewColorEffects.setId(R.id.color_effects_list_view);
    mListViewColorEffects.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
        String selectedColorEffect = mSupportedColorEffectsList.get(position);
        try {
          String userMessage = "";
          mCamera.stopPreview();
          Parameters p = mCamera.getParameters();

          // 1. First set scene mode to default - better not to set it after setting color effect
          if(isSceneModeSupported) {
            final String prevSceneMode = p.getSceneMode();
            if(!prevSceneMode.equals(defaultSceneMode)) {
              p.setSceneMode(defaultSceneMode);
              mListViewSceneModes.setItemChecked(mSupportedSceneModesList.
                  indexOf(defaultSceneMode), true);
              userMessage += "Scene Mode changed from " + prevSceneMode +
                  " to " + defaultSceneMode + "\n\n";
            }
          }

          // 2. Set white balance to default
          if(isWhiteBalanceSupported) {
            final String prevWhiteBalance = p.getWhiteBalance();
            if(!prevWhiteBalance.equals(defaultWhiteBalance)) {
              p.setWhiteBalance(defaultWhiteBalance);
              mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.
                  indexOf(defaultWhiteBalance), true);
              userMessage += "White Balance changed from " + prevWhiteBalance +
                  " to " + defaultWhiteBalance;
            }
          }

          mCamera.setParameters(p);

          // 3. Now set selected color effect - ONLY this way worked!
          Parameters params = mCamera.getParameters();
          params.setColorEffect(selectedColorEffect);

          mCamera.setParameters(params);
          mCamera.startPreview();

          // 4. Alert user for affected parameters
          if(!userMessage.equals("")) {
            Toast.makeText(PhotoActivity.this, userMessage, Toast.LENGTH_LONG).show();
          }
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, "Error setting Color Effect!",
              Toast.LENGTH_LONG).show();
        }
      }
    });

		/* -- ListView Picture Sizes -- */
    mListViewPictureSizes = new ListView(this);
    mSupportedPictureSizesList = new ArrayList<>();
    mPictureSizes = new ArrayList<>();
    final ArrayAdapter<String> pictureSizesAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_single_choice, mPictureSizes);
    mListViewPictureSizes.setAdapter(pictureSizesAdapter);
    mListViewPictureSizes.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    mListViewPictureSizes.setBackgroundColor(Color.GRAY);
    mListViewPictureSizes.setCacheColorHint(Color.GRAY);
    mListViewPictureSizes.setSelector(getResources().getDrawable(R.drawable.listselector));
    mListViewPictureSizes.setId(R.id.picture_sizes_list_view);
    mListViewPictureSizes.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
        final String selectedPictureSizeToString = mPictureSizes.get(position);
				/* Update selected Picture Size */
        try {
          for(int i = 0; i < mSupportedPictureSizesList.size(); i++) {
            final Size size = mSupportedPictureSizesList.get(i);
            final String sizeString = String.valueOf(size.width) + " x " + String.valueOf(size.height);
            if(sizeString.equals(selectedPictureSizeToString)) {
              selectedPictureSize = size;
              break;
            }
          }

          mCamera.stopPreview();
          Parameters p = mCamera.getParameters();
          p.setPictureSize(selectedPictureSize.width, selectedPictureSize.height);
          mCamera.setParameters(p);
          mCamera.startPreview();
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, "Error setting picture size !", Toast.LENGTH_SHORT).show();
        }
      }
    });
		
		/* ListView with Video Sizes */
    mListViewVideoSizes = new ListView(this);
    mSupportedVideoSizesList = new ArrayList<>();
    mVideoSizes = new ArrayList<>();
    final ArrayAdapter<String> videoSizesAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_single_choice, mVideoSizes);
    mListViewVideoSizes.setAdapter(videoSizesAdapter);
    mListViewVideoSizes.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    mListViewVideoSizes.setBackgroundColor(Color.GRAY);
    mListViewVideoSizes.setCacheColorHint(Color.GRAY);
    mListViewVideoSizes.setSelector(getResources().getDrawable(R.drawable.listselector));
    mListViewVideoSizes.setId(R.id.video_sizes_list_view);
    mListViewVideoSizes.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
        final String selectedVideoSizeToString = mVideoSizes.get(position);
				/* Update selected Video Size */
        try {
          for(int i = 0; i < mSupportedVideoSizesList.size(); i++) {
            final Size size = mSupportedVideoSizesList.get(i);
            final String sizeString = String.valueOf(size.width) + " x " + String.valueOf(size.height);
            if(sizeString.equals(selectedVideoSizeToString)) {
              selectedVideoSize = size;
              break;
            }
          }
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, "Error updating Video Size !",
              Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  /* Read Supported Camera Settings and update Settings Views with values */
  private void readCameraSettingsAndSetUpSettingsViews(Camera tCamera) {
    Parameters p = tCamera.getParameters();
		/* Read Flash Mode */
    if(p.getSupportedFlashModes() != null && p.getSupportedFlashModes().size() > 1) {
      isFlashModeSupported = true;
      final List<String> flashModes = p.getSupportedFlashModes();
      for(String flashMode : flashModes) {
        if(flashMode.equals(Parameters.FLASH_MODE_AUTO)) {
          isFlashModeAUTOSupported = true;
        } else if(flashMode.equals(Parameters.FLASH_MODE_ON)) {
          isFlashModeONSupported = true;
        } else if(flashMode.equals(Parameters.FLASH_MODE_OFF)) {
          isFlashModeOFFSupported = true;
        }
      }
    } else {
      isFlashModeSupported = false;
    }
		
		/* Read Zoom */
    if(p.isZoomSupported()) {
      defaultZoom = p.getZoom();
      maxZoom = p.getMaxZoom();
      btnZoom.setEnabled(true);
      isZoomSupported = true;
			
			/* Update Zoom Bar */
      if(zoomBar != null) {
        maxZoomTextView.setText(String.valueOf(maxZoom));
        zoomBar.setMax(p.getMaxZoom());
        zoomBar.setProgress(defaultZoom);
      }
    } else {
      isZoomSupported = false;
      btnZoom.setEnabled(false);
    }
		
		/* Read exposure compensation */
    if(p.getMaxExposureCompensation() != 0) {
      defaultExposureCompensation = p.getExposureCompensation();
      maxExposureCompensation = p.getMaxExposureCompensation();
      minExposureCompensation = p.getMinExposureCompensation();
      isExposureCompensationSupported = true;
			
			/* Update Exposure Bar */
      if(brightnessBar != null) {
        minBrightnessTextView.setText(String.valueOf(minExposureCompensation));
        maxBrightnessTextView.setText(String.valueOf(maxExposureCompensation));
        final int barMax = Math.abs(minExposureCompensation) + Math.abs(maxExposureCompensation);
        brightnessBar.setMax(barMax);
        final int brightnessProgressValue = defaultExposureCompensation +
            Math.abs(minExposureCompensation);
        brightnessBar.setProgress(brightnessProgressValue);
      }
    } else {
      isExposureCompensationSupported = false;
    }
		
		/* Read Scene Modes */
    if(p.getSupportedSceneModes() != null && p.getSupportedSceneModes().size() > 1) {
      mSupportedSceneModesList.clear();
      mSupportedSceneModesList.addAll(p.getSupportedSceneModes());
      defaultSceneMode = p.getSceneMode();
      isSceneModeSupported = true;
			
			/* Update Scene Modes ListView */
      if(mListViewSceneModes != null) {
        ((ArrayAdapter<?>) mListViewSceneModes.getAdapter()).notifyDataSetChanged();
        mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(defaultSceneMode), true);
      }
    } else {
      isSceneModeSupported = false;
      defaultSceneMode = null;
    }
		
		/* Read White Balances */
    if(p.getSupportedWhiteBalance() != null && p.getSupportedWhiteBalance().size() > 1) {
      mSupportedWhiteBalanceList.clear();
      mSupportedWhiteBalanceList.addAll(p.getSupportedWhiteBalance());
      defaultWhiteBalance = p.getWhiteBalance();
      isWhiteBalanceSupported = true;
			
			/* Update White Balance ListView */
      if(mListViewWhiteBalance != null) {
        ((ArrayAdapter<?>) mListViewWhiteBalance.getAdapter()).notifyDataSetChanged();
        mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList
            .indexOf(defaultWhiteBalance), true);
      }
    } else {
      isWhiteBalanceSupported = false;
      defaultWhiteBalance = null;
    }
		
		/* Read Color Effects */
    if(p.getSupportedColorEffects() != null && p.getSupportedColorEffects().size() > 1) {
      mSupportedColorEffectsList.clear();
      mSupportedColorEffectsList.addAll(p.getSupportedColorEffects());
      defaultColorEffect = p.getColorEffect();
      isColorEffectSupported = true;
			
			/* Update Color Effects ListView */
      if(mListViewColorEffects != null) {
        ((ArrayAdapter<?>) mListViewColorEffects.getAdapter()).notifyDataSetChanged();
        mListViewColorEffects.setItemChecked(
            mSupportedColorEffectsList.indexOf(defaultColorEffect), true);
      }
    } else {
      isColorEffectSupported = false;
      defaultColorEffect = null;
    }
		
		/* Read Picture Sizes */
    if(p.getSupportedPictureSizes() != null && p.getSupportedPictureSizes().size() > 1) {
      mSupportedPictureSizesList.clear();
      mSupportedPictureSizesList.addAll(p.getSupportedPictureSizes());
      mPictureSizes.clear();
      if(mSupportedPictureSizesList != null) {
        Size tPictureSize;
        for(int i = 0; i < mSupportedPictureSizesList.size(); i++) {
          tPictureSize = mSupportedPictureSizesList.get(i);
          mPictureSizes.add(String.valueOf(tPictureSize.width)
              + " x " + String.valueOf(tPictureSize.height));
        }
        defaultPictureSize = p.getPictureSize();
        isPictureSizeSupported = true;
				
				/* Update Picture Size ListView */
        if(mListViewPictureSizes != null) {
          ((ArrayAdapter<?>) mListViewPictureSizes.getAdapter()).notifyDataSetChanged();
          final String defaultPictureSizeToString =
              String.valueOf(defaultPictureSize.width) + " x " +
                  String.valueOf(defaultPictureSize.height);
          mListViewPictureSizes.setItemChecked(mPictureSizes.indexOf(defaultPictureSizeToString), true);
        }
      } else {
        defaultPictureSize = null;
        isPictureSizeSupported = false;
      }
    }
		
		/* Read Video Sizes */
    if(p.getSupportedVideoSizes() != null && p.getSupportedVideoSizes().size() > 1) {
      mSupportedVideoSizesList.clear();
      mSupportedVideoSizesList.addAll(p.getSupportedVideoSizes());
      mVideoSizes.clear();
      if(mSupportedVideoSizesList != null) {
        Size tVideoSize;
        for(int i = 0; i < mSupportedVideoSizesList.size(); i++) {
          tVideoSize = mSupportedVideoSizesList.get(i);
          mVideoSizes.add(String.valueOf(tVideoSize.width) + " x " + String.valueOf(tVideoSize.height));
        }
        defaultVideoSize = mSupportedVideoSizesList.get(0);
        selectedVideoSize = defaultVideoSize;
        isVideoSizeSupported = true;
				
				/* Update Video Size ListView */
        if(mListViewVideoSizes != null) {
          ((ArrayAdapter<?>) mListViewVideoSizes.getAdapter()).notifyDataSetChanged();
          final String defaultVideoSizeToString = String.valueOf(defaultVideoSize.width) +
              " x " + String.valueOf(defaultVideoSize.height);
          mListViewVideoSizes.setItemChecked(mVideoSizes.indexOf(defaultVideoSizeToString), true);
        }
      } else {
        defaultVideoSize = null;
        isVideoSizeSupported = false;
      }
    }
  }

  /* Restores camera settings and updates Settings Views */
  private void restoreCameraSettingsAndUpdateSettingsViews() {

    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    mCamera.stopPreview();
    Parameters p = mCamera.getParameters();
		
		/* Restore Camera Effects (but not when app is first launched) */
    if(!restoreCameraEffectsInOnResume) {
      restoreCameraEffectsInOnResume = true;
    } else {
			/* Restore Zoom */
      if(isZoomSupported) {
        final int lastZoom = prefs.getInt("zoom", defaultZoom);
        p.setZoom(lastZoom);
        zoomBar.setProgress(lastZoom);
      }
			/* Restore Brightness */
      if(isExposureCompensationSupported) {
        final int lastExposureCompensation = prefs.getInt("exposure_compensation",
            defaultExposureCompensation);
        p.setExposureCompensation(lastExposureCompensation);
        final int lastBrightnessProgressValue = lastExposureCompensation +
            Math.abs(minExposureCompensation);
        brightnessBar.setProgress(lastBrightnessProgressValue);
      }
			/* Restore Scene Mode */
      if(isSceneModeSupported) {
        final String lastSceneMode = prefs.getString("scene_mode", defaultSceneMode);
        p.setSceneMode(lastSceneMode);
        mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(lastSceneMode), true);
      }
			/* Restore White Balance */
      if(isWhiteBalanceSupported) {
        final String lastWhiteBalance = prefs.getString("white_balance", defaultWhiteBalance);
        p.setWhiteBalance(lastWhiteBalance);
        mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.indexOf(lastWhiteBalance), true);
      }
			/* Restore Color Effect */
      if(isColorEffectSupported) {
        final String lastColorEffect = prefs.getString("color_effect", defaultColorEffect);
        p.setColorEffect(lastColorEffect);
        mListViewColorEffects.setItemChecked(mSupportedColorEffectsList.indexOf(lastColorEffect), true);
      }
    }
		
		/* Restore camera mode (photo / video) */
    isVideoCameraMode = prefs.getBoolean("video_camera_mode", false);
    mCamera.setParameters(p);
		
		/* Restore Picture And Video Size */
    restorePictureAndVideoSize();
		
		/* Restore Flash Mode */
    restoreFlashMode();

    mCamera.startPreview();
  }

  /* Restores Picture / Video Size and updates Views */
  private void restorePictureAndVideoSize() {

    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		/* Restore Picture Size */
    if(isPictureSizeSupported) {

      int lastPictureSizeWidth = -1;
      int lastPictureSizeHeight = -1;
			
			/* Get last Picture Size width and height */
      if(isFacingBackCamera) {
        lastPictureSizeWidth = prefs.getInt(Prefs.PREF_BACK_CAMERA_PICTURE_SIZE_WIDTH,
            defaultPictureSize.width);
        lastPictureSizeHeight = prefs.getInt(Prefs.PREF_BACK_CAMERA_PICTURE_SIZE_HEIGHT,
            defaultPictureSize.height);
      } else {
        lastPictureSizeWidth = prefs.getInt(Prefs.PREF_FRONT_CAMERA_PICTURE_SIZE_WIDTH,
            defaultPictureSize.width);
        lastPictureSizeHeight = prefs.getInt(Prefs.PREF_FRONT_CAMERA_PICTURE_SIZE_HEIGHT,
            defaultPictureSize.height);
      }
			
			/* Update Picture Size and ListView Selection */
      if(lastPictureSizeWidth > 0 && lastPictureSizeHeight > 0) {
        Parameters params = mCamera.getParameters();
        params.setPictureSize(lastPictureSizeWidth, lastPictureSizeHeight);
        mCamera.setParameters(params);
        for(Size size : mSupportedPictureSizesList) {
          if(size.width == lastPictureSizeWidth && size.height == lastPictureSizeHeight) {
            selectedPictureSize = size;
            break;
          }
        }
        final String lastPictureSizeToString = String.valueOf(lastPictureSizeWidth) + " x " + String.valueOf(lastPictureSizeHeight);
        mListViewPictureSizes.setItemChecked(mPictureSizes.indexOf(lastPictureSizeToString), true);
      }
    }
		
		/* Restore Video Size */
    if(isVideoSizeSupported) {
      int lastVideoSizeWidth = -1;
      int lastVideoSizeHeight = -1;
			
			/* Get last Video Size width and height */
      if(isFacingBackCamera) {
        lastVideoSizeWidth = prefs.getInt(Prefs.PREF_BACK_CAMERA_VIDEO_SIZE_WIDTH,
            defaultVideoSize.width);
        lastVideoSizeHeight = prefs.getInt(Prefs.PREF_BACK_CAMERA_VIDEO_SIZE_HEIGHT,
            defaultVideoSize.height);
      } else {
        lastVideoSizeWidth = prefs.getInt(Prefs.PREF_FRONT_CAMERA_VIDEO_SIZE_WIDTH,
            defaultVideoSize.width);
        lastVideoSizeHeight = prefs.getInt(Prefs.PREF_FRONT_CAMERA_VIDEO_SIZE_HEIGHT,
            defaultVideoSize.height);
      }
			
			/* Update Video Size and ListView selection */
      for(Size size : mSupportedVideoSizesList) {
        if(size.width == lastVideoSizeWidth && size.height == lastVideoSizeHeight) {
          selectedVideoSize = size;
          break;
        }
      }
      final String lastVideoSizeToString = String.valueOf(lastVideoSizeWidth) +
          " x " + String.valueOf(lastVideoSizeHeight);
      mListViewVideoSizes.setItemChecked(mVideoSizes.indexOf(lastVideoSizeToString), true);
    }
  }

  /* Saves camera settings */
  private void saveCameraSettings() {
    if(mCamera == null) {
      return;
    }

    Parameters p = mCamera.getParameters();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = prefs.edit();
		
		/* Save Zoom */
    if(isZoomSupported) {
      editor.putInt("zoom", p.getZoom());
    }
		/* Save Exposure Compensation */
    if(isExposureCompensationSupported) {
      editor.putInt("exposure_compensation", p.getExposureCompensation());
    }
		/* Save Scene Mode */
    if(isSceneModeSupported) {
      editor.putString("scene_mode", p.getSceneMode());
    }
		/* Save White Balance */
    if(isWhiteBalanceSupported) {
      editor.putString("white_balance", p.getWhiteBalance());
    }
		/* Save Color Effect */
    if(isColorEffectSupported) {
      editor.putString("color_effect", p.getColorEffect());
    }
		
		/* Save last captured media file */
    if(lastCapturedMediaFile != null) {
      editor.putString("last_captured_file_path", lastCapturedMediaFile.getAbsolutePath());
    } else {
      editor.putString("last_captured_file_path", null);
    }
    editor.putBoolean("video_camera_mode", isVideoCameraMode);
    editor.putBoolean("back_camera", isFacingBackCamera);
    editor.commit();
		
		/* Save Picture and Video Size */
    savePictureAndVideoSize();
		
		/* Save Flash Mode */
    saveFlashMode();
  }

  /* Save Picture / Video Size of current camera */
  private void savePictureAndVideoSize() {

    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = settings.edit();
		
		/* Save Picture Size */
    if(isPictureSizeSupported) {
      if(isFacingBackCamera) {
        editor.putInt(Prefs.PREF_BACK_CAMERA_PICTURE_SIZE_WIDTH, selectedPictureSize.width);
        editor.putInt(Prefs.PREF_BACK_CAMERA_PICTURE_SIZE_HEIGHT, selectedPictureSize.height);
      } else {
        editor.putInt(Prefs.PREF_FRONT_CAMERA_PICTURE_SIZE_WIDTH, selectedPictureSize.width);
        editor.putInt(Prefs.PREF_FRONT_CAMERA_PICTURE_SIZE_HEIGHT, selectedPictureSize.height);
      }
    }
		
		/* Save Video Size */
    if(isVideoSizeSupported) {
      if(isFacingBackCamera) {
        editor.putInt(Prefs.PREF_BACK_CAMERA_VIDEO_SIZE_WIDTH, selectedVideoSize.width);
        editor.putInt(Prefs.PREF_BACK_CAMERA_VIDEO_SIZE_HEIGHT, selectedVideoSize.height);
      } else {
        editor.putInt(Prefs.PREF_FRONT_CAMERA_VIDEO_SIZE_WIDTH, selectedVideoSize.width);
        editor.putInt(Prefs.PREF_FRONT_CAMERA_VIDEO_SIZE_HEIGHT, selectedVideoSize.height);
      }
    }

    editor.commit();
  }

  /* Sets Camera Settings and Views to default */
  private void resetCameraSettingsAndSettingsViews() {
    mCamera.stopPreview();
    Parameters parameters = mCamera.getParameters();
		
		/* Reset Flash Mode */
    if(isFlashModeSupported) {
      parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
    }
		/* Reset Zoom */
    if(isZoomSupported) {
      parameters.setZoom(defaultZoom);
      zoomBar.setProgress(defaultZoom);
    }
		/* Reset Exposure */
    if(isExposureCompensationSupported) {
      parameters.setExposureCompensation(defaultExposureCompensation);
      brightnessBar.setProgress(defaultExposureCompensation + Math.abs(minExposureCompensation));
    }
		/* Reset Scene Mode */
    if(isSceneModeSupported) {
      parameters.setSceneMode(defaultSceneMode);
      mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(defaultSceneMode), true);
    }
		/* Reset White Balance */
    if(isWhiteBalanceSupported) {
      parameters.setWhiteBalance(defaultWhiteBalance);
      mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.indexOf(defaultWhiteBalance), true);
    }
		/* Reset Color Effect */
    if(isColorEffectSupported) {
      parameters.setColorEffect(defaultColorEffect);
      mListViewColorEffects.setItemChecked(mSupportedColorEffectsList.indexOf(defaultColorEffect), true);
    }
		/* Reset Picture Size */
    if(isPictureSizeSupported) {
      final String defaultPictureSizeToString = String.valueOf(defaultPictureSize.width) + " x " + String.valueOf(defaultPictureSize.height);
      mListViewPictureSizes.setItemChecked(mPictureSizes.indexOf(defaultPictureSizeToString), true);
    }
		/* Reset Video Size */
    if(isVideoSizeSupported) {
      final String defaultVideoSizeToString = String.valueOf(defaultVideoSize.width) + " x " + String.valueOf(defaultVideoSize.height);
      mListViewVideoSizes.setItemChecked(mVideoSizes.indexOf(defaultVideoSizeToString), true);
    }

    mCamera.setParameters(parameters);
    mCamera.startPreview();
  }

  /* Sets camera settings views to default */
  private void resetCameraSettingsViews() {
		
		/* Set Zoom to default */
    if(isZoomSupported) {
      zoomBar.setProgress(defaultZoom);
    }
		/* Set Exposure Compensation to default */
    if(isExposureCompensationSupported) {
      brightnessBar.setProgress(defaultExposureCompensation);
    }
		/* Set Scene Mode to default */
    if(isSceneModeSupported) {
      mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(defaultSceneMode), true);
    }
		/* Set White Balance to default */
    if(isWhiteBalanceSupported) {
      mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.indexOf(defaultWhiteBalance), true);
    }
		/* Set Color Effect to default */
    if(isColorEffectSupported) {
      mListViewColorEffects.setItemChecked(mSupportedColorEffectsList.indexOf(defaultColorEffect), true);
    }
		/* Set Picture Size to default */
    if(isPictureSizeSupported) {
      final String defaultPictureSizeToString = String.valueOf(defaultPictureSize.width) + " x " + String.valueOf(defaultPictureSize.height);
      mListViewPictureSizes.setItemChecked(mPictureSizes.indexOf(defaultPictureSizeToString), true);
    }
		/* Set Video Size to default */
    if(isVideoSizeSupported) {
      final String defaultVideoSizeToString = String.valueOf(defaultVideoSize.width) + " x " + String.valueOf(defaultVideoSize.height);
      mListViewVideoSizes.setItemChecked(mVideoSizes.indexOf(defaultVideoSizeToString), true);
    }
  }

  /* Restores other preferences (apart from camera settings) */
  private void restorePreferences() {
    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		/* Interval for periodic capture */
    try {
      periodicCaptureInterval = 1000 * Integer.parseInt(settings.getString("periodic_capture_interval", "2"));
    } catch(Exception e) {
      periodicCaptureInterval = 2000;
    }
		
		/* Time delay after capture */
    try {
      delayAfterCapture = 1000 * Integer.parseInt(settings.getString("delay_after_capture", "1"));
    } catch(Exception e) {
      delayAfterCapture = 1000;
    }
		
		/* Shutter sound enabled / disabled */
    final String shutter = settings.getString("shutter_sound", "enabled");
    isShutterSoundEnabled = shutter.equals("enabled") ? true : false;
  }
	
	/* Location related callbacks */

  @Override
  public void onConnected(Bundle connectionHint) {
    // Connected to Google Play services! The good stuff goes here.
		/* Get the last known location */
    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return;
    }
    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    /* Start Location updates */
    LocationRequest mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(10000);
    mLocationRequest.setFastestInterval(5000);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
  }

  @Override
  public void onConnectionFailed(ConnectionResult result) {
    // This callback is important for handling errors that may occur while
    // attempting to connect with Google.
    // More about this in the 'Handle Connection Failures' section.
    Toast.makeText(this, "Google Play Services : Connection failed !", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onConnectionSuspended(int cause) {
    // The connection has been interrupted.
    // Disable any UI components that depend on Google APIs until onConnected() is called.
    Toast.makeText(this, "Google Play Services : Connection suspended !", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onLocationChanged(Location location) {
    if(location != null) {
      mLastLocation = location;
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onResume() {
    super.onResume();
    datasource.open();
    registerReceiver(periodicCaptureReceiver, new IntentFilter(PhotoActivity.ACTION_PERIODIC_CAPTURE));
    initCameraAndPreview();
    restoreCameraSettingsAndUpdateSettingsViews();
    restorePreferences();
    restoreLastCapturedMediaAndSetThumbnail();
  }

  @Override
  protected void onPause() {
    super.onPause();
		
		/* Stop Audio Recording */
    if(isAudioRecording) {
      stopAudioRecording();
    }
		
		/* Stop Video Recording */
    if(isVideoRecording) {
      stopVideoRecording();
    }
		
		/* Stop Periodic Capture */
    if(isPeriodicCaptureOn) {
      stopPeriodicCapture();
    }
    unregisterReceiver(periodicCaptureReceiver);

    saveCameraSettings();

    closeCameraAndPreview();
    datasource.close();
		
		/* Stop Location updates */
    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
  }

  @Override
  public void onStop() {
    mGoogleApiClient.disconnect();
    super.onStop();
  }
	
	/* End : Location related callbacks */

  @Override
  public void onBackPressed() {
    if(!removeSettingsViews()) {
      super.onBackPressed();
    }
  }
	
	/* Camera and settings */

  /* Returns a new camera instance (back / front) */
  private Camera getCameraInstance(int cameraId) {
    Camera camera = null;
    try {
      camera = Camera.open(cameraId);
    } catch(Exception e) {
      return null;
    }
    return camera;
  }

  /* Inits Camera and camera preview */
  private void initCameraAndPreview() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    isFacingBackCamera = prefs.getBoolean("back_camera", true);
    if(isFacingBackCamera) {
      mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
    } else {
      mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }
    initCameraPreview();
  }

  /* Initializes camera preview */
  private void initCameraPreview() {
    mPreview = new CameraPreview(this, mCamera);
    frameLayout.addView(mPreview);
  }

  /* Closes camera and camera preview */
  private void closeCameraAndPreview() {
    if(mCamera != null) {
      mCamera.release();
      mCamera = null;
      frameLayout.removeAllViews();
      mPreview = null;
    }
  }

  /* On user click, reverses camera back and front */
  public void onClickReverseCamera(View view) {
    	
		/* Check if Preview is busy */
    if(isPreviewBusy()) {
      Toast.makeText(this, CAMERA_PREVIEW_BUSY_MSG, Toast.LENGTH_SHORT).show();
      return;
    }
		
		/* Check if device supports FRONT CAMERA */
    if(!isFrontCameraSupported) {
      Toast.makeText(this, "Front camera is not supported", Toast.LENGTH_SHORT).show();
      return;
    }
		
		/* Reverse camera */
    try {
			
			/* Save Photo Camera Flash Mode */
      if(!isVideoCameraMode && isFacingBackCamera) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Prefs.PREF_PHOTO_CAMERA_FLASH_MODE, mCamera.getParameters().getFlashMode());
        editor.commit();
      }
			
			/* Save Picture and Video Size of camera */
      savePictureAndVideoSize();
			
			/* Close camera and preview */
      closeCameraAndPreview();
			
			/* Get camera instance */
      if(isFacingBackCamera) {
        mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
      } else {
        mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
      }

      isFacingBackCamera = !isFacingBackCamera;
			
			/* Init camera preview */
      initCameraPreview();
			
			/* Update camera parameters and settings */
      mCamera.stopPreview();

      readCameraSettingsAndSetUpSettingsViews(mCamera);
			
			/* Restore Picture And Video Size */
      restorePictureAndVideoSize();
			
			/* Restore Photo Camera Flash Mode */
      if(!isVideoCameraMode && isFacingBackCamera) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Parameters params = mCamera.getParameters();
        params.setFlashMode(settings.getString(Prefs.PREF_PHOTO_CAMERA_FLASH_MODE,
            Parameters.FLASH_MODE_OFF));
        mCamera.setParameters(params);
      }

      mCamera.startPreview();
    } catch(Exception e) {
      Toast.makeText(this, "Error reversing camera!", Toast.LENGTH_LONG).show();
      return;
    }
  }
	
	/* End : Camera and settings */

  /* Checks if any operation keeps camera preview busy */
  private boolean isPreviewBusy() {
    return isCapturingPhoto || isAudioRecording || isVideoRecording || isPeriodicCaptureOn;
  }

  /* Shutter callback for playing sound on photo capture */
  private final ShutterCallback mShutterCallback = new ShutterCallback() {
    public void onShutter() {
      audioManager.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
    }
  };

  /* Shutter callback for not playing sound on photo capture */
  private final ShutterCallback mSilentShutterCallback = new ShutterCallback() {
    public void onShutter() {
      audioManager.playSoundEffect(AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }
  };

  /* Async task to capture photo */
  private class CapturePhotoTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
      buttonTakePicture.setEnabled(false);
      isCapturingPhoto = true;
//			if(cameraProgressDialog != null && !cameraProgressDialog.isShowing()) {
//				cameraProgressDialog.show();
//			}
    }

    @Override
    protected Void doInBackground(Void... params) {
			
			/* Take picture */
      if(!isShutterSoundEnabled) {
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        mCamera.takePicture(mSilentShutterCallback, null, mPictureCallback);
      } else {
        mCamera.takePicture(mShutterCallback, null, mPictureCallback);
      }
			
			/* Preview delay */
      try {
        Thread.sleep(delayAfterCapture);
      } catch(InterruptedException e) {
        e.printStackTrace();
      }
			
			/* Store Location to database */
      final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PhotoActivity.this);
      if(settings.getString("store_captures_to_db", "yes").equals("yes")) {
				
				/* Add new capture to database */
        boolean captureSavedSuccessfully = datasource.addCaptureToDatabase(
            LocationUtils.getStringLatitude(mLastLocation),
            LocationUtils.getStringLongitude(mLastLocation),
            Capture.TYPE_IMAGE,
            lastCapturedMediaFile.getAbsolutePath());
				
				/* Refresh list with all captures */
        if(captureSavedSuccessfully) {
          allCaptures.clear();
          allCaptures.addAll(datasource.getAllModels());
        }
      }

      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
//			if(cameraProgressDialog != null && cameraProgressDialog.isShowing()) {
//				cameraProgressDialog.dismiss();
//			}

      if(capturesAdapter != null) {
        capturesAdapter.notifyDataSetChanged();
      }

      mCamera.startPreview();
      isCapturingPhoto = false;
      buttonTakePicture.setEnabled(true);
			
			/* If periodic capturig is enabled, continue capturing */
      if(isPeriodicCaptureOn) {
				
				/* Check to stop periodic capture  */
        if(!infinitePeriodicCapture) {

          counterPeriodicCapture--;

          if(counterPeriodicCapture == 0) {
            stopPeriodicCapture();
            return;
          }
        }
				
				/* Set next capture */
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + periodicCaptureInterval,
            periodicCapturePendingIntent);
      }

    }
  }

  /* Callback when the picture is taken */
  private PictureCallback mPictureCallback = new PictureCallback() {
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
      lastCapturedMediaFile = MyFileUtils.getOutputMediaFile(MyFileUtils.MEDIA_TYPE_IMAGE,
          PhotoActivity.MEDIA_FOLDER_NAME);
      if(lastCapturedMediaFile == null) {
        return;
      }
      try {
        final FileOutputStream fos = new FileOutputStream(lastCapturedMediaFile);
        fos.write(data);
        fos.close();
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(lastCapturedMediaFile)));
        setThumbnailPic(thumbNailTargetWidth, thumbNailTargetHeight);
        if(!isShutterSoundEnabled) {
          audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        }
      } catch(Exception e) {

      }
    }
  };

  /* On user click captures photo */
  public void onClickCapturePhoto(View view) {
    if(isPreviewBusy()) {
      Toast.makeText(this, CAMERA_PREVIEW_BUSY_MSG, Toast.LENGTH_SHORT).show();
      return;
    }
    capturePhoto();
  }

  /* Captures photo */
  private void capturePhoto() {
    removeSettingsViews();
    capturePhotoTask = new CapturePhotoTask();
    capturePhotoTask.execute();
  }

  /* Checks if the camera supports a given flash mode */
  private boolean cameraSupportsFlashMode(String mode) {
    final List<String> flashModes = mCamera.getParameters().getSupportedFlashModes();
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

  /* Restores last captured media file and updates thumbnail icon */
  private void restoreLastCapturedMediaAndSetThumbnail() {
		
		/* Get the saved path of last capture photo */
    if(lastCapturedMediaFile == null) {
      final String path = PreferenceManager.getDefaultSharedPreferences(PhotoActivity.this).getString("last_captured_file_path", null);

      if(path != null) {
        lastCapturedMediaFile = new File(path);
      }
    }
		
		/* If last captured photo does not exist, get the last picture from storage folder */
    if(lastCapturedMediaFile == null || !lastCapturedMediaFile.exists()) {

      try {
        final File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MEDIA_FOLDER_NAME);
        final File[] files = folder.listFiles();
        if(files != null && files.length > 0) {
          lastCapturedMediaFile = files[files.length - 1];
        } else {
          lastCapturedMediaFile = null;
        }
      } catch(Exception e) {
        Toast.makeText(this, "Error restoring thumbnail icon !", Toast.LENGTH_SHORT).show();
        return;
      }
    }
		
		/* Update thumbnail picture */
    if(lastCapturedMediaFile != null && lastCapturedMediaFile.exists() && lastCapturedMediaFile.isFile()) {
      if(MyFileUtils.fileIsImage(lastCapturedMediaFile.getName())) {
        setThumbnailPic(thumbNailTargetWidth, thumbNailTargetHeight);
      } else if(MyFileUtils.fileIsVideo(lastCapturedMediaFile.getName())) {
        setThumbnailPicFromVideo();
      } else if(lastCapturedMediaFile.getName().endsWith(".3gp")) {
        setThumbnailPicFromAudio();
      }
    } else {
      mImageView.setImageBitmap(null);
    }
  }
	
	/* thumbnail pic */

  /* Sets thumbnail icon after audio capture */
  private void setThumbnailPicFromAudio() {
    mImageView.setImageDrawable(this.getResources().getDrawable(R.mipmap.mic_dark));
  }

  /* Creates thumbnail from video file (MICRO_KIND: 96 x 96) */
  private void setThumbnailPicFromVideo() {
    if(lastCapturedMediaFile != null) {
      final Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(lastCapturedMediaFile.getAbsolutePath(),
          Thumbnails.MICRO_KIND);
      mImageView.setImageBitmap(bmThumbnail);
    }
  }

  /* Sets thumnail pic given target width and height */
  private void setThumbnailPic(int target_w, int target_h) {

    if(lastCapturedMediaFile != null) {
			
			/* Get the dimensions of the bitmap */
      BitmapFactory.Options bmOptions = new BitmapFactory.Options();
      bmOptions.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(lastCapturedMediaFile.getAbsolutePath(), bmOptions);
      final int photoW = bmOptions.outWidth;
      final int photoH = bmOptions.outHeight;
			
			/* Determine how much to scale down the image */
      final int scaleFactor = Math.min(photoW / target_w, photoH / target_h);
			
			/* Decode the image file into a Bitmap sized to fill the View */
      bmOptions.inJustDecodeBounds = false;
      bmOptions.inSampleSize = scaleFactor;
      bmOptions.inPurgeable = true;

      Bitmap bitmap = BitmapFactory.decodeFile(lastCapturedMediaFile.getAbsolutePath(), bmOptions);
      mImageView.setImageBitmap(bitmap);
    }
  }
	
	/* End : thumbnail pic */
	
	/* Periodic capture */

  /* On user click starts periodic capture */
  public void onClickStartPeriodicCapture(View view) {

    if(isPeriodicCaptureOn) {
      stopPeriodicCapture();
    } else if(isPreviewBusy()) {
      Toast.makeText(this, CAMERA_PREVIEW_BUSY_MSG, Toast.LENGTH_SHORT).show();
    } else {
      showAlertDialogForPeriodicCapture();
    }
  }

  private BroadcastReceiver periodicCaptureReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if(isPeriodicCaptureOn) {
        capturePhoto();
      }
    }
  };

  /* Starts periodic photo capture */
  private void startPeriodicCapture(boolean isInfinite) {
    infinitePeriodicCapture = isInfinite;
    isPeriodicCaptureOn = true;
    periodicCaptureButton.setImageResource(R.mipmap.stop);
    Toast.makeText(this, "Periodic capture started, interval: " + periodicCaptureInterval, Toast.LENGTH_SHORT).show();
    capturePhoto();
  }

  /* Stops periodic photo capture */
  private void stopPeriodicCapture() {
    isPeriodicCaptureOn = false;
    infinitePeriodicCapture = false;
    periodicCaptureButton.setImageResource(R.mipmap.repeat);
  }

  /* Shows alert dialog when user presses to reset camera */
  private void showAlertDialogForPeriodicCapture() {
    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    alertDialog.setMessage("- Enter number of captures\n- Leave empty for continous capture");
    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT);
    input.setLayoutParams(lp);
    alertDialog.setView(input);
    alertDialog.setPositiveButton("Start",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            try {
              counterPeriodicCapture = Integer.parseInt(input.getText().toString());
            } catch(Exception e) {
              Toast.makeText(PhotoActivity.this, "Please enter a positive integer!",
                  Toast.LENGTH_SHORT).show();
              return;
            }

            if(counterPeriodicCapture <= 0) {
              Toast.makeText(PhotoActivity.this, "Please enter a positive integer!",
                  Toast.LENGTH_SHORT).show();
              return;
            }

            startPeriodicCapture(false);
          }
        });
    alertDialog.setNeutralButton("Infinite",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            startPeriodicCapture(true);
          }
        });
    alertDialog.setNegativeButton("Cancel",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
          }
        });
    alertDialog.show();
  }
	
	/* End : Periodic capture */

  /* Async Task that updates location info and current capture */
  private class UpdatePositionInfoAndCaptureTask extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {
			/* Update capture in database, and get captures from database */
      final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PhotoActivity.this);
      if(settings.getString("store_captures_to_db", "yes").equals("yes")) {
				
				/* Add new capture to database */
        boolean captureSavedSuccessfully = datasource.addCaptureToDatabase(
            LocationUtils.getStringLatitude(mLastLocation),
            LocationUtils.getStringLongitude(mLastLocation),
            params[0],
            lastCapturedMediaFile.getAbsolutePath());
				
				/* Refresh list with all captures */
        if(captureSavedSuccessfully) {
          allCaptures.clear();
          allCaptures.addAll(datasource.getAllModels());
        }

        return true;
      }

      return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
      if(capturesAdapter != null) {
        capturesAdapter.notifyDataSetChanged();
      }
      if(!result) {
        Toast.makeText(PhotoActivity.this, "Error saving Capture !", Toast.LENGTH_SHORT).show();
      }
    }
  }

  /* Stops media recorder */
  private void stopVideoRecording() {

    mMediaRecorder.stop();
    releaseMediaRecorder();
	    
	    /* Turn off FLASH if active */
    if(isFlashModeSupported) {
      Parameters params = mCamera.getParameters();
      if(params.getFlashMode().equals(Parameters.FLASH_MODE_TORCH)) {
        mCamera.stopPreview();
        params.setFlashMode(Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
        mCamera.startPreview();
      }
    }

    videoButton.setImageResource(R.mipmap.red_rect);
    isVideoRecording = false;

    setThumbnailPicFromVideo();
  }

  /* Releases media recorder */
  private void releaseMediaRecorder() {
    if(mMediaRecorder != null) {
      mMediaRecorder.reset();
      mMediaRecorder.release();
      mMediaRecorder = null;
      mCamera.lock(); /* lock camera for later use (take camera access back from MediaRecorder) */
    }
  }
	
	/* End : Video capture */
	
	/* Audio capture */

  public void onClickCaptureAudio(View view) {
    if(isAudioRecording) {
      stopAudioRecording();
    } else if(isPreviewBusy()) {
      Toast.makeText(this, CAMERA_PREVIEW_BUSY_MSG, Toast.LENGTH_SHORT).show();
    } else {
      startAudioRecording();
    }
  }

  /* Releases audio recorder */
  private void releaseAudioRecorder() {
    if(mMediaRecorder != null) {
      mMediaRecorder.stop();
      mMediaRecorder.release();
      mMediaRecorder = null;
    }
  }

  /* Stops media recorder */
  private void stopAudioRecording() {
    releaseAudioRecorder();
    btnAudioCapture.setImageResource(R.mipmap.mic);
    isAudioRecording = false;
    setThumbnailPicFromAudio();
  }

  /* Starts media recorder for audio capture */
  private void startAudioRecording() {
    try {
      mMediaRecorder = new MediaRecorder();
      mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      mMediaRecorder.setOutputFormat(OutputFormat.THREE_GPP);
      mMediaRecorder.setAudioEncoder(AudioEncoder.AMR_NB);
      lastCapturedMediaFile = MyFileUtils.getOutputMediaFile(MyFileUtils.MEDIA_TYPE_AUDIO,
          PhotoActivity.MEDIA_FOLDER_NAME);
      mMediaRecorder.setOutputFile(lastCapturedMediaFile.getAbsolutePath());
      mMediaRecorder.prepare();
      mMediaRecorder.start();
    } catch(Exception e) {
      releaseAudioRecorder();
      Toast.makeText(this, "Error starting Audio recording !", Toast.LENGTH_SHORT).show();
      return;
    }

    btnAudioCapture.setImageResource(R.mipmap.mic_stop);
    isAudioRecording = true;
		
	    /* Update current capture */
    new UpdatePositionInfoAndCaptureTask().execute(Capture.TYPE_AUDIO);
  }
	
	/* -- end : audio capture -- */
    
	/* -- Handle camera settings -- */

  /* Hide / Show Zoom Bar */
  public void onClickHandleZoom(View view) {

    if(isPreviewBusy()) {
      Toast.makeText(this, CAMERA_PREVIEW_BUSY_MSG, Toast.LENGTH_SHORT).show();
      return;
    }

    if(frameLayout.findViewById(R.id.the_zoom_layout) != null) {
      frameLayout.removeView(mZoomBarLayout);
    } else {
      removeSettingsViews();
      frameLayout.addView(mZoomBarLayout, new LayoutParams(LayoutParams.MATCH_PARENT,
          LayoutParams.WRAP_CONTENT));
    }
  }

  /* Removes all camera settings views */
  private boolean removeSettingsViews() {
    if(frameLayout.findViewById(R.id.the_zoom_layout) != null) {
      frameLayout.removeView(mZoomBarLayout);
      // Toast.makeText(this, "Zoom Bar removed", Toast.LENGTH_LONG).show();
      return true;
    } else if(frameLayout.findViewById(R.id.the_brightness_bar_layout) != null) {
      frameLayout.removeView(mBrightnessBarLayout);
      // Toast.makeText(this, "Brightness Bar removed", Toast.LENGTH_LONG).show();
      return true;
    } else if(frameLayout.findViewById(R.id.white_balance_list_view) != null) {
      frameLayout.removeView(mListViewWhiteBalance);
      // Toast.makeText(this, "White Balance removed", Toast.LENGTH_LONG).show();
      return true;
    } else if(frameLayout.findViewById(R.id.color_effects_list_view) != null) {
      frameLayout.removeView(mListViewColorEffects);
      // Toast.makeText(this, "Color Effects removed", Toast.LENGTH_LONG).show();
      return true;
    } else if(frameLayout.findViewById(R.id.scene_modes_list_view) != null) {
      frameLayout.removeView(mListViewSceneModes);
      // Toast.makeText(this, "Scene Modes removed", Toast.LENGTH_LONG).show();
      return true;
    } else if(frameLayout.findViewById(R.id.picture_sizes_list_view) != null) {
      frameLayout.removeView(mListViewPictureSizes);
      // Toast.makeText(this, "Picture Sizes removed", Toast.LENGTH_LONG).show();
      return true;
    } else if(frameLayout.findViewById(R.id.video_sizes_list_view) != null) {
      frameLayout.removeView(mListViewVideoSizes);
      // Toast.makeText(this, "Video Sizes removed", Toast.LENGTH_LONG).show();
      return true;
    } else if(frameLayout.findViewById(R.id.captures_list_view) != null) {
      frameLayout.removeView(mListViewCaptures);
      // Toast.makeText(this, "Captures removed", Toast.LENGTH_LONG).show();
      return true;
    }
    return false;
  }

  /* Shows alert dialog to reset camera */
  private void showAlertDialogToResetCamera() {
    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    alertDialog.setMessage("Reset camera settings?");
    alertDialog.setPositiveButton("Yes",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            try {
              resetCameraSettingsAndSettingsViews();
            } catch(Exception e) {
              Toast.makeText(PhotoActivity.this, "Error reseting camera !",
                  Toast.LENGTH_SHORT).show();
            }
          }
        });
    alertDialog.setNegativeButton("Cancel",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
          }
        });
    alertDialog.show();
  }

  // -- end : handle camera settings --

  // -- options menu --

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if(isPreviewBusy()) {
      Toast.makeText(this, CAMERA_PREVIEW_BUSY_MSG, Toast.LENGTH_SHORT).show();
      return false;
    }
		
		/* Check flash mode */
    if(isFlashModeSupported) {

      menu.findItem(R.id.mycamera_menu_flash).setEnabled(true);
			
			/* Flash AUTO */
      final MenuItem menuItemFlashAuto = menu.findItem(R.id.mycamera_menu_flash_auto);

      if(isFlashModeAUTOSupported) {
				
				/* video camera mode */
        if(isVideoCameraMode) {
          menuItemFlashAuto.setEnabled(false);
        }
				/* photo camera mode */
        else {
          menuItemFlashAuto.setEnabled(true);

          if(mCamera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_AUTO)) {
            menuItemFlashAuto.setChecked(true);
          }
        }
      } else {
        menuItemFlashAuto.setEnabled(false);
      }
			
			/* Flash Mode ON */
      final MenuItem menuItemFlashOn = menu.findItem(R.id.mycamera_menu_flash_on);

      if(isFlashModeONSupported) {

        menuItemFlashOn.setEnabled(true);
				
				/* video camera mode */
        if(isVideoCameraMode) {

          if(videoCameraFlashMode == VIDEO_CAMERA_FLASH_MODE_ON) {
            menuItemFlashOn.setChecked(true);
          }
        }
				/* photo camera mode */
        else {
          if(mCamera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_ON)) {
            menuItemFlashOn.setChecked(true);
          }
        }
      } else {
        menuItemFlashOn.setEnabled(false);
      }
			
			/* Flash Mode OFF */
      final MenuItem menuItemFlashOff = menu.findItem(R.id.mycamera_menu_flash_off);

      if(isFlashModeOFFSupported) {

        menuItemFlashOff.setEnabled(true);
				
				/* video camera mode */
        if(isVideoCameraMode) {

          if(videoCameraFlashMode == VIDEO_CAMERA_FLASH_MODE_OFF) {
            menuItemFlashOff.setChecked(true);
          }
        }
				/* photo camera mode */
        else {
          if(mCamera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_OFF)) {
            menuItemFlashOff.setChecked(true);
          }
        }
      } else {
        menuItemFlashOff.setEnabled(false);
      }
    } else {
      menu.findItem(R.id.mycamera_menu_flash).setEnabled(false);
    }
		
		/* Check Exposure Compensation */
    if(!isExposureCompensationSupported) {
      menu.findItem(R.id.mycamera_menu_exposure_comp).setEnabled(false);
    } else {
      menu.findItem(R.id.mycamera_menu_exposure_comp).setEnabled(true);
    }

		/* Check Scene Mode */
    if(!isSceneModeSupported) {
      menu.findItem(R.id.mycamera_menu_scene_mode).setEnabled(false);
    } else {
      menu.findItem(R.id.mycamera_menu_scene_mode).setEnabled(true);
    }
		
		/* Check White Balance */
    if(!isWhiteBalanceSupported) {
      menu.findItem(R.id.mycamera_menu_white_balance).setEnabled(false);
    } else {
      menu.findItem(R.id.mycamera_menu_white_balance).setEnabled(true);
    }
		
		/* Check Color Effect */
    if(!isColorEffectSupported) {
      menu.findItem(R.id.mycamera_menu_color_effect).setEnabled(false);
    } else {
      menu.findItem(R.id.mycamera_menu_color_effect).setEnabled(true);
    }
		
		/* Check Picture / Video Size */
    final MenuItem item = menu.findItem(R.id.mycamera_menu_picture_size);
    if(!isVideoCameraMode) {
      item.setTitle("Picture size");
    } else {
      item.setTitle("Video size");
    }

    if(isPictureSizeSupported || isVideoSizeSupported) {
      item.setEnabled(true);
    } else {
      item.setEnabled(false);
    }

    return super.onPrepareOptionsMenu(menu);
  }
	
	/* Context Menu */

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    this.getMenuInflater().inflate(R.menu.context_places, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    selectedPlace = allCaptures.get(info.position);
    if(selectedPlace == null) {
      return false;
    }
    switch(item.getItemId()) {
			
			/* Send data via HTTP */
      case R.id.context_places_send:
        if(!NetUtils.isNetworkConnected(this)) {
          Toast.makeText(this, "No Internet connection !", Toast.LENGTH_SHORT).show();
          return true;
        }
        showAlertDialogToSendHTTP();
        return true;
				
			/* Show place on Map */
      case R.id.context_places_map:
        String uri = null;
        try {
          final float lat = Float.parseFloat(selectedPlace.getLatitude());
          final float lon = Float.parseFloat(selectedPlace.getLongitude());
          uri = String.format(Locale.ENGLISH, "geo:%f,%f", lat, lon);
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, "Unknown location !", Toast.LENGTH_SHORT).show();
          return false;
        }

        final Intent intentToOpenMap = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if(intentToOpenMap.resolveActivity(getPackageManager()) != null) {
          startActivity(intentToOpenMap);
        } else {
          Toast.makeText(PhotoActivity.this, "Unable to complete this action !",
              Toast.LENGTH_SHORT).show();
        }

        return true;
			
			/* Delete selected place */
      case R.id.context_places_delete:
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Delete selected Place ?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            try {
              final int indexOfSelectedLinkToDelete = allCaptures.indexOf(selectedPlace);
              allCaptures.remove(indexOfSelectedLinkToDelete);
              datasource.deleteCaptureById(selectedPlace.getId());
              capturesAdapter.notifyDataSetChanged();
              if(allCaptures.size() <= 0) {
                frameLayout.removeView(mListViewCaptures);
              }
              Toast.makeText(PhotoActivity.this, "Place deleted successfully", Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
              Toast.makeText(PhotoActivity.this, "Error deleting Place !", Toast.LENGTH_SHORT).show();
            }
          }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {

          }
        });
        alertDialog.show();
        return true;
				
			/* Delete all places */
      case R.id.context_places_delete_all:
        AlertDialog.Builder alertDialogDeleteAll = new AlertDialog.Builder(this);
        alertDialogDeleteAll.setMessage("Delete all Places ?");
        alertDialogDeleteAll.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            try {
              allCaptures.clear();
              datasource.deleteAllCaptures();
              capturesAdapter.notifyDataSetChanged();
              frameLayout.removeView(mListViewCaptures);
              Toast.makeText(PhotoActivity.this, "All places deleted successfully", Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
              Toast.makeText(PhotoActivity.this, "Error deleting places !", Toast.LENGTH_SHORT).show();
            }
          }
        });
        alertDialogDeleteAll.setNegativeButton("No", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {

          }
        });
        alertDialogDeleteAll.show();
        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }
	
	/* End : Context Menu */
	
	/* Options Menu */

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.mycamera_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
			
			/* all captures */
      case R.id.mycamera_menu_all_captures:
        if(allCaptures == null || allCaptures.size() <= 0) {
          Toast.makeText(this, "No places", Toast.LENGTH_SHORT).show();
          return true;
        }
        if(frameLayout.findViewById(R.id.captures_list_view) != null) {
          frameLayout.removeView(mListViewCaptures);
        } else {
          removeSettingsViews();
          frameLayout.addView(mListViewCaptures,
              new LinearLayout.LayoutParams(
                  LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        return true;
			
			/* picture size / video */
      case R.id.mycamera_menu_picture_size:
        if(!isVideoCameraMode) {
          if(frameLayout.findViewById(R.id.picture_sizes_list_view) != null) {
            frameLayout.removeView(mListViewPictureSizes);
          } else {
            removeSettingsViews();
            frameLayout.addView(mListViewPictureSizes,
                new LinearLayout.LayoutParams(ImageUtils.getPixels(density, 200),
                    LayoutParams.WRAP_CONTENT));
          }
        } else {
          if(frameLayout.findViewById(R.id.video_sizes_list_view) != null) {
            frameLayout.removeView(mListViewVideoSizes);
          } else {
            removeSettingsViews();
            frameLayout.addView(mListViewVideoSizes,
                new LinearLayout.LayoutParams(ImageUtils.getPixels(density, 200),
                    LayoutParams.WRAP_CONTENT));
          }
        }
        return true;
				
			/* white balance */
      case R.id.mycamera_menu_white_balance:
        if(frameLayout.findViewById(R.id.white_balance_list_view) != null) {
          frameLayout.removeView(mListViewWhiteBalance);
        } else {
          removeSettingsViews();
          frameLayout.addView(mListViewWhiteBalance,
              new LinearLayout.LayoutParams(ImageUtils.getPixels(density, 200),
                  LayoutParams.WRAP_CONTENT));
        }
        return true;
		
			/* color effect */
      case R.id.mycamera_menu_color_effect:
        if(frameLayout.findViewById(R.id.color_effects_list_view) != null) {
          frameLayout.removeView(mListViewColorEffects);
        } else {
          removeSettingsViews();
          frameLayout.addView(mListViewColorEffects,
              new LinearLayout.LayoutParams(ImageUtils.getPixels(density, 200),
                  LayoutParams.WRAP_CONTENT));
        }
        return true;
				
			/* scene mode */
      case R.id.mycamera_menu_scene_mode:
        if(frameLayout.findViewById(R.id.scene_modes_list_view) != null) {
          frameLayout.removeView(mListViewSceneModes);
        } else {
          removeSettingsViews();
          frameLayout.addView(mListViewSceneModes,
              new LinearLayout.LayoutParams(ImageUtils.getPixels(density, 200),
                  LayoutParams.WRAP_CONTENT));
        }
        return true;
			
			/* flash auto */
      case R.id.mycamera_menu_flash_auto:
        item.setChecked(true);
        Parameters p = mCamera.getParameters();
        if(p.getFlashMode().equals(Parameters.FLASH_MODE_AUTO)) {
          return true;
        }
        mCamera.stopPreview();
        p.setFlashMode(Parameters.FLASH_MODE_AUTO);
        mCamera.setParameters(p);
        mCamera.startPreview();
        return true;
				
			/* flash on */
      case R.id.mycamera_menu_flash_on:
        item.setChecked(true);
        if(isVideoCameraMode) {
          videoCameraFlashMode = VIDEO_CAMERA_FLASH_MODE_ON;
        } else {
          Parameters par = mCamera.getParameters();
          if(par.getFlashMode().equals(Parameters.FLASH_MODE_ON)) {
            return true;
          }
          mCamera.stopPreview();
          par.setFlashMode(Parameters.FLASH_MODE_ON);
          mCamera.setParameters(par);
          mCamera.startPreview();
        }
        return true;
			
			/* flash off */
      case R.id.mycamera_menu_flash_off:
        item.setChecked(true);
        if(isVideoCameraMode) {
          videoCameraFlashMode = VIDEO_CAMERA_FLASH_MODE_OFF;
        } else {
          Parameters parameters = mCamera.getParameters();
          if(parameters.getFlashMode().equals(Parameters.FLASH_MODE_OFF)) {
            return true;
          }
          isFlashOn = false;
          mCamera.stopPreview();
          parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
          mCamera.setParameters(parameters);
          mCamera.startPreview();
        }
        return true;
				
			/* exposure compensation */
      case R.id.mycamera_menu_exposure_comp:
        if(frameLayout.findViewById(R.id.the_brightness_bar_layout) != null) {
          frameLayout.removeView(mBrightnessBarLayout);
        } else {
          removeSettingsViews();
          frameLayout.addView(mBrightnessBarLayout,
              new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        return true;
			
			/* show left controls */
      case R.id.mycamera_menu_left_controls_show:
        if(this.findViewById(R.id.activity_photo_left_controls) == null) {
          activityLayout.addView(leftControlsLayout, 0);
          item.setChecked(true);
        }
        return true;
				
			/* hide left controls */
      case R.id.mycamera_menu_left_controls_hide:
        if(this.findViewById(R.id.activity_photo_left_controls) != null) {
          activityLayout.removeView(leftControlsLayout);
          item.setChecked(true);
        }
        return true;
				
			/* reset camera */
      case R.id.mycamera_menu_reset_camera:
        showAlertDialogToResetCamera();
        return true;
			
			/* application settings */
      case R.id.mycamera_menu_app_settings:
        startActivity(new Intent(this, PrefsActivity.class));
        return true;
			
			/* device settings */
      case R.id.mycamera_menu_device_settings:
        startActivity(new Intent(Settings.ACTION_SETTINGS));
        // CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        // CamcorderProfile profile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_HIGH);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }
	/* End : Options Menu */
	
	/* -- send http -- */

  /* Shows alert dialog when user presses to reset camera */
  private void showAlertDialogToSendHTTP() {
    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    alertDialog.setMessage("Send data via HTTP ?");
    alertDialog.setPositiveButton("Send",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            sendCaptureViaHttpTask = new SendCaptureViaHttpTask(selectedPlace);
            sendCaptureViaHttpTask.execute();
          }
        });
    alertDialog.setNegativeButton("Cancel",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {

          }
        });
    alertDialog.show();
  }

  /* Async task that sends Media and Location info to server */
  private class SendCaptureViaHttpTask extends AsyncTask<Void, Void, HttpResponse> {

    private Capture capture = null;

    public SendCaptureViaHttpTask(Capture capture) {
      this.capture = capture;
    }

    @Override
    protected void onPreExecute() {
      sendCaptureViaHttpProgressDialog.show();
    }

    @Override
    protected HttpResponse doInBackground(Void... args) {
      if(capture == null) {
        return null;
      }

      HttpResponse res = null;
      try {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PhotoActivity.this);

        final HttpClient httpClient = new DefaultHttpClient();
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				
				/* Add captured file */
        final String filePath = capture.getMediaFilePath();
        if(filePath != null) {
          final File file = new File(filePath);
          if(file != null && file.exists()) {
            if(capture.getMediaType().equals(Capture.TYPE_IMAGE)) {
              builder.addPart("image", new FileBody(file));
            } else if(capture.getMediaType().equals(Capture.TYPE_VIDEO)) {
              builder.addPart("video", new FileBody(file));
            } else if(capture.getMediaType().equals(Capture.TYPE_AUDIO)) {
              builder.addPart("audio", new FileBody(file));
            }
          }
        }
				
				/* Add Location */
        builder.addTextBody("lat", String.valueOf(capture.getLatitude()));
        builder.addTextBody("lon", String.valueOf(capture.getLongitude()));
				
				/* Create HTTP Post and set Entity */
        httpPost = new HttpPost(settings.getString("server_url", null));
        httpPost.setEntity(builder.build());
				
				/* Execute post request to the server */
        res = httpClient.execute(httpPost);
      } catch(Exception e) {
        // TODO Auto-generated catch block
        return null;
      }
      return res;
    }

    @Override
    protected void onPostExecute(HttpResponse result) {
      if(sendCaptureViaHttpProgressDialog != null && sendCaptureViaHttpProgressDialog.isShowing()) {
        sendCaptureViaHttpProgressDialog.dismiss();
      }

      if(result != null) {
        Toast.makeText(PhotoActivity.this, NetUtils.getResponseText(result), Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(PhotoActivity.this, "No response", Toast.LENGTH_SHORT).show();
      }
    }
  }
}

///* Updates camera settings views */
//private void setupCameraSettingsViews(Camera c) {
//	Camera.Parameters p = c.getParameters();
//	
//	String msg = "";
//	
//	/* Update Zoom Bar */
//	if(isZoomSupported && zoomBar != null) {
//		maxZoomTextView.setText(String.valueOf(maxZoom));
//		zoomBar.setMax(p.getMaxZoom());
//		zoomBar.setProgress(defaultZoom);
//	}
//	else if(zoomBar == null) {
//		msg += "Error updating zoom bar !";
//	}
//	
//	/* Update Exposure Bar */
//	if(isExposureCompensationSupported && brightnessBar != null) {
//		minBrightnessTextView.setText(String.valueOf(minExposureCompensation));
//		maxBrightnessTextView.setText(String.valueOf(maxExposureCompensation));
//		final int barMax = Math.abs(minExposureCompensation) + Math.abs(maxExposureCompensation);
//		brightnessBar.setMax(barMax);
//		final int brightnessProgressValue = defaultExposureCompensation + Math.abs(minExposureCompensation);
//		brightnessBar.setProgress(brightnessProgressValue);
//	}
//	else if(brightnessBar == null) {
//		msg += "\n" + "Error updating brightness bar !";
//	}
//	
//	/* Update Scene Modes ListView */
//	if(isSceneModeSupported && mListViewSceneModes != null) {
//		((ArrayAdapter<?>) mListViewSceneModes.getAdapter()).notifyDataSetChanged();
//		mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(defaultSceneMode), true);
//	}
//	else if(mListViewSceneModes == null) {
//		msg += "\n" + "Error updating Scene Modes !";
//	}
//	
//	/* Update White Balance ListView */
//	if(isWhiteBalanceSupported && mListViewWhiteBalance != null) {
//		((ArrayAdapter<?>) mListViewWhiteBalance.getAdapter()).notifyDataSetChanged();
//		mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.indexOf(defaultWhiteBalance), true);
//	}
//	else if(mListViewWhiteBalance == null) {
//		msg += "\n" + "Error updating White Balance !";
//	}
//	
//	/* Update Color Effects ListView */
//	if(isColorEffectSupported && mListViewColorEffects != null) {
//		((ArrayAdapter<?>) mListViewColorEffects.getAdapter()).notifyDataSetChanged();
//		mListViewColorEffects.setItemChecked(mSupportedColorEffectsList.indexOf(defaultColorEffect), true);
//	}
//	else if(mListViewColorEffects == null) {
//		msg += "\n" + "Error updating Color Effects !";
//	}
//	
//	/* Update Picture Size ListView */
//	if(isPictureSizeSupported && mListViewPictureSizes != null) {
//		((ArrayAdapter<?>) mListViewPictureSizes.getAdapter()).notifyDataSetChanged();
//		final String defaultPictureSizeToString = String.valueOf(defaultPictureSize.width) + " x " + String.valueOf(defaultPictureSize.height);
//		mListViewPictureSizes.setItemChecked(mPictureSizes.indexOf(defaultPictureSizeToString), true);
//	}
//	else if(mListViewPictureSizes == null) {
//		msg += "\n" + "Error updating Picture Size !";
//	}
//	
//	/* Update Video Size ListView */
//	if(isVideoSizeSupported && mListViewVideoSizes != null) {
//		((ArrayAdapter<?>) mListViewVideoSizes.getAdapter()).notifyDataSetChanged();
//		final String defaultVideoSizeToString = String.valueOf(defaultVideoSize.width) + " x " + String.valueOf(defaultVideoSize.height);
//		mListViewVideoSizes.setItemChecked(mVideoSizes.indexOf(defaultVideoSizeToString), true);
//	}
//	else if(mListViewPictureSizes == null) {
//		msg += "\n" + "Error updating Video Size !";
//	}
//	
//	/* Show Error message (if error happened) */
//	if(msg != null && msg.trim().length() > 0) {
//		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
//	}
//}

///* Checks which parameters are supported by the camera */
//private void readSupportedCameraSettings(Camera tCamera) {
//	
//	Camera.Parameters p = tCamera.getParameters();
//	
//	/* Read Flash Mode */
//	if (p.getSupportedFlashModes() != null && p.getSupportedFlashModes().size() > 1) {
//		isFlashModeSupported = true;
//	} 
//	else {
//		isFlashModeSupported = false;
//	}
//	
//	/* Read Zoom */
//	if (p.isZoomSupported()) {
//		defaultZoom = p.getZoom(); 
//		maxZoom = p.getMaxZoom();
//		btnZoom.setEnabled(true);
//		isZoomSupported = true;
//	} 
//	else {
//		isZoomSupported = false;
//		btnZoom.setEnabled(false);
//	}
//	
//	/* Read exposure compensation */
//	if (p.getMaxExposureCompensation() != 0) {
//		defaultExposureCompensation = p.getExposureCompensation(); 																		
//		maxExposureCompensation = p.getMaxExposureCompensation();
//		minExposureCompensation = p.getMinExposureCompensation();
//		isExposureCompensationSupported = true;
//	} 
//	else {
//		isExposureCompensationSupported = false;
//	}
//	
//	/* Read Scene Modes */
//	if (p.getSupportedSceneModes() != null && p.getSupportedSceneModes().size() > 1) {
//		mSupportedSceneModesList.clear();
//		mSupportedSceneModesList.addAll(p.getSupportedSceneModes());
//		defaultSceneMode = p.getSceneMode();
//		isSceneModeSupported = true;
//	} 
//	else {
//		isSceneModeSupported = false;
//		defaultSceneMode = null;
//	}
//	
//	/* Read White Balances */																
//	if (p.getSupportedWhiteBalance() != null && p.getSupportedWhiteBalance().size() > 1) {
//		mSupportedWhiteBalanceList.clear();
//		mSupportedWhiteBalanceList.addAll(p.getSupportedWhiteBalance());
//		defaultWhiteBalance = p.getWhiteBalance();
//		isWhiteBalanceSupported = true;
//	} 
//	else {
//		isWhiteBalanceSupported = false;
//		defaultWhiteBalance = null;
//	}
//	
//	/* Read Color Effects */
//	if (p.getSupportedColorEffects() != null && p.getSupportedColorEffects().size() > 1) {
//		mSupportedColorEffectsList.clear();
//		mSupportedColorEffectsList.addAll(p.getSupportedColorEffects());
//		defaultColorEffect = p.getColorEffect();
//		isColorEffectSupported = true;
//	} 
//	else {
//		isColorEffectSupported = false;
//		defaultColorEffect = null;
//	}
//	
//	/* Read Picture Sizes */
//	if (p.getSupportedPictureSizes() != null && p.getSupportedPictureSizes().size() > 1) {
//		mSupportedPictureSizesList.clear();
//		mSupportedPictureSizesList.addAll(p.getSupportedPictureSizes());
//		mPictureSizes.clear();
//		if (mSupportedPictureSizesList != null) {
//			Size tPictureSize;
//			for (int i = 0; i < mSupportedPictureSizesList.size(); i++) {
//				tPictureSize = mSupportedPictureSizesList.get(i);
//				mPictureSizes.add(String.valueOf(tPictureSize.width) + " x " + String.valueOf(tPictureSize.height));
//			}
//			defaultPictureSize = p.getPictureSize();
//			isPictureSizeSupported = true;
//		} 
//		else {
//			defaultPictureSize = null;
//			isPictureSizeSupported = false;
//		}
//	}
//	
//	/* Read Video Sizes */
//	if (p.getSupportedVideoSizes() != null && p.getSupportedVideoSizes().size() > 1) {
//		mSupportedVideoSizesList.clear();
//		mSupportedVideoSizesList.addAll(p.getSupportedVideoSizes());
//		mVideoSizes.clear();
//		if (mSupportedVideoSizesList != null) {
//			Size tVideoSize;
//			for (int i = 0; i < mSupportedVideoSizesList.size(); i++) {
//				tVideoSize = mSupportedVideoSizesList.get(i);
//				mVideoSizes.add(String.valueOf(tVideoSize.width) + " x " + String.valueOf(tVideoSize.height));
//			}
//			defaultVideoSize = mSupportedVideoSizesList.get(0);
//			selectedVideoSize = defaultVideoSize;
//			isVideoSizeSupported = true;
//		} 
//		else {
//			defaultVideoSize = null;
//			isVideoSizeSupported = false;
//		}
//	}
//}