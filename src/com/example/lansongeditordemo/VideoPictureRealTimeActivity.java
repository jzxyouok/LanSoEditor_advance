package com.example.lansongeditordemo;

import java.io.IOException;
import java.util.Locale;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

import com.example.lansongeditordemo.MediaPoolView.onViewAvailable;
import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapSprite;
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * 演示: 使用MediaPool来实现 视频和图片的实时叠加. 
 *
 */
public class VideoPictureRealTimeActivity extends Activity implements OnSeekBarChangeListener {
    private static final String TAG = "VideoActivity";

    private String mVideoPath;

    private MediaPoolView mPlayView;
    
    private MediaPlayer mplayer=null;
    private MediaPlayer mplayer2=null;
    private ISprite  mSpriteMain=null;
    private BitmapSprite mBitmapSprite=null;
    
    private String editTmpPath=null;
    private String dstPath=null;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		 Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
        setContentView(R.layout.mediapool_layout);
        
        mVideoPath = getIntent().getStringExtra("videopath");
        mPlayView = (MediaPoolView) findViewById(R.id.mediapool_view);
        
        initSeekBar(R.id.id_mediapool_skbar_rotate,360); //角度是旋转360度.
        initSeekBar(R.id.id_mediapool_skbar_move,100);   //move的百分比暂时没有用到,举例而已.
        initSeekBar(R.id.id_mediapool_skbar_scale,800);   //这里设置最大可放大8倍
        
        initSeekBar(R.id.id_mediapool_skbar_red,100);  //red最大为100
        initSeekBar(R.id.id_mediapool_skbar_green,100);
        initSeekBar(R.id.id_mediapool_skbar_blue,100);
        initSeekBar(R.id.id_mediapool_skbar_alpha,100);
        
        
        findViewById(R.id.id_mediapool_saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(FileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(VideoPictureRealTimeActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(VideoPictureRealTimeActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        findViewById(R.id.id_mediapool_saveplay).setVisibility(View.GONE);
        
        editTmpPath=SDKFileUtils.newMp4PathInBox();
        dstPath=SDKFileUtils.newMp4PathInBox();
    }
    private void initSeekBar(int resId,int maxvalue)
    {
    	SeekBar   skbar=(SeekBar)findViewById(resId);
           skbar.setOnSeekBarChangeListener(this);
           skbar.setMax(maxvalue);
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//showHintDialog();
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
					
					//completion不确定会在什么时候停，故需要判断是否为null
					if(mPlayView!=null && mPlayView.isRunning()){
						mPlayView.stopMediaPool();
						
						toastStop();
						
						if(FileUtils.fileExist(editTmpPath)){
							VideoEditor.encoderAddAudio(mVideoPath,editTmpPath,SDKDir.TMP_DIR,dstPath);
							FileUtils.deleteFile(editTmpPath);
							findViewById(R.id.id_mediapool_saveplay).setVisibility(View.VISIBLE);
						}else{
							Log.e(TAG," player completion, but file:"+editTmpPath+" is not exist!!!");
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
    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    	Log.i(TAG,"录制已停止!!");
    	
    }
    private void start(MediaPlayer mp)
    {
    	
    		MediaInfo info=new MediaInfo(mVideoPath,false);
        	info.prepare();
        	
        	if(DemoCfg.ENCODE){
        		//设置使能 实时录制, 即把正在MediaPool中呈现的画面实时的保存下来,实现所见即所得的模式
        		mPlayView.setRealEncodeEnable(480,480,1000000,(int)info.vFrameRate,editTmpPath);
        	}
        	//设置当前MediaPool的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
    		mPlayView.setMediaPoolSize(480,480,new onMediaPoolSizeChangedListener() {
			
			@Override
			public void onSizeChanged(int viewWidth, int viewHeight) {
				// TODO Auto-generated method stub
				// 开始mediaPool的渲染线程. 
				mPlayView.startMediaPool(null,null);
				//获取一个主视频的 VideoSprite
				mSpriteMain=mPlayView.obtainMainVideoSprite(mplayer.getVideoWidth(),mplayer.getVideoHeight());
				if(mSpriteMain!=null){
					mplayer.setSurface(new Surface(mSpriteMain.getVideoTexture()));
				}
				mplayer.start();
				addBitmapSprite();
			}
		});
    		
    		
    }
    private void addBitmapSprite()
    {
    	mBitmapSprite=mPlayView.obtainBitmapSprite(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
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
				if(mBitmapSprite!=null){
					mBitmapSprite.setRotate(progress);
				}
				break;
			case R.id.id_mediapool_skbar_move:
					if(mBitmapSprite!=null){
						 xpos+=10;
						 ypos+=10;
						 
						 if(xpos>mPlayView.getViewWidth())
							 xpos=0;
						 if(ypos>mPlayView.getViewWidth())
							 ypos=0;
						 mBitmapSprite.setPosition(xpos, ypos);
					}
				break;				
			case R.id.id_mediapool_skbar_scale:
				if(mBitmapSprite!=null){
					mBitmapSprite.setScale(progress);
				}
			break;		
			case R.id.id_mediapool_skbar_red:
					if(mBitmapSprite!=null){
						mBitmapSprite.setRedPercent(progress);  //设置每个RGBA的比例,默认是1
					}
				break;

			case R.id.id_mediapool_skbar_green:
					if(mBitmapSprite!=null){
						mBitmapSprite.setGreenPercent(progress);
					}
				break;

			case R.id.id_mediapool_skbar_blue:
					if(mBitmapSprite!=null){
						mBitmapSprite.setBluePercent(progress);
					}
				break;

			case R.id.id_mediapool_skbar_alpha:
					if(mBitmapSprite!=null){
						mBitmapSprite.setAlphaPercent(progress);
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
