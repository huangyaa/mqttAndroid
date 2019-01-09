package com.espressif.iot.esptouch.demo_activity.entity;

/**
 * 分享设备信息类
 */
public class ShareDeviceEntity {

    private String deviceCode;

    private String sharedMobile;

    private String addAuthority;

    private String delAuthority;

    private String updateAuthority;

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode( String deviceCode ) {
        this.deviceCode = deviceCode;
    }

    public String getSharedMobile() {
        return sharedMobile;
    }

    public void setSharedMobile( String sharedMobile ) {
        this.sharedMobile = sharedMobile;
    }

    public String getAddAuthority() {
        return addAuthority;
    }

    public void setAddAuthority( String addAuthority ) {
        this.addAuthority = addAuthority;
    }

    public String getDelAuthority() {
        return delAuthority;
    }

    public void setDelAuthority( String delAuthority ) {
        this.delAuthority = delAuthority;
    }

    public String getUpdateAuthority() {
        return updateAuthority;
    }

    public void setUpdateAuthority( String updateAuthority ) {
        this.updateAuthority = updateAuthority;
    }
}
