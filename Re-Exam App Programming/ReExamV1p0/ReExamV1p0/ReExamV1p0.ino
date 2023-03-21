#include <ArduinoJson.h>
#include <WiFi.h>
#include <PubSubClient.h>

#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_ADXL343.h>

#define ADXL343_SCK 21
#define ADXL343_SCL 22

Adafruit_ADXL343 accel = Adafruit_ADXL343(12345);
bool isReading = true;


///////////////// Set up wifi //////////////////////////
char* ssid = "FRITZ!Box 6660 Cable ES_Ext"; // Have your own ssid here
char* password =  "50271227895200380657"; // Have your own password here
const char* subscribeTopic = "APP/subscribe"; // Have your subscription topic in here
const char* publishTopic = "APP/publish"; // Have your publish topic here
const char* mqtt_Server =  "broker.mqtt-dashboard.com"; //
const int mqttPort = 1883;

const char* clientName = "Accel_Client";  // make a unique client for yourself

WiFiClient espClient;  // make the name unique
PubSubClient client(espClient);

const int capacity = JSON_OBJECT_SIZE(4);
StaticJsonDocument<capacity> temp;
char Buffer[256] = {0};

/////////////////////////////////////////////////////


// WIFI/MQTT related ///////////////////////////////////////////////////////////////////////////////////////
void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}


// constant mqtt connection
void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (client.connect(clientName)) {
      Serial.println("connected");
      client.subscribe(subscribeTopic);

    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 1 second");
      // Wait 1 second before retrying

    }
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");

  String messageTemp;
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
    messageTemp += (char)payload[i];
  }

  Serial.println();

  if (String(topic) == subscribeTopic)
  {
    if (messageTemp == "0")
    {
      isReading = false;
    } else
    {
      isReading = true;
    }
  }

}
///////////////////////////////////////////////////////////////////////////////////////////



void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  /* Initialise the sensor */
  if (!accel.begin())
  {
    /* There was a problem detecting the ADXL343 ... check your connections */
    Serial.println("Ooops, no ADXL343 detected ... Check your wiring!");
    while (1);
  }

  accel.setRange(ADXL343_RANGE_16_G);

  setup_wifi();
  client.setServer(mqtt_Server, 1883);
  client.subscribe(subscribeTopic);
  client.setCallback(callback);

}

void loop() {
  // put your main code here, to run repeatedly:

  if (!client.connected()) {
    reconnect();
  } else {
    client.loop();
  }
  
  /* Get a new sensor event */
  if (isReading)
  {
    sensors_event_t event;
    accel.getEvent(&event);

    temp["X"] = event.acceleration.x;
    temp["Y"] = event.acceleration.y;
    temp["Z"] = event.acceleration.z; 
    size_t n = serializeJson(temp, Buffer);
    client.publish(publishTopic, Buffer, n);
    
  }

  delay(100);

}
