package com.ht.bathanimationsample.controls;


import com.ht.bathanimationsample.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;



/********************************************************************
 * 
 *	Custom control to display Temperature change 
 * 
 * 
 *********************************************************************/
public class TemperatureGauge extends RelativeLayout
{

	
	
	private ImageView 	imgDialHand;
	
	private TextView 	txtLowerUnit;
	private TextView 	txtUpperUnit;
	private TextView 	txtLowerTens;
	private TextView 	txtUpperTens;
	private TextView 	txtLowerTenths;
	private TextView 	txtUpperTenths;
	
	private Float		currentAngle;
	private Float		finalAngle;
	private Float		currentTemperature;
	
	
	private Integer		newTensDegrees;
	private Integer		oldTensDegrees;
	private Integer		newUnitsDegrees;
	private Integer		oldUnitsDegrees;
	private Integer		newTenthsDegrees;
	private Integer		oldTenthsDegrees;
	
	private Boolean 	animated;
	
	private Bitmap		bmpDialHand;
	
	private Context 	ctxActivity;
	
	public TemperatureGauge(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialise(context);
		ctxActivity = context;
		animated = false;
	}

	
	
	public TemperatureGauge(Context context)
	{
		super(context);
		initialise(context);
		ctxActivity = context;
	}

	private void initialise(Context context)
	{
		
		
		LayoutInflater layoutInflator = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
	    layoutInflator.inflate( R.layout.temp_gauge_view, this );
	    
	    imgDialHand 		= (ImageView) findViewById(R.id.img_dial);
	    
	    txtLowerUnit 		= (TextView) findViewById(R.id.temp_units_lower_text_view);
	 	txtUpperUnit 		= (TextView) findViewById(R.id.temp_units_upper_text_view);
	 	txtLowerTens 		= (TextView) findViewById(R.id.temp_tens_lower_text_view);
	 	txtUpperTens 		= (TextView) findViewById(R.id.temp_tens_upper_text_view);
	 	txtLowerTenths		= (TextView) findViewById(R.id.temp_hundreds_lower_text_view);
	 	txtUpperTenths 		= (TextView) findViewById(R.id.temp_hundreds_upper_text_view);
	 		 	
	 	currentAngle		= -90f;
	 	finalAngle			= -90f;
	 	
		newTensDegrees 		= 0;
		oldTensDegrees 		= 0;
		newUnitsDegrees 	= 0;
		oldUnitsDegrees 	= 0;
		newTenthsDegrees	= 0;
		oldTenthsDegrees	= 0;
		
		currentTemperature	= 0f;
		
	 	bmpDialHand			= BitmapFactory.decodeResource(getResources(), R.drawable.dial_hand);
	 	
	 
	}
	

	/********************************************************************
	 * 
	 *	Method to display animation on the dial and the figures
	 *
	 *	the numbers flick down like an old clock
	 * 
	 * 
	 *********************************************************************/
	public void increaseTemp(Float tempChange)
	{
		// no need to do any work if nothing has changed
		
		if (tempChange != 0f)
		{
			currentTemperature 	+= tempChange;
			
			/*
			 *  Set tens , units and tenth's
			 *  No need to calculate the same thing more than once
			 */
			
			String temp 		= String.valueOf(currentTemperature);
			Integer indexDot 	= temp.indexOf(".");
			
			newTensDegrees 		= Integer.parseInt(temp.substring(indexDot-2,1));		
			newUnitsDegrees 	= Integer.parseInt(temp.substring(indexDot-1,2));		
			newTenthsDegrees	= Integer.parseInt(temp.substring(indexDot+1,4));
			
			finalAngle 			+= tempChange * 0.4f;
		
			RotateAnimation turnNeedleAnim = new RotateAnimation(currentAngle, finalAngle,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.77f);
			turnNeedleAnim.setAnimationListener(neddleAnimListener);
			turnNeedleAnim.setDuration(800);
			turnNeedleAnim.setFillEnabled(true);
			turnNeedleAnim.setFillAfter(true);
			turnNeedleAnim.setInterpolator(new AccelerateInterpolator());
			
			Animation inAnimation  = AnimationUtils.loadAnimation(ctxActivity, R.anim.degrees_in_anim); 		
			Animation outAnimation = AnimationUtils.loadAnimation(ctxActivity, R.anim.degrees_out_anim);
			
			outAnimation.setAnimationListener(degChangeAnimListener);
	
			
			imgDialHand.startAnimation(turnNeedleAnim);

	
			txtLowerTens.startAnimation( outAnimation );
			txtUpperTens.startAnimation( inAnimation  );
			
			txtLowerUnit.startAnimation( outAnimation );
			txtUpperUnit.startAnimation( inAnimation  );
			
			txtLowerTenths.startAnimation( outAnimation );
			txtUpperTenths.startAnimation( inAnimation  );
		}
	}
	
	
	AnimationListener degChangeAnimListener = new AnimationListener()
	{

		@Override
		public void onAnimationEnd(Animation arg0)
		{
			 String	valTens = String.valueOf(newTensDegrees);
			 String	valUnits = String.valueOf(newUnitsDegrees);
			 String	valTenths= String.valueOf(newTenthsDegrees);
			 
			 
			 oldTenthsDegrees	= newTenthsDegrees;
			 oldTensDegrees 	= newTensDegrees;
			 oldUnitsDegrees 	= newUnitsDegrees;
				
			 txtUpperTens.setText(null);
			 txtLowerTens.setText(valTens);
			 
			 txtUpperUnit.setText(null);
			 txtLowerUnit.setText(valUnits);
			
			 txtUpperTenths.setText(null);
			 txtLowerTenths.setText(valTenths);
		    	
		}

		@Override
		public void onAnimationRepeat(Animation arg0)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0)
		{
			// TODO Auto-generated method stub
			
		}		
	};

	
	private AnimationListener neddleAnimListener = new AnimationListener()
	{

		@Override
		public void onAnimationStart(Animation animation)
		{
			
		}

		@Override
		public void onAnimationEnd(Animation animation)
		{
			// redraw the image in the new location
//			
//			Matrix rotationMtx = imgDialHand.getImageMatrix();
//			rotationMtx.postRotate(finalAngle, imgDialHand.getWidth() * 0.5f, imgDialHand.getHeight() * 0.77f);
//			imgDialHand.setScaleType(ScaleType.MATRIX); 
//			
//			imgDialHand.setImageMatrix(rotationMtx);
			
			currentAngle = finalAngle;
		}

		@Override
		public void onAnimationRepeat(Animation animation)
		{
			
		}
		
	};
	
	public void decreaseTemp(Integer units)
	{
			 
	}
	
}
