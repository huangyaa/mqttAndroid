package com.espressif.iot.esptouch.demo_activity.dummy;

public class MessageEvent {
    private String message;
    private String mac;
    private String type;
    private String name;

    public MessageEvent(String message,String mac,String type,String name) {
        this.message = message;
        this.mac = mac;
        this.type = type;
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public String getMac() {
        return mac;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}