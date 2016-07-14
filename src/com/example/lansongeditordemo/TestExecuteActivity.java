package com.example.lansongeditordemo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.insta.IF1977Filter;
import org.insta.IFEarlybirdFilter;
import org.insta.IFRiseFilter;

import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHighlightShadowFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageMonochromeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageOpacityFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePixelationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePosterizeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageRGBFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageVignetteFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageWhiteBalanceFilter;

import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapSprite;
import com.lansosdk.box.FilterExecute;
import com.lansosdk.box.ISprite;
import com.lansosdk.box.ScaleExecute;
import com.lansosdk.box.onFilterExecuteCompletedListener;
import com.lansosdk.box.onFilterExecuteProssListener;
import com.lansosdk.box.onMediaPoolCompletedListener;
import com.lansosdk.box.onMediaPoolProgressListener;
import com.lansosdk.box.onScaleCompletedListener;
import com.lansosdk.box.onScaleProgressListener;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.utils.snoCrashHandler;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TestExecuteActivity extends Activity{

	TextView tvHint;
	String videoPath=null;
	
	private static final String TAG="testVideoColor";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
		 
		 setContentView(R.layout.test_video_function);
		 tvHint=(TextView)findViewById(R.id.id_test_video_function_hint);
		 
		 videoPath=getIntent().getStringExtra("videopath");
	}
	
	@Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//Log.i(TAG,"testcolor edit2--------------------");
				
				testColorEdit2(videoPath,"/sdcard/E2.mp4");
			}
		}, 1000);
    }
	VideoEditor mVideoEditer;
	private void testColorEdit2(String srcVideo,String dstVideo)
	{
		FilterExecute vEdit=new FilterExecute(TestExecuteActivity.this,srcVideo,480,480,1000000,dstVideo);
		vEdit.switchFilterTo(new IFRiseFilter(getBaseContext()));
		
		vEdit.setOnProgessListener(new onFilterExecuteProssListener() {
			
			@Override
			public void onProgress(FilterExecute v, long currentTimeUS) {
				// TODO Auto-generated method stub
				tvHint.setText(String.valueOf(currentTimeUS));
			}
		});
		vEdit.setOnCompletedListener(new onFilterExecuteCompletedListener() {
			
			@Override
			public void onCompleted(FilterExecute v) {
				// TODO Auto-generated method stub
				tvHint.setText("Completed!!!FilterExecute");
			}
		});
		vEdit.start();
	}
}
