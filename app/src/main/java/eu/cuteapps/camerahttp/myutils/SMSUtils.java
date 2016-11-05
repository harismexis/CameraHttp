package eu.cuteapps.camerahttp.myutils;
//package haris.mobile.outland.myutils;
//
//import java.io.ByteArrayInputStream;
//import java.io.OutputStream;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.net.Uri;
//import android.util.Log;
//
//public class SMSUtils {
//	
//	public static Uri insert(Context context, String[] to, String subject, byte[] imageBytes) {
//		
//	    try {
//	    	
//	        Uri destUri = Uri.parse("content://mms");
//
//	        // Get thread id
//	        Set<String> recipients = new HashSet<String>();
//	        recipients.addAll(Arrays.asList(to));
//	        long thread_id = getOrCreateThreadId(context, recipients);
//	        Log.e(">>>>>>>", "Thread ID is " + thread_id);
//
//	        // Create a dummy sms
//	        ContentValues dummyValues = new ContentValues();
//	        dummyValues.put("thread_id", thread_id);
//	        dummyValues.put("body", "Dummy SMS body.");
//	        Uri dummySms = context.getContentResolver().insert(Uri.parse("content://sms/sent"), dummyValues);
//
//	        // Create a new message entry
//	        long now = System.currentTimeMillis();
//	        ContentValues mmsValues = new ContentValues();
//	        mmsValues.put("thread_id", thread_id);
//	        mmsValues.put("date", now/1000L);
//	        mmsValues.put("msg_box", MESSAGE_TYPE_OUTBOX);
//	        //mmsValues.put("m_id", System.currentTimeMillis());
//	        mmsValues.put("read", 1);
//	        mmsValues.put("sub", subject);
//	        mmsValues.put("sub_cs", 106);
//	        mmsValues.put("ct_t", "application/vnd.wap.multipart.related");
//	        mmsValues.put("exp", imageBytes.length);
//	        mmsValues.put("m_cls", "personal");
//	        mmsValues.put("m_type", 128); // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
//	        mmsValues.put("v", 19);
//	        mmsValues.put("pri", 129);
//	        mmsValues.put("tr_id", "T"+ Long.toHexString(now));
//	        mmsValues.put("resp_st", 128);
//
//	        // Insert message
//	        Uri res = context.getContentResolver().insert(destUri, mmsValues);
//	        String messageId = res.getLastPathSegment().trim();
//	        Log.e(">>>>>>>", "Message saved as " + res);
//
//	        // Create part
//	        createPart(context, messageId, imageBytes);
//
//	        // Create addresses
//	        for (String addr : to)
//	        {
//	            createAddr(context, messageId, addr);
//	        }
//
//	        //res = Uri.parse(destUri + "/" + messageId);
//
//	        // Delete dummy sms
//	        context.getContentResolver().delete(dummySms, null, null);
//
//	        return res;
//	    }
//	    catch (Exception e)
//	    {
//	        e.printStackTrace();
//	    }
//
//	    return null;
//	}
//
//	private static Uri createPart(Context context, String id, byte[] imageBytes) throws Exception
//	{
//	    ContentValues mmsPartValue = new ContentValues();
//	    mmsPartValue.put("mid", id);
//	    mmsPartValue.put("ct", "image/png");
//	    mmsPartValue.put("cid", "<" + System.currentTimeMillis() + ">");
//	    Uri partUri = Uri.parse("content://mms/" + id + "/part");
//	    Uri res = context.getContentResolver().insert(partUri, mmsPartValue);
//	    Log.e(">>>>>>>", "Part uri is " + res.toString());
//
//	    // Add data to part
//	    OutputStream os = context.getContentResolver().openOutputStream(res);
//	    ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
//	    byte[] buffer = new byte[256];
//	    for (int len=0; (len=is.read(buffer)) != -1;)
//	    {
//	        os.write(buffer, 0, len);
//	    }
//	    os.close();
//	    is.close();
//
//	    return res;
//	}
//
//	private static Uri createAddr(Context context, String id, String addr) throws Exception
//	{
//	    ContentValues addrValues = new ContentValues();
//	    addrValues.put("address", addr);
//	    addrValues.put("charset", "106");
//	    addrValues.put("type", 151); // TO
//	    Uri addrUri = Uri.parse("content://mms/"+ id +"/addr");
//	    Uri res = context.getContentResolver().insert(addrUri, addrValues);
//	    Log.e(">>>>>>>", "Addr uri is " + res.toString());
//
//	    return res;
//	}
//
//}
