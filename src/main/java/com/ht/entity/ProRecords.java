package com.ht.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 测试结果保存
 */
// @Data
@Entity
@Table(name = "prorecords")
public class ProRecords {
    @Id
    @Column(name = "visual_part_number")
    private String visualPartNumber;

    @Column(name = "resistor_id")
    private String resistorID;   //
    @Column(name = "R25")
    private Double R25;   //
    @Column(name = "R16")
    private Double R16;   //
    @Column(name = "RNTC")
    private Double Rntc;   //
    @Column(name = "TNTC")
    private Double Tntc;

    @Column(name = "qr_code")
    private String qrCode; // QRCode 二维码
    @Column(name = "pro_date")
    private Date proDate; //
    @Column(name = "comments")
    private String comments;

    /* 2020-11-04 增加零漂 */
    @Column(name = "zerov25")
    private double zerov25;
    @Column(name = "zerov16")
    private double zerov16;


    public void setVisualPartNumber(String visualPartNumber) {
        this.visualPartNumber = visualPartNumber;
    }

    public String getVisualPartNumber() {
        return this.visualPartNumber;
    }

    public String getResistorID() {
        return resistorID;
    }

    public void setResistorID(String resistorID) {
        this.resistorID = resistorID;
    }

    public Double getR25() {
        return R25;
    }

    public void setR25(double r25) {
        R25 = Double.valueOf(r25);
    }

    public Double getR16() {
        return R16;
    }

    public void setR16(double r16) {
        R16 = Double.valueOf(r16);
    }

    public Double getRntc() {
        return Rntc;
    }

    public void setRntc(double ntcRValue) {
        Rntc = Double.valueOf(ntcRValue);
    }

    public Double getTntc() {
        return Tntc;
    }

    public void setTntc(double ntcTValue) {
        Tntc = Double.valueOf(ntcTValue);
    }

    /* 2020-11-04 增加零漂 */
    public Double getZerov25() {
        return zerov25;
    }

    public void setZerov25(double zv25) {
        zerov25 = Double.valueOf(zv25);
    }

    public Double getZerov16() {
        return zerov16;
    }

    public void setZerov16(double zv16) {
        zerov16 = Double.valueOf(zv16);
    }

    public String getProCode() {
        return qrCode;
    }

    public void setProCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Date getProDate() {
        return proDate;
    }

    public void setProDate(Date proDate) {
        this.proDate = proDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");  // 2020-11-05 修正格式 S是毫秒
        return "ProRecords{" +
                "visualPartNumber='" + visualPartNumber +
                "', ResistorID='" + resistorID +
                "', R25=" + R25 +
                ", R16=" + R16 +
                ", Rntc=" + Rntc +
                ", Tntc=" + Tntc +
                ", zerov25=" + zerov25 + /* 2020-11-04 增加零漂 */
                ", zerov16=" + zerov16 +
                ", QRCode='" + qrCode + '\'' +
                ", ProDate='" + sdf.format(proDate) + '\'' +
                ", Comments='" + comments + '\'' +
                "'}";
    }
}

