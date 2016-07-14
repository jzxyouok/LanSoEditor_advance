package com.example.lansongeditordemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.lansoeditor.demo.R;
import com.lansosdk.box.ISprite;

public class TestTouchView extends MediaPoolView{

	  
    public TestTouchView(Context context) {
        super(context);
    }

    public TestTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TestTouchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
  
    ISprite bitmap=null;
    
    @Override
  public boolean onTouchEvent(MotionEvent event) {
  	// TODO Auto-generated method stub
  	
  	
  	 switch (event.getAction())   
       {  
	       case MotionEvent.ACTION_DOWN:  
//	    	   	Log.i("test","ACTION_DOWN:"+event.getRawX()+" Y:"+event.getRawY());
//	    	   	Log.i("test","ACTION_DOWN:"+event.getX()+" Y:"+event.getY());
	    	   	if(bitmap!=null){
	    	   		bitmap.release();
	    	   	}
	    	   	bitmap=obtainBitmapSprite(BitmapFactory.decodeResource(getResources(), R.drawable.arrow_red));
	    	   	if(bitmap!=null){
	    	   		bitmap.setVisibility(ISprite.INVISIBLE);
	    	   	}	
	          return true; 
	       case MotionEvent.ACTION_MOVE:  
	           // No volume/brightness action if coef < 2 or a secondary display is connected  
//	    		Log.i("test","ACTION_MOVE:"+event.getX()+" Y:"+event.getY());
	    		if(bitmap!=null){
	    			bitmap.setPosition(event.getX(),event.getY());
	    			bitmap.setVisibility(ISprite.VISIBLE);
	    		}
	    		  return true;   
	 
	       case MotionEvent.ACTION_UP:  
	           // Seek  
//	    		Log.i("test","ACTION_UP:"+event.getX()+" Y:"+event.getY());
	    		
	    		if(bitmap!=null){
	    			bitmap.setVisibility(ISprite.INVISIBLE);
	    			removeSprite(bitmap);
	    		}
	    		break;
	    }  
  	 	return super.onTouchEvent(event);
  }
    
}
