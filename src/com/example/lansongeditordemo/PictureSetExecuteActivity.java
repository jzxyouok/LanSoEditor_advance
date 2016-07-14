package com.example.lansongeditordemo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapSprite;
import com.lansosdk.box.ISprite;
import com.lansosdk.box.MediaPool;
import com.lansosdk.box.MediaPoolPictureExecute;
import com.lansosdk.box.MediaPoolVideoExecute;
import com.lansosdk.box.ViewSprite;
import com.lansosdk.box.onMediaPoolCompletedListener;
import com.lansosdk.box.onMediaPoolProgressListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.utils.FileUtils;
import com.lansosdk.videoeditor.utils.snoCrashHandler;

/**
 * 后台执行 照片影集的功能. 
 *
 */
public class PictureSetExecuteActivity extends Activity{

	int videoDuration;
	boolean isRuned=false;
	TextView tvProgressHint;
	 TextView tvHint;
	 
	 
	    private String editTmpPath=null;
	    private String dstPath=null;
	    
	    private String picBackGround=null;
	    
	    private ArrayList<SlideEffect>  slideEffectArray;
	private static final String TAG="PictureSetExecuteActivity";
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
					Intent intent=new Intent(PictureSetExecuteActivity.this,VideoPlayerActivity.class);
	    	    	intent.putExtra("videopath", dstPath);
	    	    	startActivity(intent);
				}else{
					 Toast.makeText(PictureSetExecuteActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
				}
			}
		});
       DisplayMetrics dm = new DisplayMetrics();// 获取屏幕密度（方法2）
       dm = getResources().getDisplayMetrics();
        
           
      int screenWidth  = dm.widthPixels;	
       
      picBackGround=SDKDir.TMP_DIR+"/"+"picname.jpg";   
      if(screenWidth>=1080){
    	  CopyFileFromAssets.copy(getApplicationContext(), "pic1080x1080u2.jpg", SDKDir.TMP_DIR, "picname.jpg");
      }  
      else{
    	  CopyFileFromAssets.copy(getApplicationContext(), "pic720x720.jpg", SDKDir.TMP_DIR, "picname.jpg");
      }
      
       editTmpPath=SDKFileUtils.newMp4PathInBox();
     //  dstPath=SDKFileUtils.newMp4PathInBox();
       dstPath="/sdcard/p1.mp4";
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
		 vMediaPool=new MediaPoolPictureExecute(getApplicationContext(), 480, 480, 26*1000, 25, 1000000, editTmpPath);
		
		vMediaPool.setMediaPoolProgessListener(new onMediaPoolProgressListener() {
			
			@Override
			public void onProgress(MediaPool v, long currentTimeUs) {
				// TODO Auto-generated method stub
				tvProgressHint.setText(String.valueOf(currentTimeUs));
			
				 if(slideEffectArray!=null && slideEffectArray.size()>0){
					  for(SlideEffect item: slideEffectArray){
						  item.run(currentTimeUs/1000);
					  }
				  }
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
		vMediaPool.pauseUpdateMediaPool();
		
	      vMediaPool.obtainBitmapSprite(BitmapFactory.decodeFile(picBackGround));
	      
	      slideEffectArray=new ArrayList<SlideEffect>();
	      
			//这里同时获取多个,只是不显示出来.
	      getFifthSprite(R.drawable.pic1,0,5000);  		//1--5秒.
	      getFifthSprite(R.drawable.pic2,5000,10000);  //5--10秒.
	      getFifthSprite(R.drawable.pic3,10000,15000);	//10---15秒 
	      getFifthSprite(R.drawable.pic4,15000,20000);  //15---20秒
	      getFifthSprite(R.drawable.pic5,20000,25000);  //20---25秒
	      
	      vMediaPool.resumeUpdateMediaPool();
	}
	
//	private void testMediaPoolExecute()
//	{
//		if(isExecuting)
//			return ;
//		
//		isExecuting=true;
//		//注意:这里的是直接把MediaPool设置为480x480,execute是没有自动缩放到屏幕的宽度的,如果加载图片,则最大的图片为480x480,如果超过则只显示480x480的部分. 
//		 vMediaPool=new MediaPoolPictureExecute(getApplicationContext(), 480, 480, 2*1000, 25, 1000000, editTmpPath);
//		
//		vMediaPool.setMediaPoolProgessListener(new onMediaPoolProgressListener() {
//			
//			@Override
//			public void onProgress(MediaPool v, long currentTimeUs) {
//				// TODO Auto-generated method stub
//				tvProgressHint.setText(String.valueOf(currentTimeUs));
//			
//					int scale= 100- (int)(currentTimeUs/2000000.f*100);
//					Log.i("test","scale="+scale);
//				 	bitmapSprite.setScale(scale);
//			}
//		});
//		vMediaPool.setMediaPoolCompletedListener(new onMediaPoolCompletedListener() {
//			
//			@Override
//			public void onCompleted(MediaPool v) {
//				// TODO Auto-generated method stub
//				tvProgressHint.setText("MediaPoolExecute Completed!!!");
//				
//				isExecuting=false;
//				
//
//				if(FileUtils.fileExist(editTmpPath)){
//					VideoEditor.executeH264WrapperMp4(editTmpPath,dstPath);
//					findViewById(R.id.id_video_edit_btn2).setEnabled(true);
//				}
//			}
//		});
//		
//		vMediaPool.pauseUpdateMediaPool();
//		vMediaPool.start();
//		
//		
//		bitmapSprite= vMediaPool.obtainBitmapSprite(BitmapFactory.decodeFile(picBackGround));
//	
//	      vMediaPool.resumeUpdateMediaPool();
//	}
	  private void getFifthSprite(int resId,long startMS,long endMS)
	    {
	    	ISprite item=vMediaPool.obtainBitmapSprite(BitmapFactory.decodeResource(getResources(), resId));
			SlideEffect  slide=new SlideEffect(item, 25, startMS, endMS, true);
			slideEffectArray.add(slide);
			
	    }
}	