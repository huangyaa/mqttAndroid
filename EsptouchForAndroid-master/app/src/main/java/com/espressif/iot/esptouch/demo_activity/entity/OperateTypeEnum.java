package com.espressif.iot.esptouch.demo_activity.entity;

import com.google.zxing.common.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum OperateTypeEnum {
    OPEN("1", "开"),
    CLOSE("2", "关"),
    ADD_TIMER("3", "设置定时器"),
    DEL_TIMER("4", "删除定时器"),
    CLOSE_TIMER("5", "关闭定时器"),
    OPEN_TIMER("6", "打开定时器"),
    ALL_OPEN("7", "打开所有开关"),
    ALL_CLOSE("8", "关闭所有开关"),
    ;

    private String key;

    private String value;


    OperateTypeEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static Map<String ,String > getValueMap(){
        Map<String,String> enumMap = new HashMap<>();
        for (OperateTypeEnum typeEnum : OperateTypeEnum.values()) {
            enumMap.put(typeEnum.getKey(),typeEnum.getValue());
        }
        return enumMap;
    }
}
