package com.ht.bathanimationsample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ht.bathanimationsample.controls.TemperatureGauge;
import com.ht.bathanimationsample.controls.WaterMonitor;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/*****************************************************
 * 
 * 	This Activity simulates a home automation system
 * 
 * 	With more time i would have refined the animations and 
 *  added methods for draining water and allowing the bath 
 *  to cool over time ( updating the temperature via other data remoptely ) 	
 * 
 * @author Hanson Aboagye - July 2013
 * 
 * 
 *****************************************************/
public class BathTempControlActivity extends Activity
{

	/*
	 * Different intervals are needed to show change depending on the 
	 * flow of water 
	 *	
	 * For the purposes of the simulation i have reduced the timings by a factor of 10 and the size of the 
	 * bath
	 * 
	 * changing the SIM_FACTOR will change the speed of the simulation
	 * 
	 */

	private static final Long SIM_FACTOR		= 1l; 

	
	private static final Long HOT_INTERVAL 		= 3600l * SIM_FACTOR;
	private static final Long COLD_INTERVAL 	= 3000l * SIM_FACTOR;
	private static final Long BOTH_INTERVAL		= 1634l * SIM_FACTOR;
	
	private static final Long BATH_VOLUME		= 15l * SIM_FACTOR; 
	
	
	//volume change - millilitres per millisecond (to simplify using the intervals) 
	
	private static final Float COLD_VOL_CHANGE 	= 0.2f;
	private static final Float HOT_VOL_CHANGE 	= 0.167f;
		
	
	private TextView 		txtHotTapLabel;
	private TextView 		txtColdTapLabel;

	private ImageView 		imgHotTap;
	private ImageView 		imgColdTap;
			
	private String 			config = "http://static.content.akqa.net/mobile-test/bath.json"; // set for now 
	
	private JSONObject 		configJSON;
	
	private Float 			currentTemp;
		
	private Handler 		tapAnimationHandler;
	private Handler 		waterTemperatureHandler;
	private Handler 		waterVolumeHandler;

	private TemperatureGauge tempGauge;
	private WaterMonitor 	 waterMonitor;
	
	private Boolean			hotOn = false;
	private Boolean			coldOn = false;
	
	private static Float	coldVolume 		= 0.0f;
	private static Float 	hotVolume 		= 0.0f;
	private static Float	coldTemp 		= 0.0f;
	private static Float 	hotTemp 		= 0.0f;	
	private static Float 	HOT_VOLUME 		= 0f;
	private static Float 	COLD_VOLUME		= 0f;
	
	private static String	coldTempString 	= "0.0";
	private static String 	hotTempString 	= "0.0";
	
	
	private static Long		updateInterval;  
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bath_temp_control);
	
		txtHotTapLabel 	= (TextView) findViewById(R.id.txtHot_tap);
		txtColdTapLabel = (TextView) findViewById(R.id.txtCold_tap);
	
		imgHotTap 		= (ImageView)findViewById(R.id.imgHotTap);
		imgColdTap 		= (ImageView)findViewById(R.id.imgColdTap);
		
		tempGauge 		= (TemperatureGauge) findViewById(R.id.tempGauge); 
		waterMonitor	= (WaterMonitor) findViewById(R.id.waterMonitor1);
		
		tapAnimationHandler		= new Handler();		
		waterTemperatureHandler = new Handler();
		waterVolumeHandler 		= new Handler();
		
		imgHotTap.setOnClickListener(tapClickListener);
		imgColdTap.setOnClickListener(tapClickListener);
		
		
		
		String jsonString = "";
		try
		{
			jsonString = new GetJSONTask().execute().get();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
		
		configJSON = extractJSON(jsonString);
	   
	   
		/*
		 *  In case the file is not retrievable substitute ? for the temperature
		 *  This is more user friendly than displaying 0 which is ambiguous
		 */
		   
		hotTempString 	= configJSON.optString("hot_water");
		coldTempString = configJSON.optString("cold_water");
		
		if (hotTempString.equals(""))
			hotTempString = "?";
		else
			hotTemp = Float.parseFloat(hotTempString);
		   			
		if (coldTempString.equals(""))
			coldTempString = "?";
		else
			coldTemp = Float.parseFloat(coldTempString);
		   
		txtHotTapLabel.setText(txtHotTapLabel.getText()	+ "(" + hotTempString  + " Deg.)");
		txtColdTapLabel.setText(txtColdTapLabel.getText() + "(" + coldTempString  + " Deg.)");
	
		currentTemp 	= 0.0f;
		updateInterval 	= 0l;
	}
		
	/**************************************************************
	 * 
	 * OnClick listener for both taps
	 * 
	 **************************************************************/
	private OnClickListener tapClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			tapAnimationHandler.post(new TapAnimationRunnable(v));
		}
	};
	
	/**************************************************************
	 * 
	 * Method to download the temperature  file
	 * 
	 **************************************************************/
	private String readJSONFile(String url) throws IOException, MalformedURLException
	{
		String line;
		
		URL 			jsonUrl		= new URL(url);
		URLConnection 	conn		= jsonUrl.openConnection();
		InputStream 	resStream	= conn.getInputStream();
	
		BufferedReader 	buffReader 	= new BufferedReader(new InputStreamReader(resStream));
		StringBuilder 	finalString	= new StringBuilder();
		
		while ((line = buffReader.readLine()) != null) 
		{
			finalString.append(line);
		}
      
		
		return finalString.toString();
	}
	
	/**************************************************************
	 * 
	 * Method to extrac JSONObject from the file
	 * 
	 **************************************************************/
	private JSONObject extractJSON(String msg)
	{
		
		if (msg == null)
			return new JSONObject();
		
		Integer start = msg.indexOf("{");
		String rMessage = "";
		
		if (start > 0)
		{
			rMessage = msg.substring(start);
		}
		else
		{
			if (start == 0)
			{
				rMessage = msg;
			}
			else
			{
				return new JSONObject();
			}
		}
		
		try
		{
			return new JSONObject(rMessage);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return new JSONObject();
		}
	}

	
	
	/**********************************************************************
	 * 
	 *	Main procedure to implement the formula to calculate the  
	 * 	water temperature
	 * 
	 *  The formula used is V1 X (C1 - T') = V2 X (T' - C2)
	 * 	where 
	 * 	V1 = volume of cold water
	 *  V2 = volume of hot water
	 *  C1 = temperature of cold water
	 *  C2 = temperature of hot water
	 *  T' = final temperature
	 *  
	 *  We are ignoring time factors and assuming that mixing is instant and uniform
	 *
	 * @param coldTempString 	= C1 
	 * @param hotTempString 	= C2
	 * @param coldVol 			= V1
	 * @param hotVol  			= V2
	 * 
	 * 	  
	 * @return finalTemperature = T'
	 * 
	 **********************************************************************/
	private Float getWaterTemperature(Float coldTemp, Float hotTemp, Float coldVol, Float hotVol)
	{
		
		Float A = coldTemp*coldVol;
		Float B = hotTemp*hotVol;
		
		Float result = (A + B)/(coldVol+hotVol);
		
		return result;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bath_temp_control, menu);
		return true;
	}
	

	/**************************************************************
	 * 
	 * Runnable to update the water temperature
	 * 
	 **************************************************************/
	Runnable updateWaterTemperature = new Runnable()
	{

		@Override
		public void run()
		{

			if (hotOn)
			{
				HOT_VOLUME += updateInterval * HOT_VOL_CHANGE / 1000;
			}			
			if (coldOn)
			{
				COLD_VOLUME += updateInterval * COLD_VOL_CHANGE / 1000;
			}
			
			Float temp = getWaterTemperature(coldTemp,hotTemp,COLD_VOLUME,HOT_VOLUME);
			
			Float tempChange = temp - currentTemp;
						
			tempGauge.increaseTemp(tempChange);
			currentTemp = temp;
			
			Integer totalVolume = Math.round(HOT_VOLUME + COLD_VOLUME);
			
			if (totalVolume <= BATH_VOLUME)
			{
				waterTemperatureHandler.postDelayed(updateWaterTemperature,updateInterval);
			}
			else
			{
				hotOn = false;
				coldOn = false;				
			}			
		}		
	};
	
	/**************************************************************
	 * 
	 * Runnable to update the water volume
	 * 
	 **************************************************************/
	Runnable updateWaterVolume = new Runnable()
	{
		@Override
		public void run()
		{		
			long totalVolume = Math.round(HOT_VOLUME + COLD_VOLUME) / SIM_FACTOR;
			
			if (totalVolume <= BATH_VOLUME)
			{
				
				waterMonitor.increaseLevel(totalVolume);
				waterVolumeHandler.postDelayed(this,updateInterval);
			}
			else
			{
				Toast.makeText(BathTempControlActivity.this, "Bath Full", Toast.LENGTH_LONG).show();
				hotOn = false;
				coldOn = false;
				
			}
		}		
	};

	/**************************************************************
	 * 
	 * 	AsyncTask to download file. May be called more than 
	 * 	once to update temperature
	 * 
	 **************************************************************/	
	private class GetJSONTask extends AsyncTask<Void, Void, String>
    {

		
		@Override
		protected void onPreExecute()
		{
			//Get URL from menu
			
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params)
		{

			String res = null;
			try
			{
				res = readJSONFile(config);
			}
			catch (MalformedURLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return res;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
		}
    }
	

	/**************************************************************
	 * 
	 * Runnable class to run the tap animation.
	 * 
	 * If i had more time i would have linked it to a 
	 * touch Listener to show movement in line with touch
	 * 
	 ***************************************************************/
	private class TapAnimationRunnable implements Runnable, AnimationListener
    {
		View animTarget;
		
		public TapAnimationRunnable(View v)
		{
			animTarget  = v;
		}
		
		
		@Override
		public void run()
		{
			
			Animation turnOnAnimation = AnimationUtils.loadAnimation(BathTempControlActivity.this, 
											R.anim.tap_on_anim);		
			Animation turnOffAnimation = AnimationUtils.loadAnimation(BathTempControlActivity.this, 
											R.anim.tap_off_anim );
			
			
			turnOnAnimation.setAnimationListener( this );
		
			/*
			 *  Need to change the animation update interval with the flow of the water
			 *  This is important to show that the water has stopped at the right time 
			 *	and to issue a warning at the right time
			 *  
			 *  I am synchronizing on updateinterval to make sure that it is not accessed
			 *  by another thread at the same time
			 */
			synchronized(updateInterval)
			{				
			
				if (animTarget.getTag().equals("cold"))
				{
					if (coldOn)
					{
						animTarget.startAnimation(turnOffAnimation);
						coldOn = false;
					}
					else
					{
						animTarget.startAnimation(turnOnAnimation);
						coldOn = true;
						updateInterval = COLD_INTERVAL;
						if (hotOn)
							updateInterval = BOTH_INTERVAL;
					}				
				}
				else
				{
					if (hotOn)
					{
						animTarget.startAnimation(turnOffAnimation);
						hotOn = false;
					}
					else
					{
						animTarget.startAnimation(turnOnAnimation);
						hotOn = true;
						updateInterval = HOT_INTERVAL;
						if (coldOn)
							updateInterval = BOTH_INTERVAL;
					}
					
				}
			}
		}

		@Override
		public void onAnimationStart( Animation animation )
		{
			// NOT USED
		}

		@Override
    	public void onAnimationRepeat( Animation animation )
		{
			// NOT USED
		}

		@Override
		public void onAnimationEnd( Animation animation )
		{
			waterTemperatureHandler.postDelayed(updateWaterTemperature,updateInterval);
			waterVolumeHandler.postDelayed(updateWaterVolume, updateInterval);
		}
    }

}
