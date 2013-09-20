package com.ht.akqatest.controls;

import com.ht.akqatest.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TemperatureGauge extends RelativeLayout
{

	private final TranslateAnimation degreeChangeAnim = new TranslateAnimation(0, 0, 0, 90);
	
	private ImageView 	imgDialHand;
	
	private TextView 	txtLowerUnit;
	private TextView 	txtUpperUnit;
	private TextView 	txtLowerTens;
	private TextView 	txtUpperTens;
	private TextView 	txtLowerHundreds;
	private TextView 	txtUpperHundreds;
	
	private Float		yRotationCenter;
	private Float		xRotationCenter;
	private Float		currentAngle;
	private Float		finalAngle;

	private Integer		tensDegrees;
	private Integer		unitDegrees;
	private Integer		tenthsDegrees;
	
	private Bitmap		bmpDialHand;
	
	public TemperatureGauge(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialise(context);
	}

	public TemperatureGauge(Context context)
	{
		super(context);
		initialise(context);
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
	 	txtLowerHundreds	= (TextView) findViewById(R.id.temp_hundreds_lower_text_view);
	 	txtUpperHundreds 	= (TextView) findViewById(R.id.temp_hundreds_upper_text_view);
		
	 	yRotationCenter 	= (imgDialHand.getHeight() * 0.1f);
	 		 	
	 	currentAngle		= -90f;
	 	finalAngle			= 0.0f;
	 	
		tensDegrees 		= 0;
		unitDegrees 		= 0;
		tenthsDegrees 		= 0;
	 	
	 	bmpDialHand		= BitmapFactory.decodeResource(getResources(), R.drawable.dial_hand);
	}
	
	public void increaseTemp(Integer units)
	{
		finalAngle += units * 0.33f;
		
		RotateAnimation turnNeedleAnim = new RotateAnimation(currentAngle, finalAngle,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.8f);
		turnNeedleAnim.setAnimationListener(neddleAnimListener);
		turnNeedleAnim.setDuration(2400);
		turnNeedleAnim.setFillEnabled(true);
		turnNeedleAnim.setFillAfter(true);
		    
		
		turnNeedleAnim.setInterpolator(new AccelerateInterpolator());
		imgDialHand.startAnimation(turnNeedleAnim);

		
	}
	
	
	AnimationListener degTensChangeAnimListener = new AnimationListener()
	{

		@Override
		public void onAnimationEnd(Animation arg0)
		{
			 String	val = String.valueOf(tensDegrees -1);
			 String	val2 = String.valueOf(tensDegrees);
			 
			 if (tensDegrees == 0)
				 val = "0";
			 if (tensDegrees <= 10)
			 {
				 val = "0" + val;
				 if (tensDegrees <= 9)
				 {
					 val2 = "0" + val2;
				 }
			 }
			 
			 txtUpperTens.setText(val);
			 txtLowerTens.setText(String.valueOf(val2));
		    	
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
			Matrix rotationMtx = imgDialHand.getImageMatrix();
			rotationMtx.postRotate(finalAngle-7, imgDialHand.getWidth() * 0.5f, imgDialHand.getHeight() * 0.8f);
			imgDialHand.setScaleType(ScaleType.MATRIX); 
			
			imgDialHand.setImageMatrix(rotationMtx);
			
//			Bitmap newDialAngle = Bitmap.createBitmap(bmpDialHand, 0, 0, bmpDialHand.getWidth(),bmpDialHand.getHeight(), rotationMtx, true);
//			imgDialHand.setImageBitmap(newDialAngle);	
//			
			currentAngle = finalAngle;
		//	imgDialHand.invalidate();
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
