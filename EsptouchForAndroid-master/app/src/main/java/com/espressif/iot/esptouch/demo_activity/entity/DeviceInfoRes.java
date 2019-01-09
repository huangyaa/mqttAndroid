package com.espressif.iot.esptouch.demo_activity.entity;

import java.util.List;

public class DeviceInfoRes {
    private String status;
    private String message;
    private List<DeviceInfoEntity> data;

    public String getStatus() {
        return status;
    }

    public void setStatus( String status ) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    public List<DeviceInfoEntity> getData() {
        return data;
    }

    public void setData( List<DeviceInfoEntity> data ) {
        this.data = data;
    }
}
