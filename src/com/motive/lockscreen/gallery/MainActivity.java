package com.motive.lockscreen.gallery;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.motive.lockscreen.MotiveLockScreen;
import com.motive.lockscreen.R;
import com.motive.lockscreen.prefs.JSONSharedPreferences;
import com.motive.lockscreen.prefs.Prefs;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class MainActivity extends Activity {

	private static final String TAG = null;
	GridView gridGallery;
	Handler handler;
	GalleryAdapter adapter;

	ArrayList<CustomGallery> dataT;

	ImageView imgSinglePick;
	Button btnGalleryPick;
	Button btnLaunchLockScreen;
	Button btnLaunchSettings;
	Button btnClearImages;

	String action;
	ViewSwitcher viewSwitcher;
	ImageLoader imageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		initImageLoader();
		init();
	}

	private void initImageLoader() {
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
		.cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565).build();
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
				this).defaultDisplayImageOptions(defaultOptions).memoryCache(
						new WeakMemoryCache());

		ImageLoaderConfiguration config = builder.build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
	}

	private void init() {

		handler = new Handler();
		gridGallery = (GridView) findViewById(R.id.gridGallery);
		gridGallery.setFastScrollEnabled(true);
		adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
		adapter.setMultiplePick(false);
		gridGallery.setAdapter(adapter);

		viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
		viewSwitcher.setDisplayedChild(1);

		imgSinglePick = (ImageView) findViewById(R.id.imgSinglePick);
		imgSinglePick.setImageResource(R.drawable.images);

		btnGalleryPick = (Button) findViewById(R.id.btnGalleryPick);
		btnGalleryPick.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
				startActivityForResult(i, 200);

			}
		});

		btnLaunchLockScreen = (Button) findViewById(R.id.btnLaunch);
		btnLaunchLockScreen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent lockScreenIntent =
						new Intent(getApplicationContext(), MotiveLockScreen.class);
				if (dataT != null && !dataT.isEmpty()) {
					lockScreenIntent.putExtra(MotiveLockScreen.MOTIVE_LOCK_IMAGES, dataT);
				}
				startActivity(lockScreenIntent);
				finish();
			}
		});

		btnLaunchSettings = (Button) findViewById(R.id.btnSettings);
		btnLaunchSettings.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(getApplicationContext(), Prefs.class));

			}

		});
		btnClearImages = (Button) findViewById(R.id.btnClear);
		btnClearImages.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dataT.clear();
				JSONSharedPreferences.remove(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()),MotiveLockScreen.IMAGES);
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().remove(MotiveLockScreen.CURRENT_IMAGE).apply();
				viewSwitcher.setDisplayedChild(1);
			}

		});
		int sdk = android.os.Build.VERSION.SDK_INT;
		if(sdk > android.os.Build.VERSION_CODES.HONEYCOMB) {
			btnClearImages.setVisibility(View.GONE);
			btnLaunchSettings.setVisibility(View.GONE);
		}
		dataT = new ArrayList<CustomGallery>();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		JSONArray ims;
		try {
			if ((ims = JSONSharedPreferences.loadJSONArray(prefs, MotiveLockScreen.IMAGES)) != null) {
				for (int i = 0; i < ims.length(); i++) {
					try {
						CustomGallery  cg = new CustomGallery();
						cg.sdcardPath = ims.getString(i);
						dataT.add(cg);
					} catch (JSONException e) {
						Log.e(TAG, e.getLocalizedMessage(),e);
					}
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, e.getLocalizedMessage(),e);
		}

		if (!dataT.isEmpty()) {
			viewSwitcher.setDisplayedChild(0);
			adapter.addAll(dataT);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
			adapter.clear();

			viewSwitcher.setDisplayedChild(1);
			String single_path = data.getStringExtra("single_path");
			imageLoader.displayImage("file://" + single_path, imgSinglePick);

		} else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
			String[] all_path = data.getStringArrayExtra("all_path");

			dataT = new ArrayList<CustomGallery>();

			for (String string : all_path) {
				CustomGallery item = new CustomGallery();
				item.sdcardPath = string;

				dataT.add(item);
			}

			viewSwitcher.setDisplayedChild(0);
			adapter.addAll(dataT);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Settings")
		.setOnMenuItemClickListener(new OnMenuItemClickListener () {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				startActivity(new Intent(getApplicationContext(), Prefs.class));
				return false;
			}
		}).setIcon(R.drawable.ic_settings);

		menu.add("Clear Images")
		.setOnMenuItemClickListener(new OnMenuItemClickListener () {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				dataT.clear();
				JSONSharedPreferences.remove(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()),MotiveLockScreen.IMAGES);
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().remove(MotiveLockScreen.CURRENT_IMAGE).apply();
				viewSwitcher.setDisplayedChild(1);
				return false;
			}
		}).setIcon(R.drawable.ic_clear);

		return super.onCreateOptionsMenu(menu);
	}


}
