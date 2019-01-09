package com.espressif.iot.esptouch.demo_activity.entity;

public class DeviceInfoEntity {
    private Integer id;

    private Integer userId;

    private String deviceCode;

    private String type;

    private Integer shareUserId;

    private String status;

    private Integer switchNum;

    private String deviceName;

    public Integer getId() {
        return id;
    }

    public void setId( Integer id ) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId( Integer userId ) {
        this.userId = userId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode( String deviceCode ) {
        this.deviceCode = deviceCode;
    }

    public String getType() {
        return type;
    }

    public void setType( String type ) {
        this.type = type;
    }

    public Integer getShareUserId() {
        return shareUserId;
    }

    public void setShareUserId( Integer shareUserId ) {
        this.shareUserId = shareUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus( String status ) {
        this.status = status;
    }

    public Integer getSwitchNum() {
        return switchNum;
    }

    public void setSwitchNum( Integer switchNum ) {
        this.switchNum = switchNum;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName( String deviceName ) {
        this.deviceName = deviceName;
    }
}
