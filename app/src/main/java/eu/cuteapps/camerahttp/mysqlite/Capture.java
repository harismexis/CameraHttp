package eu.cuteapps.camerahttp.mysqlite;

public class Capture {

  public static final String CAPTURE_TYPE_VIDEO = "video";
  public static final String CAPTURE_TYPE_IMAGE = "image";
  public static final String CAPTURE_TYPE_AUDIO = "audio";

  private String id;
  private String latitude;
  private String longitude;
  private String date;
  private String mediaType;
  private String mediaFilePath;

  public Capture(String id, String latitude, String longitude, String date,
                 String mediaType, String mediaFilePath) {
    this.id = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.date = date;
    this.mediaType = mediaType;
    this.mediaFilePath = mediaFilePath;
  }

  public String getAllCaptureInfoToString() {
    return "No " + id + ": " + mediaType + "\n" +
        "Latitude: " + latitude + "\n" +
        "Longitude: " + longitude + "\n" +
        "Date: " + date;
  }

  public String getId() {
    return id;
  }

  public String getLatitude() {
    return latitude;
  }

  public String getLongitude() {
    return longitude;
  }

  public String getDate() {
    return date;
  }

  public String getMediaType() {
    return mediaType;
  }

  public String getMediaFilePath() {
    return mediaFilePath;
  }
}