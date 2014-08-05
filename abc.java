package com.ustbqianlongzhanlan;


import com.example.test05.pic.PhotoViewAttacher;
import com.ustbqianlongzhanlan.R;
import com.ustbqianlongzhanlan.R.id;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;


import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ViewFlipper;
import android.view.View.OnTouchListener;

public class ServiceActivity extends Activity implements
		OnTouchListener,OnGestureListener  {
	///////do zoom stuff
	private String PackName="com.ustbqianlong";
	private Matrix mSuppMatrix = new Matrix();
	private Matrix mBaseMatrix = new Matrix();
	private Matrix mDisplayMatrix = new Matrix();
	private static boolean isZoom =false;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	//////
	private String path = null;
	private String num = null;
	private String n = null;
	private Bitmap bitmap = null;
	private Thread thread = null;
	private MediaPlayer mediaPlayer = null;
	private String dirName = Environment.getExternalStorageDirectory()
			.toString() + "/com.example.museum/text/";
	private String dir = null;
	private String[] filenames = null;
	private ImageButton nextPic;
	private TextView thingName = null;
	static private boolean isPause = false;
	static private boolean isReleased = false;
	static int playingProgress=0;
	static boolean isFirstPlay=true;
	
	private ImageView like = null;
	private PictureShowUtils pictureShowUtils;
	//private GestureDetector gestureDetector = null;
	
	private GestureDetector ZoomgestureDetector = null;
	private ViewFlipper viewFlipper = null;
	private Activity mActivity = null;
	private DBManager dbManager = null;
	private Bitmap likeIcon;
	private Bitmap dislikeIcon;
	private Integer languageInteger;
	////////////////
	Button FloatingPlayBtn;
	static boolean CheckStateFlag=false;
	// /////////////added
	public String language = "chinese";

	// //////////////////
	SeekBar musicBar = null;
	ImageView iv = null;
	ImageView button1;
	ImageView button2;
	ImageView button3;
	ImageView button4;
	private PhotoViewAttacher mAttacher;
	private Intent intent;
	Handler handler = new Handler();
	Runnable updateThread = new Runnable(){
	     public void run() {
	    	 
	    	 if(mediaPlayer!=null){
	      if(mediaPlayer.isPlaying()){
	    		 //获得歌曲现在播放位置并设置成播放进度条的值
	        musicBar.setProgress(mediaPlayer.getCurrentPosition());
	        handler.postDelayed(updateThread, 100);
	      		}	
	    	 }//每次延迟100毫秒再启动线程
	     
	     }
	    };
	// @Override
	// protected void onPause()
	// {
	// super.onPause();
	// onBackPressed();
	// }
	@Override
	protected void onResume() {
		super.onResume();
		preparePicture();
		ArrayList<ArrayList<String>> downloadFileList = new ArrayList<ArrayList<String>>();
		intent = getIntent();
		String i = intent.getStringExtra("number");
		
		XMLDecode xmlDecode = new XMLDecode();
	 	if( pictureShowUtils.getCount()<=1)
	 	{
	 		
	 		nextPic.setVisibility(View.INVISIBLE);
	 		
	 	} 
		ExhibitInformation exhibitInformation = xmlDecode.getExhibitInformation(Integer.valueOf(i), ServiceActivity.this);                                   
		if (bitmap == null || pictureShowUtils.getCount() != exhibitInformation.getImage().length) {
			
			
			for(String name : exhibitInformation.getImage()){
				ArrayList<String> tempList = new ArrayList<String>();
			tempList.add("image/" + name);
			tempList.add("text/" + (Integer.valueOf(i)).toString() + "/pic/"
					+ name);
			downloadFileList.add(tempList);
			}

			DownloadRunnable.addDownloadFileList(downloadFileList);
			DownloadRunnable.creatThreadNumber(1, null, new Handler(), true);
		}

	
		// /////////////////////////
		Intent intent = getIntent();
		num = intent.getStringExtra("name");
		n = intent.getStringExtra("number");
		if (intent.getStringExtra("fromMain").equals("N")) {
			if (intent.getStringExtra("language").equals("chinese"))
				num = "CA_" + (new Integer(n)).toString() + ".mp3";
			else if (intent.getStringExtra("language").equals("english"))
				num = "EN_" + (new Integer(n)).toString() + ".mp3";
		}
		path = dirName + n;
		// //////////////////////

		try {
				
					playMusic();
					
				
				prepareProgressBar();
				
				button3.setVisibility(View.GONE);
				
				
			
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(ServiceActivity.this, e.toString(),
					Toast.LENGTH_LONG).show();
			if(mediaPlayer!=null){
			mediaPlayer.release();
			mediaPlayer = null;
			}
			ArrayList<String> tempArrayList = new ArrayList<String>();
			if (intent.getStringExtra("language").equals("chinese")) {
				tempArrayList.add("audio-ca/" + num.toString());
			} else {
				tempArrayList.add("audio-en/" + num.toString());
			}
			tempArrayList.add("text/" + (new Integer(n)).toString() + "/mp3/"
					+ num.toString());
			ArrayList<ArrayList<String>> tempArrayList2 = new ArrayList<ArrayList<String>>();
			tempArrayList2.add(tempArrayList);
			DownloadRunnable.addDownloadFileList(tempArrayList2);
			DownloadRunnable.creatThreadNumber(1, null, null, true);
			// Intent result = new Intent(ServiceActivity.this,
			// MainMenuView.class);
			// result.putExtra("language",intent.getBundleExtra("language"));
			// startActivity(result);

		}
		
		if (intent.getStringExtra("language").equals("chinese"))
			languageInteger = AppConstant.DaoConstant.chinese;
		else {
			languageInteger = AppConstant.DaoConstant.english;
		}
		Exhibit exhibit = dbManager.findExhibitById(Integer.valueOf(n),
				AppConstant.DaoConstant.likeExhibit, languageInteger);
		if (exhibit == null) {
			like.setImageBitmap(dislikeIcon);
		} else {
			like.setImageBitmap(likeIcon);
		}
	}
	@Override 
	public void onDestroy()
	{
	
//		if(mediaPlayer!=null)
//		{
//			if(mediaPlayer.isPlaying()){
//				mediaPlayer.stop();
//			}
//		mediaPlayer.release();
//		}
//		isReleased=true;
		if(mediaPlayer!=null){
			mediaPlayer.release();
			mediaPlayer = null;
			}
		if(bitmap!=null){ 
			if(!bitmap.isRecycled()){ 
			bitmap.recycle(); //回收图片所占的内存 
			bitmap=null; 
			System.gc(); //提醒系统及时回收 
			} 
			}
		super.onDestroy();
		
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SysApplication.getInstance().addActivity(this); // aaaaaaaaaaaaaaaaaaa
		language = "chinese";
		final Intent intent = getIntent();
		num = intent.getStringExtra("name");
		dbManager = new DBManager(ServiceActivity.this);
		dislikeIcon= BitmapFactory.decodeResource(getResources(), R.drawable.unselected);
		likeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.selected);
		// if (num != null && !"".equals(num)) {
		// for (int i = 0; i < num.length(); i++) {
		// if (num.charAt(i) >= 48 && num.charAt(i) <= 57)
		// n += num.charAt(i);
		// }
		// }
		n = intent.getStringExtra("number");
		path = dirName + n;
		pictureShowUtils = new PictureShowUtils(path);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
		setContentView(R.layout.activity_main1);
		Toast.makeText(getApplicationContext(), "双指缩放放大图片", Toast.LENGTH_SHORT).show();
		//mActivity = this;
		//FloatingPlayBtn=new Button(this);
		//mActivity.addContentView(FloatingPlayBtn, new LayoutParams(100, 100));
		thingName=(TextView)findViewById(R.id.thingName);
		button1 = (ImageView) findViewById(R.id.button1);
//		button2 = (ImageView) findViewById(R.id.button2);
		button3 = (ImageView) findViewById(R.id.play);//play button
		button4 = (ImageView) findViewById(R.id.pause);
		nextPic=(ImageButton)findViewById(R.id.nextPic);
		thingName.setText(dbManager.findExhibitById((Integer.valueOf(n))-1, 1, 0).name);
		nextPic.setOnClickListener(new Listener0());
		button1.setOnClickListener(new Listener1());
//		button2.setOnClickListener(new Listener2());
		button3.setOnClickListener(new Listener3());//play pressed
		button4.setOnClickListener(new Listener4());
		viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
		
		/////zoom stuff
		ZoomgestureDetector = new GestureDetector(this, new MyGestureDetector());
		//viewFlipper.setOnTouchListener(this);
		
		ZoomgestureDetector.setOnDoubleTapListener(new OnDoubleTapListener() {
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onDoubleTapEvent(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				// TODO Auto-generated method stub
				ImageView temp_iv = (ImageView) viewFlipper.getCurrentView();
				
				if(isZoom==true)
				{
					
					mSuppMatrix.setScale(1f, 1f);
					mDisplayMatrix.set(mBaseMatrix);
					mDisplayMatrix.postConcat(mSuppMatrix);
					temp_iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					temp_iv.setImageMatrix(mDisplayMatrix);
					//mSuppMatrix=temp_iv.getImageMatrix();
					
					isZoom=false;
				}
				else
				{
					mSuppMatrix.setScale(2f, 2f,e.getX(),e.getY());
					mDisplayMatrix.set(mBaseMatrix);
					mDisplayMatrix.postConcat(mSuppMatrix);
					temp_iv.setScaleType(ImageView.ScaleType.MATRIX);
					temp_iv.setImageMatrix(mDisplayMatrix);
					mSuppMatrix=temp_iv.getImageMatrix();
				
				
					isZoom = true;
				}
				temp_iv.invalidate();
				return true;
			}
		});
		//viewFlipper.setLongClickable(true);
		//////
		like = (ImageView) findViewById(R.id.button2);
		if(intent.getStringExtra("language").equals("chinese"))
			languageInteger = AppConstant.DaoConstant.chinese;
		else {
			languageInteger = AppConstant.DaoConstant.english;
		}
		like.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Exhibit exhibit = dbManager.findExhibitById(Integer.valueOf(n), AppConstant.DaoConstant.likeExhibit, languageInteger);
				if(exhibit == null){
					if((exhibit = dbManager.findExhibitById(Integer.valueOf(n), AppConstant.DaoConstant.allExhibit, languageInteger))!= null){
						
						List<Exhibit> exhibits = new ArrayList<Exhibit>();
						exhibits.add(exhibit);
						dbManager.add(exhibits, 2);
						like.setImageBitmap(likeIcon);
					}
					else {
						return;
					}
				}
				else {
					if((exhibit = dbManager.findExhibitById(Integer.valueOf(n), AppConstant.DaoConstant.allExhibit, languageInteger))!= null){	
						List<Exhibit> exhibits = new ArrayList<Exhibit>();
						dbManager.delete(exhibit.name);
						like.setImageBitmap(dislikeIcon);
						
					}
					else {
						return;
					}
				}
			}
		});

//		preparePicture();
		// ///commented
		// playMusic();
		// prepareProgressBar();
		// button3.setVisibility(View.GONE);

	}
	
	@Override
	public void onBackPressed() {
		isFirstPlay=true;
		handler.removeCallbacks(updateThread);
		playingProgress=0;
		isReleased = true;
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer=null;
		}
	
		if(bitmap!=null){ 
			if(!bitmap.isRecycled()){ 
			bitmap.recycle(); //回收图片所占的内存 
			bitmap=null; 
			System.gc(); //提醒系统及时回收 
			} 
			}
		CheckStateFlag=false;
		finish();
	}

	@SuppressWarnings("deprecation")
	void preparePicture() {
		//gestureDetector = new GestureDetector(this); // 声明检测手势事件
		for (int i = 0; i < pictureShowUtils.getCount(); i++) { // 添加图片源
			iv = new ImageView(this);
			bitmap = BitmapFactory.decodeFile(pictureShowUtils.getImageAt(i));
			iv.setImageBitmap(bitmap);
			//iv.setPadding(0, 0, 0, 0);
			
			iv.setScaleType(ImageView.ScaleType.FIT_XY);
			iv.setAdjustViewBounds(true);
			viewFlipper.addView(iv, new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
		}

		viewFlipper.setAutoStart(false); // 设置自动播放功能（点击事件，前自动播放）
		
		
		ImageView temp_iv = (ImageView) viewFlipper.getCurrentView();
		temp_iv.setOnTouchListener(new MulitPointTouchListener(temp_iv));
		//viewFlipper.setFlipInterval(3000);
//		if (viewFlipper.isAutoStart() && !viewFlipper.isFlipping()) {
//			viewFlipper.startFlipping();
//		}
	}

	void playMusic() throws Exception {
		dir = path + "/mp3/";
		// filenames = new File(dir).list();
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
		}
		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				CheckStateFlag=true;
			}
		});
		mediaPlayer.reset();
		mediaPlayer.setDataSource(dir + num);
		mediaPlayer.prepare();
		
		mediaPlayer.seekTo(playingProgress);
		
		mediaPlayer.start();
		isFirstPlay=false;
		isPause = false;
		isReleased = false;
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				button3.setVisibility(View.VISIBLE);
				button4.setVisibility(View.GONE);
			}
		});
	}
	
	
	
	
	/**
	 * 返回当前的应用是否处于前台显示状态
	 * @param $packageName
	 * @return
	 */
	private boolean isTopActivity(String $packageName) 
	{
	    //_context是一个保存的上下文
	    ActivityManager __am = (ActivityManager) this.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
	    List<ActivityManager.RunningAppProcessInfo> __list = __am.getRunningAppProcesses();
	    if(__list.size() == 0) return false;
	    for(ActivityManager.RunningAppProcessInfo __process:__list)
	    {
	      
	        if(__process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
	                __process.processName.equals($packageName))
	        {
	            return true;
	        }
	    }
	    return false;
	}
	
	@Override
	protected void onPause(){
		CheckStateFlag=false;
		 handler.removeCallbacks(thread);
	
		if (mediaPlayer != null)
		{ 	
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;}
		if (bitmap != null) {
			bitmap.recycle();
		}
		super.onPause();
		viewFlipper.destroyDrawingCache();
		viewFlipper.removeAllViews();
	}
	void prepareProgressBar() {
		musicBar = (SeekBar) findViewById(R.id.progressBar1);
		musicBar.setOnSeekBarChangeListener(new ProcessBarListener());
		musicBar.setMax(mediaPlayer.getDuration());

	    handler.post(updateThread);
	}

	class ProcessBarListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			if (fromUser == true) {
				mediaPlayer.seekTo(progress);
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
	class Listener0 implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			viewFlipper.showNext();
			
			setMulitListenerForCurrentView();
		}
		
	}

	class Listener1 implements OnClickListener {

		@Override
		public void onClick(View v) {
			isReleased = true;
			if (mediaPlayer != null) {
				//mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer=null;
			}
			if (bitmap != null) {
				bitmap.recycle();
			}
			CheckStateFlag=false;
			finish();
		}

	}

	// class Listener2 implements OnClickListener {
	//
	// @Override
	// public void onClick(View v) {
	// }
	//
	// }

	class Listener3 implements OnClickListener {

		@Override
		public void onClick(View v) {

			mediaPlayer.start();
		    handler.post(thread);
			isPause = false;
			button4.setVisibility(View.VISIBLE);
			button3.setVisibility(View.GONE);
		}

	}

	class Listener4 implements OnClickListener {

		@Override
		public void onClick(View v) {
			mediaPlayer.pause();
			isPause = true;
			handler.removeCallbacks(thread);
			button3.setVisibility(View.VISIBLE);
			button4.setVisibility(View.GONE);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		return ZoomgestureDetector.onTouchEvent(event);
		//return gestureDetector.onTouchEvent(event); // 注册手势事件
	}


	
	

	

	


	

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return ZoomgestureDetector.onTouchEvent(event);
	}
	class MyGestureDetector extends SimpleOnGestureListener 
	{
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			
			viewFlipper.showNext();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			
			viewFlipper.showPrevious();
			}
			
			ImageView temp_iv = (ImageView) viewFlipper.getCurrentView();
			temp_iv.setOnTouchListener(new MulitPointTouchListener(temp_iv));
			return false;
		}
		@Override
		public void onLongPress(MotionEvent e)
		{
			//ImageView temp_iv = (ImageView) viewFlipper.getCurrentView();
			//mSuppMatrix.postTranslate(2, 2);
			//mBaseMatrix.setScale(2f,2f);
			//mDisplayMatrix.setScale(2f,2f);
			//mSuppMatrix.reset();
			
			//mDisplayMatrix.postScale(0.5f, 0.5f, e.getRawX(), e.getRawY());
			//mDisplayMatrix.postConcat(mSuppMatrix);
			
			//temp_iv.setImageMatrix(mDisplayMatrix);
			//mSuppMatrix.set(temp_iv.getImageMatrix());
//			
//			mSuppMatrix.setScale(2f, 2f,e.getX(),e.getY());
//			mDisplayMatrix.set(mBaseMatrix);
//			mDisplayMatrix.postConcat(mSuppMatrix);
//			temp_iv.setImageMatrix(mDisplayMatrix);
//			mSuppMatrix=iv.getImageMatrix();
//		
//			temp_iv.invalidate();
//			isZoom = true;
		}
	}



	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	public class MulitPointTouchListener implements OnTouchListener { 

		Matrix matrix = new Matrix(); 
		Matrix savedMatrix = new Matrix(); 

		public ImageView image; 
		static final int NONE = 0; 
		static final int DRAG = 1; 
		static final int ZOOM = 2; 
		int mode = NONE; 

		PointF start = new PointF(); 
		PointF mid = new PointF(); 
		float oldDist = 1f; 


		public MulitPointTouchListener(ImageView image) { 
		super(); 
		this.image = image; 
		} 

		@Override 
		public boolean onTouch(View v, MotionEvent event) { 
			
		this.image.setScaleType(ScaleType.MATRIX); 

		ImageView view = (ImageView) v; 
		// dumpEvent(event); 

		switch (event.getAction() & MotionEvent.ACTION_MASK) { 

		case MotionEvent.ACTION_DOWN: 

		//Log.w("FLAG", "ACTION_DOWN"); 
		matrix.set(view.getImageMatrix()); 
		savedMatrix.set(matrix); 
		start.set(event.getX(), event.getY()); 
		mode = DRAG; 
		break; 
		case MotionEvent.ACTION_POINTER_DOWN: 
		//Log.w("FLAG", "ACTION_POINTER_DOWN"); 
		oldDist = spacing(event); 
		if (oldDist > 10f) { 
		savedMatrix.set(matrix); 
		midPoint(mid, event); 
		mode = ZOOM; 
		} 
		break; 
		case MotionEvent.ACTION_UP: 
		//Log.w("FLAG", "ACTION_UP"); 
		case MotionEvent.ACTION_POINTER_UP: 
		//Log.w("FLAG", "ACTION_POINTER_UP"); 
		mode = NONE; 
		break; 
		case MotionEvent.ACTION_MOVE: 
		//Log.w("FLAG", "ACTION_MOVE"); 
		if (mode == DRAG) { 
		matrix.set(savedMatrix); 
		matrix.postTranslate(event.getX() - start.x, event.getY() 
		- start.y); 
		} else if (mode == ZOOM) { 
		float newDist = spacing(event); 
		if (newDist > 10f) { 
		matrix.set(savedMatrix); 
		float scale = newDist / oldDist; 
		matrix.postScale(scale, scale, mid.x, mid.y); 
		} 
		} 
		break; 
		} 

		view.setImageMatrix(matrix); 
		return true; 
		} 
		private void midPoint(PointF point, MotionEvent event) { 
			float x = event.getX(0) + event.getX(1); 
			float y = event.getY(0) + event.getY(1); 
			point.set(x / 2, y / 2); 
			} 
		private float spacing(MotionEvent event) { 
			float x = event.getX(0) - event.getX(1); 
			float y = event.getY(0) - event.getY(1); 
			return FloatMath.sqrt(x * x + y * y); 
			} 
	}
	void setMulitListenerForCurrentView()
	{
		
		
		ImageView temp_iv = (ImageView) viewFlipper.getCurrentView();
		temp_iv.setOnTouchListener(new MulitPointTouchListener(temp_iv));
	}
}
