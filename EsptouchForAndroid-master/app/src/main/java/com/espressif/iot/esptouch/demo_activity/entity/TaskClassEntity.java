package com.espressif.iot.esptouch.demo_activity.entity;

public class TaskClassEntity {
    private Integer id;
    private String deviceCode;
    private String command;
    private String switchName;
    private String type;
    private String excuTime;
    private String excuDate;
    private String status;
    private String mobile;

    public Integer getId() {
        return id;
    }

    public void setId( Integer id ) {
        this.id = id;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode( String deviceCode ) {
        this.deviceCode = deviceCode;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand( String command ) {
        this.command = command;
    }

    public String getExcuTime() {
        return excuTime;
    }

    public void setExcuTime( String excuTime ) {
        this.excuTime = excuTime;
    }

    public String getType() {
        return type;
    }

    public void setType( String type ) {
        this.type = type;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus( String status ) {
        this.status = status;
    }

    public String getSwitchName() {
        return switchName;
    }

    public void setSwitchName( String switchName ) {
        this.switchName = switchName;
    }

    public String getExcuDate() {
        return excuDate;
    }

    public void setExcuDate( String excuDate ) {
        this.excuDate = excuDate;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile( String mobile ) {
        this.mobile = mobile;
    }
}
