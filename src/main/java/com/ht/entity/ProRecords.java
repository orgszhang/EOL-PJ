package com.ht.entity;

import com.ht.base.SpringContext;
import com.ht.repository.LatestQRCodeRepo;
import com.ht.repository.ProRecordsRepo;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

/**
 * 测试结果保存
 */
// @Data
@Entity
@Table(name = "ProRecords")
public class ProRecords {
    @Id
    @Column(name = "VisualPartNumber")
    private String visualPartNumber;

    @Column(name = "R25")
    private Double R25;   //
    @Column(name = "R16")
    private Double R16;   //
    @Column(name = "RNTC")
    private Double Rntc;   //
    @Column(name = "TNTC")
    private Double Tntc;

    @Column(name = "QRCode")
    private String qrCode; // QRCode 二维码
    @Column(name = "ProDate")
    private Date proDate; //
    @Column(name = "Comments")
    private String comments;

    public void setVisualPartNumber(String visualPartNumber) {
        this.visualPartNumber = visualPartNumber;
    }

    public String getVisualPartNumber() {
        return this.visualPartNumber;
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
        return "ProRecords{" +
                "visualPartNumber='" + visualPartNumber +
                "', R25=" + R25 +
                ", R16=" + R16 +
                ", Rntc=" + Rntc +
                ", Tntc=" + Tntc +
                ", QRCode='" + qrCode + '\'' +
                ", ProDate='" + proDate + '\'' +
                ", Comments='" + comments + '\'' +
                "'}";
    }
}

