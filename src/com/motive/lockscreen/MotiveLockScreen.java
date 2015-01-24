package com.motive.lockscreen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.motive.lockscreen.R;
import com.motive.lockscreen.gallery.CustomGallery;
import com.motive.lockscreen.motivationals.net.MotivationsFetcher;
import com.motive.lockscreen.prefs.JSONSharedPreferences;
import com.motive.lockscreen.prefs.Prefs;
import com.motive.lockscreen.widget.slider.SlidingTab;
import com.motive.lockscreen.widget.slider.SlidingTab.OnTriggerListener;


public class MotiveLockScreen extends Activity {
	protected static final int MESSAGE_SET_MOTIVATIONAL_MESSAGE = 0;
	protected static final int MESSAGE_SET_MOTIVATIONAL_ERROR = 1;
	protected static final int MESSAGE_RESET_MOTIVATIONAL_BUTTON = 2;
	protected static final int MESSAGE_DISPLAY_TOAST = 3;
	protected static final String TAG = MotiveLockScreen.class.getSimpleName();
	protected static boolean running = false;
	public static final String MOTIVE_LOCK_IMAGES = "MOTIVE_LOCK_IMAGES";
	public static final String CURRENT_IMAGE = "CURRENT_IMAGE";
	public static final String IMAGES = "IMAGES";

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MESSAGE_SET_MOTIVATIONAL_MESSAGE:
				quoteBtn.setTextColor(Color.WHITE);
				quoteBtn.setText(msg.obj.toString());
				break;
			case MESSAGE_RESET_MOTIVATIONAL_BUTTON:
				quoteBtn.setTextColor(Color.WHITE);
				quoteBtn.setText("Tap to refresh Quotes");
				break;
			case MESSAGE_SET_MOTIVATIONAL_ERROR:
				quoteBtn.setTextColor(Color.RED);
				quoteBtn.setText(msg.obj.toString());
				break;
			case MESSAGE_DISPLAY_TOAST:
				Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	private static IntentFilter s_intentFilter;
	private ArrayList<String> images;

	static {
		s_intentFilter = new IntentFilter();
		s_intentFilter.addAction(Intent.ACTION_TIME_TICK);
		s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		s_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
	}
	private final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(Intent.ACTION_TIME_CHANGED) ||
					action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				changeActivityBackground();
				if (!Prefs.randomMotivationals(getApplicationContext())) {
					new Thread(motivational).start();
				}
			}
		}
	};

	private Runnable motivational = new Runnable () {

		@Override
		public void run() {

			try {

				sendMessage(MESSAGE_SET_MOTIVATIONAL_MESSAGE,
						Prefs.randomMotivationals(getApplicationContext()) ?
								Prefs.motivationalType(getApplicationContext()) == 1 ? MotivationsFetcher.getRandomBibleVerse(Prefs.randomBibleVersesFrom(getApplicationContext())
										).toString() : MotivationsFetcher.getRandomQuote().toString()
										:	Prefs.motivationalType(getApplicationContext()) == 1 ? MotivationsFetcher.getDailyBibleVerse().toString() : MotivationsFetcher.getDailyQuote().toString());
			} catch(Exception e) {
				sendMessage(MESSAGE_SET_MOTIVATIONAL_ERROR,"Cant fetch motivationals.Check Network");
				Log.e(TAG, e.getLocalizedMessage(),e);
				new Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						sendMessage(MESSAGE_RESET_MOTIVATIONAL_BUTTON,null);
					}

				}, 5*1000);
			}

		}

	};
	private RelativeLayout mainView;
	private Button quoteBtn;
	private Messenger messenger;

	public void setActivityBackground(int bgRes) {
		View view = getWindow().getDecorView();
		view.setBackgroundResource(bgRes);
	}

	protected void changeActivityBackground() {
		loadImages();
		if (!images.isEmpty()) {
			Collections.shuffle(images);
			setActivityBackground(Uri.parse("file://"+images.get(0)));
			Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
			editor.putString(CURRENT_IMAGE, images.get(0));
			editor.commit();
		}
	}

	private void loadImages() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		JSONArray ims;
		try {
			if ((ims = JSONSharedPreferences.loadJSONArray(prefs, IMAGES)) != null) {
				images = new ArrayList<String>();
				for (int i = 0; i < ims.length(); i++) {
					try {
						images.add(ims.getString(i));
					} catch (JSONException e) {
						Log.e(TAG, e.getLocalizedMessage(),e);
					}
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, e.getLocalizedMessage(),e);
		}

	}


	public void setActivityBackground(Uri file) {

		Bitmap bitmapImage = readBitmap(file);
		BitmapDrawable bgrImage = new BitmapDrawable(bitmapImage);
		mainView.setBackgroundDrawable(bgrImage);

	}

	public Bitmap readBitmap(Uri selectedImage) {
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2; //reduce quality
		AssetFileDescriptor fileDescriptor =null;
		try {
			fileDescriptor = getContentResolver().openAssetFileDescriptor(selectedImage,"r");
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getLocalizedMessage(),e);
		}
		finally{
			try {
				bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
				fileDescriptor.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bm;
	}

	private void sendMessage(int what, String theMsg) {
		Message msg = new Message();
		msg.what = what;
		msg.obj = theMsg;
		try {
			messenger.send(msg);
		} catch(RemoteException e) {
			Log.e(TAG, e.getLocalizedMessage(),e);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(m_timeChangedReceiver);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		registerReceiver(m_timeChangedReceiver, s_intentFilter);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		messenger = new Messenger(mHandler);

		setContentView(R.layout.lockscreen_sliding_tab);
		mainView = (RelativeLayout)findViewById(R.id.mainlockview);
		quoteBtn = (Button)findViewById(R.id.quote_btn);
		quoteBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(motivational).start();;
			}
		});

		SlidingTab st = (SlidingTab) findViewById(R.id.myslider);
		DigitalClock dc = (DigitalClock) findViewById(R.id.digitalClock1);
		dc.setTextColor(Color.WHITE);
		st.setOnTriggerListener(new OnTriggerListener() {

			@Override
			public void onTrigger(View v, int whichHandle) {
				switch(whichHandle) {
				case LEFT_HANDLE:
					running = false;
					finish();
					break;
				case RIGHT_HANDLE:
					changeActivityBackground();
					break;
				}

			}

			@Override
			public void onGrabbedStateChange(View v, int grabbedState) {
				// TODO Auto-generated method stub

			}

		});

		if(getIntent()!=null) {
			if (getIntent().hasExtra("kill")&&getIntent().getExtras().getInt("kill")==1){
				running = false;
				finish();
			}
			if (getIntent().hasExtra(MOTIVE_LOCK_IMAGES)) {
				ArrayList<CustomGallery> images = (ArrayList<CustomGallery>) getIntent().getExtras().getSerializable(MOTIVE_LOCK_IMAGES);
				if (!images.isEmpty()) {
					JSONArray imgPaths = new JSONArray();
					for (CustomGallery cg: images) {
						imgPaths.put(cg.sdcardPath);
					}
					JSONSharedPreferences.saveJSONArray(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()), IMAGES, imgPaths);
					Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
					editor.putString(CURRENT_IMAGE, images.get(0).sdcardPath);
					editor.commit();
				}
			}

		}
		try{

			startService(new Intent(this,MotiveLockService.class));

			KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
			KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
			lock.disableKeyguard();
			StateListener phoneStateListener = new StateListener();
			TelephonyManager telephonyManager =(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
			telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);

			//PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);

			// PowerManager.WakeLock w1 =pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.FULL_WAKE_LOCK,"MyApp");
			// w1.acquire();
			// w1.release();
		}catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, e.getLocalizedMessage(),e);
		}
		String path;
		if ((path =PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(CURRENT_IMAGE, null)) != null) {
			Log.e(TAG, path);
			setActivityBackground(Uri.parse("file://"+path));
		}
		// android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 2000);
		quoteBtn.performClick();
		running = true;
	}
	class StateListener extends PhoneStateListener{
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			super.onCallStateChanged(state, incomingNumber);
			switch(state){
			case TelephonyManager.CALL_STATE_RINGING:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				finish();
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			}
		}
	};

	@Override
	public void onBackPressed() {
		// Don't allow back to dismiss.
		return;
	}


	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN||keyCode == KeyEvent.KEYCODE_POWER||keyCode == KeyEvent.KEYCODE_VOLUME_UP||keyCode == KeyEvent.KEYCODE_CAMERA) {
			//this is where I can do my stuff
			return true; //because I handled the event
		}
		if(keyCode == KeyEvent.KEYCODE_HOME){
			/* WindowManager.LayoutParams params = getWindow().getAttributes();
    	   params.screenBrightness = 1;
    	   getWindow().setAttributes(params);*/

			return false;
		}
		return false;
	}

	@Override
	public void onAttachedToWindow() {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if(sdk < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			//getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
			/*getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);

			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
		} else {

		}
		super.onAttachedToWindow();
	}



	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_POWER ||event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN||event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
			//Intent i = new Intent(this, NewActivity.class);
			//startActivity(i);
			return false;
		}
		if(event.getKeyCode() == KeyEvent.KEYCODE_HOME){
			/*WindowManager.LayoutParams params = getWindow().getAttributes();
    		 params.screenBrightness = 1;
    		 getWindow().setAttributes(params);*/

			return false;
		}
		return false;
	}


}