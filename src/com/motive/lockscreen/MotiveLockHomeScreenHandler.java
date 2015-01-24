package com.motive.lockscreen;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

public class MotiveLockHomeScreenHandler extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//startService(new Intent(this,MyService.class));
		if (!MotiveLockScreen.running) {
			finish();
			Intent intent=null;
			final PackageManager packageManager=getPackageManager();
			for(final ResolveInfo resolveInfo:packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_DEFAULT_ONLY))
			{
				if(!getPackageName().equals(resolveInfo.activityInfo.packageName))  //if this activity is not in our activity (in other words, it's another default home screen)
				{
					intent = new Intent().addCategory(Intent.CATEGORY_HOME).setAction(Intent.ACTION_MAIN) .setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
					break;
				}
			}
			if (intent != null) {
				startActivity(intent);
			}
		}




		/*getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	        KeyguardManager km =(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
	        k1 = km.newKeyguardLock("IN");
	        k1.disableKeyguard();*/


	}

}
