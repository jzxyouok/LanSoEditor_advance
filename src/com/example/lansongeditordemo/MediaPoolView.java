package com.example.lansongeditordemo;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TableLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

import com.lansosdk.box.BitmapSprite;
import com.lansosdk.box.FilterSprite;
import com.lansosdk.box.ISprite;
import com.lansosdk.box.MediaPoolUpdateMode;
import com.lansosdk.box.MediaPoolViewRender;
import com.lansosdk.box.TextureRenderView;
import com.lansosdk.box.VideoSprite;
import com.lansosdk.box.ViewSprite;
import com.lansosdk.box.onMediaPoolCompletedListener;
import com.lansosdk.box.onMediaPoolProgressListener;
import com.lansosdk.box.onMediaPoolSizeChangedListener;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.MediaSource;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;


/**
 *  视频处理预览和实时保存的View, 继承自FrameLayout.
 *  
 *   适用在增加到UI界面中, 一边预览,一边实时保存的场合.
 *
 */
public class MediaPoolView extends FrameLayout {
	
	private static final String TAG="MediaPoolView";
	private static final boolean VERBOSE = true;  
  
    private int mVideoRotationDegree;

    private TextureRenderView mTextureRenderView;
 	
    private MediaPoolViewRender  renderer;
 	
 	private SurfaceTexture mSurfaceTexture=null;
 	

 	
 	private int encWidth,encHeight,encBitRate,encFrameRate;
 	/**
 	 *  经过宽度对齐到手机的边缘后, 缩放后的宽高,作为opengl的宽高. 
 	 */
 	private int viewWidth,viewHeight;  
 	
 	  
    public MediaPoolView(Context context) {
        super(context);
        initVideoView(context);
    }

    public MediaPoolView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    public MediaPoolView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MediaPoolView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVideoView(context);
    }


    private void initVideoView(Context context) {
        setTextureView();

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }
    static final int AR_ASPECT_FIT_PARENT = 0; // without clip
    static final int AR_ASPECT_FILL_PARENT = 1; // may clip
    static final int AR_ASPECT_WRAP_CONTENT = 2;
    static final int AR_MATCH_PARENT = 3;
    static final int AR_16_9_FIT_PARENT = 4;
    static final int AR_4_3_FIT_PARENT = 5;

    
    private void setTextureView() {
    	mTextureRenderView = new TextureRenderView(getContext());
    	mTextureRenderView.setSurfaceTextureListener(new SurfaceCallback());
    	
    	mTextureRenderView.setAspectRatio(AR_ASPECT_FIT_PARENT);
        
    	View renderUIView = mTextureRenderView.getView();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,//<--------------需要调整这里视频的宽度
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        renderUIView.setLayoutParams(lp);
        addView(renderUIView);
        mTextureRenderView.setVideoRotation(mVideoRotationDegree);
    }
    private String encodeOutput=null; //编码输出路径
    
    private MediaPoolUpdateMode mUpdateMode=MediaPoolUpdateMode.ALL_VIDEO_READY;
    private int mAutoFlushFps=0;
    
    /**
     * 设置MediaPool的刷新模式,默认 {@link MediaPool.UpdateMode#ALL_VIDEO_READY};
     * 
     * @param mode
     * @param autofps  //自动刷新的参数,每秒钟刷新几次(即视频帧率).当自动刷新的时候有用, 不是自动,则不起作用.
     */
    public void setUpdateMode(MediaPoolUpdateMode mode,int autofps)
    {
    	mAutoFlushFps=autofps;
    	
    	mUpdateMode=mode;
    	
    	if(renderer!=null)
    	{
    		 renderer.setUpdateMode(mUpdateMode,mAutoFlushFps);
    	}
    }
    /**
     * 获取当前渲染画面的 宽度
     * @return
     */
    public int getViewWidth(){
    	return viewWidth;
    }
    /**
     * 获得当前渲染画面的高度.
     * @return
     */
    public int getViewHeight(){
    	return viewHeight;
    }
    
    //此回调仅仅是作为演示: 当跳入到别的Activity后的返回时,会再次预览当前画面的功能. 
    //你完全可以重新按照你的界面需求来修改这个MediaPoolView类.
    public interface onViewAvailable {	    
        void viewAvailable(MediaPoolView v);
    }
	private onViewAvailable mViewAvailable=null;
	public void setOnViewAvailable(onViewAvailable listener)
	{
		mViewAvailable=listener;
	}
	
	
	private class SurfaceCallback implements SurfaceTextureListener {
    			
				/**
				 *  Invoked when a {@link TextureView}'s SurfaceTexture is ready for use.
				 *   当画面呈现出来的时候, 会调用这个回调.
				 *   
				 *  当Activity跳入到别的界面后,这时会调用{@link #onSurfaceTextureDestroyed(SurfaceTexture)} 销毁这个Texture,
				 *  如果您想在再次返回到当前Activity时,再次显示预览画面, 可以在这个方法里重新设置一遍MediaPool,并再次startMediaPool 
				 */
				
    	        @Override
    	        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    	            mSurfaceTexture = surface;
    	            viewHeight=height;
    	            viewWidth=width;
    	            if(mViewAvailable!=null)
    	            	mViewAvailable.viewAvailable(null);
//    	            Log.i(TAG,"VIEW-------->onSurfaceTextureAvailable");
    	        }
    	        
    	        /**
    	         * Invoked when the {@link SurfaceTexture}'s buffers size changed.
    	         * 当创建的TextureView的大小改变后, 会调用回调.
    	         * 
    	         * 当您本来设置的大小是480x480,而MediaPool会自动的缩放到父view的宽度时,会调用这个回调,提示大小已经改变, 这时您可以开始startMediaPool
    	         * 如果你设置的大小更好等于当前Texture的大小,则不会调用这个, 详细的注释见 startMediaPool
    	         */
    	        @Override
    	        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    	            mSurfaceTexture = surface;
    	            viewHeight=height;
    	            viewWidth=width;
//    	            Log.i(TAG,"VIEW-------->onSurfaceTextureSizeChanged");
	        			if(mSizeChangedCB!=null)
	        				mSizeChangedCB.onSizeChanged(width, height);
    	        }
    	
    	        /**
    	         *  Invoked when the specified {@link SurfaceTexture} is about to be destroyed.
    	         *  
    	         *  当您跳入到别的Activity的时候, 会调用这个,销毁当前Texture;
    	         *  
    	         */
    	        @Override
    	        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    	            mSurfaceTexture = null;
    	            viewHeight=0;
    	            viewWidth=0;
//    	            Log.i(TAG,"VIEW-------->onSurfaceTextureDestroyed");
    	            return false;
    	        }
    	
    	        /**
    	         * 
    	         * Invoked when the specified {@link SurfaceTexture} is updated through
    	         * {@link SurfaceTexture#updateTexImage()}.
    	         * 
    	         *每帧视频如果更新了, 则会调用这个!!!! 
    	         */
    	        @Override
    	        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//    	        	Log.i(TAG,"VIEW-------->onSurfaceTextureUpdated");
    	        }
    }
		/**
		 * 
		 * 设置使能 实时录制, 即把正在MediaPool中呈现的画面实时的保存下来,实现所见即所得的模式
		 * 
		 * @param encW  录制视频的宽度
		 * @param encH  录制视频的高度
		 * @param encBr 录制视频的bitrate,
		 * @param encFr 录制视频的 帧率
		 * @param outPath  录制视频的保存路径.
		 */
	   public void setRealEncodeEnable(int encW,int encH,int encBr,int encFr,String outPath)
	    {
	    	if(outPath!=null && encW>0 && encH>0 && encBr>0 && encFr>0){
	    			encWidth=encW;
			        encHeight=encH;
			        encBitRate=encBr;
			        encFrameRate=encFr;
			        encodeOutput=outPath;
	    	}else{
	    		Log.w(TAG,"enable real encode is error,may be outpath is null");
	    		encodeOutput=null;
	    	}
	    }
	
	   private onMediaPoolSizeChangedListener mSizeChangedCB=null; 
	   /**
	    * 设置当前MediaPool的宽度和高度,并把宽度自动缩放到父view的宽度,然后等比例调整高度.
	    * 
	    * 如果在父view中已经预设好了希望的宽高,则可以不调用这个方法,直接 {@link #startMediaPool(onMediaPoolProgressListener, onMediaPoolCompletedListener)}
	    * 可以通过 {@link #getViewHeight()} 和 {@link #getViewWidth()}来得到当前view的宽度和高度.
	    * 
	    * 
	    * 注意: 这里的宽度和高度,会根据手机屏幕的宽度来做调整,默认是宽度对齐到手机的左右两边, 然后调整高度, 把调整后的高度作为mediaPool渲染线程的真正宽度和高度.
	    * 注意: 此方法需要在 {@link #startMediaPool(onMediaPoolProgressListener, onMediaPoolCompletedListener)} 前调用.
	    * 比如设置的宽度和高度是480,480, 而父view的宽度是等于手机分辨率是1080x1920,则mediaPool默认对齐到手机宽度1080,然后把高度也按照比例缩放到1080.
	    * 
	    * @param width  预设值的宽度
	    * @param height 预设值的高度 
	    * @param cb   设置好后的回调, 注意:如果预设值的宽度和高度经过调整后 已经和父view的宽度和高度一致,则不会触发此回调(当然如果已经是希望的宽高,您也不需要调用此方法).
	    */
	public void setMediaPoolSize(int width,int height,onMediaPoolSizeChangedListener cb){
		
		if (width != 0 && height != 0 && cb!=null) {
            if (mTextureRenderView != null) {
            	
                mTextureRenderView.setVideoSize(width, height);
                mTextureRenderView.setVideoSampleAspectRatio(1,1);
                mSizeChangedCB=cb;
            }
            requestLayout();
        }
	}

	/**
	 * 开始mediaPool的渲染线程. 
	 * 此方法需要在 {@link onMediaPoolSizeChangedListener} 完成后调用.
	 * 可以先通过 {@link #setMediaPoolSize(int, int, onMediaPoolSizeChangedListener)}来设置宽高,然后在回调中执行此方法.
	 * 
	 * @param progresslistener
	 * @param completedListener
	 */
	public void startMediaPool(onMediaPoolProgressListener progresslistener,onMediaPoolCompletedListener completedListener)
	{
		 if( mSurfaceTexture!=null)
         {
 			renderer=new MediaPoolViewRender(getContext(), viewWidth, viewHeight);  //<----从这里去建立MediaPool线程.
 			if(renderer!=null){
 				renderer.setDisplaySurface(new Surface(mSurfaceTexture));
 				renderer.setEncoderEnable(encWidth,encHeight,encBitRate,encFrameRate,encodeOutput);
 				
 				renderer.setUpdateMode(mUpdateMode,mAutoFlushFps);
 				 
 				renderer.setMediaPoolProgessListener(progresslistener);
 				renderer.setMediaPoolCompletedListener(completedListener);
 				
 				renderer.start();
 			}
         }
	}
	/**
	 * 当前MediaPool是否在工作.
	 * @return
	 */
	public boolean isRunning()
	{
		if(renderer!=null)
			return renderer.isRunning();
		else
			return false;
	}
	/**
	 * 停止MediaPool的渲染线程.
	 */
	public void stopMediaPool()
	{
			if (renderer != null){
	        	renderer.release();
	        	renderer=null;
	        }
	}
	/**
	 * 作用同 {@link #setMediaPoolSize(int, int, onMediaPoolSizeChangedListener)}, 只是有采样宽高比, 如用我们的VideoPlayer则使用此方法,
	 * 建议用 {@link #setMediaPoolSize(int, int, onMediaPoolSizeChangedListener)}
	 * @param width
	 * @param height
	 * @param sarnum  如mediaplayer设置后,可以为1,
	 * @param sarden
	 * @param cb
	 */
	public void setMediaPoolSize(int width,int height,int sarnum,int sarden,onMediaPoolSizeChangedListener cb)
	{
		if (width != 0 && height != 0) {
            if (mTextureRenderView != null) {
                mTextureRenderView.setVideoSize(width, height);
                mTextureRenderView.setVideoSampleAspectRatio(sarnum,sarden);
            }
            mSizeChangedCB=cb;
            requestLayout();
        }
	}
	/**
	 * 获取一个主视频的 VideoSprite
	 * @param width 主视频的画面宽度  建议用 {@link MediaInfo#vWidth}来赋值
	 * @param height 
	 * @return
	 */
	public VideoSprite obtainMainVideoSprite(int width, int height)
    {
		VideoSprite ret=null;
	    
		if(renderer!=null)
			ret=renderer.obtainMainVideoSprite(width, height);
		else{
			Log.e(TAG,"setMainVideoSprite error render is not avalid");
		}
		return ret;
    }
	public FilterSprite obtainFilterSprite(int width, int height,GPUImageFilter filter)
    {
		FilterSprite ret=null;
	    
		if(renderer!=null)
			ret=renderer.obtainFilterSprite(width, height,filter);
		else{
			Log.e(TAG,"obtainFilterSprite error render is not avalid");
		}
		return ret;
    }
	/**
	 * 获取一个VideoSprite
	 * @param width  当前Sprite视频的宽度
	 * @param height 当前Sprite视频的高度
	 * @return
	 */
	public VideoSprite obtainSubVideoSprite(int width, int height)
	{
		if(renderer!=null)
			return renderer.obtainVideoSprite(width,  height);
		else{
			Log.e(TAG,"obtainSubVideoSprite error render is not avalid");
			return null;
		}
	}
	/**
	 * 获取一个BitmapSprite
	 * @param bmp  图片的bitmap对象
	 * @return 一个BitmapSprite对象
	 */
	public BitmapSprite obtainBitmapSprite(Bitmap bmp)
	{
		Log.i(TAG,"imgBitmapSprite:"+bmp.getWidth()+" height:"+bmp.getHeight());
    	
		if(renderer!=null)
			return renderer.obtainBitmapSprite(bmp);
		else{
			Log.e(TAG,"obtainBitmapSprite error render is not avalid");
			return null;
		}
	}
	 
	/**
	 * 
	 * 获得一个 ViewSprite
	 * 
	 * @return
	 */
	 public ViewSprite obtainViewSprite()
	 {
			if(renderer!=null)
				return renderer.obtainViewSprite();
			else{
				Log.e(TAG,"obtainCanvasSprite error render is not avalid");
				return null;
			}
	 }
	 /**
	  * 从渲染线程列表中移除并销毁这个Sprite, 
	  * @param sprite
	  */
	public void removeSprite(ISprite sprite)
	{
		if(renderer!=null)
			renderer.removeSprite(sprite);
		else{
			Log.w(TAG,"setMainVideoSprite error render is not avalid");
		}
	}
	 /**
	    * 切换 滤镜
	    * 
	    * @param filter  切换后的滤镜对象.
	    * @return 切换成功返回true, 否则返回false
	    */
	   public boolean  switchFilterTo(FilterSprite sprite, GPUImageFilter filter) {
	    	if(renderer!=null){
	    		return renderer.switchFilterTo(sprite, filter);
	    	}
    		return false;
	    }
}
