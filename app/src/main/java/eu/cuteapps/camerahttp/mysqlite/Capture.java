package eu.cuteapps.camerahttp.mysqlite;

public class Capture {

  public static final String TYPE_IMAGE = "image";
  public static final String TYPE_VIDEO = "video";
  public static final String TYPE_AUDIO = "audio";
  //public static final String TYPE_NONE = "none";

  private String id;
  private String latitude;
  private String longitude;
  private String mediaType;
  private String mediaFilePath;

  public Capture(String id, String latitude, String longitude, String mediaType, String mediaFilePath) {
    this.id = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.mediaType = mediaType;
    this.mediaFilePath = mediaFilePath;
  }

  public Capture(String latitude, String longitude, String mediaType, String mediaFilePath) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.mediaType = mediaType;
    this.mediaFilePath = mediaFilePath;
  }

  public String getAllCaptureInfoToString() {
    return "No " + id + ":\n" + "Latitude = " + latitude + "\n" + "Longitude = " + longitude;
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

  public String getMediaType() {
    return mediaType;
  }

  public String getMediaFilePath() {
    return mediaFilePath;
  }
}

//public String getMediaFilePathToString() {
//if(this.mediaFilePath == null) {
//	return "no file path";
//}
//else {
//	return this.mediaFilePath;
//}
//}

//public String getCaptureInfoToStringNoId() {
//return  
//		"Latitude = " + latitude + "\n" +
//		"Longitude = " + longitude + "\n" +
//		"Media type = " + mediaType + "\n" +
//		"Media path = " + this.getMediaFilePathToString();
//}