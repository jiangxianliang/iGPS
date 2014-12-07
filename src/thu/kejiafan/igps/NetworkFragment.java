package thu.kejiafan.igps;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import thu.kejiafan.igps.R.id;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * 
 * @author XQY
 * "status":102,"message":"MCODE参数不存在，mobile类型mcode参数必需"
 * Line: 115
 */

public class NetworkFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.network, container, false);
		initWidget(view);
		handler4Wifi.post(runnable4Wifi);
		
		return view;
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub		
		super.onPause();
		
		Config.statistics.pageviewEnd(this, "NetworkFragment");
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub		
		super.onResume();
		
		Config.statistics.pageviewStart(this, "NetworkFragment");
	}
	
	private void initWidget(View view) {
    	Config.btnRun = (Button) view.findViewById(id.btnRun);
    	Config.tvDataConnectionState = (TextView) view.findViewById(id.dataConnection);
    	Config.tvWiFiConnection = (TextView) view.findViewById(id.wifiConnection);
		Config.tvMacAddress = (TextView) view.findViewById(id.macAddress);
		Config.tvIPAddress = (TextView) view.findViewById(id.ipAddress); 
		Config.tvWiFiInfo = (TextView) view.findViewById(id.wifiInfo);
		Config.tvTestReport = (TextView) view.findViewById(id.testState);
		Config.tvPhoneLocation = (TextView) view.findViewById(id.phoneLocation);
		Config.tvPhoneLocation.setSingleLine(false);
		
	    Config.btnRun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				if (Config.wifiState.equals("Disconnected")) {
					if (Config.dataConnectionState.equals("Disconnected")
							|| Config.dataConnectionState.equals("Unknown")) {
						Config.tvTestReport.setText("网络已断开，请检查网络连接");
						return;
					}
				}
				
				/**
				 * 0:Unknown
				 * 1:正在查询
				 * 2:查询完毕
				 */
				Config.testFlag = 1;
//				Config.isBtnRun = true;
//				Config.tvTestReport.setText("功能暂未实现，请关注后续版本");
				if (Config.latitude == 0 || Config.longitude == 0) {
					Config.tvTestReport.setText("GPS定位失败: Location is invalid");
				} else {
//					String jsonString = Config.gpsBaiduAPIString
//							+ Config.latitude + "," + Config.longitude
//							+ Config.gpsBaiduAPIString2;
					
					final String jsonString = Config.gpsJuheAPIString
							+ Config.latitude + "&lng=" + Config.longitude
							+ Config.gpsJuheAPIString2;
					Config.tvTestReport.setText("GPS定位中: Location is valid");

					new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
//							String testString = Config.gpsBaiduAPIString
//									+ "39.99613038478137,116.32673446572039"
//									+ Config.gpsBaiduAPIString2;					
							
//							String res = HttpUtil.getRequest(testString);
//							System.out.println(res);
							
//							String jsonString = "http://apis.juhe.cn/geo/?key=ea43042b0eca8a483c399d888ccf2d73&lat=39.99613038478137&lng=116.32673446572039&type=1";
							
							HttpClient client = new DefaultHttpClient();
					        StringBuilder builder = new StringBuilder();
					        HttpGet get = new HttpGet(jsonString);
					        try {
					            HttpResponse response = client.execute(get);
					            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));  
					            for (String s = reader.readLine(); s != null; s = reader.readLine()) {  
					                builder.append(s);
					            }
					            JSONObject jsonObject = new JSONObject(builder.toString());
					            String result = jsonObject.getString("result");
					            jsonObject = new JSONObject(result);
				                String addressString = jsonObject.getString("address");
				                String businessString = jsonObject.getString("business");
				                Config.currentPhoneLocation = addressString + ":" + businessString;
								System.out.println(Config.currentPhoneLocation);
								Config.testFlag = 2;
								handler4ShowLocation.post(runnable4ShowLocation);
					        } catch (Exception e) {
					            e.printStackTrace();
					        }
						}
					}).start();
				}
				
//				//for test
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub				
//						HttpClient client = new DefaultHttpClient();
//				        StringBuilder builder = new StringBuilder();
//				        HttpGet get = new HttpGet(Config.baiduTest);
//				        try {
//				            HttpResponse response = client.execute(get);
//				            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));  
//				            for (String s = reader.readLine(); s != null; s = reader.readLine()) {  
//				                builder.append(s);
//				            }
//				            System.out.println(builder.toString());
//				        } catch (Exception e) {
//				            e.printStackTrace();
//				        }
//					}
//				}).start();
			}
		});
	}
	
	private Handler handler4ShowLocation = new Handler();
	
	private Runnable runnable4ShowLocation = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler4ShowLocation.postDelayed(runnable4ShowLocation, 1000);
			if (Config.testFlag > 1) {
				Config.tvTestReport.setText("GPS定位成功");
				Config.tvPhoneLocation.setText(Config.currentPhoneLocation);
				handler4ShowLocation.removeCallbacks(runnable4ShowLocation);
				Config.testFlag = 0;
			} else {

			}
		}
	};
	
	private Handler handler4Wifi = new Handler();

	private Runnable runnable4Wifi = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Config.tvDataConnectionState.setText(Config.dataConnectionState);
			/**
			 * 是否连接Wifi
			 */
			try {
				ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

				if (activeNetInfo != null
						&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					
					WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
					Config.wifiState = "Connected:" + wifiInfo.getSSID();
					Config.wifiInfo = "RSSI:" + wifiInfo.getRssi() + " LinkSpeed:" + wifiInfo.getLinkSpeed();
					Config.macAddress = wifiInfo.getMacAddress();
					Config.ipAddress = SignalUtil.int2IP(wifiInfo.getIpAddress());
					Config.tvWiFiConnection.setText(Config.wifiState);
					Config.tvMacAddress.setText(Config.macAddress);
					Config.tvIPAddress.setText(Config.ipAddress);
					
				} else {
					Config.wifiState = "Disconnected";
					Config.tvWiFiConnection.setText(Config.wifiState);
					Config.wifiInfo = "WiFi连接后有效";
				}	
				Config.tvWiFiInfo.setText(Config.wifiInfo);
			} catch (Exception e) {
				// TODO: handle exception
			}

			handler4Wifi.postDelayed(runnable4Wifi, 5000);
		}
	};
}