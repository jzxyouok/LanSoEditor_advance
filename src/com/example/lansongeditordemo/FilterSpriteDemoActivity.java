package com.example.lansongeditordemo;


import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

import com.example.lansongeditordemo.GPUImageFilterTools.FilterAdjuster;
import com.example.lansongeditordemo.GPUImageFilterTools.OnGpuImageFilterChosenListener;
import com.lansoeditor.demo.R;
import com.lansosdk.box.AudioEncodeDecode;
import com.lansosdk.box.FilterSprite;
import com.lansosdk.box.MediaPool;
import com.lansosdk.box.MediaPoolUpdateMode;
import com.lansosdk.box.AudioMixManager;
import com.lansosdk.box.VideoSprite;
import com.lansosdk.box.ViewSprite;
import com.lansosdk.box.ISprite;
import com.lansosdk.box.onMediaPoolCompletedListener;
import com.lansosdk.box.onMediaPoolProgressListener;
import com.lansosdk.box.onMediaPoolSizeChangedListener;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.MediaSource;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.player.IMediaPlayer;
import com.lansosdk.videoeditor.player.IMediaPlayer.OnPlayerPreparedListener;
import com.lansosdk.videoeditor.player.VPlayer;
import com.lansosdk.videoeditor.utils.FileUtils;
import com.lansosdk.videoeditor.utils.Utils;
import com.lansosdk.videoeditor.utils.snoCrashHandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 演示滤镜模块的 FilterSprite的使用, 可以在播放过程中切换滤镜, 在滤镜处理过程中, 增加其他的sprite,比如增加一个BitmapSprite和 ViewSprite等等.
 * 
 * 代码流程: 从layout中得到MediaPoolView,并从MediaPoolView中获取多个sprite,并对sprite进行滤镜, 缩放等操作.
 *
 *
 */
public class FilterSpriteDemoActivity extends Activity {
    private static final String TAG = "VideoActivity";

    private String mVideoPath;

    private MediaPoolView mMediaPoolView;
    
    private MediaPlayer mplayer=null;
    
    private VideoSprite subVideoSprite=null;
    private FilterSprite  filterSprite=null;
    
    private SeekBar skbarFilterAdjuster;
    
    private String editTmpPath=null;
    private String dstPath=null;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		 Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
        setContentView(R.layout.filter_sprite_demo_layout);
        
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mMediaPoolView = (MediaPoolView) findViewById(R.id.id_filtersprite_demo_view);
        
        skbarFilterAdjuster=(SeekBar)findViewById(R.id.id_filtersprite_demo_seek1);
        
        skbarFilterAdjuster.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				  if (mFilterAdjuster != null) {
			            mFilterAdjuster.adjust(progress);
			        }
			}
		});
        skbarFilterAdjuster.setMax(100);
        
        findViewById(R.id.id_filtersprite_demo_selectbtn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				selectFilter();
			}
		});
        
        findViewById(R.id.id_filtersprite_saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(FileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(FilterSpriteDemoActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(FilterSpriteDemoActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
 
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				 startPlayVideo();
			}
		}, 100);
    }
    private FilterAdjuster mFilterAdjuster;
    /**
     * 选择滤镜效果, 当前SDK支持44中滤镜.
     */
    private void selectFilter()
    {
    	GPUImageFilterTools.showDialog(this, new OnGpuImageFilterChosenListener() {

            @Override
            public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
            	
            	//在这里通过MediaPool线程去切换 filterSprite的滤镜
	         	   if(mMediaPoolView.switchFilterTo(filterSprite,filter)){
	         		   mFilterAdjuster = new FilterAdjuster(filter);
	
	         		   //如果这个滤镜 可调, 显示可调节进度条.
	         		    findViewById(R.id.id_filtersprite_demo_seek1).setVisibility(
	         		            mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
	         	   }
            }
        });
    }
    private void startPlayVideo()
    {
          if (mVideoPath != null){
        	  mplayer=new MediaPlayer();
        	  try {
				mplayer.setDataSource(mVideoPath);
				
			}  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	  mplayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					// TODO Auto-generated method stub
					start(mp);
				}
			});
        	  mplayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					if(mMediaPoolView!=null && mMediaPoolView.isRunning()){
						mMediaPoolView.stopMediaPool();
					}
				}
			});
        	  mplayer.prepareAsync();
          }
          else {
              Log.e(TAG, "Null Data Source\n");
              finish();
              return;
          }
    }
    private void start(MediaPlayer mp)
    {
    	MediaInfo info=new MediaInfo(mVideoPath);
    	info.prepare();
		
    	mMediaPoolView.setUpdateMode(MediaPoolUpdateMode.ALL_VIDEO_READY,25);
    	
    	if(DemoCfg.ENCODE){
    		mMediaPoolView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
    	}
    	
    	//设置当前MediaPool的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
    	mMediaPoolView.setMediaPoolSize(480,480,new onMediaPoolSizeChangedListener() {
			
			@Override
			public void onSizeChanged(int viewWidth, int viewHeight) {
				// TODO Auto-generated method stub
				mMediaPoolView.startMediaPool(new MediaPoolProgressListener(),new MediaPoolCompleted());
				
				filterSprite=mMediaPoolView.obtainFilterSprite(mplayer.getVideoWidth(),mplayer.getVideoHeight(),new GPUImageSepiaFilter());
				if(filterSprite!=null){
					mplayer.setSurface(new Surface(filterSprite.getVideoTexture()));
				}
				mplayer.start();
			}
		});
    }
    //MediaPool完成后的回调.
    private class MediaPoolCompleted implements onMediaPoolCompletedListener
    {

		@Override
		public void onCompleted(MediaPool v) {
			// TODO Auto-generated method stub
			Log.i(TAG,"MediaPoolCompleted: !!!!!!!!!:");
			
			toastStop();
			
			if(FileUtils.fileExist(editTmpPath)){
				VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
				FileUtils.deleteFile(editTmpPath);
			}
			toastStop();
		}
    }
    //MediaPool进度回调.每一帧都返回一个回调.
    private class MediaPoolProgressListener implements onMediaPoolProgressListener
    {

		@Override
		public void onProgress(MediaPool v, long currentTimeUs) {
			// TODO Auto-generated method stub
//			  Log.i(TAG,"MediaPoolProgressListener: us:"+currentTimeUs);
			  if(currentTimeUs>=26*1000*1000)  //26秒.多出一秒,让图片走完.
			  {
				  mMediaPoolView.stopMediaPool();
			  }
		}
    }
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
   @Override
protected void onDestroy() {
	// TODO Auto-generated method stub
	super.onDestroy();
	if(mplayer!=null){
		mplayer.stop();
		mplayer.release();
		mplayer=null;
	}
	if(mMediaPoolView!=null){
		mMediaPoolView.stopMediaPool();
		mMediaPoolView=null;        		   
	}
	 if(FileUtils.fileExist(dstPath)){
     	FileUtils.deleteFile(dstPath);
     }
     if(FileUtils.fileExist(editTmpPath)){
     	FileUtils.deleteFile(editTmpPath);
     } 
}

}
