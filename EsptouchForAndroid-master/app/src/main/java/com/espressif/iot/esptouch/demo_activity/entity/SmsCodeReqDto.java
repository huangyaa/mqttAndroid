package com.espressif.iot.esptouch.demo_activity.entity;

import java.util.Date;

public class SmsCodeReqDto {
    private String smsType;

    private String phone;

    /**
     * 唯一编码
     */
    private String uniqueCode;

    /**
     * 时间
     */
    private String time;

    /**
     * 签名
     */
    private String sign;

    /**
     * 平台 1：android；2：ios
     */
    private String platform;

    public String getSmsType() {
        return smsType;
    }

    public void setSmsType(String smsType) {
        this.smsType = smsType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public String getTime() {
        return time;
    }

    public void setTime( String time ) {
        this.time = time;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
