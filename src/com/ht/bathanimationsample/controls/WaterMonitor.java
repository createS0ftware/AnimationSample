package com.ht.bathanimationsample.controls;


import com.ht.bathanimationsample.R;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;


/********************************************************************
 * 
 *	Custom control to display water change 
 * 
 * 
 *********************************************************************/
public class WaterMonitor extends RelativeLayout
{

	private ImageView 	imgWaterLevel;
	private ImageView 	imgBathMarkings;
	
	private Integer 	finalHeight;
	private Integer 	nextHeight;
	
	private Boolean 	measured;

	private ValueAnimator anim;
	
	public WaterMonitor(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialise(context);
	}

	public WaterMonitor(Context context)
	{
		super(context);
		initialise(context);
	}

	
	
	private void initialise(Context context)
	{
		LayoutInflater layoutInflator = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
	    layoutInflator.inflate( R.layout.bath_view, this );

	    imgWaterLevel 	= (ImageView) findViewById(R.id.img_water);
	    imgBathMarkings = (ImageView) findViewById(R.id.img_monitor);
		
	    measured 		= false;
		nextHeight 		= 0;
		
		
	}
	
	
	/*
	 * 
	 * Method to animate the increase of the water 
	 * There is an offset of - 10 added to account for the edges of the image
	 * 
	 * With more time that offset could have been calculated
	 * 
	 */
	public void increaseLevel(long totalVolume)
	{
		// Measure once here because the image is drawn now 
		
		
		
		if (!measured)
		{
			finalHeight = (imgBathMarkings.getMeasuredHeight() * 25 /32) - 10;
			measured = true;
		}
		
		nextHeight = (int) (nextHeight + (totalVolume * finalHeight/34)); 
				
		if (nextHeight >= finalHeight)
			return;
		
		// Unfortunately the animator needs to be redefined each time
		
		anim = ValueAnimator.ofInt(imgWaterLevel.getMeasuredHeight(), nextHeight );
	    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() 
	    {
	        @Override
	        public void onAnimationUpdate(ValueAnimator valueAnimator) 
	        {
	            int val = (Integer) valueAnimator.getAnimatedValue();
	            ViewGroup.LayoutParams layoutParams = imgWaterLevel.getLayoutParams();
	            layoutParams.height = val;
	            imgWaterLevel.setLayoutParams(layoutParams);
	        }
	    });
	    
	    anim.setDuration(400);
	    anim.start(); 
	}
	
	public void decreaseLevel(Integer units)
	{
		// TODO - Add function to drain water 
	}
	
	
	
}
