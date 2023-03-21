package com.example.partythermometer;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.partythermometer.mqtt.SimpleMqttClient;
import com.example.partythermometer.mqtt.data.MqttMessage;
import com.hivemq.client.mqtt.datatypes.MqttQos;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    // private int criticalTemp = 25;
    //private SeekBar barTemperature;
    // private TextView txtCriticalTemp;
    // private int oldCriticalTemp = -1;
    private TextView txtTemp;
    private TextView txt_xValue;
    private TextView txt_yValue;
    private TextView txt_zValue;

    private ProgressBar bar_X;
    private ProgressBar bar_Y;
    private ProgressBar bar_Z;


    private Handler handler = new Handler();



    //region Constants
    private static final String chatTopic = "APP/publish";
    private static final String mqtt_publishTopic = "APP/subscribe";
    private SimpleMqttClient mqtt_client;

    //endregion

    //region Properties
    private SimpleMqttClient client;

    //endregion



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*     txtCriticalTemp = findViewById(R.id.txt_xValue);
        barTemperature = findViewById(R.id.bar_X);
        barTemperature.setMin(0);
        barTemperature.setMax(50);
        barTemperature.setProgress(criticalTemp);*/


        txtTemp = findViewById(R.id.txt_Temp);
        txt_xValue = findViewById(R.id.txt_xValue);
        txt_yValue = findViewById(R.id.txt_yValue);
        txt_zValue = findViewById(R.id.txt_zValue);

        bar_X = findViewById(R.id.bar_X);
        bar_Y = findViewById(R.id.bar_Y);
        bar_Z = findViewById(R.id.bar_Z);

        bar_X.setMin(-20);
        bar_X.setMax(20);

        bar_Y.setMin(-20);
        bar_Y.setMax(20);

        bar_Z.setMin(-20);
        bar_Z.setMax(20);

        txtTemp.setText("00.00");
        txt_xValue.setText("00.00");
        txt_yValue.setText("00.00");
        txt_zValue.setText("00.00");





       //txtCriticalTemp.setText(String.valueOf(criticalTemp));


  /*      barTemperature.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                criticalTemp = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                txtCriticalTemp.setText(String.valueOf(criticalTemp));
            }
        });*/


        client = new SimpleMqttClient("broker.hivemq.com", 1883, "Accel_Client");



    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!client.isConnected()) {
            // only proceed if we are not connected already
            connect();
            subscribe(chatTopic);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        // unsubscribe and disconnect when stopping the activity
        client.unsubscribe(chatTopic);
        client.disconnect();
    }



    //region MQTT related methods
    private void connect() {
        // establish connection to server (asynchronous)
        client.connect(new SimpleMqttClient.MqttConnection(this) {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Connection successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable error) {
                showError("Unable to connect");
            }
        });
    }

    private void subscribe(String topic) {
        // subscribe to chatTopic (asynchronous)
        client.subscribe(new SimpleMqttClient.MqttSubscription(this, chatTopic) {
            @Override
            public void onMessage(String topic, String payload) {
                // new message arrived

                // deserialize JSOn into ChatMessage object
                try {

                    MqttMessage msg = deserializeMessage(payload);

                 /*   if (Float.parseFloat(msg.getMessage()) > criticalTemp && criticalTemp != oldCriticalTemp)
                    {
                        oldCriticalTemp = criticalTemp;
                        Toast.makeText(MainActivity.this, "It's gettin hot in here", Toast.LENGTH_LONG).show();
                    }
*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String valueX = msg.getX();
                            String valueY = msg.getY();
                            String valueZ = msg.getZ();

                            valueX = valueX.substring(0, Math.min(valueX.length(), 5));
                            valueY = valueY.substring(0, Math.min(valueY.length(), 5));
                            valueZ = valueZ.substring(0, Math.min(valueZ.length(), 5));


                            txtTemp.setText("X:"+valueX + " Y:"+valueY +" Z:"+ valueZ);
                            txt_xValue.setText(valueX);
                            txt_yValue.setText(valueY);
                            txt_zValue.setText(valueZ);

                            double dvaluex = Double.parseDouble(valueX);
                            double dvaluey = Double.parseDouble(valueY);
                            double dvaluez = Double.parseDouble(valueZ);

                            int ivaluex = (int) dvaluex;
                            int ivaluey = (int) dvaluey;
                            int ivaluez = (int) dvaluez;

                            bar_X.setProgress(ivaluex);
                            bar_Y.setProgress(ivaluey);
                            bar_Z.setProgress(ivaluez);




                        }
                    });


                    //new SimpleMqttClient.MqttPublish(activity,"APP/subscribe","1");


                } catch(JSONException je) {
                    Log.e("JSON", "Error while deserializing payload", je);
                    showError("Invalid chat message received");
                }
            }

            @Override
            public void onError(Throwable error) {
                showError("Unable to join topic");
            }
        });


    }



    //method publish message to MQTT topic
    private void publish(String topic, String payload) {

        mqtt_client.publish(new SimpleMqttClient.MqttPublish(this, topic, payload) {

            @Override
            public void onSuccess() {
                super.onSuccess();
            }

            @Override
            public void onError(Throwable error) {
                super.onError(error);
                showError("Unable to publish topic.");
            }
        });
    }

    private JSONObject serializeMessage(String mode) throws JSONException {
        JSONObject publish_msg = new JSONObject();
        publish_msg.put("mode", mode);
        return publish_msg;
    }

    //method MQTT publish mode
    private void mqtt_publish_mode(String mode) {
        JSONObject payload = null;
        try {
            payload = serializeMessage(mode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        publish(mqtt_publishTopic, payload.toString());
    }








    // Show Toast on error
    private void showError(String msg) {

        Toast.makeText(MainActivity.this, String.format("Unexpected error: %s. Check the log for details", msg), Toast.LENGTH_LONG).show();

    }
    //endregion

    //region JSON serialization/deserialization
    private MqttMessage deserializeMessage(String json) throws JSONException{
        JSONObject jMessage = new JSONObject(json);

        String x_value = jMessage.getString("X");
        String y_value = jMessage.getString("Y");
        String z_value = jMessage.getString("Z");

        MqttMessage newMsg = new MqttMessage(x_value, y_value, z_value);

        return newMsg;
    }
    //endregion
  /*  private void sendResponse() {
        new SimpleMqttClient.MqttPublish(this,"APP/subscribe","messageTemp");
        //client.publish("APP/subscribe", "1", MqttQos.AT_LEAST_ONCE);
    }

    private void scheduleResponse() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendResponse();
            }
        }, 200);
    }*/


}