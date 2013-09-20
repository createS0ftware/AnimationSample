package com.ht.akqatest.controls;

import com.ht.akqatest.R;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WaterMonitor extends RelativeLayout
{

	private ImageView imgWaterLevel;
	private ImageView imgBathMarkings;
	
	private Integer finalHeight;
	
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
		
	    
	    
	}
	
	public void increaseLevel(Integer units)
	{
		
		finalHeight = (imgWaterLevel.getMeasuredHeight() *25 /30) + 10 + (units * 25/30);
		
		ValueAnimator anim = ValueAnimator.ofInt(imgWaterLevel.getMeasuredHeight(), finalHeight );
	    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
