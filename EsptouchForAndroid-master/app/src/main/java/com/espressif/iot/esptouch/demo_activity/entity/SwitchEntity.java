package com.espressif.iot.esptouch.demo_activity.entity;

public class SwitchEntity {
    private Integer id;
    private String switchName;
    private String deviceCode;
    private String switchStatus;

    /**
     * 在页面展示的序号
     */
    private String seq;

    public Integer getId() {
        return id;
    }

    public void setId( Integer id ) {
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

    public String getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus( String switchStatus ) {
        this.switchStatus = switchStatus;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq( String seq ) {
        this.seq = seq;
    }
}
