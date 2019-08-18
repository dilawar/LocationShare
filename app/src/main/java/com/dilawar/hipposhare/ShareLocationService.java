package com.dilawar.hipposhare;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;

import androidx.core.app.JobIntentService;
import android.util.Log;

import java.util.Locale;
import java.util.Date;

import android.widget.Toast;

import android.location.Location;
import android.app.Activity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.Executor;

/**
 * Example implementation of a JobIntentService.
 */
public class ShareLocationService extends JobIntentService
{
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;


    // shouldContinue https://stackoverflow.com/a/22967394/1805129
    public static volatile boolean shouldContinue = true;
    public static volatile boolean isBusy = false;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work)
    {
        Log.i("INFO", "Queuing work");
        enqueueWork(context, ShareLocationService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent)
    {
        if( isBusy )
        {
            Log.i( "INFO", "Already sharing location...");
            return;
        }

        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        Log.i("ShareLocationService", "Executing work: " + intent);

        toast("We are sharing location now... ");
        isBusy = true;
        shouldContinue = true;

        while(true)
        {

            // Get location here.
            mFusedLocationClient.getLastLocation().addOnSuccessListener( 
                    //this.getMainExecutor(), 
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location)
                        {
                            if (location != null)
                            {
                                double wayLatitude = location.getLatitude();
                                double wayLongitude = location.getLongitude();
                                if( location.hasAccuracy())
                                {
                                    String now = new Date().toString();
                                    double accuracy = location.getAccuracy();
                                    double speed = location.getSpeed();
                                    Log.i("Location", String.format(Locale.US, "%s: %s, %s, %s"
                                                , now, wayLatitude, wayLongitude, speed));
                                }
                            }
                        }
                    });

            // Wait every 10 seconds.
            android.os.SystemClock.sleep(10*1000);
            if( shouldContinue == false)
                break;
        }

        if( shouldContinue == false)
        {
            isBusy = false;
            shouldContinue = true;

            stopSelf();
            return;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        toast("All work complete");
    }

    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text)
    {
        mHandler.post(new Runnable()
                {
                    @Override public void run()
                    {
                        Toast.makeText(ShareLocationService.this, text, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
