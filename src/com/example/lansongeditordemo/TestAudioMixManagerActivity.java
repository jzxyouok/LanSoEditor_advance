package com.example.lansongeditordemo;

import java.io.IOException;

import com.example.lansongeditordemo.VideoEditDemoActivity.SubAsyncTask;
import com.lansosdk.box.AudioEncodeDecode;
import com.lansosdk.box.AudioMixManager;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.utils.FileUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class TestAudioMixManagerActivity extends Activity{

	
	private String videoPath="/sdcard/2x.mp4";
	private String dstPath=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.test_audiomix_layout);
		
		
		findViewById(R.id.id_test_audio_mix1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isPcmMix=true;
				new SubAsyncTask().execute();
			}
		});
		findViewById(R.id.id_test_audio_mix2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isPcmMix=false;
				new SubAsyncTask().execute();
			}
		});
		findViewById(R.id.id_test_audio_play).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				playAudio();
			}
		});
	  
	}
	private MediaPlayer audioPlayer=null;
	private void playAudio(){
		
		if(dstPath !=null && MediaInfo.isSupport(dstPath))
		{
			audioPlayer=new MediaPlayer();
			try {
				audioPlayer.setDataSource(dstPath);
				audioPlayer.prepare();
				audioPlayer.start();
				
				audioPlayer.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						// TODO Auto-generated method stub
						audioPlayer.release();
						audioPlayer=null;
					}
				});
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(audioPlayer!=null){
			audioPlayer.stop();
			audioPlayer.release();
			audioPlayer=null;
		}
	}
	
	private boolean isPcmMix=false;
	private String pcmMix()
	{
		 String ret=null;
		if(FileUtils.fileExist(videoPath) && FileUtils.fileExist("/sdcard/wuya.aac") && FileUtils.fileExist("/sdcard/hongdou10s.mp3"))
		{
				VideoEditor et=new VideoEditor();
			      et.executeDeleteVideo("/sdcard/2x.mp4", "/sdcard/2x_audio.aac");
			      AudioEncodeDecode.decodeAudio("/sdcard/2x_audio.aac", "/sdcard/2x_audio.pcm");
			      AudioEncodeDecode.decodeAudio("/sdcard/wuya.aac", "/sdcard/wuya.pcm");
			      AudioEncodeDecode.decodeAudio("/sdcard/hongdou10s.mp3", "/sdcard/hongdou10s.pcm");
			        
			      AudioMixManager mixMng=new AudioMixManager(getApplicationContext());

			      
			      mixMng.pushMainAudioSprite("/sdcard/2x_audio.pcm", 44100, 2, 21000, true);
			      
			      
			      mixMng.pushSubAudioSprite("/sdcard/hongdou10s.pcm", 44100, 2, 10000,true,3000);
			      mixMng.pushSubAudioSprite("/sdcard/wuya.pcm", 44100, 2, 2500,true,0);
			      ret=mixMng.executeAudioMix();
			        
			      mixMng.release();
		}
		return ret;
	}
	private String audioMix()
	{
		if(FileUtils.fileExist("/sdcard/hongdou10s.mp3") && FileUtils.fileExist("/sdcard/wuya.aac"))
		{
			VideoEditor mMediaEditor = new VideoEditor();
			String ret="/sdcard/audio_amix.aac";
			mMediaEditor.executeAudioMix("/sdcard/hongdou10s.mp3","/sdcard/wuya.aac",5000,5000,ret);
			return ret;
		}
		return null;
	}
	ProgressDialog  mProgressDialog;
	public class SubAsyncTask extends AsyncTask<Object, Object, Boolean>{
		  @Override
		protected void onPreExecute() {
		// TODO Auto-generated method stub
			  mProgressDialog = new ProgressDialog(TestAudioMixManagerActivity.this);
	          mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	          mProgressDialog.setMessage("正在处理中...");
	          mProgressDialog.setCancelable(false);
	          mProgressDialog.show();
	          super.onPreExecute();
		}
	    @Override
	    protected synchronized Boolean doInBackground(Object... params) {
	    	// TODO Auto-generated method stub
	    	
	    	//这里应该计算源视频的宽度和高度， 和目标视频的宽度高度之间的比例
	    	//然后把比例， 作为缩小 bitrate的参数
	    		
	    	if(isPcmMix){
	    		dstPath=pcmMix();
	    	}else{
	    		dstPath=audioMix();
	    	}
	    	
	    	return null;
	    }
	@Override
	protected void onPostExecute(Boolean result) { 
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if( mProgressDialog!=null){
     		 mProgressDialog.cancel();
     		 mProgressDialog=null;
		}
	}
}
}
