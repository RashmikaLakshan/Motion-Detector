package com.rashnanayakkara.mqttproject;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;



public class MainActivity extends AppCompatActivity {

    public MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions options;
    private ImageLoader imageloader;
    private Vibrator vibrator;
    private Ringtone ring;

    final String serverUri = "tcp://broker.hivemq.com:1883";
    private String clientId ;
    final String[] topics = {"Sem3-iot-01","Sem3-iot-02","Sem3-iot-03"};
    private ImageView img;
    private TextView text;
    //sem3_iot_01 ==> view message
    //sem3_iot_02 ==> view image
    //sem3_iot_03 ==> request motion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.imageView);
        text = findViewById(R.id.textView);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ring = RingtoneManager.getRingtone(getApplicationContext(), uri);
        imageloader = ImageLoader.getInstance();
        imageloader.init(new ImageLoaderConfiguration.Builder(getApplicationContext()).build());

        clientId = MqttClient.generateClientId();
        mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), serverUri, clientId);
        helper();

    }

    public void helper(){
        options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

        try {
            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    setSubscription();
                    Toast.makeText(MainActivity.this,"Connected !",Toast.LENGTH_LONG).show();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Error: "+exception.getMessage());
                    exception.printStackTrace();
                    Toast.makeText(MainActivity.this,"Connection Faild !",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                switch(topic){
                    case "Sem3-iot-01":
                        text.setText(new String(message.getPayload()));
                        break;

                    default:
                        imageloader.displayImage(new String(message.getPayload()), img);
                        break;
                }

                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                ring.play();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void pub(View v){
        String message = "Send a photo";
        try {
            mqttAndroidClient.publish(topics[2], message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void setSubscription(){
        try{
            for(String topic:topics){
                mqttAndroidClient.subscribe(topic,0);
            }
        }catch (MqttException e){
            e.printStackTrace();
            System.out.println("subscribe is not working");
        }
    }

    public void conn(View v){
        try {
            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this,"Connected !",Toast.LENGTH_LONG).show();
                    setSubscription();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this,"Connection Failed !",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            System.err.println("Exception subscribing");
            e.printStackTrace();
        }
    }

    public void disConn(View v){
        try {
            IMqttToken token = mqttAndroidClient.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this,"Disconnected !",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this,"Could not Disconnect..!",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            System.err.println("Exceptions subscribing");
            e.printStackTrace();
        }
    }
}
