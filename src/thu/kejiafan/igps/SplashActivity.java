package thu.kejiafan.igps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Window;

public class SplashActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash_screen);
	    // Make sure the splash screen is shown in portrait orientation
	    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
	    	    
	    getTelephoneInfo();
	    createFilePath();// 创建日志路径
		writeLog();
		
	    new Handler().postDelayed(new Runnable() {  
            public void run() {  
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);  
                SplashActivity.this.finish();
            }
        }, 2000);
	}
	
	private void createFilePath() {
		String gpxContentName = "  <name>iGPS " + Config.phoneModel + " ("
				+ Config.osVersion + " " + Config.providerName + ") "
				+ Config.IMEI + " " + Config.IMSI + "</name>";
		String currentString = Config.gpxDateFormat.format(new Date(System.currentTimeMillis()));
		String gpxString = currentString.replace(" ", "T");
		String gpxDate = "  <time>" + gpxString + "Z</time>";
		String[] logStartString = new String[] {
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
				"<gpx",
				"  version=\"1.1\"",
				"  creator=\"iGPS\"",
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
				"  xmlns=\"http://www.topografix.com/GPX/1/1\"",
				"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1",
				"  http://www.topografix.com/GPX/1/1/gpx.xsd\"",
				"  xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\">",
				"<trk>", gpxContentName, gpxDate, "<trkseg>" };
		try {
			String gpxFileString = Build.MODEL + "_" + Config.IMEI + ".gpx";
			gpxFileString = gpxFileString.replace(" ", "_");
			Config.fosMobile = this.openFileOutput("Mobile.txt", Context.MODE_PRIVATE);
			Config.fosSignal = this.openFileOutput("Signal.txt", Context.MODE_PRIVATE);
			Config.fosSpeed = this.openFileOutput(gpxFileString, Context.MODE_PRIVATE);
			Config.fosCell = this.openFileOutput("Cell.txt", Context.MODE_PRIVATE);
			
			if (android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)) {					
				String mobiPath = android.os.Environment.getExternalStorageDirectory() + "/Wmap";
				File mobileFile = new File(mobiPath);
				mobileFile.mkdirs();
				String pathDate = Config.dirDateFormat.format(new Date(System.currentTimeMillis()));
				String fileNameString = mobiPath + "/WMAP_" + pathDate +".gpx";
				Config.fosDNS = new FileOutputStream(fileNameString, true);
				
				for (int i = 0; i < logStartString.length; i++) {
					Config.fosSpeed.write(logStartString[i].getBytes());
					Config.fosSpeed.write(System.getProperty("line.separator").getBytes());
					Config.fosDNS.write(logStartString[i].getBytes());
					Config.fosDNS.write(System.getProperty("line.separator").getBytes());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getTelephoneInfo() {
    	Config.phoneModel = Build.MODEL + " (" + Build.MANUFACTURER + ")";
		Config.osVersion = Build.VERSION.RELEASE + "  Level:" + Build.VERSION.SDK_INT;
		
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Config.providerName = telephonyManager.getNetworkOperatorName();
        Config.IMEI = telephonyManager.getDeviceId();
		Config.IMSI = telephonyManager.getSubscriberId();
		if (Config.providerName == null) {
			if (Config.IMSI.startsWith("46000")
					|| Config.IMSI.startsWith("46002")
					|| Config.IMSI.startsWith("46007")) {
				Config.providerName = "中国移动";
			} else if (Config.IMSI.startsWith("46001")) {
				Config.providerName = "中国联通";
			} else if (Config.IMSI.startsWith("46003")) {
				Config.providerName = "中国电信";
			} else {
				Config.providerName = "非大陆用户";
			}
		}
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		Config.subtypeName = networkInfo.getSubtypeName();
  	}
	
	void writeLog() {
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String infoString = "PhoneModel=" + Config.phoneModel + 
				"\nosVersion=" + Config.osVersion + 
				"\nProviderName=" + Config.providerName + 
				"\nDetailedState=" + networkInfo.getDetailedState() + 
				"\nReason=" + networkInfo.getReason() + 
				"\nSubtypeName=" + networkInfo.getSubtypeName() + 
				"\nExtraInfo=" + networkInfo.getExtraInfo() + 
				"\nTypeName=" + networkInfo.getTypeName() + 
				"\nIMEI=" + telephonyManager.getDeviceId() + 
				"\nIMSI=" + telephonyManager.getSubscriberId() + 
				"\nNetworkOperatorName=" + telephonyManager.getNetworkOperatorName() + 
				"\nSimOperatorName=" + telephonyManager.getSimOperatorName() + 
				"\nSimSerialNumber=" + telephonyManager.getSimSerialNumber();
		try {
			if (Config.fosMobile != null) {
				Config.fosMobile.write(infoString.getBytes());
				Config.fosMobile.write(System.getProperty("line.separator").getBytes());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
