package com.example.lansongeditordemo;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;


import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.lansoeditor.demo.R;
import com.lansosdk.box.AudioEncodeDecode;
import com.lansosdk.box.AudioSprite;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.utils.FileUtils;
import com.lansosdk.videoeditor.utils.snoCrashHandler;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaRecorder.AudioSource;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Video;
import android.support.v4.content.PermissionChecker;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener{


	 private static final String TAG="MainActivity";
	 private static final boolean VERBOSE = false;   
	   
	 private final static int SELECT_FILE_REQUEST_CODE=10;
	 
	 
	private TextView tvVideoPath;
	private boolean isPermissionOk=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
        setContentView(R.layout.activity_main);
        
        LanSoEditor.initSo();
        
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
            	isPermissionOk=true;
                Toast.makeText(MainActivity.this, R.string.message_granted, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(String permission) {
            	isPermissionOk=false;
                String message = String.format(Locale.getDefault(), getString(R.string.message_denied), permission);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
       
        
        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        
           
          int screenWidth  = dm.widthPixels;	
          int  screenHeight = dm.heightPixels;		
           
        Log.i(TAG,"当前视频分辨率是:"+screenWidth+"x"+screenHeight);
        
        
        tvVideoPath=(TextView)findViewById(R.id.id_main_tvvideo);
        
        findViewById(R.id.id_main_demofilter).setOnClickListener(this);
        findViewById(R.id.id_main_demofiltersprite).setOnClickListener(this);
        
        
        
        findViewById(R.id.id_main_demoedit).setOnClickListener(this);
        
        findViewById(R.id.id_main_twovideooverlay).setOnClickListener(this);
        findViewById(R.id.id_main_videobitmapoverlay).setOnClickListener(this);
        findViewById(R.id.id_main_viewspritedemo).setOnClickListener(this);
        findViewById(R.id.id_main_pictures).setOnClickListener(this);
        
        
        findViewById(R.id.id_main_scaleexecute).setOnClickListener(this);
        findViewById(R.id.id_main_filterexecute).setOnClickListener(this);
        findViewById(R.id.id_main_mediapoolexecute).setOnClickListener(this);
        findViewById(R.id.id_main_mediapoolpictureexecute).setOnClickListener(this);
        findViewById(R.id.id_main_testaudiomix).setOnClickListener(this);
        
        findViewById(R.id.id_main_testvideoplay).setOnClickListener(this);
        findViewById(R.id.id_main_viewremark).setOnClickListener(this);

        findViewById(R.id.id_main_mediapoolexecute_filter).setOnClickListener(this);
        
        
        
        findViewById(R.id.id_main_select_video).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startSelectVideoActivity();
			}
		});
        findViewById(R.id.id_main_use_default_videobtn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new CopyDefaultVideoAsyncTask().execute();
			}
		});
        
    }
    private boolean isStarted=false;
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	if(isStarted)
    		return;
    	
    	 new Handler().postDelayed(new Runnable() {
 			
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				isStarted=true;
// 				showHintDialog();
 				
// 				startVideoFilterDemo(); 
// 				startFilterExecuteActivity();
 			 
// 				startVideoEditDemo();
 			
// 				String path=etVideoPath.getText().toString();
// 		    	Intent intent=new Intent(MainActivity.this,ScaleExecuteActivity.class);
// 		    	intent.putExtra("videopath", path);
// 		    	startActivity(intent);
 		    	
// 				String path=etVideoPath.getText().toString();
// 				MediaPoolDemoActivity.intentTo(MainActivity.this, path);
 				
// 				startExecuteDemo(MediaPoolExecuteActivity.class);
 			}
 		}, 1000);
    }
    private void showHintDialog(String hint){
    	new AlertDialog.Builder(this)
		.setTitle("提示")
		.setMessage(hint)
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		})
        .show();
    }
    private void showHintDialog()
   	{
      	 	Calendar c = Calendar.getInstance();
   		int year=c.get(Calendar.YEAR);
   		int month=c.get(Calendar.MONTH)+1;
   		
   		int lyear=VideoEditor.getLimitYear();
   		int lmonth=VideoEditor.getLimitMonth();
   		
   		Log.i(TAG,"current year is:"+year+" month is:"+month +" limit year:"+lyear+" limit month:"+lmonth);
   		String timeHint=getResources().getString(R.string.sdk_limit);
   		timeHint=String.format(timeHint, lyear,lmonth);
   		
   		new AlertDialog.Builder(this)
   		.setTitle("提示")
   		.setMessage(timeHint)
           .setPositiveButton("确定", new DialogInterface.OnClickListener() {
   			
   			@Override
   			public void onClick(DialogInterface dialog, int which) {
   				// TODO Auto-generated method stub
   				
   				showHintDialog("注意:当前已是发行商用版本.\n\n后期会依次增加各种场景的举例:比如浪漫情诗,图片影集等例子,核心功能基本不变,不影响您的使用.请知悉~~");
   			}
   		})
           .show();
   	}
    public static void setIdentityM(float[] sm, int smOffset) {
        for (int i=0 ; i<16 ; i++) {
            sm[smOffset + i] = 0;
        }
        for(int i = 0; i < 16; i += 5) {
            sm[smOffset + i] = 1.0f;
            Log.i("modelView3",String.valueOf(sm[smOffset + i])+" i="+i);
        }
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	LanSoEditor.unInitSo();
    	SDKFileUtils.deleteDir(new File(SDKDir.TMP_DIR));
    }
    
    
    private boolean checkPath(){
    	if(tvVideoPath.getText()!=null && tvVideoPath.getText().toString().isEmpty()){
    		Toast.makeText(MainActivity.this, "请输入视频地址", Toast.LENGTH_SHORT).show();
    		return false;
    	}	
    	else{
    		String path=tvVideoPath.getText().toString();
    		if((new File(path)).exists()==false){
    			Toast.makeText(MainActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
    			return false;
    		}else{
    			MediaInfo info=new MediaInfo(path,false);
    			boolean ret=info.prepare();
    	        Log.i(TAG,"info:"+info.toString());
    	        
    			return ret;
    		}
    	}
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(checkPath()==false)
			return;
		switch (v.getId()) {
			case R.id.id_main_demofilter:
				startExecuteDemo(FilterPreviewDemoActivity.class);
				break;
			case R.id.id_main_demofiltersprite:
				startExecuteDemo(FilterSpriteDemoActivity.class);
				
				break;
			case R.id.id_main_demoedit:
				startExecuteDemo(VideoEditDemoActivity.class);
				break;
			case R.id.id_main_twovideooverlay:
				startExecuteDemo(VideoVideoRealTimeActivity.class);
				break;
			case R.id.id_main_videobitmapoverlay:
				startExecuteDemo(VideoPictureRealTimeActivity.class);
				break;
			case R.id.id_main_pictures:
				startExecuteDemo(PictureSetRealTimeActivity.class);
				break;
			case R.id.id_main_viewspritedemo:
				startExecuteDemo(VideoViewRealTimeActivity.class);
				break;
			case R.id.id_main_scaleexecute:
				startExecuteDemo(ScaleExecuteActivity.class);
				break;
			case R.id.id_main_filterexecute:
				startExecuteDemo(FilterExecuteActivity.class);
				break;
			case R.id.id_main_mediapoolexecute_filter:
				startExecuteDemo(FilterSpriteExecuteActivity.class);
				break;
			case R.id.id_main_mediapoolexecute:
				startExecuteDemo(VideoVideoExecuteActivity.class);
				break;
			case R.id.id_main_mediapoolpictureexecute:
				startExecuteDemo(PictureSetExecuteActivity.class);
				break;
			case R.id.id_main_testaudiomix:
				startExecuteDemo(TestAudioMixManagerActivity.class);
				break;
			case R.id.id_main_testvideoplay:
				startExecuteDemo(VideoPlayerActivity.class);
				break;
			case R.id.id_main_viewremark:
				startExecuteDemo(VideoRemarkActivity.class);
				break;
			default:
				break;
		}
	}
	  	private void startSelectVideoActivity()
	    {
	    	Intent i = new Intent(this, FileExplorerActivity.class);
		    startActivityForResult(i,SELECT_FILE_REQUEST_CODE);
	    }
	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	// TODO Auto-generated method stub
	    	super.onActivityResult(requestCode, resultCode, data);
	    	switch (resultCode) {
			case RESULT_OK:
					if(requestCode==SELECT_FILE_REQUEST_CODE){
						Bundle b = data.getExtras();   
			    		String string = b.getString("SELECT_VIDEO");   
						Log.i("sno","SELECT_VIDEO is:"+string);
						if(tvVideoPath!=null)
							tvVideoPath.setText(string);
					}
				break;

			default:
				break;
			}
	    }
	   	private void startExecuteDemo(Class<?> cls)
	   	{
	   		String path=tvVideoPath.getText().toString();
	    	Intent intent=new Intent(MainActivity.this,cls);
	    	intent.putExtra("videopath", path);
	    	startActivity(intent);
	   	}
	   @SuppressLint("NewApi") 
		  public static boolean selfPermissionGranted(Context context,String permission) {
		        // For Android < Android M, self permissions are always granted.
		        boolean result = true;
		        int targetSdkVersion = 0;
		        try {
		            final PackageInfo info = context.getPackageManager().getPackageInfo(
		                    context.getPackageName(), 0);
		            targetSdkVersion = info.applicationInfo.targetSdkVersion;
		        } catch (PackageManager.NameNotFoundException e) {
		            e.printStackTrace();
		        }

		        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

		            if (targetSdkVersion >= Build.VERSION_CODES.M) {
		                // targetSdkVersion >= Android M, we can
		                // use Context#checkSelfPermission
		                result = context.checkSelfPermission(permission)
		                        == PackageManager.PERMISSION_GRANTED;
		            } else {
		                // targetSdkVersion < Android M, we have to use PermissionChecker
		                result = PermissionChecker.checkSelfPermission(context, permission)
		                        == PermissionChecker.PERMISSION_GRANTED;
		            }
		        }
		        return result;
		    }
	   //--------------------------------------------
		private ProgressDialog  mProgressDialog;
	   public class CopyDefaultVideoAsyncTask extends AsyncTask<Object, Object, Boolean>{
			  @Override
			protected void onPreExecute() {
			// TODO Auto-generated method stub
				  mProgressDialog = new ProgressDialog(MainActivity.this);
		          mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		          mProgressDialog.setMessage("正在拷贝...");
		          mProgressDialog.setCancelable(false);
		          mProgressDialog.show();
		          super.onPreExecute();
			}
   	    @Override
   	    protected synchronized Boolean doInBackground(Object... params) {
   	    	// TODO Auto-generated method stub
   	    	
   	    	
          String str=SDKDir.TMP_DIR+"ping20s.mp4";
          if(FileUtils.fileExist(str)==false){
          	CopyFileFromAssets.copy(getApplicationContext(), "ping20s.mp4", SDKDir.TMP_DIR, "ping20s.mp4");
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
 		 String str=SDKDir.TMP_DIR+"ping20s.mp4";
 		 if(FileUtils.fileExist(str)){
 			 Toast.makeText(getApplicationContext(), "默认视频文件拷贝完成.路径是:"+str, Toast.LENGTH_SHORT).show();
 			 if(tvVideoPath!=null)
 				tvVideoPath.setText(str);
 		 }else{
 			Toast.makeText(getApplicationContext(), "抱歉! 默认视频文件拷贝失败,请联系我们 "+str, Toast.LENGTH_SHORT).show();
 		 }
 	}
 }
}
