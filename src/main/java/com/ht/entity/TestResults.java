package com.ht.entity;



import javax.persistence.*;

import java.util.Date;

@Entity
@Table(name = "TestResults")
public class TestResults {
    /*

    ResistorID	nvarchar(12)
    MaskID	int
    MainCurr	float
    V25	float
    R25	float
    V16	float
    R16	float
    NTC_R	float
    NTC_T	float
    TestTime	datetime
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="VisualPartNumber")
    private String visualPartNumber;

    @Column(name="MainCurr")
    private double mainCurr;
    @Column(name="V25")
    private double v25;
    @Column(name="R25")
    private double r25;
    @Column(name="V16")
    private double v16;
    @Column(name="R16")
    private double r16;
    @Column(name="ResistorOK")
    private boolean resistorOK;

    @Column(name="RNTC")
    private double ntcR;
    @Column(name="TNTC")
    private double ntcT;
    @Column(name="NTCOK")
    private boolean NTC_OK;
    @Column(name="TestTime")
    private Date testTime;

    public long getId() {
        return this.id;
    }

    public String getVisualPartNumber() {
        return visualPartNumber;
    }

    public void setVisualPartNumber(String resistorID) {
        this.visualPartNumber = resistorID;
    }

    public double getMainCurr() {
        return mainCurr;
    }

    public void setMainCurr(double mainCurr) {
        this.mainCurr = mainCurr;
    }

    public double getV25() {
        return v25;
    }

    public void setV25(double v25) {
        this.v25 = v25;
    }

    public double getR25() {
        return r25;
    }

    public void setR25(double r25) {
        this.r25 = r25;
    }

    public double getV16() {
        return v16;
    }

    public void setV16(double v16) {
        this.v16 = v16;
    }

    public double getR16() {
        return r16;
    }

    public void setR16(double r16) {
        this.r16 = r16;
    }

    public boolean getResistorOK() {
        return resistorOK;
    }

    public void setResistorOK(boolean isOK) {
        this.resistorOK = isOK;
    }

    public double getNtcR() {
        return ntcR;
    }

    public void setNtcR(double ntcR) {
        this.ntcR = ntcR;
    }

    public double getNtcT() {
        return ntcT;
    }

    public void setNtcT(double ntcT) {
        this.ntcT = ntcT;
    }

    public Date getTestTime() {
        return testTime;
    }

    public void setTestTime(Date testTime) {
        this.testTime = testTime;
    }

    public boolean getNTC_OK() {
        return NTC_OK;
    }

    public void setNTC_OK(boolean isOK) {
        this.NTC_OK = isOK;
    }

    @Override
    public String toString() {
        return "TestResults{" +
                "visualPartNumber='" + visualPartNumber + '\'' +
                "', mainCurr=" + mainCurr +
                ", v25=" + v25 +
                ", r25=" + r25 +
                ", v16=" + v16 +
                ", r16=" + r16 +
                ", resistorOK=" + resistorOK +
                ", ntcR=" + ntcR +
                ", ntcT=" + ntcT +
                ", NTC_OK=" + NTC_OK +
                ", testTime=" + testTime +
                '}';
    }
}
