package com.espressif.iot.esptouch.demo_activity.entity;

public class StreamEntity {
    private Integer id;

    private String msisdn;

    private String iccid;

    private String imsi;

    private String imei;

    private String carrieroperator;

    private String streamPackage;

    private String inStockDate;

    private String activateDate;

    private String chargeEndDate;

    private String useStream;

    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getCarrieroperator() {
        return carrieroperator;
    }

    public void setCarrieroperator(String carrieroperator) {
        this.carrieroperator = carrieroperator;
    }

    public String getStreamPackage() {
        return streamPackage;
    }

    public void setStreamPackage( String streamPackage ) {
        this.streamPackage = streamPackage;
    }

    public String getInStockDate() {
        return inStockDate;
    }

    public void setInStockDate(String inStockDate) {
        this.inStockDate = inStockDate;
    }

    public String getActivateDate() {
        return activateDate;
    }

    public void setActivateDate(String activateDate) {
        this.activateDate = activateDate;
    }

    public String getChargeEndDate() {
        return chargeEndDate;
    }

    public void setChargeEndDate(String chargeEndDate) {
        this.chargeEndDate = chargeEndDate;
    }

    public String getUseStream() {
        return useStream;
    }

    public void setUseStream(String useStream) {
        this.useStream = useStream;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
