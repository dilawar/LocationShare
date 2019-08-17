package com.dilawar.hipposhare;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import androidx.core.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

/**
 * Example implementation of a JobIntentService.
 */
public class ShareLocationService extends JobIntentService
{
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

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
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        Log.i("ShareLocationService", "Executing work: " + intent);
        String label = intent.getStringExtra("label");
        if (label == null)
        {
            label = intent.toString();
        }
        toast("Executing: " + label);
        for (int i = 0; i < 5; i++)
        {
            Log.i("ShareLocationService", "Running service " + (i + 1)
                  + "/5 @ " + SystemClock.elapsedRealtime());
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
            }
        }
        Log.i("ShareLocationService", "Completed service @ " + SystemClock.elapsedRealtime());
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
