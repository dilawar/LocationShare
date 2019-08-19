package com.dilawar.hipposhare;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.core.app.JobIntentService;
import android.util.Log;

import java.util.Locale;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.DataOutputStream;

import org.json.JSONObject;

import android.widget.Toast;

import android.location.Location;
import android.app.Activity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

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

    private RequestQueue mRequestQueue;


    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Disable strict mode. Ideally I should use AsyncTask
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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

        mRequestQueue = Volley.newRequestQueue( getApplicationContext() );
        String urlAddr = "https://www.ncbs.res.in/hippo/api/geolocation/submit";

        while(true)
        {

            // Get location here.
            mFusedLocationClient.getLastLocation().addOnSuccessListener
                ( new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location)
                    {
                        if (location != null)
                        {

                            if( location.hasAccuracy())
                            {
                                String now = new Date().toString();

                                double lat = location.getLatitude();
                                double lng = location.getLongitude();
                                double accuracy = location.getAccuracy();
                                double speed = location.getSpeed();
                                double alt = location.getAltitude();

                                Log.i("Location", String.format(Locale.US, "%s: %s, %s, %s", now, lat, lng, speed)); 

                                if(speed >= 0.0)
                                {
                                    StringRequest req = new StringRequest(Request.Method.POST, urlAddr, 
                                            new Response.Listener<String>()
                                            {
                                                @Override
                                                public void onResponse(String response) 
                                                {
                                                    Log.i("response", response);
                                                }
                                            }, 
                                            new Response.ErrorListener() {
                                                @Override 
                                                public void onErrorResponse(VolleyError err) {
                                                    Log.e( "Error", err.getMessage(), err);
                                                }
                                            }
                                    ) {
                                        @Override 
                                        public Map<String, String> getHeaders() throws AuthFailureError
                                        {
                                            Map<String, String> header = super.getHeaders();
                                            return header;
                                        }

                                        @Override
                                        public Map<String, String> getParams() 
                                        {
                                            Map<String, String> data = new HashMap<String, String>();
                                            data.put("latitude", String.valueOf(lat));
                                            data.put("longitude", String.valueOf(lng));
                                            data.put("speed", String.valueOf(speed));
                                            data.put("accuracy", String.valueOf(accuracy));
                                            data.put("altitude", String.valueOf(alt));
                                            data.put("session", "HIPPO_TRACKER_APP");
                                            Log.d("POST", data.toString());
                                            return data;
                                        }

                                    };
                                    mRequestQueue.add(req);
                                }
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
