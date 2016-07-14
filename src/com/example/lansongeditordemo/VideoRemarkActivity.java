package com.example.lansongeditordemo;

import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

import com.lansoeditor.demo.R;
import com.lansosdk.box.AudioEncodeDecode;
import com.lansosdk.box.MediaPoolUpdateMode;
import com.lansosdk.box.AudioMixManager;
import com.lansosdk.box.VideoSprite;
import com.lansosdk.box.ViewSprite;
import com.lansosdk.box.ISprite;
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
 *  演示: 使用MediaPool完成 视频的实时标记.
 *  
 *  在视频处理的过程中, 提供 重写MediaPool中的onTouchEvent方法的类TestTouchView,当点击这个View时,获取到点击位置, 并获取一个BitmapSprite, 并再次位置显示叠加图片.move时,移动图片.
 *  在手指抬起后, 释放BitmapSprite,从而实时滑动画面实时标记的效果.
 *
 */
public class VideoRemarkActivity extends Activity{
    private static final String TAG = "VideoRemarkActivity";

    private String mVideoPath;

    private TestTouchView mPlayView;
    
    private MediaPlayer mplayer=null;
    
    private ISprite  mSpriteMain=null;
    
    private String editTmpPath=null;
    private String dstPath=null;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		 Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
		 setContentView(R.layout.mediapool_touch_layout);
        
        
        mVideoPath = getIntent().getStringExtra("videopath");
        
        Log.i(TAG,"videopath:"+mVideoPath);
        
        mPlayView = (TestTouchView) findViewById(R.id.mediapool_view);
        
        findViewById(R.id.id_mediapool_saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(FileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(VideoRemarkActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(VideoRemarkActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        findViewById(R.id.id_mediapool_saveplay).setVisibility(View.GONE);
        
        editTmpPath=FileUtils.createFile(SDKDir.TMP_DIR, ".mp4");
        dstPath=SDKFileUtils.newFilePath(SDKDir.TMP_DIR, ".mp4");
        
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
					if(mPlayView!=null && mPlayView.isRunning()){
						mPlayView.stopMediaPool();
						
						toastStop();
						
						if(FileUtils.fileExist(editTmpPath)){
							VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
							FileUtils.deleteFile(editTmpPath);
							findViewById(R.id.id_mediapool_saveplay).setVisibility(View.VISIBLE);
						}
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
		
    	mPlayView.setUpdateMode(MediaPoolUpdateMode.ALL_VIDEO_READY,25);
    	
    	mPlayView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
    	mPlayView.setMediaPoolSize(480,480,new onMediaPoolSizeChangedListener() {
			
			@Override
			public void onSizeChanged(int viewWidth, int viewHeight) {
				// TODO Auto-generated method stub
				mPlayView.startMediaPool(null,null);
				
				mSpriteMain=mPlayView.obtainMainVideoSprite(mplayer.getVideoWidth(),mplayer.getVideoHeight());
				if(mSpriteMain!=null){
					mplayer.setSurface(new Surface(mSpriteMain.getVideoTexture()));
				}
				mplayer.start();
			}
		});
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
		if(mPlayView!=null){
			mPlayView.stopMediaPool();
			mPlayView=null;        		   
		}
		if(FileUtils.fileExist(editTmpPath)){
			FileUtils.deleteFile(editTmpPath);
			editTmpPath=null;
		}
		if(FileUtils.fileExist(dstPath)){
			FileUtils.deleteFile(dstPath);
			dstPath=null;
		}
	}
}
