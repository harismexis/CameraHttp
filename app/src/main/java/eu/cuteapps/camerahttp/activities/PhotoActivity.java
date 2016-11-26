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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

import eu.cuteapps.camerahttp.constants.TextConstants;
import eu.cuteapps.camerahttp.mycamerapreview.CameraPreview;
import eu.cuteapps.camerahttp.R;
import eu.cuteapps.camerahttp.constants.Actions;
import eu.cuteapps.camerahttp.constants.CameraConstants;
import eu.cuteapps.camerahttp.constants.GalleryFileTypes;
import eu.cuteapps.camerahttp.constants.HttpParams;
import eu.cuteapps.camerahttp.constants.Prefs;
import eu.cuteapps.camerahttp.myadapters.CapturesAdapter;
import eu.cuteapps.camerahttp.mysqlite.Capture;
import eu.cuteapps.camerahttp.mysqlite.MySQLiteCapturesDataSource;
import eu.cuteapps.camerahttp.myutils.DateUtil;
import eu.cuteapps.camerahttp.myutils.LogUtils;
import eu.cuteapps.camerahttp.myutils.ViewUtils;
import eu.cuteapps.camerahttp.myutils.LocationUtils;
import eu.cuteapps.camerahttp.myutils.MyFileUtils;
import eu.cuteapps.camerahttp.myutils.MyProgressDialogs;
import eu.cuteapps.camerahttp.myutils.NetUtils;

public class PhotoActivity extends AppCompatActivity implements ConnectionCallbacks,
    OnConnectionFailedListener, LocationListener {

  private static final String TAG = "PhotoActivity";

  private static final int ONE_SECOND_IN_MILLIS = 1000;
  private static final int DEFAULT_CAPTURE_INTERVAL_IN_SEC = 2;
  private int captureIntervalInSec;

  private int videoCameraFlashMode = CameraConstants.VIDEO_CAMERA_FLASH_MODE_OFF;

  private SendCaptureViaHttpTask sendCaptureViaHttpTask;
  private ProgressDialog sendCaptureViaHttpProgressDialog;
  private HttpPost httpPost;

  private AudioManager audioManager;
  private LinearLayout leftControlsLayout;
  private LinearLayout activityLayout;
  private int delayAfterCapture = ONE_SECOND_IN_MILLIS;
  private boolean isShutterSoundEnabled = true;
  private File lastCapturedMediaFile;

  private Location mLastLocation;
  private GoogleApiClient mGoogleApiClient;

  private CapturePhotoTask capturePhotoTask;

  private boolean isFlashModeSupported = false;
  private boolean isFlashModeAUTOSupported = false;
  private boolean isFlashModeONSupported = false;
  private boolean isFlashModeOFFSupported = false;
  private boolean isFlashOn = false;

  private SeekBar zoomBar;
  private LinearLayout mZoomBarLayout;
  private TextView minZoomTextView;
  private TextView maxZoomTextView;
  private int defaultZoom;
  private int maxZoom;

  private SeekBar brightnessBar;
  private LinearLayout mBrightnessBarLayout;
  private TextView minBrightnessTextView;
  private TextView maxBrightnessTextView;
  private int maxExposureCompensation;
  private int minExposureCompensation;
  private int defaultExposureCompensation;

  private ListView mListViewWhiteBalance;
  private List<String> mSupportedWhiteBalanceList;
  private String defaultWhiteBalance;

  private ListView mListViewColorEffects;
  private List<String> mSupportedColorEffectsList;
  private String defaultColorEffect;

  private ListView mListViewSceneModes;
  private List<String> mSupportedSceneModesList;
  private String defaultSceneMode;

  private ListView mListViewPictureSizes;
  private List<Size> mSupportedPictureSizesList;
  private ArrayList<String> mPictureSizes;
  private Size defaultPictureSize;
  private Size selectedPictureSize;

  private ListView mListViewVideoSizes;
  private List<Size> mSupportedVideoSizesList;
  private ArrayList<String> mVideoSizes;
  private Size defaultVideoSize;
  private Size selectedVideoSize;

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

  private ImageButton btnAudioCapture;
  private boolean isAudioRecording = false;

  private ImageButton buttonTakePicture;
  private Camera mCamera;
  private CameraPreview mPreview;
  private FrameLayout frameLayout;
  private boolean isCapturingPhoto = false;
  private ImageView mImageViewThumbnail;
  private ProgressDialog cameraProgressDialog;
  private int thumbNailTargetWidth;
  private int thumbNailTargetHeight;

  private AlarmManager alarmManager;
  private Intent periodicCaptureIntent;
  private PendingIntent periodicCapturePendingIntent;
  private boolean isPeriodicCaptureOn = false;
  private int counterPeriodicCapture = 0;
  private boolean infinitePeriodicCapture = false;
  private ImageButton periodicCaptureButton;

  private ImageButton videoButton;
  private MediaRecorder mMediaRecorder;
  private boolean isVideoRecording = false;
  private boolean isVideoCameraMode = false;
  private ImageButton switchPhotoVideoBtn;

  private ListView mListViewCaptures;
  private MySQLiteCapturesDataSource datasource;
  private ArrayList<Capture> allCaptures;
  private Capture selectedCapture;
  private CapturesAdapter capturesAdapter;

  private boolean restoreCameraEffectsInOnResume = false;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_photo);

    PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();

    datasource = new MySQLiteCapturesDataSource(this);
    datasource.open();

    alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    activityLayout = (LinearLayout) this.findViewById(R.id.activity_photo_layout);
    leftControlsLayout = (LinearLayout) this.findViewById(R.id.activity_photo_left_controls);
    frameLayout = (FrameLayout) findViewById(R.id.activity_photo_preview);

    frameLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        removeSettingsViews();
      }
    });

    cameraProgressDialog = MyProgressDialogs.getCircleProgressDialog(this,
        getString(R.string.capturing_photo));
    cameraProgressDialog.setCancelable(false);

    sendCaptureViaHttpProgressDialog = MyProgressDialogs.getCircleProgressDialog(this,
        getString(R.string.sending_capture_via_http));
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

    mListViewCaptures = new ListView(this);
    allCaptures = datasource.getAllModels();
    capturesAdapter = new CapturesAdapter(this, allCaptures);
    mListViewCaptures.setAdapter(capturesAdapter);
    mListViewCaptures.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue));
    mListViewCaptures.setCacheColorHint(ContextCompat.getColor(this, R.color.light_blue));
    mListViewCaptures.setId(R.id.captures_list_view);
    registerForContextMenu(mListViewCaptures);

    btnZoom = (ImageButton) this.findViewById(R.id.activity_photo_button_zoom);
    btnReverse = (ImageButton) this.findViewById(R.id.activity_photo_button_reverse);
    btnAudioCapture = (ImageButton) this.findViewById(R.id.activity_photo_button_audio_capture);

    switchPhotoVideoBtn = (ImageButton) this.findViewById(R.id.activity_photo_switch_video_photo_cam_btn);
    switchPhotoVideoBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if(isPreviewBusy()) {
          Toast.makeText(PhotoActivity.this, R.string.camera_is_busy, Toast.LENGTH_SHORT).show();
          return;
        }
        try {
          saveFlashMode();
          closeCameraAndPreview();
          resetCameraSettingsViews();

          if(isVideoCameraMode) { /* Switch to photo camera */
            switchPhotoVideoBtn.setImageResource(R.mipmap.switch_video_cam);
            periodicCaptureButton.setVisibility(View.VISIBLE);
            videoButton.setVisibility(View.GONE);
            buttonTakePicture.setVisibility(View.VISIBLE);
          } else { /* Switch to video camera */
            switchPhotoVideoBtn.setImageResource(R.mipmap.switch_photo_cam);
            periodicCaptureButton.setVisibility(View.GONE);
            videoButton.setVisibility(View.VISIBLE);
            buttonTakePicture.setVisibility(View.GONE);
          }

          isVideoCameraMode = !isVideoCameraMode;

          if(isFacingBackCamera) {
            mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
          } else {
            mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
          }

          initCameraPreview();

          /* Update Video / Picture size */
          if(isVideoCameraMode) {
            final String selectedVideoSizeToString = String.valueOf(selectedVideoSize.width) +
                " x " + String.valueOf(selectedVideoSize.height);
            mListViewVideoSizes.setItemChecked(mVideoSizes.indexOf(selectedVideoSizeToString), true);
          } else {
            mCamera.stopPreview();
            Parameters params = mCamera.getParameters();
            params.setPictureSize(selectedPictureSize.width, selectedPictureSize.height);
            mCamera.setParameters(params);
            mCamera.startPreview();

            final String selectedPictureSizeToString = String.valueOf(selectedPictureSize.width) +
                " x " + String.valueOf(selectedPictureSize.height);
            mListViewPictureSizes.setItemChecked(mPictureSizes.indexOf(selectedPictureSizeToString),
                true);
          }

          /* Update Flash Mode */
          if(!isVideoCameraMode && isFlashModeSupported) {
            final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(PhotoActivity.this);
            mCamera.stopPreview();
            Parameters params = mCamera.getParameters();
            params.setFlashMode(settings.getString(Prefs.PREF_PHOTO_CAMERA_FLASH_MODE,
                Parameters.FLASH_MODE_OFF));
            mCamera.setParameters(params);
            mCamera.startPreview();
          }
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, R.string.error_switching_photo_video,
              Toast.LENGTH_SHORT).show();
        }
      }
    });

    buttonTakePicture = (ImageButton) this.findViewById(R.id.activity_photo_button_capture);

    periodicCaptureButton = (ImageButton) findViewById(R.id.activity_photo_button_periodic_capture);
    periodicCaptureIntent = new Intent(Actions.ACTION_PERIODIC_CAPTURE);
    periodicCapturePendingIntent = PendingIntent.getBroadcast(this, 1, periodicCaptureIntent,
        PendingIntent.FLAG_CANCEL_CURRENT);

    videoButton = (ImageButton) findViewById(R.id.activity_photo_button_video);
    videoButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if(isVideoRecording) {
          stopVideoRecording();
        } else if(isPreviewBusy()) {
          Toast.makeText(PhotoActivity.this, R.string.camera_is_busy, Toast.LENGTH_SHORT).show();
        } else {
          removeSettingsViews();
          CamcorderProfile camcorderProfile;
          try {
            if(isFacingBackCamera && videoCameraFlashMode == CameraConstants.VIDEO_CAMERA_FLASH_MODE_ON) {
              mCamera.stopPreview();
              Parameters params = mCamera.getParameters();
              params.setFlashMode(Parameters.FLASH_MODE_TORCH);
              mCamera.setParameters(params);
              mCamera.startPreview();
            }

            mMediaRecorder = new MediaRecorder();
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);

            if(isFacingBackCamera) {
              mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
              mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
              camcorderProfile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK,
                  CamcorderProfile.QUALITY_HIGH);
              mMediaRecorder.setProfile(camcorderProfile);
            } else {
              mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
              mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
              camcorderProfile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_FRONT,
                  CamcorderProfile.QUALITY_480P);
              mMediaRecorder.setProfile(camcorderProfile);
              mMediaRecorder.setVideoFrameRate(10);
            }

            mMediaRecorder.setVideoSize(selectedVideoSize.width, selectedVideoSize.height);

            lastCapturedMediaFile = MyFileUtils.getOutputMediaFile(MyFileUtils.MEDIA_TYPE_VIDEO,
                GalleryFileTypes.MEDIA_FOLDER_NAME);

            if(lastCapturedMediaFile != null) {
              mMediaRecorder.setOutputFile(lastCapturedMediaFile.toString());
              sendBroadcast(new Intent( /* Add video to gallery */
                  Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                  Uri.fromFile(lastCapturedMediaFile)));
            } else {
              Toast.makeText(PhotoActivity.this,
                  getString(R.string.error_creating_video_file), Toast.LENGTH_SHORT).show();
            }

            mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
          } catch(Exception e) {
            releaseMediaRecorder();
            Toast.makeText(PhotoActivity.this, R.string.error_recording_video,
                Toast.LENGTH_SHORT).show();
            return;
          }

          videoButton.setImageResource(R.mipmap.stop);
          isVideoRecording = true;
          new SaveCaptureTask().execute(Capture.CAPTURE_TYPE_VIDEO);
        }
      }
    });

    mImageViewThumbnail = (ImageView) findViewById(R.id.activity_photo_camera_thumbnail);
    mImageViewThumbnail.setScaleType(ScaleType.FIT_XY);
    thumbNailTargetWidth = (int) getResources().getDimension(R.dimen.thumbnail_imageview_width);
    thumbNailTargetHeight = (int) getResources().getDimension(R.dimen.thumbnail_imageview_height);
    mImageViewThumbnail.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if(lastCapturedMediaFile == null) {
          Toast.makeText(PhotoActivity.this, R.string.no_captured_media_file_found,
              Toast.LENGTH_SHORT).show();
          return;
        }
        openMediaInGallery(lastCapturedMediaFile);
      }
    });

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
      Toast.makeText(this, R.string.error_checking_for_front_camera, Toast.LENGTH_LONG).show();
    } finally {
      if(c != null) {
        c.release();
      }
    }

    /* Get last used camera to read its parameters */
    if(PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean(Prefs.PREF_IS_FACING_BACK_CAMERA, true)) {
      c = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
    } else {
      c = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }
    readCameraSettingsAndSetUpSettingsViews(c);
    c.release();

    /* Check if camera was in video camera mode so as to update buttons */
    isVideoCameraMode = PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean(Prefs.PREF_IS_VIDEO_CAMERA_MODE, false);
    if(isVideoCameraMode) {
      switchPhotoVideoBtn.setImageResource(R.mipmap.switch_photo_cam);
      buttonTakePicture.setVisibility(View.GONE);
      periodicCaptureButton.setVisibility(View.GONE);
      videoButton.setVisibility(View.VISIBLE);
    }
  }

  private void openMediaInGallery(File mediaFile) {
    if(isPreviewBusy()) {
      Toast.makeText(PhotoActivity.this, R.string.camera_is_busy, Toast.LENGTH_SHORT).show();
      return;
    }
    final Intent intent = new Intent(Intent.ACTION_VIEW);
    if(MyFileUtils.fileIsImage(mediaFile.getName())) {
      intent.setDataAndType(Uri.fromFile(mediaFile), GalleryFileTypes.TYPE_IMAGE);

    } else if(MyFileUtils.fileIsVideo(mediaFile.getName())) {
      intent.setDataAndType(Uri.fromFile(mediaFile), GalleryFileTypes.TYPE_VIDEO);

    } else if(MyFileUtils.fileIsAudio(mediaFile.getName())) {
      intent.setDataAndType(Uri.fromFile(mediaFile), GalleryFileTypes.TYPE_AUDIO);

    } else {
      Toast.makeText(PhotoActivity.this, R.string.unknown_file_type, Toast.LENGTH_SHORT).show();
      return;
    }

    if(intent.resolveActivity(getPackageManager()) == null) {
      Toast.makeText(PhotoActivity.this, R.string.unable_to_complete_this_action,
          Toast.LENGTH_SHORT).show();
      return;
    }
    startActivity(intent);
  }

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
    editor.apply();
  }

  private void restoreFlashMode() {
    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    videoCameraFlashMode = settings.getInt(Prefs.PREF_VIDEO_CAMERA_FLASH_MODE,
        CameraConstants.VIDEO_CAMERA_FLASH_MODE_OFF);
    if(!isVideoCameraMode && isFlashModeSupported) {
      Parameters params = mCamera.getParameters();
      params.setFlashMode(settings.getString(Prefs.PREF_PHOTO_CAMERA_FLASH_MODE,
          Parameters.FLASH_MODE_OFF));
      mCamera.setParameters(params);
    }
  }

  private void createCameraSettingsViews() {
    final LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);

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

    mListViewSceneModes = new ListView(this);
    mSupportedSceneModesList = new ArrayList<>();
    final ArrayAdapter<String> sceneModesAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_single_choice, mSupportedSceneModesList);
    mListViewSceneModes.setAdapter(sceneModesAdapter);
    mListViewSceneModes.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    mListViewSceneModes.setBackgroundColor(Color.GRAY);
    mListViewSceneModes.setCacheColorHint(Color.GRAY);
    mListViewSceneModes.setSelector(ContextCompat.getDrawable(this, R.drawable.listselector));
    mListViewSceneModes.setId(R.id.scene_modes_list_view);
    mListViewSceneModes.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
        String selectedSceneMode = mSupportedSceneModesList.get(position);
        try {
          mCamera.stopPreview();
          Parameters p = mCamera.getParameters();

          String previousWhiteBalance = null;
          if(isWhiteBalanceSupported) {
            previousWhiteBalance = p.getWhiteBalance();
          }

          String previousColorEffect = null;
          if(isColorEffectSupported) {
            previousColorEffect = p.getColorEffect();
          }

          mCamera.setParameters(p);

          Parameters parameters = mCamera.getParameters();
          parameters.setSceneMode(selectedSceneMode);
          mCamera.setParameters(parameters);
          mCamera.startPreview();

          /* Check if white balance changed and if so, update list view */
          String whiteBalanceMessage = TextConstants.EMPTY_STRING;
          if(previousWhiteBalance != null) {
            final String newWhiteBalance = mCamera.getParameters().getWhiteBalance();
            if(!previousWhiteBalance.equals(newWhiteBalance)) {
              mListViewWhiteBalance.setItemChecked(
                  mSupportedWhiteBalanceList.indexOf(newWhiteBalance),
                  true);
              whiteBalanceMessage = getString(R.string.white_balance_changed_from_to,
                  previousWhiteBalance, newWhiteBalance);
            }
          }

          /* Check if color effect changed and if so, update list view */
          String colorEffectMessage = TextConstants.EMPTY_STRING;
          if(previousColorEffect != null) {
            final String newColorEffect = mCamera.getParameters().getColorEffect();
            if(!previousColorEffect.equals(newColorEffect)) {
              mListViewColorEffects.setItemChecked(
                  mSupportedColorEffectsList.indexOf(newColorEffect),
                  true);
              colorEffectMessage = getString(R.string.color_effect_changed_from_to,
                  previousColorEffect, newColorEffect);
            }
          }

          /* Notify user for affected parameters */
          final String userMessage = (whiteBalanceMessage + TextConstants.TWO_NEW_LINES +
              colorEffectMessage).trim();
          if(!userMessage.equals(TextConstants.EMPTY_STRING)) {
            Toast.makeText(PhotoActivity.this, userMessage, Toast.LENGTH_LONG).show();
          }
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, R.string.error_setting_scene_mode,
              Toast.LENGTH_SHORT).show();
        }
      }
    });

    mListViewWhiteBalance = new ListView(this);
    mSupportedWhiteBalanceList = new ArrayList<>();
    final ArrayAdapter<String> whiteBalanceAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_single_choice, mSupportedWhiteBalanceList);
    mListViewWhiteBalance.setAdapter(whiteBalanceAdapter);
    mListViewWhiteBalance.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    mListViewWhiteBalance.setBackgroundColor(Color.GRAY);
    mListViewWhiteBalance.setCacheColorHint(Color.GRAY);
    mListViewWhiteBalance.setSelector(ContextCompat.getDrawable(this, R.drawable.listselector));
    mListViewWhiteBalance.setId(R.id.white_balance_list_view);
    mListViewWhiteBalance.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
        String selectedWhiteBalance = mSupportedWhiteBalanceList.get(position);
        try {
          mCamera.stopPreview();
          Parameters p = mCamera.getParameters();

          /* Set scene mode to default */
          String sceneModeMessage = TextConstants.EMPTY_STRING;
          if(isSceneModeSupported) {
            final String prevSceneMode = p.getSceneMode();
            if(!prevSceneMode.equals(defaultSceneMode)) {
              p.setSceneMode(defaultSceneMode);
              mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(defaultSceneMode),
                  true);
              sceneModeMessage = getString(R.string.scene_mode_changed_from_to,
                  prevSceneMode, defaultSceneMode);
            }
          }

          /* Set color effect to default */
          String colorEffectMessage = TextConstants.EMPTY_STRING;
          if(isColorEffectSupported) {
            String previousColorEffect = p.getColorEffect();
            if(!previousColorEffect.equals(defaultColorEffect)) {
              p.setColorEffect(defaultColorEffect);
              mListViewColorEffects.setItemChecked(mSupportedColorEffectsList.
                  indexOf(defaultColorEffect), true);
              colorEffectMessage = getString(R.string.color_effect_changed_from_to,
                  previousColorEffect, defaultColorEffect);
            }
          }

          mCamera.setParameters(p);

          /* Set selected white balance */
          Parameters params = mCamera.getParameters();
          params.setWhiteBalance(selectedWhiteBalance);

          mCamera.setParameters(params);
          mCamera.startPreview();

          /* Alert user for affected parameters */
          final String userMessage = (sceneModeMessage + TextConstants.TWO_NEW_LINES +
              colorEffectMessage).trim();
          if(!userMessage.equals(TextConstants.EMPTY_STRING)) {
            Toast.makeText(PhotoActivity.this, userMessage, Toast.LENGTH_LONG).show();
          }
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, R.string.error_setting_white_balance,
              Toast.LENGTH_SHORT).show();
        }
      }
    });

    mListViewColorEffects = new ListView(this);
    mSupportedColorEffectsList = new ArrayList<>();
    final ArrayAdapter<String> colorEffectsAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_single_choice, mSupportedColorEffectsList);
    mListViewColorEffects.setAdapter(colorEffectsAdapter);
    mListViewColorEffects.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    mListViewColorEffects.setBackgroundColor(Color.GRAY);
    mListViewColorEffects.setCacheColorHint(Color.GRAY);
    mListViewColorEffects.setSelector(ContextCompat.getDrawable(this, R.drawable.listselector));
    mListViewColorEffects.setId(R.id.color_effects_list_view);
    mListViewColorEffects.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
        String selectedColorEffect = mSupportedColorEffectsList.get(position);
        try {
          mCamera.stopPreview();
          Parameters p = mCamera.getParameters();

          /* Set scene mode to default */
          String sceneModeMessage = TextConstants.EMPTY_STRING;
          if(isSceneModeSupported) {
            final String prevSceneMode = p.getSceneMode();
            if(!prevSceneMode.equals(defaultSceneMode)) {
              p.setSceneMode(defaultSceneMode);
              mListViewSceneModes.setItemChecked(mSupportedSceneModesList.
                  indexOf(defaultSceneMode), true);
              sceneModeMessage = getString(R.string.scene_mode_changed_from_to,
                  prevSceneMode, defaultSceneMode);
            }
          }

          /* Set white balance to default */
          String whiteBalanceMessage = TextConstants.EMPTY_STRING;
          if(isWhiteBalanceSupported) {
            final String prevWhiteBalance = p.getWhiteBalance();
            if(!prevWhiteBalance.equals(defaultWhiteBalance)) {
              p.setWhiteBalance(defaultWhiteBalance);
              mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.
                  indexOf(defaultWhiteBalance), true);
              whiteBalanceMessage = getString(R.string.white_balance_changed_from_to,
                  prevWhiteBalance, defaultWhiteBalance);
            }
          }

          mCamera.setParameters(p);

          /* Set selected color effect (ONLY this way worked!) */
          Parameters params = mCamera.getParameters();
          params.setColorEffect(selectedColorEffect);
          mCamera.setParameters(params);
          mCamera.startPreview();

          /* Alert user for affected parameters */
          final String userMessage = (sceneModeMessage + TextConstants.TWO_NEW_LINES +
              whiteBalanceMessage).trim();
          if(!userMessage.equals(TextConstants.EMPTY_STRING)) {
            Toast.makeText(PhotoActivity.this, userMessage, Toast.LENGTH_LONG).show();
          }
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, R.string.error_setting_color_effect,
              Toast.LENGTH_LONG).show();
        }
      }
    });

    mListViewPictureSizes = new ListView(this);
    mSupportedPictureSizesList = new ArrayList<>();
    mPictureSizes = new ArrayList<>();
    final ArrayAdapter<String> pictureSizesAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_single_choice, mPictureSizes);
    mListViewPictureSizes.setAdapter(pictureSizesAdapter);
    mListViewPictureSizes.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    mListViewPictureSizes.setBackgroundColor(Color.GRAY);
    mListViewPictureSizes.setCacheColorHint(Color.GRAY);
    mListViewPictureSizes.setSelector(ContextCompat.getDrawable(this, R.drawable.listselector));
    mListViewPictureSizes.setId(R.id.picture_sizes_list_view);
    mListViewPictureSizes.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
        final String selectedPictureSizeToString = mPictureSizes.get(position);
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
          Toast.makeText(PhotoActivity.this, R.string.error_setting_picture_size,
              Toast.LENGTH_SHORT).show();
        }
      }
    });

    mListViewVideoSizes = new ListView(this);
    mSupportedVideoSizesList = new ArrayList<>();
    mVideoSizes = new ArrayList<>();
    final ArrayAdapter<String> videoSizesAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_single_choice, mVideoSizes);
    mListViewVideoSizes.setAdapter(videoSizesAdapter);
    mListViewVideoSizes.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    mListViewVideoSizes.setBackgroundColor(Color.GRAY);
    mListViewVideoSizes.setCacheColorHint(Color.GRAY);
    mListViewVideoSizes.setSelector(ContextCompat.getDrawable(this, R.drawable.listselector));
    mListViewVideoSizes.setId(R.id.video_sizes_list_view);
    mListViewVideoSizes.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
        final String selectedVideoSizeToString = mVideoSizes.get(position);
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
          Toast.makeText(PhotoActivity.this, R.string.error_updating_video_size,
              Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  private void readCameraSettingsAndSetUpSettingsViews(Camera tCamera) {
    Parameters p = tCamera.getParameters();

    /* Flash Mode */
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

    /* Zoom */
    if(p.isZoomSupported()) {
      defaultZoom = p.getZoom();
      maxZoom = p.getMaxZoom();
      btnZoom.setEnabled(true);
      isZoomSupported = true;

      if(zoomBar != null) {
        maxZoomTextView.setText(String.valueOf(maxZoom));
        zoomBar.setMax(p.getMaxZoom());
        zoomBar.setProgress(defaultZoom);
      }
    } else {
      isZoomSupported = false;
      btnZoom.setEnabled(false);
    }

    /* Exposure compensation */
    if(p.getMaxExposureCompensation() != 0) {
      defaultExposureCompensation = p.getExposureCompensation();
      maxExposureCompensation = p.getMaxExposureCompensation();
      minExposureCompensation = p.getMinExposureCompensation();
      isExposureCompensationSupported = true;

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

    /* Scene Modes */
    if(p.getSupportedSceneModes() != null && p.getSupportedSceneModes().size() > 1) {
      mSupportedSceneModesList.clear();
      mSupportedSceneModesList.addAll(p.getSupportedSceneModes());
      defaultSceneMode = p.getSceneMode();
      isSceneModeSupported = true;

      if(mListViewSceneModes != null) {
        ((ArrayAdapter<?>) mListViewSceneModes.getAdapter()).notifyDataSetChanged();
        mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(defaultSceneMode), true);
      }
    } else {
      isSceneModeSupported = false;
      defaultSceneMode = null;
    }

    /* White Balances */
    if(p.getSupportedWhiteBalance() != null && p.getSupportedWhiteBalance().size() > 1) {
      mSupportedWhiteBalanceList.clear();
      mSupportedWhiteBalanceList.addAll(p.getSupportedWhiteBalance());
      defaultWhiteBalance = p.getWhiteBalance();
      isWhiteBalanceSupported = true;

      if(mListViewWhiteBalance != null) {
        ((ArrayAdapter<?>) mListViewWhiteBalance.getAdapter()).notifyDataSetChanged();
        mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.indexOf(defaultWhiteBalance),
            true);
      }
    } else {
      isWhiteBalanceSupported = false;
      defaultWhiteBalance = null;
    }

    /* Color Effects */
    if(p.getSupportedColorEffects() != null && p.getSupportedColorEffects().size() > 1) {
      mSupportedColorEffectsList.clear();
      mSupportedColorEffectsList.addAll(p.getSupportedColorEffects());
      defaultColorEffect = p.getColorEffect();
      isColorEffectSupported = true;

      if(mListViewColorEffects != null) {
        ((ArrayAdapter<?>) mListViewColorEffects.getAdapter()).notifyDataSetChanged();
        mListViewColorEffects.setItemChecked(
            mSupportedColorEffectsList.indexOf(defaultColorEffect), true);
      }
    } else {
      isColorEffectSupported = false;
      defaultColorEffect = null;
    }

    /* Picture Sizes */
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

    /* Video Sizes */
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

  private void restoreCameraSettingsAndUpdateSettingsViews() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    mCamera.stopPreview();
    Parameters p = mCamera.getParameters();

    /* Restore Camera Effects (not when app is first launched) */
    if(!restoreCameraEffectsInOnResume) {
      restoreCameraEffectsInOnResume = true;
    } else {

      if(isZoomSupported) {
        final int lastZoom = prefs.getInt(Prefs.PREF_ZOOM, defaultZoom);
        p.setZoom(lastZoom);
        zoomBar.setProgress(lastZoom);
      }

      if(isExposureCompensationSupported) {
        final int lastExposureCompensation = prefs.getInt(Prefs.PREF_EXPOSURE_COMPENSATION,
            defaultExposureCompensation);
        p.setExposureCompensation(lastExposureCompensation);
        final int lastBrightnessProgressValue = lastExposureCompensation +
            Math.abs(minExposureCompensation);
        brightnessBar.setProgress(lastBrightnessProgressValue);
      }

      if(isSceneModeSupported) {
        final String lastSceneMode = prefs.getString(Prefs.PREF_SCENE_MODE,
            defaultSceneMode);
        p.setSceneMode(lastSceneMode);
        mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(lastSceneMode), true);
      }

      if(isWhiteBalanceSupported) {
        final String lastWhiteBalance = prefs.getString(Prefs.PREF_WHITE_BALANCE,
            defaultWhiteBalance);
        p.setWhiteBalance(lastWhiteBalance);
        mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.indexOf(lastWhiteBalance), true);
      }

      if(isColorEffectSupported) {
        final String lastColorEffect = prefs.getString(Prefs.PREF_COLOR_EFFECT,
            defaultColorEffect);
        p.setColorEffect(lastColorEffect);
        mListViewColorEffects.setItemChecked(mSupportedColorEffectsList.indexOf(lastColorEffect), true);
      }
    }
    isVideoCameraMode = prefs.getBoolean(Prefs.PREF_IS_VIDEO_CAMERA_MODE, false);
    mCamera.setParameters(p);
    restorePictureAndVideoSize();
    restoreFlashMode();
    mCamera.startPreview();
  }

  private void restorePictureAndVideoSize() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    if(isPictureSizeSupported) {
      int lastPictureSizeWidth = -1;
      int lastPictureSizeHeight = -1;

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
        final String lastPictureSizeToString = String.valueOf(lastPictureSizeWidth) +
            " x " + String.valueOf(lastPictureSizeHeight);
        mListViewPictureSizes.setItemChecked(mPictureSizes.indexOf(lastPictureSizeToString), true);
      }
    }

    if(isVideoSizeSupported) {
      int lastVideoSizeWidth = -1;
      int lastVideoSizeHeight = -1;

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

  private void saveCameraSettings() {
    if(mCamera == null) {
      return;
    }
    Parameters p = mCamera.getParameters();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = prefs.edit();
    if(isZoomSupported) {
      editor.putInt(Prefs.PREF_ZOOM, p.getZoom());
    }
    if(isExposureCompensationSupported) {
      editor.putInt(Prefs.PREF_EXPOSURE_COMPENSATION, p.getExposureCompensation());
    }
    if(isSceneModeSupported) {
      editor.putString(Prefs.PREF_SCENE_MODE, p.getSceneMode());
    }
    if(isWhiteBalanceSupported) {
      editor.putString(Prefs.PREF_WHITE_BALANCE, p.getWhiteBalance());
    }
    if(isColorEffectSupported) {
      editor.putString(Prefs.PREF_COLOR_EFFECT, p.getColorEffect());
    }
    if(lastCapturedMediaFile != null) {
      editor.putString(Prefs.PREF_LAST_CAPTURED_FILE_PATH, lastCapturedMediaFile.getAbsolutePath());
    } else {
      editor.putString(Prefs.PREF_LAST_CAPTURED_FILE_PATH, null);
    }
    editor.putBoolean(Prefs.PREF_IS_VIDEO_CAMERA_MODE, isVideoCameraMode);
    editor.putBoolean(Prefs.PREF_IS_FACING_BACK_CAMERA, isFacingBackCamera);
    editor.apply();
    savePictureAndVideoSize();
    saveFlashMode();
  }

  private void savePictureAndVideoSize() {
    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = settings.edit();
    if(isPictureSizeSupported) {
      if(isFacingBackCamera) {
        editor.putInt(Prefs.PREF_BACK_CAMERA_PICTURE_SIZE_WIDTH, selectedPictureSize.width);
        editor.putInt(Prefs.PREF_BACK_CAMERA_PICTURE_SIZE_HEIGHT, selectedPictureSize.height);
      } else {
        editor.putInt(Prefs.PREF_FRONT_CAMERA_PICTURE_SIZE_WIDTH, selectedPictureSize.width);
        editor.putInt(Prefs.PREF_FRONT_CAMERA_PICTURE_SIZE_HEIGHT, selectedPictureSize.height);
      }
    }
    if(isVideoSizeSupported) {
      if(isFacingBackCamera) {
        editor.putInt(Prefs.PREF_BACK_CAMERA_VIDEO_SIZE_WIDTH, selectedVideoSize.width);
        editor.putInt(Prefs.PREF_BACK_CAMERA_VIDEO_SIZE_HEIGHT, selectedVideoSize.height);
      } else {
        editor.putInt(Prefs.PREF_FRONT_CAMERA_VIDEO_SIZE_WIDTH, selectedVideoSize.width);
        editor.putInt(Prefs.PREF_FRONT_CAMERA_VIDEO_SIZE_HEIGHT, selectedVideoSize.height);
      }
    }
    editor.apply();
  }

  private void resetCameraSettingsAndSettingsViews() {
    mCamera.stopPreview();
    Parameters parameters = mCamera.getParameters();
    if(isFlashModeSupported) {
      parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
    }
    if(isZoomSupported) {
      parameters.setZoom(defaultZoom);
      zoomBar.setProgress(defaultZoom);
    }
    if(isExposureCompensationSupported) {
      parameters.setExposureCompensation(defaultExposureCompensation);
      brightnessBar.setProgress(defaultExposureCompensation + Math.abs(minExposureCompensation));
    }
    if(isSceneModeSupported) {
      parameters.setSceneMode(defaultSceneMode);
      mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(defaultSceneMode), true);
    }
    if(isWhiteBalanceSupported) {
      parameters.setWhiteBalance(defaultWhiteBalance);
      mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.indexOf(defaultWhiteBalance), true);
    }
    if(isColorEffectSupported) {
      parameters.setColorEffect(defaultColorEffect);
      mListViewColorEffects.setItemChecked(mSupportedColorEffectsList.indexOf(defaultColorEffect), true);
    }
    if(isPictureSizeSupported) {
      final String defaultPictureSizeToString = String.valueOf(defaultPictureSize.width) +
          " x " + String.valueOf(defaultPictureSize.height);
      mListViewPictureSizes.setItemChecked(mPictureSizes.indexOf(defaultPictureSizeToString), true);
    }
    if(isVideoSizeSupported) {
      final String defaultVideoSizeToString = String.valueOf(defaultVideoSize.width) +
          " x " + String.valueOf(defaultVideoSize.height);
      mListViewVideoSizes.setItemChecked(mVideoSizes.indexOf(defaultVideoSizeToString), true);
    }
    mCamera.setParameters(parameters);
    mCamera.startPreview();
  }

  private void resetCameraSettingsViews() {
    if(isZoomSupported) {
      zoomBar.setProgress(defaultZoom);
    }
    if(isExposureCompensationSupported) {
      brightnessBar.setProgress(defaultExposureCompensation);
    }
    if(isSceneModeSupported) {
      mListViewSceneModes.setItemChecked(mSupportedSceneModesList.indexOf(defaultSceneMode), true);
    }
    if(isWhiteBalanceSupported) {
      mListViewWhiteBalance.setItemChecked(mSupportedWhiteBalanceList.indexOf(defaultWhiteBalance), true);
    }
    if(isColorEffectSupported) {
      mListViewColorEffects.setItemChecked(mSupportedColorEffectsList.indexOf(defaultColorEffect), true);
    }
    if(isPictureSizeSupported) {
      final String defaultPictureSizeToString = String.valueOf(defaultPictureSize.width) +
          " x " + String.valueOf(defaultPictureSize.height);
      mListViewPictureSizes.setItemChecked(mPictureSizes.indexOf(defaultPictureSizeToString), true);
    }
    if(isVideoSizeSupported) {
      final String defaultVideoSizeToString = String.valueOf(defaultVideoSize.width) +
          " x " + String.valueOf(defaultVideoSize.height);
      mListViewVideoSizes.setItemChecked(mVideoSizes.indexOf(defaultVideoSizeToString), true);
    }
  }

  private void restorePreferences() {
    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    final String captureIntervalToString = settings.getString(Prefs.PREF_PERIODIC_CAPTURE_INTERVAL,
        String.valueOf(DEFAULT_CAPTURE_INTERVAL_IN_SEC));
    try {
      captureIntervalInSec = Integer.parseInt(captureIntervalToString);
    } catch(Exception e) {
      captureIntervalInSec = DEFAULT_CAPTURE_INTERVAL_IN_SEC;
    }

    try {
      delayAfterCapture = ONE_SECOND_IN_MILLIS * Integer.parseInt(settings
          .getString(Prefs.PREF_DELAY_AFTER_CAPTURE, "1"));
    } catch(Exception e) {
      delayAfterCapture = ONE_SECOND_IN_MILLIS;
    }

    final String shutter = settings.getString(Prefs.PREF_SHUTTER_SOUND, "enabled");
    isShutterSoundEnabled = shutter.equals("enabled");
  }

  @Override
  public void onConnected(Bundle connectionHint) {
    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
      return;
    }
    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    LocationRequest mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(LocationUtils.LOCATION_UPDATE_INTERVAL);
    mLocationRequest.setFastestInterval(LocationUtils.LOCATION_UPDATE_FASTEST_INTERVAL);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
  }

  @Override
  public void onConnectionFailed(ConnectionResult result) {
    Toast.makeText(this, R.string.connection_to_google_play_services_failed,
        Toast.LENGTH_LONG).show();
  }

  @Override
  public void onConnectionSuspended(int cause) {
    Toast.makeText(this, R.string.connection_to_google_play_services_suspended,
        Toast.LENGTH_SHORT).show();
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
    registerReceiver(periodicCaptureReceiver, new IntentFilter(Actions.ACTION_PERIODIC_CAPTURE));
    initCameraAndPreview();
    restoreCameraSettingsAndUpdateSettingsViews();
    restorePreferences();
    restoreLastCapturedMediaAndSetThumbnail();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if(isAudioRecording) {
      stopAudioRecording();
    }
    if(isVideoRecording) {
      stopVideoRecording();
    }
    if(isPeriodicCaptureOn) {
      stopPeriodicCapture();
    }
    unregisterReceiver(periodicCaptureReceiver);
    saveCameraSettings();
    closeCameraAndPreview();
    datasource.close();
    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
  }

  @Override
  public void onStop() {
    mGoogleApiClient.disconnect();
    super.onStop();
  }

  @Override
  public void onBackPressed() {
    if(!removeSettingsViews()) {
      super.onBackPressed();
    }
  }

  private Camera getCameraInstance(int cameraId) {
    Camera camera;
    try {
      camera = Camera.open(cameraId);
    } catch(Exception e) {
      return null;
    }
    return camera;
  }

  private void initCameraAndPreview() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    isFacingBackCamera = prefs.getBoolean(Prefs.PREF_IS_FACING_BACK_CAMERA, true);
    if(isFacingBackCamera) {
      mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
    } else {
      mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }
    initCameraPreview();
  }

  private void initCameraPreview() {
    mPreview = new CameraPreview(this, mCamera);
    frameLayout.addView(mPreview);
  }

  private void closeCameraAndPreview() {
    if(mCamera != null) {
      mCamera.release();
      mCamera = null;
      frameLayout.removeAllViews();
      mPreview = null;
    }
  }

  public void onClickReverseCamera(View view) {
    if(isPreviewBusy()) {
      Toast.makeText(this, R.string.camera_is_busy, Toast.LENGTH_SHORT).show();
      return;
    }

    if(!isFrontCameraSupported) {
      Toast.makeText(this, R.string.front_camera_is_not_supported, Toast.LENGTH_SHORT).show();
      return;
    }

    /* Reverse camera */
    try {
      if(!isVideoCameraMode && isFacingBackCamera) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Prefs.PREF_PHOTO_CAMERA_FLASH_MODE, mCamera.getParameters().getFlashMode());
        editor.apply();
      }
      savePictureAndVideoSize();
      closeCameraAndPreview();

      if(isFacingBackCamera) {
        mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
      } else {
        mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
      }

      isFacingBackCamera = !isFacingBackCamera;

      initCameraPreview();
      mCamera.stopPreview();
      readCameraSettingsAndSetUpSettingsViews(mCamera);
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
      Toast.makeText(this, R.string.error_reversing_camera, Toast.LENGTH_LONG).show();
    }
  }

  private boolean isPreviewBusy() {
    return isCapturingPhoto || isAudioRecording || isVideoRecording || isPeriodicCaptureOn;
  }

  private final ShutterCallback mShutterCallback = new ShutterCallback() {
    public void onShutter() {
      audioManager.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
    }
  };

  private final ShutterCallback mSilentShutterCallback = new ShutterCallback() {
    public void onShutter() {
      audioManager.playSoundEffect(AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }
  };

  private class CapturePhotoTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected void onPreExecute() {
      buttonTakePicture.setEnabled(false);
      isCapturingPhoto = true;
    }

    @Override
    protected Void doInBackground(Void... params) {
      if(!isShutterSoundEnabled) {
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        mCamera.takePicture(mSilentShutterCallback, null, mPictureCallback);
      } else {
        mCamera.takePicture(mShutterCallback, null, mPictureCallback);
      }

      try {
        Thread.sleep(delayAfterCapture);
      } catch(InterruptedException e) {
        e.printStackTrace();
      }

      /* Store Capture to database */
      final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PhotoActivity.this);
      if(settings.getString(Prefs.PREF_STORE_CAPTURES_TO_DB, "yes").equals("yes")) {
        try {
          datasource.addCaptureToDatabase(
              LocationUtils.getStringLatitude(mLastLocation),
              LocationUtils.getStringLongitude(mLastLocation),
              DateUtil.getCurrentDateTime(),
              Capture.CAPTURE_TYPE_IMAGE,
              lastCapturedMediaFile.getAbsolutePath());
          allCaptures.clear();
          allCaptures.addAll(datasource.getAllModels());
        } catch(Exception e) {
          LogUtils.log(TAG, e.getMessage());
        }
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      if(capturesAdapter != null) {
        capturesAdapter.notifyDataSetChanged();
      }

      mCamera.startPreview();
      isCapturingPhoto = false;
      buttonTakePicture.setEnabled(true);

      /* If periodic capture is enabled, continue capturing */
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
        alarmManager.set(AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + captureIntervalInSec * ONE_SECOND_IN_MILLIS,
            periodicCapturePendingIntent);
      }
    }
  }

  private PictureCallback mPictureCallback = new PictureCallback() {
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
      lastCapturedMediaFile = MyFileUtils.getOutputMediaFile(MyFileUtils.MEDIA_TYPE_IMAGE,
          GalleryFileTypes.MEDIA_FOLDER_NAME);
      if(lastCapturedMediaFile == null) {
        return;
      }
      try {
        final FileOutputStream fos = new FileOutputStream(lastCapturedMediaFile);
        fos.write(data);
        fos.close();
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
            Uri.fromFile(lastCapturedMediaFile)));
        setThumbnailPic(thumbNailTargetWidth, thumbNailTargetHeight);
        if(!isShutterSoundEnabled) {
          audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        }
      } catch(Exception e) {

      }
    }
  };

  public void onClickCapturePhoto(View view) {
    if(isPreviewBusy()) {
      Toast.makeText(this, R.string.camera_is_busy, Toast.LENGTH_SHORT).show();
      return;
    }
    capturePhoto();
  }

  private void capturePhoto() {
    removeSettingsViews();
    capturePhotoTask = new CapturePhotoTask();
    capturePhotoTask.execute();
  }

  private void restoreLastCapturedMediaAndSetThumbnail() {
    if(lastCapturedMediaFile == null) {
      final String path = PreferenceManager.getDefaultSharedPreferences(PhotoActivity.this)
          .getString(Prefs.PREF_LAST_CAPTURED_FILE_PATH, null);

      if(path != null) {
        lastCapturedMediaFile = new File(path);
      }
    }

    /* If last captured photo does not exist, get the last picture from storage folder */
    if(lastCapturedMediaFile == null || !lastCapturedMediaFile.exists()) {
      try {
        final File folder = new File(Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            GalleryFileTypes.MEDIA_FOLDER_NAME);
        final File[] files = folder.listFiles();
        if(files != null && files.length > 0) {
          lastCapturedMediaFile = files[files.length - 1];
        } else {
          lastCapturedMediaFile = null;
        }
      } catch(Exception e) {
        Toast.makeText(this, R.string.error_restoring_thumbnail_icon, Toast.LENGTH_SHORT).show();
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
      mImageViewThumbnail.setImageBitmap(null);
    }
  }

  private void setThumbnailPicFromAudio() {
    mImageViewThumbnail.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.mic_dark));
  }

  private void setThumbnailPicFromVideo() {
    if(lastCapturedMediaFile != null) {
      final Bitmap bmThumbnail = ThumbnailUtils
          .createVideoThumbnail(lastCapturedMediaFile.getAbsolutePath(), Thumbnails.MICRO_KIND);
      mImageViewThumbnail.setImageBitmap(bmThumbnail);
    }
  }

  private void setThumbnailPic(int target_w, int target_h) {
    if(lastCapturedMediaFile != null) {
      BitmapFactory.Options bmOptions = new BitmapFactory.Options();
      bmOptions.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(lastCapturedMediaFile.getAbsolutePath(), bmOptions);
      final int photoW = bmOptions.outWidth;
      final int photoH = bmOptions.outHeight;
      final int scaleFactor = Math.min(photoW / target_w, photoH / target_h);
      bmOptions.inJustDecodeBounds = false;
      bmOptions.inSampleSize = scaleFactor;
      bmOptions.inPurgeable = true;
      Bitmap bitmap = BitmapFactory.decodeFile(lastCapturedMediaFile.getAbsolutePath(), bmOptions);
      mImageViewThumbnail.setImageBitmap(bitmap);
    }
  }

  public void onClickStartPeriodicCapture(View view) {
    if(isPeriodicCaptureOn) {
      stopPeriodicCapture();
    } else if(isPreviewBusy()) {
      Toast.makeText(this, R.string.camera_is_busy, Toast.LENGTH_SHORT).show();
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

  private void startPeriodicCapture(boolean isInfinite) {
    infinitePeriodicCapture = isInfinite;
    isPeriodicCaptureOn = true;
    periodicCaptureButton.setImageResource(R.mipmap.stop);
    Toast.makeText(this,
        getString(R.string.periodic_capture_started_with_interval,
            String.valueOf(captureIntervalInSec)),
        Toast.LENGTH_SHORT).show();
    capturePhoto();
  }

  private void stopPeriodicCapture() {
    isPeriodicCaptureOn = false;
    infinitePeriodicCapture = false;
    periodicCaptureButton.setImageResource(R.mipmap.repeat);
  }

  private void showAlertDialogForPeriodicCapture() {
    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    alertDialog.setMessage(getString(R.string.enter_number_of_captures_or_empty_for_continuous_capture));
    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT);
    input.setLayoutParams(lp);
    alertDialog.setView(input);
    alertDialog.setPositiveButton(getString(R.string.start),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            try {
              counterPeriodicCapture = Integer.parseInt(input.getText().toString());
            } catch(Exception e) {
              Toast.makeText(PhotoActivity.this, R.string.please_enter_a_positive_integer,
                  Toast.LENGTH_SHORT).show();
              return;
            }

            if(counterPeriodicCapture <= 0) {
              Toast.makeText(PhotoActivity.this, R.string.please_enter_a_positive_integer,
                  Toast.LENGTH_SHORT).show();
              return;
            }

            startPeriodicCapture(false);
          }
        });
    alertDialog.setNeutralButton(getString(R.string.infinite), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        startPeriodicCapture(true);
      }
    });
    alertDialog.setNegativeButton(getString(R.string.cancel),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {

          }
        });
    alertDialog.show();
  }

  private class SaveCaptureTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
      final SharedPreferences settings = PreferenceManager
          .getDefaultSharedPreferences(PhotoActivity.this);
      if(settings.getString(Prefs.PREF_STORE_CAPTURES_TO_DB, "yes").equals("yes")) {
        try {
          datasource.addCaptureToDatabase(
              LocationUtils.getStringLatitude(mLastLocation),
              LocationUtils.getStringLongitude(mLastLocation),
              DateUtil.getCurrentDateTime(),
              params[0],
              lastCapturedMediaFile.getAbsolutePath());
          allCaptures.clear();
          allCaptures.addAll(datasource.getAllModels());
        } catch(Exception e) {
          LogUtils.log(TAG, e.getMessage());
        }
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      if(capturesAdapter != null) {
        capturesAdapter.notifyDataSetChanged();
      }
    }
  }

  private void stopVideoRecording() {
    mMediaRecorder.stop();
    releaseMediaRecorder();
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

  private void releaseMediaRecorder() {
    if(mMediaRecorder != null) {
      mMediaRecorder.reset();
      mMediaRecorder.release();
      mMediaRecorder = null;
      mCamera.lock();
    }
  }

  public void onClickCaptureAudio(View view) {
    if(isAudioRecording) {
      stopAudioRecording();
    } else if(isPreviewBusy()) {
      Toast.makeText(this, R.string.camera_is_busy, Toast.LENGTH_SHORT).show();
    } else {
      startAudioRecording();
    }
  }

  private void releaseAudioRecorder() {
    if(mMediaRecorder != null) {
      mMediaRecorder.stop();
      mMediaRecorder.release();
      mMediaRecorder = null;
    }
  }

  private void stopAudioRecording() {
    releaseAudioRecorder();
    btnAudioCapture.setImageResource(R.mipmap.mic);
    isAudioRecording = false;
    setThumbnailPicFromAudio();
  }

  private void startAudioRecording() {
    try {
      mMediaRecorder = new MediaRecorder();
      mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      mMediaRecorder.setOutputFormat(OutputFormat.THREE_GPP);
      mMediaRecorder.setAudioEncoder(AudioEncoder.AMR_NB);
      lastCapturedMediaFile = MyFileUtils.getOutputMediaFile(MyFileUtils.MEDIA_TYPE_AUDIO,
          GalleryFileTypes.MEDIA_FOLDER_NAME);
      if(lastCapturedMediaFile != null) {
        mMediaRecorder.setOutputFile(lastCapturedMediaFile.getAbsolutePath());
      } else {
        Toast.makeText(this, getString(R.string.error_creating_audio_file),
            Toast.LENGTH_SHORT).show();
      }
      mMediaRecorder.prepare();
      mMediaRecorder.start();
    } catch(Exception e) {
      releaseAudioRecorder();
      Toast.makeText(this, R.string.error_starting_audio_recording, Toast.LENGTH_SHORT).show();
      return;
    }

    btnAudioCapture.setImageResource(R.mipmap.mic_stop);
    isAudioRecording = true;
    new SaveCaptureTask().execute(Capture.CAPTURE_TYPE_AUDIO);
  }

  public void onClickHandleZoom(View view) {
    if(isPreviewBusy()) {
      Toast.makeText(this, R.string.camera_is_busy, Toast.LENGTH_SHORT).show();
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

  private boolean removeSettingsViews() {
    if(frameLayout.findViewById(R.id.the_zoom_layout) != null) {
      frameLayout.removeView(mZoomBarLayout);
      return true;
    } else if(frameLayout.findViewById(R.id.the_brightness_bar_layout) != null) {
      frameLayout.removeView(mBrightnessBarLayout);
      return true;
    } else if(frameLayout.findViewById(R.id.white_balance_list_view) != null) {
      frameLayout.removeView(mListViewWhiteBalance);
      return true;
    } else if(frameLayout.findViewById(R.id.color_effects_list_view) != null) {
      frameLayout.removeView(mListViewColorEffects);
      return true;
    } else if(frameLayout.findViewById(R.id.scene_modes_list_view) != null) {
      frameLayout.removeView(mListViewSceneModes);
      return true;
    } else if(frameLayout.findViewById(R.id.picture_sizes_list_view) != null) {
      frameLayout.removeView(mListViewPictureSizes);
      return true;
    } else if(frameLayout.findViewById(R.id.video_sizes_list_view) != null) {
      frameLayout.removeView(mListViewVideoSizes);
      return true;
    } else if(frameLayout.findViewById(R.id.captures_list_view) != null) {
      frameLayout.removeView(mListViewCaptures);
      return true;
    }
    return false;
  }

  private void showAlertDialogToResetCamera() {
    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    alertDialog.setMessage(R.string.reset_camera_settings);
    alertDialog.setPositiveButton(R.string.yes,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            try {
              resetCameraSettingsAndSettingsViews();
            } catch(Exception e) {
              Toast.makeText(PhotoActivity.this, R.string.error_resetting_camera,
                  Toast.LENGTH_SHORT).show();
            }
          }
        });
    alertDialog.setNegativeButton(R.string.cancel,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
          }
        });
    alertDialog.show();
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if(isPreviewBusy()) {
      Toast.makeText(this, R.string.camera_is_busy, Toast.LENGTH_SHORT).show();
      return false;
    }

    if(isFlashModeSupported) {
      menu.findItem(R.id.camera_menu_flash).setEnabled(true);

      final MenuItem menuItemFlashAuto = menu.findItem(R.id.camera_menu_flash_auto);
      if(isFlashModeAUTOSupported) {
        if(isVideoCameraMode) {
          menuItemFlashAuto.setEnabled(false);
        } else {
          menuItemFlashAuto.setEnabled(true);
          if(mCamera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_AUTO)) {
            menuItemFlashAuto.setChecked(true);
          }
        }
      } else {
        menuItemFlashAuto.setEnabled(false);
      }

      final MenuItem menuItemFlashOn = menu.findItem(R.id.camera_menu_flash_on);
      if(isFlashModeONSupported) {
        menuItemFlashOn.setEnabled(true);
        if(isVideoCameraMode) {
          if(videoCameraFlashMode == CameraConstants.VIDEO_CAMERA_FLASH_MODE_ON) {
            menuItemFlashOn.setChecked(true);
          }
        } else {
          if(mCamera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_ON)) {
            menuItemFlashOn.setChecked(true);
          }
        }
      } else {
        menuItemFlashOn.setEnabled(false);
      }

      final MenuItem menuItemFlashOff = menu.findItem(R.id.camera_menu_flash_off);
      if(isFlashModeOFFSupported) {
        menuItemFlashOff.setEnabled(true);
        if(isVideoCameraMode) {
          if(videoCameraFlashMode == CameraConstants.VIDEO_CAMERA_FLASH_MODE_OFF) {
            menuItemFlashOff.setChecked(true);
          }
        } else {
          if(mCamera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_OFF)) {
            menuItemFlashOff.setChecked(true);
          }
        }
      } else {
        menuItemFlashOff.setEnabled(false);
      }
    } else {
      menu.findItem(R.id.camera_menu_flash).setEnabled(false);
    }

    if(!isExposureCompensationSupported) {
      menu.findItem(R.id.camera_menu_exposure_comp).setEnabled(false);
    } else {
      menu.findItem(R.id.camera_menu_exposure_comp).setEnabled(true);
    }

    if(!isSceneModeSupported) {
      menu.findItem(R.id.camera_menu_scene_mode).setEnabled(false);
    } else {
      menu.findItem(R.id.camera_menu_scene_mode).setEnabled(true);
    }

    if(!isWhiteBalanceSupported) {
      menu.findItem(R.id.camera_menu_white_balance).setEnabled(false);
    } else {
      menu.findItem(R.id.camera_menu_white_balance).setEnabled(true);
    }

    if(!isColorEffectSupported) {
      menu.findItem(R.id.camera_menu_color_effect).setEnabled(false);
    } else {
      menu.findItem(R.id.camera_menu_color_effect).setEnabled(true);
    }

    final MenuItem item = menu.findItem(R.id.camera_menu_picture_size);
    if(!isVideoCameraMode) {
      item.setTitle(R.string.picture_size);
    } else {
      item.setTitle(R.string.video_size);
    }

    if(isPictureSizeSupported || isVideoSizeSupported) {
      item.setEnabled(true);
    } else {
      item.setEnabled(false);
    }

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    this.getMenuInflater().inflate(R.menu.context_places, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    selectedCapture = allCaptures.get(info.position);
    if(selectedCapture == null) {
      return false;
    }
    switch(item.getItemId()) {

      case R.id.context_places_send:
        if(!NetUtils.isNetworkConnected(this)) {
          Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
          return true;
        }
        showAlertDialogToSendHTTP();
        return true;

      case R.id.context_places_map:
        String uri = null;
        try {
          final float lat = Float.parseFloat(selectedCapture.getLatitude());
          final float lon = Float.parseFloat(selectedCapture.getLongitude());
          uri = String.format(Locale.ENGLISH, "geo:%f,%f", lat, lon);
        } catch(Exception e) {
          Toast.makeText(PhotoActivity.this, R.string.unknown_location, Toast.LENGTH_SHORT).show();
          return false;
        }

        final Intent intentToOpenMap = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if(intentToOpenMap.resolveActivity(getPackageManager()) != null) {
          startActivity(intentToOpenMap);
        } else {
          Toast.makeText(PhotoActivity.this, R.string.unable_to_complete_this_action,
              Toast.LENGTH_SHORT).show();
        }
        return true;

      case R.id.context_places_open:
        if(selectedCapture.getMediaFilePath() == null) {
          Toast.makeText(this, R.string.no_captured_media_file_found, Toast.LENGTH_SHORT);
          return false;
        }
        openMediaInGallery(new File(selectedCapture.getMediaFilePath()));
        return true;

      case R.id.context_places_delete:
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(getString(R.string.delete_selected_capture));
        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            try {
              final int indexOfSelectedLinkToDelete = allCaptures.indexOf(selectedCapture);
              allCaptures.remove(indexOfSelectedLinkToDelete);
              datasource.deleteCaptureById(selectedCapture.getId());
              capturesAdapter.notifyDataSetChanged();
              if(allCaptures.size() <= 0) {
                frameLayout.removeView(mListViewCaptures);
              }
              Toast.makeText(PhotoActivity.this, getString(R.string.capture_deleted),
                  Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
              Toast.makeText(PhotoActivity.this, getString(R.string.error_deleting_capture),
                  Toast.LENGTH_SHORT).show();
            }
          }
        });
        alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {

          }
        });
        alertDialog.show();
        return true;

      case R.id.context_places_delete_all:
        AlertDialog.Builder alertDialogDeleteAll = new AlertDialog.Builder(this);
        alertDialogDeleteAll.setMessage(getString(R.string.delete_all_captures));
        alertDialogDeleteAll.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            try {
              allCaptures.clear();
              datasource.deleteAllCaptures();
              capturesAdapter.notifyDataSetChanged();
              frameLayout.removeView(mListViewCaptures);
              Toast.makeText(PhotoActivity.this, getString(R.string.all_captures_deleted),
                  Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
              Toast.makeText(PhotoActivity.this, getString(R.string.error_deleting_captures),
                  Toast.LENGTH_SHORT).show();
            }
          }
        });
        alertDialogDeleteAll.setNegativeButton(getString(R.string.no),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {

              }
            });
        alertDialogDeleteAll.show();
        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.camera_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {

      case R.id.camera_menu_all_captures:
        if(allCaptures == null || allCaptures.size() <= 0) {
          Toast.makeText(this, getString(R.string.no_captures), Toast.LENGTH_SHORT).show();
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

      case R.id.camera_menu_picture_size:
        if(!isVideoCameraMode) {
          if(frameLayout.findViewById(R.id.picture_sizes_list_view) != null) {
            frameLayout.removeView(mListViewPictureSizes);
          } else {
            removeSettingsViews();
            frameLayout.addView(mListViewPictureSizes,
                new LinearLayout.LayoutParams(ViewUtils.getPixelsFromDps(200, this),
                    LayoutParams.WRAP_CONTENT));
          }
        } else {
          if(frameLayout.findViewById(R.id.video_sizes_list_view) != null) {
            frameLayout.removeView(mListViewVideoSizes);
          } else {
            removeSettingsViews();
            frameLayout.addView(mListViewVideoSizes,
                new LinearLayout.LayoutParams(ViewUtils.getPixelsFromDps(200, this),
                    LayoutParams.WRAP_CONTENT));
          }
        }
        return true;

      case R.id.camera_menu_white_balance:
        if(frameLayout.findViewById(R.id.white_balance_list_view) != null) {
          frameLayout.removeView(mListViewWhiteBalance);
        } else {
          removeSettingsViews();
          frameLayout.addView(mListViewWhiteBalance,
              new LinearLayout.LayoutParams(ViewUtils.getPixelsFromDps(200, this),
                  LayoutParams.WRAP_CONTENT));
        }
        return true;

      case R.id.camera_menu_color_effect:
        if(frameLayout.findViewById(R.id.color_effects_list_view) != null) {
          frameLayout.removeView(mListViewColorEffects);
        } else {
          removeSettingsViews();
          frameLayout.addView(mListViewColorEffects,
              new LinearLayout.LayoutParams(ViewUtils.getPixelsFromDps(200, this),
                  LayoutParams.WRAP_CONTENT));
        }
        return true;

      case R.id.camera_menu_scene_mode:
        if(frameLayout.findViewById(R.id.scene_modes_list_view) != null) {
          frameLayout.removeView(mListViewSceneModes);
        } else {
          removeSettingsViews();
          frameLayout.addView(mListViewSceneModes,
              new LinearLayout.LayoutParams(ViewUtils.getPixelsFromDps(200, this),
                  LayoutParams.WRAP_CONTENT));
        }
        return true;

      case R.id.camera_menu_flash_auto:
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

      case R.id.camera_menu_flash_on:
        item.setChecked(true);
        if(isVideoCameraMode) {
          videoCameraFlashMode = CameraConstants.VIDEO_CAMERA_FLASH_MODE_ON;
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

      case R.id.camera_menu_flash_off:
        item.setChecked(true);
        if(isVideoCameraMode) {
          videoCameraFlashMode = CameraConstants.VIDEO_CAMERA_FLASH_MODE_OFF;
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

      case R.id.camera_menu_exposure_comp:
        if(frameLayout.findViewById(R.id.the_brightness_bar_layout) != null) {
          frameLayout.removeView(mBrightnessBarLayout);
        } else {
          removeSettingsViews();
          frameLayout.addView(mBrightnessBarLayout,
              new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        return true;

      case R.id.camera_menu_left_controls_show:
        if(this.findViewById(R.id.activity_photo_left_controls) == null) {
          activityLayout.addView(leftControlsLayout, 0);
          item.setChecked(true);
        }
        return true;

      case R.id.camera_menu_left_controls_hide:
        if(this.findViewById(R.id.activity_photo_left_controls) != null) {
          activityLayout.removeView(leftControlsLayout);
          item.setChecked(true);
        }
        return true;

      case R.id.camera_menu_reset_camera:
        showAlertDialogToResetCamera();
        return true;

      case R.id.camera_menu_app_settings:
        startActivity(new Intent(this, PrefsActivity.class));
        return true;

      case R.id.camera_menu_device_settings:
        startActivity(new Intent(Settings.ACTION_SETTINGS));
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void showAlertDialogToSendHTTP() {
    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
    alertDialog.setMessage(getString(R.string.send_capture_via_http));
    alertDialog.setPositiveButton(getString(R.string.send),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            sendCaptureViaHttpTask = new SendCaptureViaHttpTask(selectedCapture);
            sendCaptureViaHttpTask.execute();
          }
        });
    alertDialog.setNegativeButton(getString(R.string.cancel),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {

          }
        });
    alertDialog.show();
  }

  private class SendCaptureViaHttpTask extends AsyncTask<Void, Void, HttpResponse> {
    private Capture capture;

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

      HttpResponse res;
      try {
        final SharedPreferences settings = PreferenceManager
            .getDefaultSharedPreferences(PhotoActivity.this);
        final HttpClient httpClient = new DefaultHttpClient();
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        /* Add captured file */
        final String filePath = capture.getMediaFilePath();
        if(filePath != null) {
          final File file = new File(filePath);
          if(file.exists()) {
            if(capture.getMediaType().equals(Capture.CAPTURE_TYPE_IMAGE)) {
              builder.addPart(HttpParams.HTTP_PARAM_NAME_IMAGE, new FileBody(file));
            } else if(capture.getMediaType().equals(Capture.CAPTURE_TYPE_VIDEO)) {
              builder.addPart(HttpParams.HTTP_PARAM_NAME_VIDEO, new FileBody(file));
            } else if(capture.getMediaType().equals(Capture.CAPTURE_TYPE_AUDIO)) {
              builder.addPart(HttpParams.HTTP_PARAM_NAME_AUDIO, new FileBody(file));
            }
          }
        }

        builder.addTextBody(HttpParams.HTTP_PARAM_NAME_LAT, String.valueOf(capture.getLatitude()));
        builder.addTextBody(HttpParams.HTTP_PARAM_NAME_LON, String.valueOf(capture.getLongitude()));

        httpPost = new HttpPost(settings.getString(Prefs.PREF_SERVER_URL, null));
        httpPost.setEntity(builder.build());

        res = httpClient.execute(httpPost);
      } catch(Exception e) {
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
        Toast.makeText(PhotoActivity.this, getString(R.string.no_server_response), Toast.LENGTH_SHORT).show();
      }
    }
  }
}