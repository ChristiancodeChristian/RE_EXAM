package com.example.partythermometer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.partythermometer.mqtt.SimpleMqttClient;
import com.example.partythermometer.mqtt.data.MqttMessage;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {


    private TextView txtTemp;
    private int criticalTemp = 25;
    private SeekBar barTemperature;
    private TextView txtCriticalTemp;
    private int oldCriticalTemp = -1;


    //region Constants
    private static final String chatTopic = "APP/publish";

    //endregion

    //region Properties
    private SimpleMqttClient client;

    //endregion



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTemp = findViewById(R.id.txt_Temp);
        txtCriticalTemp = findViewById(R.id.txt_CriticalTemp);
        barTemperature = findViewById(R.id.bar_Temp);
        barTemperature.setMin(0);
        barTemperature.setMax(50);
        barTemperature.setProgress(criticalTemp);

        txtTemp.setText("00.00");
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

                            //txtTemp.setText(String.format(valueX,",",valueY));
                            txtTemp.setText( valueX + valueY +valueZ);
                        }
                    });
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
}