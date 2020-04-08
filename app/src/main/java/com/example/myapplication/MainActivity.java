package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MainActivity extends AppCompatActivity {

    private MqttAsyncClient mqttClient;
    private TextView statusTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String clientId = "Android_App";
        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        conOpt.setUserName("xyutlglh");
        conOpt.setPassword("lEGQulPYASzn".toCharArray());
        statusTextview = findViewById(R.id.textView);


        MqttCallback mqttCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d("Hz",cause.toString() + " 40");
            }


            @Override
            public void messageArrived(String topic, final MqttMessage message) {
                Log.d("Hz",message.toString() + " 43");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        statusTextview.setText(message.toString());

                    }
                });

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        };


//        try {
//            mqttClient = new MqttClient("tcp://mqtt.gbridge.io:1883", clientId, new MemoryPersistence());
//            mqttClient.setCallback(mqttCallback);
//            mqttClient.connect(conOpt);
//            mqttClient.subscribe("gBridge/u4628/stat/tasmota/POWER", 1);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }


        try {
            mqttClient = new MqttAsyncClient("tcp://tailor.cloudmqtt.com:12390", clientId, new MemoryPersistence());
            mqttClient.setCallback(mqttCallback);
            mqttClient.connect(conOpt,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Hz","Connected 73");
                    try {
                        mqttClient.subscribe("stat/tasmota/POWER",1);
                        MqttMessage message = new MqttMessage("".getBytes());
                        message.setQos(1);
                        mqttClient.publish("cmnd/tasmota/power",message);
                    } catch (MqttException e) {
                        Log.d("Hz",e.getMessage() + " 73");
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (MqttException e) {
           Log.d("Hz",e.getMessage() + " 70");
        }


//
createNotificationChannel();



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mqttClient != null){
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        if(!mqttClient.isConnected()){
            try {
                mqttClient.connect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        super.onResume();
    }

    public void on(View view) {
        MqttMessage message = new MqttMessage("toggle".getBytes());
        message.setQos(1);
        try {
            mqttClient.publish("cmnd/tasmota/power", message);
        } catch (MqttException e) {
            Log.d("Hz",e.getMessage() + " 100");
        }
    }

    public void startPowerService(View view) {
        if(isServiceRunning()){
            Toast.makeText(getApplicationContext(),"Already Monitoring", Toast.LENGTH_LONG).show();
        }else {
            final Intent intent = new Intent(this, PowerMonitor.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(intent);
            } else {
                this.startService(intent);
            }
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("01", "BattFull", importance);
            channel.setDescription("Battery Full Notification");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void stopPowerService(View view) {
        if(isServiceRunning()) {
            final Intent intent = new Intent(this, PowerMonitor.class);
            stopService(intent);
        }else{
            Toast.makeText(getApplicationContext(), "Not Monitoring", Toast.LENGTH_LONG).show();
        }

    }
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.myapplication.PowerMonitor".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
