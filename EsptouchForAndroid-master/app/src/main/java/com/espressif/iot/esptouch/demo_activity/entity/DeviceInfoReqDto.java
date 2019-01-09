package com.espressif.iot.esptouch.demo_activity.entity;

public class DeviceInfoReqDto {
    /**
     * 用户名
     */
    private String mobile;

    private String deviceCode;

    private String deviceName;

    /**
     * （1：wifi设备；2：gprs设备）
     */
    private String type;

    private String shareMobile;

    /**
     * 1：绑定；2：分享
     */
    private String status;

    /**
     * 开关数量
     */
    private Integer switchNum;

    public String getMobile() {
        return mobile;
    }

    public void setMobile( String mobile ) {
        this.mobile = mobile;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode( String deviceCode ) {
        this.deviceCode = deviceCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName( String deviceName ) {
        this.deviceName = deviceName;
    }

    public String getType() {
        return type;
    }

    public void setType( String type ) {
        this.type = type;
    }

    public String getShareMobile() {
        return shareMobile;
    }

    public void setShareMobile( String shareMobile ) {
        this.shareMobile = shareMobile;
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
}
