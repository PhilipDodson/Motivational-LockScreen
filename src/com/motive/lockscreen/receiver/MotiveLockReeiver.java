package com.motive.lockscreen.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.motive.lockscreen.MotiveLockScreen;

public class MotiveLockReeiver extends BroadcastReceiver  {
	 public static boolean wasScreenOn = true;

	@Override
	public void onReceive(Context context, Intent intent) {
   if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
          	wasScreenOn=false;
        	Intent intent11 = new Intent(context,MotiveLockScreen.class);
        	intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        	context.startActivity(intent11);

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

        	wasScreenOn=true;
        	Intent intent11 = new Intent(context,MotiveLockScreen.class);
        	intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        }
       else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
    	   KeyguardManager.KeyguardLock k1;
    	   KeyguardManager km =(KeyguardManager)context.getSystemService(context.KEYGUARD_SERVICE);
    	   k1 = km.newKeyguardLock("IN");
    	   k1.disableKeyguard();

    	   Intent intent11 = new Intent(context, MotiveLockScreen.class);

    	   intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	   context.startActivity(intent11);

        	//  Intent intent = new Intent(context, LockPage.class);
	        //  context.startActivity(intent);
	        //  Intent serviceLauncher = new Intent(context, UpdateService.class);
	        //  context.startService(serviceLauncher);
	        //  Log.v("TEST", "Service loaded at start");
       }

    }


}
