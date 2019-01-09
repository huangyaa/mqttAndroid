package com.espressif.iot.esptouch.demo_activity.entity;

public class LoginReqDto {
    private String mobile;

    private String passWord;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
