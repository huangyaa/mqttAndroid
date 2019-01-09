package com.espressif.iot.esptouch.demo_activity.entity;

public class AutoSwitchSetEntity {

    private String id;

    private String switchName;

    private String deviceCode;

    private String delayTime;

    private String taskStatus;

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getSwitchName() {
        return switchName;
    }

    public void setSwitchName( String switchName ) {
        this.switchName = switchName;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode( String deviceCode ) {
        this.deviceCode = deviceCode;
    }

    public String getDelayTime() {
        return delayTime;
    }

    public void setDelayTime( String delayTime ) {
        this.delayTime = delayTime;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus( String taskStatus ) {
        this.taskStatus = taskStatus;
    }
}
