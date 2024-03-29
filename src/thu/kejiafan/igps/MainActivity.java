package thu.kejiafan.igps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaFile;
import com.baidu.frontia.api.FrontiaStorage;
import com.baidu.frontia.api.FrontiaStorageListener.FileProgressListener;
import com.baidu.frontia.api.FrontiaStorageListener.FileTransferListener;
import com.baidu.mobstat.SendStrategyEnum;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Frontia.init(MainActivity.this, Config.APIKEY);
		
		initAll();
		Config.startTime = System.currentTimeMillis();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		Config.statistics.pageviewEnd(this, "MainActivity");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		Config.statistics.pageviewStart(this, "MainActivity");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(1, 1, 0, "数据备份");
		menu.add(1, 2, 0, "完全退出");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case 1:
			Toast.makeText(getApplicationContext(), "请关注我们的后续版本", Toast.LENGTH_SHORT).show();
			break;
		case 2:
			collectInfo();
//			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initAll() {
		// 设置屏幕常亮
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);		
		Config.resources = getResources();
        initWidth();
        initTextView();
        initViewPager();
        
        // 调用百度统计
        Config.statistics = Frontia.getStatistics();
        Config.statistics.setAppDistributionChannel("Weebly");
        Config.statistics.start(SendStrategyEnum.APP_START, 10, 1, false);
        Config.statistics.enableExceptionLog();//开启异常日志
        Config.statistics.setReportId(Config.reportId);
	}
	
	private void initTextView() {
        Config.tvTabPhone = (TextView) findViewById(R.id.tv_tab_phone);
        Config.tvTabNetwork = (TextView) findViewById(R.id.tv_tab_network);
        Config.tvTabAbout = (TextView) findViewById(R.id.tv_tab_about);

        Config.tvTabPhone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Config.mPager.setCurrentItem(0);
			}
		});
        Config.tvTabNetwork.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Config.mPager.setCurrentItem(1);
			}
		});
        Config.tvTabAbout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Config.mPager.setCurrentItem(2);
			}
		});
    }

    private void initViewPager() {
    	Config.mPager = (ViewPager) findViewById(R.id.vPager);
    	Config.fragmentsList = new ArrayList<Fragment>();

		Fragment phoneFragment = new PhoneFragment();
		Fragment networkFragment = new NetworkFragment();
		Fragment aboutFragment = new AboutFragment();

        Config.fragmentsList.add(phoneFragment);
        Config.fragmentsList.add(networkFragment);
        Config.fragmentsList.add(aboutFragment);
        
        Config.mPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), Config.fragmentsList));
        Config.mPager.setCurrentItem(0);
        Config.mPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void initWidth() {
        Config.ivBottomLine = (ImageView) findViewById(R.id.iv_bottom_line);
        Config.bottomLineWidth = Config.ivBottomLine.getLayoutParams().width;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;

        LayoutParams params = (LayoutParams) Config.ivBottomLine.getLayoutParams();
        params.height = 4;
		params.width = (int) (screenW/ 3.0);
        Config.ivBottomLine.setLayoutParams(params);
        
        Config.position_one = (int) (screenW / 3.0);
        Config.position_two = Config.position_one * 2;
        Config.position_three = Config.position_one * 3;
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("Exit");
			builder.setMessage("退出iGPS?");
			builder.setPositiveButton("返回",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							return;
						}
					});
			builder.setNegativeButton("上传并退出",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							Toast.makeText(getApplicationContext(), "uploading...", Toast.LENGTH_LONG).show();
							
							collectInfo();
						}
					});
			builder.show();
		}

		return super.onKeyDown(keyCode, event);
	}
	
	void uploadFile(FrontiaStorage mCloudStorage, final FrontiaFile mFile) {
		final ProgressDialog pDialog = new ProgressDialog(MainActivity.this);
    	pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	pDialog.setTitle("iGPS");
    	pDialog.setIcon(R.drawable.ic_launcher);
    	pDialog.setMessage("log is uploading..."); 
    	pDialog.setIndeterminate(false);
    	pDialog.setCancelable(false);
    	pDialog.show();
    	
    	mCloudStorage.uploadFile(mFile,
                new FileProgressListener() {
                    @Override
                    public void onProgress(String source, long bytes, long total) {                		
                    	pDialog.setMessage("log is uploading: " + bytes * 100 / total + "%");
                    	Config.tvTestReport.setText("log is uploading: " + bytes * 100 / total + "%");
                    }
                },
                new FileTransferListener() {
                    @Override
                    public void onSuccess(String source, String newTargetName) {
                    	mFile.setRemotePath(newTargetName);
//                    	deleteFile();
                    	Config.tvTestReport.setText("日志上传成功");
                    	android.os.Process.killProcess(android.os.Process.myPid());
                    }

                    @Override
                    public void onFailure(String source, int errCode, String errMsg) {
                    	Config.tvTestReport.setText("日志上传失败");
                    	android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }
        );
	}
	
	private String zipFile() {
		try {
			String dataPath = this.getFilesDir().getPath() + "/";			
			String path = dataPath;
			if (Config.wifiState.equals("Disconnected")) {
				path = path + Config.networkTypeString + "_" + Build.MODEL
						+ "_" + Config.IMEI + ".zip";
			} else {
				path = path + "WiFi_" + Config.networkTypeString + "_"
						+ Build.MODEL + "_" + Config.IMEI + ".zip";
			}
			path = path.replace(" ", "_");
			FileOutputStream fos = new FileOutputStream(path);
			ZipOutputStream zos = new ZipOutputStream(fos);
			for (int i = 0; i < Config.filename.length; i++) {
				ZipEntry zipEntry = new ZipEntry(Config.filename[i]);
				zos.putNextEntry(zipEntry);
				File file = new File(dataPath + Config.filename[i]);
				FileInputStream is = new FileInputStream(file);
				byte[] buffer = new byte[10240];
				int byteCount = 0;
				while ((byteCount = is.read(buffer)) >= 0) {
					zos.write(buffer, 0, byteCount);
				}
				zos.flush();
				zos.closeEntry();
				is.close();
			}
			zos.finish();
			zos.close();
			return path;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public void collectInfo() {
    	Config.tvTestReport.setText("log is preparing...");
    	try {
    		Config.fosSpeed.write(("</trkseg>").getBytes());
			Config.fosSpeed.write(System.getProperty("line.separator").getBytes());
			Config.fosSpeed.write(("</trk>").getBytes());
			Config.fosSpeed.write(System.getProperty("line.separator").getBytes());
			Config.fosSpeed.write(("</gpx>").getBytes());
			Config.fosSpeed.write(System.getProperty("line.separator").getBytes());
			Config.fosSpeed.close();
			
			Config.fosDNS.write(("</trkseg>").getBytes());
			Config.fosDNS.write(System.getProperty("line.separator").getBytes());
			Config.fosDNS.write(("</trk>").getBytes());
			Config.fosDNS.write(System.getProperty("line.separator").getBytes());
			Config.fosDNS.write(("</gpx>").getBytes());
			Config.fosDNS.write(System.getProperty("line.separator").getBytes());
			Config.fosDNS.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// 只上传gpx
    	String gpxFileString = Build.MODEL + "_" + Config.IMEI + ".gpx";
    	gpxFileString = gpxFileString.replace(" ", "_");
    	String zipPath = this.getFilesDir().getPath() + "/" + gpxFileString;
    	Config.tvTestReport.setText("log is uploading: " + "0 %");
    	if (zipPath != null) {
    		String remotePath = "";
			if (Config.wifiState.equals("Disconnected")) {
				remotePath = Config.networkTypeString + "_" + gpxFileString;
			} else {
				remotePath = "WiFi_" + gpxFileString;
			}
    		remotePath = remotePath.replace(" ", "_");
    		
    		FrontiaFile mFile = new FrontiaFile();
    		mFile.setNativePath(zipPath);
    		mFile.setRemotePath(remotePath);
    		FrontiaStorage mCloudStorage = Frontia.getStorage();
        	
    		uploadFile(mCloudStorage, mFile);    		
		}
    	
//    	//上传gpx
//    	zipPath = zipFile();
//    	Config.tvTestReport.setText("log is uploading: " + "0 %");
//    	if (zipPath != null) {
//    		String remotePath = "";
//			if (Config.wifiState.equals("Disconnected")) {
//				remotePath = Config.networkTypeString + "_" + Build.MODEL + ".zip";
//			} else {
//				remotePath = "WiFi_" + Config.networkTypeString + "_"
//						+ Build.MODEL + ".zip";
//			}
//    		remotePath = remotePath.replace(" ", "_");
//    		
//    		FrontiaFile mFile = new FrontiaFile();
//    		mFile.setNativePath(zipPath);
//    		mFile.setRemotePath(remotePath);
//    		FrontiaStorage mCloudStorage = Frontia.getStorage();
//        	
//    		uploadFile(mCloudStorage, mFile);    		
//		}
    }
}
