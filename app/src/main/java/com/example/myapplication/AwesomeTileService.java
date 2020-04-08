package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;


import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AwesomeTileService extends TileService {


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context,intent.toString(),Toast.LENGTH_LONG).show();
            getQsTile().setState(Tile.STATE_INACTIVE);
            Toast.makeText(getApplicationContext(),"Start",Toast.LENGTH_LONG).show();
            getQsTile().updateTile();
        }
    };






    private Vibrator vb;
    @Override
    public void onTileAdded() {
        getQsTile().setState(Tile.STATE_INACTIVE);
        getQsTile().updateTile();

        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {


    }

    @Override
    public void onStopListening() {

        super.onStopListening();
    }

    @Override
    public void onClick() {
        vb = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        if(getQsTile().getState() == Tile.STATE_INACTIVE){
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://api.lifx.com/v1/lights/all/state";
            StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            Log.d("Response", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray res = jsonObject.getJSONArray("results");
                                JSONObject fin = res.getJSONObject(0);
                                Log.d("Res", fin.toString());
                                if(fin.getString("status").equals("ok")){
                                    getQsTile().setState(Tile.STATE_ACTIVE);
                                    getQsTile().updateTile();
                                }else if(fin.getString("status").equals("offline")){
                                    Toast.makeText(getApplicationContext(),"Light Offline",Toast.LENGTH_LONG).show();
                                    vb.vibrate(120);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(),"Something Went Wrong",Toast.LENGTH_LONG).show();
                                vb.vibrate(120);
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof NoConnectionError) {
                                Toast.makeText(getApplicationContext(), "Internet not Available", Toast.LENGTH_LONG).show();
                                vb.vibrate(120);
                            }else {
                                Toast.makeText(getApplicationContext(), "Something Went Wrong", Toast.LENGTH_LONG).show();
                                vb.vibrate(120);
                            }
                        }
                    }
            ) {

                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();
                    params.put("power","on");
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String>  params = new HashMap<>();
                    params.put("Authorization", "Bearer c988c158ba6cdadd6dbd5e35f72bbc42a7c199a2598d23ec87b45700252f249b");
                    return params;
                }
            };

            queue.add(putRequest);


        }else if(getQsTile().getState() == Tile.STATE_ACTIVE){
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://api.lifx.com/v1/lights/all/state";
            StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            Log.d("Response", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray res = jsonObject.getJSONArray("results");
                                JSONObject fin = res.getJSONObject(0);
                                Log.d("Res", fin.toString());
                                if(fin.getString("status").equals("ok")){
                                    getQsTile().setState(Tile.STATE_INACTIVE);
                                    getQsTile().updateTile();
                                }else if(fin.getString("status").equals("offline")){
                                    Toast.makeText(getApplicationContext(),"Light Offline",Toast.LENGTH_LONG).show();
                                    vb.vibrate(120);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(),"Something Went Wrong",Toast.LENGTH_LONG).show();
                                vb.vibrate(120);
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof NoConnectionError) {
                                Toast.makeText(getApplicationContext(), "Internet not Available", Toast.LENGTH_LONG).show();
                                vb.vibrate(120);
                            }else {
                                Toast.makeText(getApplicationContext(), "Something Went Wrong", Toast.LENGTH_LONG).show();
                                vb.vibrate(120);
                            }
                        }
                    }
            ) {

                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();
                    //
                    params.put("power","off");
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String>  params = new HashMap<>();
                    params.put("Authorization", "Bearer c988c158ba6cdadd6dbd5e35f72bbc42a7c199a2598d23ec87b45700252f249b");

                    return params;
                }
            };

            queue.add(putRequest);


        }

        super.onClick();
    }
}
