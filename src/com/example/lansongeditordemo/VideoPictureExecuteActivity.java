package com.example.lansongeditordemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lansosdk.box.BitmapSprite;
import com.lansosdk.box.MediaPool;
import com.lansosdk.box.MediaPoolPictureExecute;
import com.lansosdk.box.MediaPoolVideoExecute;
import com.lansosdk.box.ViewSprite;
import com.lansosdk.box.onMediaPoolCompletedListener;
import com.lansosdk.box.onMediaPoolProgressListener;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.utils.FileUtils;
import com.lansosdk.videoeditor.utils.snoCrashHandler;

public class VideoPictureExecuteActivity extends Activity{

	int videoDuration;
	boolean isRuned=false;
	TextView tvProgressHint;
	 TextView tvHint;
	 
	 
	    private String editTmpPath=null;
	    private String dstPath=null;
	    
	    
	private static final String TAG="MediaPoolExecuteActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
		 
		 
		 setContentView(R.layout.video_edit_demo_layout);
		 tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
		 
		 tvHint.setText(R.string.mediapoolexecute_demo_hint);
   
		 tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
		 
       findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					testMediaPoolExecute();
			}
		});
       
       findViewById(R.id.id_video_edit_btn2).setEnabled(false);
       findViewById(R.id.id_video_edit_btn2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(FileUtils.fileExist(dstPath)){
					Intent intent=new Intent(VideoPictureExecuteActivity.this,VideoPlayerActivity.class);
	    	    	intent.putExtra("videopath", dstPath);
	    	    	startActivity(intent);
				}else{
					 Toast.makeText(VideoPictureExecuteActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
				}
			}
		});
       
      
       editTmpPath=SDKFileUtils.newMp4PathInBox();
       dstPath=SDKFileUtils.newMp4PathInBox();
	}
   @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	if(vMediaPool!=null){
    		vMediaPool.release();
    		vMediaPool=null;
    	}
    	 if(FileUtils.fileExist(dstPath)){
      	   FileUtils.deleteFile(dstPath);
         }
         if(FileUtils.fileExist(editTmpPath)){
      	   FileUtils.deleteFile(editTmpPath);
         } 
    }
	   
	VideoEditor mVideoEditer;
	BitmapSprite bitmapSprite=null;
	MediaPoolPictureExecute  vMediaPool=null;
	private boolean isExecuting=false;

	
	private void testMediaPoolExecute()
	{
		if(isExecuting)
			return ;
		
		isExecuting=true;
		//注意:这里的是直接把MediaPool设置为480x480,execute是没有自动缩放到屏幕的宽度的,如果加载图片,则最大的图片为480x480,如果超过则只显示480x480的部分. 
		 vMediaPool=new MediaPoolPictureExecute(getApplicationContext(), 480, 480, 10*1000, 25, 1000000, editTmpPath);
		
		vMediaPool.setMediaPoolProgessListener(new onMediaPoolProgressListener() {
			
			@Override
			public void onProgress(MediaPool v, long currentTimeUs) {
				// TODO Auto-generated method stub
				tvProgressHint.setText(String.valueOf(currentTimeUs));
			
				//6秒后消失
				if(currentTimeUs>6000000 && bitmapSprite!=null)  
					v.removeSprite(bitmapSprite);
				
				//3秒的时候,放大一倍.
				if(currentTimeUs>3000000 && bitmapSprite!=null)  
					bitmapSprite.setScale(50);
			}
		});
		vMediaPool.setMediaPoolCompletedListener(new onMediaPoolCompletedListener() {
			
			@Override
			public void onCompleted(MediaPool v) {
				// TODO Auto-generated method stub
				tvProgressHint.setText("MediaPoolExecute Completed!!!");
				
				isExecuting=false;
				

				if(FileUtils.fileExist(editTmpPath)){
					VideoEditor.executeH264WrapperMp4(editTmpPath,dstPath);
					findViewById(R.id.id_video_edit_btn2).setEnabled(true);
				}
			}
		});
		
		vMediaPool.start();
		bitmapSprite=vMediaPool.obtainBitmapSprite(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		
		bitmapSprite.setPosition(300, 200);
		vMediaPool.obtainBitmapSprite(BitmapFactory.decodeResource(getResources(), R.drawable.xiaolian));	
	}
}	