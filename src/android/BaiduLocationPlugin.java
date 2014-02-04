package com.yili.phonegap.plugins;

import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class BaiduLocationPlugin extends CordovaPlugin {

	private static final String STOP_ACTION = "stop";
	private static final String GET_ACTION = "get";
	private LocationClient locationClient = null;
	private JSONObject jsonObj = new JSONObject();
	private boolean result = false;
	private CallbackContext callbackContext;

	private static final Map<Integer, String> ERROR_MESSAGE_MAP = new HashMap<Integer, String>();

	static {
		ERROR_MESSAGE_MAP.put(61, "GPS定位结果");
		ERROR_MESSAGE_MAP.put(62, "扫描整合定位依据失败。此时定位结果无效。");
		ERROR_MESSAGE_MAP.put(63, "网络异常，没有成功向服务器发起请求。此时定位结果无效。");
		ERROR_MESSAGE_MAP.put(65, "定位缓存的结果。");
		ERROR_MESSAGE_MAP.put(66, "离线定位结果，通过调用时对应的返回结果。");
		ERROR_MESSAGE_MAP.put(67, "离线定位失败，通过调用时对应的返回结果。");
		ERROR_MESSAGE_MAP.put(68, "网络连接失败时，查找本地离线定位时对应的返回结果。");
		ERROR_MESSAGE_MAP.put(161, "表示网络定位结果。");
		ERROR_MESSAGE_MAP.put(162, "服务端定位失败。");
	};

	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
		setCallbackContext(callbackContext);
		if (GET_ACTION.equals(action)) {
			cordova.getActivity().runOnUiThread(new Runnable() {

				public void run() {
					locationClient = new LocationClient(cordova.getActivity());
					locationClient.setAK("aoQOqOOpbMB3qLuYx6Kc9hPG");
					LocationClientOption option = new LocationClientOption();
					option.setOpenGps(false);
					option.setCoorType("bd09ll");
					option.setPriority(LocationClientOption.NetWorkFirst);
					option.setProdName("BaiduLoc");
					option.setScanSpan(5000);
					locationClient.setLocOption(option);

					locationClient.registerLocationListener(new BDLocationListener() {
						public void onReceiveLocation(BDLocation location) {
							if (location == null)
								return;
							try {
								JSONObject coords = new JSONObject();
								coords.put("latitude", location.getLatitude());
								coords.put("longitude", location.getLongitude());
								coords.put("radius", location.getRadius());

								jsonObj.put("coords", coords);

								int locationType = location.getLocType();

								jsonObj.put("locationType", locationType);
								jsonObj.put("code", locationType);
								jsonObj.put("message", ERROR_MESSAGE_MAP.get(locationType));

								switch (location.getLocType()) {
								
								case BDLocation.TypeGpsLocation: 
									coords.put("speed", location.getSpeed());
									coords.put("altitude", location.getAltitude());
									jsonObj.put("SatelliteNumber", location.getSatelliteNumber());
									break;
								
								case BDLocation.TypeNetWorkLocation:
									jsonObj.put("AddrStr", location.getAddrStr());
									break;
								}
								
								Log.d("BaiduLocationPlugin", "run: " + jsonObj.toString());
								callbackContext.success(jsonObj);
								result = true;
							} catch (JSONException e) {
								callbackContext.error(e.getMessage());
								result = true;
							}

						}

						public void onReceivePoi(BDLocation poiLocation) {
							// TODO Auto-generated method stub
						}
					});
					locationClient.start();
					locationClient.requestLocation();
				}

			});
		} else if (STOP_ACTION.equals(action)) {
			locationClient.stop();
			callbackContext.success(200);
		} else {
			callbackContext.error(PluginResult.Status.INVALID_ACTION.toString());
		}

		while (result == false) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return result;
	}

	public void onDestroy() {
		if (locationClient != null && locationClient.isStarted()) {
			locationClient.stop();
			locationClient = null;
		}
		super.onDestroy();
	}

	public CallbackContext getCallbackContext() {
		return callbackContext;
	}

	public void setCallbackContext(CallbackContext callbackContext) {
		this.callbackContext = callbackContext;
	}
}