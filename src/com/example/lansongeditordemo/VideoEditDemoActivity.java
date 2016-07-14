package com.example.lansongeditordemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lansoeditor.demo.R;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;
import com.lansosdk.videoeditor.utils.FileUtils;
import com.lansosdk.videoeditor.utils.snoCrashHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
/**
 * 视频编辑基本功能SDK的演示, 我们的视频基本功能是在ffmpeg基础上做了深入的优化, 增加了硬件解码器和硬件编码器, 
 * 
 * 此版本包括基本功能SDK , 如果您想单独测试基本功能SDK , 可以从这里下载:https://github.com/LanSoSdk/LanSoEditor_common
 * 
 * 用基本功能SDK可以完成: 裁剪,剪切,分离,合并,转换,拼接,水印,叠加,混合,转码等场合;
 * 
 * 另外我们写了一遍在android平台上做视频开发的经验总结, 您可以参考下:http://my.oschina.net/u/1032636/blog/710005
 * 
 * 兼容完整的ffmpeg命令.这里仅举例画面裁剪的功能,来演示我们优化的ffmpeg硬件加速功能,更多可以在\n https://github.com/LanSoSdk/LanSoEditor_common \n这里下载,测试\n\n,
	    功能流程: 解码得到画面--->把画面的高度裁剪一半-->保存为新的画面
 *
 */
public class VideoEditDemoActivity extends Activity{

	String videoPath=null;
	VideoEditor mMediaEditor = new VideoEditor();
	ProgressDialog  mProgressDialog;
	int videoDuration;
	boolean isRuned=false;
	MediaInfo   mMediaInfo;
	TextView tvProgressHint;
	private final static String TAG="videoEditDemoActivity";
	private String dstPath=null;
	
	  @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
			 Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
	        
			 videoPath=getIntent().getStringExtra("videopath");
			 mMediaInfo=new MediaInfo(videoPath);
			 
			 setContentView(R.layout.video_edit_demo_layout);
			 TextView tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
			 tvHint.setText(R.string.video_editor_demo_hint);
        
			 tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
			 
	        findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(videoPath==null)
						return ;
					
					mMediaInfo.prepare();
					
					Log.i(TAG,mMediaInfo.toString());
					
					if(mMediaInfo.vDuration>60*1000){//大于60秒
						showHintDialog();
					}else{
						new SubAsyncTask().execute();
					}
				}
			});
	        
	        findViewById(R.id.id_video_edit_btn2).setEnabled(false);
	        findViewById(R.id.id_video_edit_btn2).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(FileUtils.fileExist(dstPath)){
						Intent intent=new Intent(VideoEditDemoActivity.this,VideoPlayerActivity.class);
		    	    	intent.putExtra("videopath", dstPath);
		    	    	startActivity(intent);
					}
				}
			});
	        mMediaEditor.setOnProgessListener(new onVideoEditorProgressListener() {
				
				@Override
				public void onProgress(VideoEditor v, int percent) {
					// TODO Auto-generated method stub
					tvProgressHint.setText(String.valueOf(percent)+"%");
				}
			});
	        dstPath=SDKFileUtils.newMp4PathInBox();
	  } 
	  
	  @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		 if(FileUtils.fileExist(dstPath)){
		     	FileUtils.deleteFile(dstPath);
		     }
	}
		private void showHintDialog()
		{
			new AlertDialog.Builder(this)
			.setTitle("提示")
			.setMessage("视频过大,可能会需要一段时间,您确定要处理吗?")
	        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					new SubAsyncTask().execute();
				}
			})
			.setNegativeButton("取消", null)
	        .show();
		}
	  public class SubAsyncTask extends AsyncTask<Object, Object, Boolean>{
			  @Override
			protected void onPreExecute() {
			// TODO Auto-generated method stub
				  mProgressDialog = new ProgressDialog(VideoEditDemoActivity.this);
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
      	    	float dstBr=(float)mMediaInfo.vBitRate;
      	    	dstBr*=0.7f;
      	    	int dstBr2=(int)dstBr;
      	    	mMediaEditor.executeVideoFrameCrop(videoPath, mMediaInfo.vWidth, mMediaInfo.vHeight/2, 0, 0, dstPath, mMediaInfo.vCodecName,dstBr2);
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
    		  findViewById(R.id.id_video_edit_btn2).setEnabled(true);
    	}
    }
}

