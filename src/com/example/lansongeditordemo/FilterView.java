package com.example.lansongeditordemo;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.widget.FrameLayout;


import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

import com.lansosdk.box.FilterViewRender;
import com.lansosdk.box.TextureRenderView;
import com.lansosdk.box.onFilterViewSizeChangedListener;


/**
 * 为视频滤镜封装的 view, 继承自FrameLayout.
 * 此类仅仅为了视频滤镜使用, 和MediaPool是相对独立的两个架构,没有关系.
 * 如果您仅仅做视频滤镜的操作,而不需要增加另外的功能, 可以使用这个做. 
 * 如果您需要在视频滤镜的过程中, 增加另外一些图片, 文字等效果, 请使用 {@link MediaPoolView}
 *
 */
public class FilterView extends FrameLayout {
    private String TAG = "LanSoSDK";
  
    private int mVideoRotationDegree;

    private TextureRenderView mTextureRenderView;
 	private FilterViewRender  renderer;
 	private SurfaceTexture mSurfaceTexture=null;
 	
 	private int viewWidth,viewHeight;  //setGlWidth/height经过textureview手机宽度对齐调整后的宽度和高度.
 	private int videoWidth,videoHeight;  //视频本身的宽度和高度.
 	
 	private int glWidth,glHeight;  //设置的opengl的宽度和高度.
 	private int encBitRate,encFrameRate;
    private String encOutputPath=null; //编码输出路径
 	
 	  
    public FilterView(Context context) {
        super(context);
        initVideoView(context);
    }

    public FilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    public FilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FilterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
    /**
     * 获取当前渲染线程的宽度
     * @return
     */
    public int getViewWidth(){
    	return viewWidth;
    }
    /**
     * 获取当前渲染线程的高度.
     * @return
     */
    public int getViewHeight(){
    	return viewHeight;
    }
    
	private class SurfaceCallback implements SurfaceTextureListener {
    			
    	        @Override
    	        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    	            mSurfaceTexture = surface;
    	            viewHeight=height;
    	            viewWidth=width;
    	        }
    	
    	        @Override
    	        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    	            mSurfaceTexture = surface;
    	            viewHeight=height;
    	            viewWidth=width;
    	            
    	            if(mSizeChangedCB!=null)
    	            	mSizeChangedCB.onSizeChanged(width, height);
    	        }
    	
    	        @Override
    	        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    	            mSurfaceTexture = null;
    	            return false;
    	        }
    	
    	        @Override
    	        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    	        }
    }
	
	/**
	 * 设置使能 实时录制
	 * 
	 * @param bitrate  录制视频的码率
	 * @param framerate  帧率
	 * @param outPath  保存的路径
	 */
	   public void setRealEncodeEnable(int bitrate,int framerate,String outPath)
	    {
	    	if(outPath!=null && bitrate>0 && framerate>0){
	    		encOutputPath=outPath;
	    		encBitRate=bitrate;
	    		encFrameRate=framerate;
	    	}else{
	    		Log.w(TAG,"enable real encode is error,may be outpath is null");
	    		encOutputPath=null;
	    	}
	    }
	   /**
	    * 获取渲染线程中创建的Surface, 这个surface用来把外面的视频源通过 {@link MediaPlayer#setSurface(Surface)},把视频画面渲染到此surface上.
	    * @return
	    */
	   public Surface getSurface()
	   {
			if(renderer!=null)
	    		return renderer.getSurface();
	    	else
	    		return null;
	   }
	   /**
	    * 切换 滤镜
	    * 
	    * @param filter  切换后的滤镜对象.
	    * @return 切换成功返回true, 否则返回false
	    */
	   public boolean  switchFilterTo(final GPUImageFilter filter) {
	    	if(renderer!=null)
	    		return renderer.switchFilterTo(filter);
	    	else
	    		return false;
	    }
	   
	   private onFilterViewSizeChangedListener mSizeChangedCB=null; 
	   /**
	    * 设置 滤镜渲染的大小.
	    * 
	    * @param glwidth  渲染线程opengl的预设宽度
	    * @param glheight 渲染线程opengl的预设高度.
	    * @param videoW   需要渲染视频的画面宽度
	    * @param videoH   需要渲染视频的画面高度
	    * @param cb     filter的自适应屏幕的宽度调整后的回调.
	    */
	public void setFilterRenderSize(int glwidth,int glheight,int videoW,int videoH,onFilterViewSizeChangedListener cb)
	{
		
		if(videoW>0 && videoH>0)
		{
			videoWidth=videoW;
			videoHeight=videoH;
		}
		if (glwidth != 0 && glheight != 0 && cb!=null) {
			
			glWidth=glwidth;
			glHeight=glheight;
			
            if (mTextureRenderView != null) {
                mTextureRenderView.setVideoSize(glwidth, glheight);
                mTextureRenderView.setVideoSampleAspectRatio(1,1);
                mSizeChangedCB=cb;
            }
            requestLayout();
        }
	}
	/**
	 * 设置这个之前需要 设置是否encoder {@link #setRealEncodeEnable(String)}
	 * @param width
	 * @param height
	 * @param sarnum  如采用mediaplayer设置,可以为1,
	 * @param sarden
	 * @param cb
	 */
//	public void setMediaPoolSize(int width,int height,int sarnum,int sarden,onFilterViewSizeChangedListener cb)
//	{
//		if (width != 0 && height != 0) {
//            if (mTextureRenderView != null) {
//                mTextureRenderView.setVideoSize(width, height);
//                mTextureRenderView.setVideoSampleAspectRatio(sarnum,sarden);
//            }
//            mSizeChangedCB=cb;
//            requestLayout();
//        }
//	}
	/**
	 * 开始filter 渲染线程.
	 */
	public void start()
	{
		 if(renderer==null && mSurfaceTexture!=null && viewHeight>0 && viewWidth>0)
         {
	            if(renderer==null)
	            {
	            	renderer=new FilterViewRender(getContext(), mSurfaceTexture, viewWidth, viewHeight,videoWidth,videoHeight);
	            	if(encOutputPath!=null){
	            		renderer.setEncoderEnable(glWidth, glWidth, encBitRate, encFrameRate, encOutputPath);
	            	}
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
	 * 停止当前渲染线程.
	 */
	public void stop()
	{
		 if (renderer != null){
	        	renderer.release();
	        	renderer=null;
	        }
	}
	
}
