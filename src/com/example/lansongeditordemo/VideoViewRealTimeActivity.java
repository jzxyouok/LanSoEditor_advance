package com.example.lansongeditordemo;

import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

import com.lansoeditor.demo.R;
import com.lansosdk.box.ViewSprite;
import com.lansosdk.box.ISprite;
import com.lansosdk.box.onMediaPoolSizeChangedListener;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.MediaSource;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
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
 *  演示: 视频和UI界面的 实时叠加.
 *  
 *  mGLRelativeLayout是要叠加的UI界面.
 *
 */
public class VideoViewRealTimeActivity extends Activity implements OnSeekBarChangeListener {
    private static final String TAG = "VideoActivity";

    private String mVideoPath;

    private MediaPoolView mPlayView;
    
    private MediaPlayer mplayer=null;
    private MediaPlayer mplayer2=null;
    
    private ISprite  mSpriteMain=null;
    private ViewSprite mViewSprite=null;
    
//    
    private String editTmpPath=null;
    private String dstPath=null;

    private GLRelativeLayout mGLRelativeLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		 Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
        setContentView(R.layout.video_view_realtime_layout);
        
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mPlayView = (MediaPoolView) findViewById(R.id.id_vview_realtime_mediapool_view);
        
        initSeekBar(R.id.id_mediapool_skbar_rotate,360); //角度是旋转360度.
        initSeekBar(R.id.id_mediapool_skbar_move,100);   //move的百分比暂时没有用到,举例而已.
        initSeekBar(R.id.id_mediapool_skbar_scale,800);   //这里设置最大可放大8倍
        
        initSeekBar(R.id.id_mediapool_skbar_red,100);  //red最大为100
        initSeekBar(R.id.id_mediapool_skbar_green,100);
        initSeekBar(R.id.id_mediapool_skbar_blue,100);
        initSeekBar(R.id.id_mediapool_skbar_alpha,100);
        
        mGLRelativeLayout=(GLRelativeLayout)findViewById(R.id.id_vview_realtime_gllayout);
        
        
        findViewById(R.id.id_vview_realtime_saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(FileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(VideoViewRealTimeActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(VideoViewRealTimeActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
    	findViewById(R.id.id_vview_realtime_saveplay).setVisibility(View.GONE);
    	

        editTmpPath=SDKFileUtils.newMp4PathInBox();
            dstPath=SDKFileUtils.newMp4PathInBox();
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	findViewById(R.id.id_vview_realtime_saveplay).setVisibility(View.GONE);
    	new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				 startPlayVideo();
			}
		}, 100);
    }
    private void initSeekBar(int resId,int maxvalue)
    {
    	SeekBar   skbar=(SeekBar)findViewById(resId);
           skbar.setOnSeekBarChangeListener(this);
           skbar.setMax(maxvalue);
    }
    private void startPlayVideo()
    {
          if (mVideoPath != null)
          {
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
					
					Log.i(TAG,"media player is completion!!!!");
					if(mPlayView!=null && mPlayView.isRunning()){
						mPlayView.stopMediaPool();
						
						toastStop();
						
						if(FileUtils.fileExist(editTmpPath)){
							VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR, dstPath);
							FileUtils.deleteFile(editTmpPath);
					    	findViewById(R.id.id_vview_realtime_saveplay).setVisibility(View.VISIBLE);
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
    	MediaInfo info=new MediaInfo(mVideoPath,false);
    	info.prepare();
    	
    	if(DemoCfg.ENCODE){
    		mPlayView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
    	}
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
				addViewSprite();
//				startPlayer2();
			}
		});
    }
    
    private void addViewSprite()
    {
    	mViewSprite=mPlayView.obtainViewSprite();
        mGLRelativeLayout.setViewSprite(mViewSprite);
        mGLRelativeLayout.invalidate();
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
    	
    	if(mplayer2!=null){
    		mplayer2.stop();
    		mplayer2.release();
    		mplayer2=null;
    	}
    	
    	if(mPlayView!=null){
    		mPlayView.stopMediaPool();
    		mPlayView=null;        		   
    	}
    	  if(FileUtils.fileExist(dstPath)){
          	FileUtils.deleteFile(dstPath);
          }
          if(FileUtils.fileExist(editTmpPath)){
          	FileUtils.deleteFile(editTmpPath);
          } 
    }
    private float xpos=0,ypos=0;

    /**
     * 提示:实际使用中没有主次之分, 只要是继承自ISprite的对象(FilterSprite除外),都可以调节,这里仅仅是举例
     */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
			case R.id.id_mediapool_skbar_rotate:
				if(mViewSprite!=null){
					mViewSprite.setRotate(progress);
				}
				break;
			case R.id.id_mediapool_skbar_move:
					if(mViewSprite!=null){
						 xpos+=10;
						 ypos+=10;
						 
						 if(xpos>mPlayView.getViewWidth())
							 xpos=0;
						 if(ypos>mPlayView.getViewWidth())
							 ypos=0;
						 mViewSprite.setPosition(xpos, ypos);
					}
				break;				
			case R.id.id_mediapool_skbar_scale:
				if(mViewSprite!=null){
					mViewSprite.setScale(progress);
				}
			break;		
			case R.id.id_mediapool_skbar_red:
					if(mViewSprite!=null){
						mViewSprite.setRedPercent(progress);  //设置每个RGBA的比例,默认是1
					}
				break;

			case R.id.id_mediapool_skbar_green:
					if(mViewSprite!=null){
						mViewSprite.setGreenPercent(progress);
					}
				break;

			case R.id.id_mediapool_skbar_blue:
					if(mViewSprite!=null){
						mViewSprite.setBluePercent(progress);
					}
				break;

			case R.id.id_mediapool_skbar_alpha:
					if(mViewSprite!=null){
						mViewSprite.setAlphaPercent(progress);
					}
				break;
				
			default:
				break;
		}
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
