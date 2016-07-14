package com.example.lansongeditordemo;

import java.io.IOException;

import com.lansoeditor.demo.R;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.player.IMediaPlayer;
import com.lansosdk.videoeditor.player.IRenderView;
import com.lansosdk.videoeditor.player.VPlayer;
import com.lansosdk.videoeditor.player.IMediaPlayer.OnPlayerPreparedListener;
import com.lansosdk.videoeditor.utils.TextureRenderView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 视频播放 
 *
 */
public class VideoPlayerActivity extends Activity {
	   
   	private static final boolean VERBOSE = true; 
    private static final String TAG = "VideoPlayerActivity";
    
	private TextureRenderView textureView;
    private MediaPlayer mediaPlayer=null;  
    private VPlayer  vplayer=null;
    String videoPath=null;
    private boolean isSupport=false;
    private int screenWidth,screenHeight;
    private MediaInfo mInfo;
    
    private TextView tvSizeHint;
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.player_layout);  
        textureView=(TextureRenderView)findViewById(R.id.surface1);
        
        videoPath=getIntent().getStringExtra("videopath");
        
        TextView tvScreen=(TextView)findViewById(R.id.id_palyer_screenhinit);
        TextView tvVideoRatio=(TextView)findViewById(R.id.id_palyer_videoRatio);
        TextView tvVideoDuration=(TextView)findViewById(R.id.id_palyer_videoduration);
        
        tvSizeHint =(TextView)findViewById(R.id.id_palyer_videosizehint);
        
        
        
        DisplayMetrics dm = new DisplayMetrics();// 获取屏幕密度（方法2）
        dm = getResources().getDisplayMetrics();
         screenWidth  = dm.widthPixels;
         screenHeight =dm.heightPixels;
         
         String str="当前屏幕分辨率：";
         str+=String.valueOf(screenWidth);
         str+="x";
         str+=String.valueOf(screenHeight);
         tvScreen.setText(str);

         mInfo=new MediaInfo(videoPath,false);
         if(mInfo.prepare()==false){
        	 showHintDialog();
        	 isSupport=false;
         }else{
        	 isSupport=true;
        	  str="当前视频分辨率：";
             str+=String.valueOf(mInfo.vWidth);
             str+="x";
             str+=String.valueOf(mInfo.vHeight);
             tvVideoRatio.setText(str); 
             
             str="当前视频时长:";
             str+=String.valueOf(mInfo.vDuration);
             tvVideoDuration.setText(str);
         }
         
         
        textureView.setSurfaceTextureListener(new SurfaceTextureListener() {
			
			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture surface) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
					int height) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
					int height) {
				// TODO Auto-generated method stub
				if(isSupport){
					play(new Surface(surface));
//					startVPlayer(new Surface(surface));
				}
			}
		});
    }  
    private void showHintDialog(){
    	   
    		new AlertDialog.Builder(this)
		.setTitle("提示")
		.setMessage("抱歉,暂时不支持当前视频格式")
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		})
        .show();
    
    }
    
    public void play(Surface surface)  {  

    	if(videoPath==null)
    		return ;
    	
        mediaPlayer = new MediaPlayer();  
        mediaPlayer.reset();  
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				 Toast.makeText(VideoPlayerActivity.this, "视频播放完毕!", Toast.LENGTH_SHORT).show();
			}
		});
        
        try {
			mediaPlayer.setDataSource(videoPath);
			  mediaPlayer.setSurface(surface);  
		        mediaPlayer.prepare();  
		        
		        Log.i("sno","mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight()"+mediaPlayer.getVideoWidth()+mediaPlayer.getVideoHeight());
		       
		        
		      //因为是竖屏.宽度小于高度.
		        if(screenWidth>mInfo.vWidth){
		        	tvSizeHint.setText(R.string.origal_width);
		        	textureView.setDispalyRatio(IRenderView.AR_ASPECT_WRAP_CONTENT);
		        	
		        }else{  //大于屏幕的宽度
		        	
		        	tvSizeHint.setText(R.string.fix_width);
		        	textureView.setDispalyRatio(IRenderView.AR_ASPECT_FIT_PARENT);
		        }
		        
		        
		        textureView.setVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
		        textureView.requestLayout();
		        
		        mediaPlayer.start();  
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }  
  
    private void startVPlayer(final Surface surface)
    {
    	vplayer=new VPlayer(this);
    	vplayer.setVideoPath(videoPath);
    	vplayer.setOnPreparedListener(new OnPlayerPreparedListener() {
    			
    			@Override
    			public void onPrepared(IMediaPlayer mp) {
    				// TODO Auto-generated method stub
    						vplayer.setSurface(surface);
    						 Log.i("sno","mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight()"+mp.getVideoWidth()+mp.getVideoHeight());
    					        
    					    textureView.setVideoSize(mp.getVideoWidth(), mp.getVideoHeight());
    				        textureView.requestLayout();
    				        vplayer.start();
    					}
    			});
    	vplayer.prepareAsync();
    }
    
    @Override  
    protected void onPause() {  
        super.onPause();
        
        if (mediaPlayer!=null) {  
        	mediaPlayer.stop();
        	mediaPlayer.release();
        	mediaPlayer=null;  
        }  
        if(vplayer!=null){
    		vplayer.stop();
    		vplayer.release();
    		vplayer=null;
    	}
    }  
  
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
    }  
}
