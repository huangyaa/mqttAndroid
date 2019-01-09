package com.espressif.iot.esptouch.demo_activity.entity;

public class ResultInfo <T>{
    private String status;
    private String message;
    private T data;

    public String getStatus() {
        return status;
    }

    public void setStatus( String status ) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
