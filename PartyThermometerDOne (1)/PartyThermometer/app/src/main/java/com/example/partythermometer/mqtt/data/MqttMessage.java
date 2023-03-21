package com.example.partythermometer.mqtt.data;

public class MqttMessage {
    /*private final String user;
    private final String message;*/

    private final String X;
    private final String Y;
    private final String Z;


    public MqttMessage(String X, String Y, String Z) {
       /* this.user = user;
        this.message = message;*/
        this.X = X;
        this.Y = Y;
        this.Z = Z;

    }
    public String getX(){
        return X;
    }

    public String getY() {
        return Y;
    }

    public String getZ() {
        return Z;
    }

    /* public String getUser() {
        return user;
    }*/

}