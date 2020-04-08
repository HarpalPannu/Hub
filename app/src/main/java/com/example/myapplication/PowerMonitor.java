package com.example.myapplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Random;

public class PowerMonitor extends Service {
    private MqttAsyncClient mqttClient;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        String clientId = "Android_App";
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            if(status == BatteryManager.BATTERY_STATUS_FULL){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "01")
                        .setSmallIcon(R.drawable.light_bulb)
                        .setContentTitle("Battery Full")
                        .setContentText("Battery Full")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(1, builder.build());
                try {
                    mqttClient = new MqttAsyncClient("tcp:/tailor.cloudmqtt.com:12390", clientId, new MemoryPersistence());
                    // mqttClient.setCallback(mqttCallback);
                    MqttConnectOptions conOpt = new MqttConnectOptions();
                    conOpt.setCleanSession(true);
                    conOpt.setUserName("xyutlglh");
                    conOpt.setPassword("lEGQulPYASzn".toCharArray());
                    mqttClient.connect(conOpt,null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d("Hz","Connected 73");
                            try {
                                MqttMessage message = new MqttMessage("off".getBytes());
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

                new CountDownTimer(10000, 1000) {

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        try {
                            mqttClient.disconnect();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        stopSelf();
                    }
                }.start();

            }


        }
    };

    public PowerMonitor() {
    }


    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, filter);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, "01")
                .setContentTitle("Battery Monitoring")
                .setSmallIcon(R.drawable.light_bulb)
                .setContentIntent(pendingIntent)
                .build();
        Random rand = new Random();
        startForeground(rand.nextInt(1000), notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
