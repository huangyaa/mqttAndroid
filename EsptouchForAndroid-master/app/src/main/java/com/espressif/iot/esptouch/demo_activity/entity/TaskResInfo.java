package com.espressif.iot.esptouch.demo_activity.entity;

import java.util.List;

public class TaskResInfo {
    private String status;
    private String message;
    private List<TaskClassEntity> data;

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

    public List<TaskClassEntity> getData() {
        return data;
    }

    public void setData( List<TaskClassEntity> data ) {
        this.data = data;
    }
}
