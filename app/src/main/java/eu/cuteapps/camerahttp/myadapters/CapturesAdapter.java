package eu.cuteapps.camerahttp.myadapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import eu.cuteapps.camerahttp.R;
import eu.cuteapps.camerahttp.mysqlite.Capture;

public class CapturesAdapter extends ArrayAdapter<Capture> {

  private Context context;
  private ArrayList<Capture> captures;

  private int thumbNailTargetWidth;
  private int thumbNailTargetHeight;

  public CapturesAdapter(Context context, ArrayList<Capture> captures) {
    super(context, R.layout.capture_row, captures);
    this.context = context;
    this.captures = captures;

    thumbNailTargetWidth = (int) context.getResources().getDimension(R.dimen.list_row_imageview_width);
    thumbNailTargetHeight = (int) context.getResources().getDimension(R.dimen.list_row_imageview_height);
  }

  static class ViewHolder {
    protected TextView textView;
    protected ImageView imageView;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if(convertView == null) {
      LayoutInflater inflater = ((Activity) context).getLayoutInflater();
      convertView = inflater.inflate(R.layout.capture_row, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.imageView = (ImageView) convertView.findViewById(R.id.capture_row_icon);
      viewHolder.textView = (TextView) convertView.findViewById(R.id.capture_row_text);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    final Capture capture = captures.get(position);
    if(capture != null) {

      final String mediaFilePath = capture.getMediaFilePath();
      final String mediaType = capture.getMediaType();

      if(mediaFilePath != null && mediaType != null) {

        if(mediaType.equals(Capture.CAPTURE_TYPE_IMAGE)) {

          BitmapFactory.Options bmOptions = new BitmapFactory.Options();
          bmOptions.inJustDecodeBounds = true;
          BitmapFactory.decodeFile(mediaFilePath, bmOptions);
          final int photoW = bmOptions.outWidth;
          final int photoH = bmOptions.outHeight;
          final int scaleFactor = Math.min(photoW / thumbNailTargetWidth,
              photoH / thumbNailTargetHeight);
          bmOptions.inJustDecodeBounds = false;
          bmOptions.inSampleSize = scaleFactor;
          bmOptions.inPurgeable = true;
          Bitmap bitmap = BitmapFactory.decodeFile(mediaFilePath, bmOptions);

          viewHolder.imageView.setImageBitmap(bitmap);

        } else if(mediaType.equals(Capture.CAPTURE_TYPE_VIDEO)) {
          final Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(mediaFilePath,
              Thumbnails.MICRO_KIND);
          viewHolder.imageView.setImageBitmap(bmThumbnail);

        } else if(mediaType.equals(Capture.CAPTURE_TYPE_AUDIO)) {
          viewHolder.imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.mic_dark));
        }
      }
      viewHolder.textView.setText(captures.get(position).getAllCaptureInfoToString());
    }
    return convertView;
  }

}