package com.readboy.wearlauncher.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.readboy.wearlauncher.R;
import com.readboy.wearlauncher.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class WeatherController extends BroadcastReceiver{
	private static String TAG = "WeatherController";
	public static final String ACTION_WEATHER_RESULT = "com.readboy.wearlauncher.weather.WEATHER_RESULT";
	public static final String ACTION_WEATHER_GET = "com.readboy.wearlauncher.weather.GET_WEATHER";
	//最美天气
	//public static final String ACTION_WEATHER_RESULT = "com.icoolme.android.weather.WEATHER_RESULT";
	//public static final String ACTION_WEATHER_GET ="com.icoolme.android.weather.GET_WEATHER";

	public static final String WEATHER_PACKAGE_NAME = "com.readboy.wearweather";
	public static final String WEATHER_CLASS_NAME = "com.readboy.wearweather.MainActivity";
	
	private Context mContext;
	ArrayList<WeatherChangedCallback> mWeatherChangedCallbacks =
            new ArrayList<WeatherChangedCallback>();
	
	public interface WeatherChangedCallback {
        void onWeatherChanged(String weatherCode, String temperature);
    }
	
	public void addWeatherChangedCallback(WeatherChangedCallback cb) {
		mWeatherChangedCallbacks.add(cb);
    }

    public void removeWeatherChangedCallback(WeatherChangedCallback cb) {
    	mWeatherChangedCallbacks.remove(cb);
    }
	
	public WeatherController(Context context){
		mContext = context;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(TextUtils.equals(ACTION_WEATHER_RESULT, action)){
			Bundle bundle = intent.getBundleExtra("WeatherResult");
			if(bundle != null){
				try {
					String weatherCode = bundle.getString("weather_code");
					String temperature = bundle.getString("temperature");
					String weather = bundle.getString("weather");
					Log.i("Weather","weatherCode=" +weatherCode + ", temperature="+temperature + ", weather="+weather);
					for (WeatherChangedCallback cb : mWeatherChangedCallbacks) {
						cb.onWeatherChanged(weatherCode, temperature);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				for (String key: bundle.keySet()){  
					Log.i("Weather", "WEATHER_RESULT Key=" + key + ", content=" +bundle.getString(key));
				} 
			}
		}
	}
	
	public void startWeatherService(){
    	Intent intent = new Intent();
    	intent.setAction(ACTION_WEATHER_GET);
    	intent.putExtra("sendBroadWhenDataChange",true);
    	ArrayList<String> result = new ArrayList<String>();
    	result.add("weather_code");
    	result.add("temperature");
    	intent.putStringArrayListExtra("result", result);
    	Intent tmp = Utils.createExplicitFromImplicitIntent(mContext,intent);
    	if(tmp != null){
    		Intent eintent = new Intent(tmp);
    		mContext.startService(eintent);
    	}else{
    		for (WeatherChangedCallback cb : mWeatherChangedCallbacks) {
	            cb.onWeatherChanged(null, null);
	        }
    	}
    }

	/*public void startWeatherService(){
		Intent intent = new Intent();
		intent.setAction(ACTION_WEATHER_GET);
		intent.putExtra("defaultCity2", true);
		intent.putExtra("sendBroadWhenDataChange",true);
		intent.putExtra("appID","3001");
		ArrayList<String> result = new ArrayList<String>();
		result.add("wea");
		result.add("tmp");
		intent.putStringArrayListExtra("result", result);
		Intent tmp = Utils.createExplicitFromImplicitIntent(mContext,intent);
		if(tmp != null){
			Intent eintent = new Intent(tmp);
			mContext.startService(eintent);
		}else{
			for (WeatherChangedCallback cb : mWeatherChangedCallbacks) {
				cb.onWeatherChanged(null, null);
			}
		}
	}*/
	
	public static Bitmap getWeatherBitmapFromAsset(Context context,String path, String name) {
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour >= 6 && hour <= 18) {
			path = "weather_icon_day/";
		} else {
			path = "weather_icon_night/";
		}
		String assetName = path + name;
		try {
			//eclipse
//			AssetManager assetManager = context.getAssets();
//			InputStream is = assetManager.open(assetName);
			//as
			InputStream is = context.getClass().getClassLoader().getResourceAsStream("assets/"+assetName);
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get weather description base on weather code
	 * @param context
	 * @param weatherCode
	 * @return
	 */
	public static String getWeatherTypeByWeatherCode(Context context,String weatherCode){
		int type = 999;
		try{
			if(weatherCode != null){
				type = Integer.parseInt(weatherCode);
			}
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		String[] w = context.getResources().getStringArray(R.array.weather_style);
		if(mWeatherTypeMap.containsKey(type)){
			return w[mWeatherTypeMap.get(type)];
		}
		return w[mWeatherTypeMap.get(Integer.valueOf(999))];
	}
	public static String getWeatherDesc(Context context,String weatherCode ,String temperature){
		if (temperature == null || weatherCode == null){
			return context.getResources().getString(R.string.unkown);
		}
		String desc = "";
		desc = getWeatherTypeByWeatherCode(context,weatherCode);
		return  desc + " " + temperature + "°";
	}
	/**
	 * Get weather icon name base on weather code
	 * @param weatherCode
	 * @return
	 */
	public static String getWeatherIconNameByWeatherCode(String weatherCode){
		int type = -1;
		try{
			if(weatherCode != null){
				type = Integer.parseInt(weatherCode);
			}

		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		for(int key : mWeatherIconMap.keySet()){
			if(type <= key){
				return mWeatherIconMap.get(key);
			}
		}
		return mWeatherIconMap.get(Integer.valueOf(-1));
	}

	private static final HashMap<Integer, String> mWeatherIconMap = new LinkedHashMap <Integer, String>(){
		private static final long serialVersionUID = 1L;
		{
			put(Integer.valueOf(-1), "none.png");
			put(Integer.valueOf(0), "weather_icon_0.png");
			put(Integer.valueOf(1), "weather_icon_1.png");
			put(Integer.valueOf(2), "weather_icon_2.png");
			put(Integer.valueOf(3), "weather_icon_3.png");
			put(Integer.valueOf(4), "weather_icon_4.png");
			put(Integer.valueOf(5), "weather_icon_5.png");
			put(Integer.valueOf(7), "weather_icon_7.png");
			put(Integer.valueOf(8), "weather_icon_8.png");
			put(Integer.valueOf(9), "weather_icon_9.png");
			put(Integer.valueOf(10), "weather_icon_10.png");
			put(Integer.valueOf(13), "weather_icon_13.png");
			put(Integer.valueOf(14), "weather_icon_14.png");
			put(Integer.valueOf(16), "weather_icon_16.png");
			put(Integer.valueOf(18), "weather_icon_18.png");
			put(Integer.valueOf(19), "weather_icon_19.png");
			put(Integer.valueOf(20), "weather_icon_20.png");
			put(Integer.valueOf(29), "weather_icon_29.png");
			put(Integer.valueOf(53), "weather_icon_53.png");

			put(Integer.valueOf(301), "weather_icon_7.png");
			put(Integer.valueOf(302), "weather_icon_14.png");
		}
	};

	private static final HashMap<Integer, Integer> mWeatherTypeMap = new LinkedHashMap<Integer, Integer>(){
		private static final long serialVersionUID = 1L;
		{
			put(Integer.valueOf(0), Integer.valueOf(0));
		    put(Integer.valueOf(1), Integer.valueOf(1));
		    put(Integer.valueOf(2), Integer.valueOf(2));
		    put(Integer.valueOf(3), Integer.valueOf(3));
		    put(Integer.valueOf(4), Integer.valueOf(4));
		    put(Integer.valueOf(5), Integer.valueOf(5));
		    put(Integer.valueOf(6), Integer.valueOf(6));
		    put(Integer.valueOf(7), Integer.valueOf(7));
		    put(Integer.valueOf(8), Integer.valueOf(8));
		    put(Integer.valueOf(9), Integer.valueOf(9));
		    put(Integer.valueOf(10), Integer.valueOf(10));
		    put(Integer.valueOf(11), Integer.valueOf(11));
		    put(Integer.valueOf(12), Integer.valueOf(12));
		    put(Integer.valueOf(13), Integer.valueOf(13));
		    put(Integer.valueOf(14), Integer.valueOf(14));
		    put(Integer.valueOf(15), Integer.valueOf(15));
		    put(Integer.valueOf(16), Integer.valueOf(16));
		    put(Integer.valueOf(17), Integer.valueOf(17));
		    put(Integer.valueOf(18), Integer.valueOf(18));
		    put(Integer.valueOf(19), Integer.valueOf(19));
		    put(Integer.valueOf(20), Integer.valueOf(20));
		    put(Integer.valueOf(21), Integer.valueOf(21));
		    put(Integer.valueOf(22), Integer.valueOf(22));
		    put(Integer.valueOf(23), Integer.valueOf(23));
		    put(Integer.valueOf(24), Integer.valueOf(24));
		    put(Integer.valueOf(25), Integer.valueOf(25));
		    put(Integer.valueOf(26), Integer.valueOf(26));
		    put(Integer.valueOf(27), Integer.valueOf(27));
		    put(Integer.valueOf(28), Integer.valueOf(28));
		    put(Integer.valueOf(29), Integer.valueOf(29));
		    put(Integer.valueOf(30), Integer.valueOf(30));
		    put(Integer.valueOf(31), Integer.valueOf(31));
		    put(Integer.valueOf(32), Integer.valueOf(32));
		    put(Integer.valueOf(33), Integer.valueOf(33));
		    put(Integer.valueOf(34), Integer.valueOf(34));
		    put(Integer.valueOf(35), Integer.valueOf(35));
		    put(Integer.valueOf(53), Integer.valueOf(36));
			put(Integer.valueOf(999), Integer.valueOf(37));
			put(Integer.valueOf(301), Integer.valueOf(38));
			put(Integer.valueOf(302), Integer.valueOf(39));

		}
	};
}
