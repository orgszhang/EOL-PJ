package com.ht.entity;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


//@Data
@Entity
@Table(name = "latestqrcodes")
public class LatestQRCodes {
    private static final Log logger = LogFactory.getLog(LatestQRCodes.class);

    @Column(name = "CustomerPartNo")
    private String customerPartNo;
    @Column(name = "LatestQRCode")
    private String latestQRCode;

    @Id
    public String getCustomerPartNo() {
        return customerPartNo;
    }

    public void setCustomerPartNo(String customerPartNo) {
        this.customerPartNo = customerPartNo;
    }

    public String getLatestQRCode() {
        return latestQRCode;
    }

    public void setLatestQRCode(String latestQRCode) {
        this.latestQRCode = latestQRCode;
    }


    @Override
    public String toString() {
        return "BarCodeConfig{" +
                "customerPartNo='" + customerPartNo + '\'' +
                ", latestQRCode='" + latestQRCode + '\'' +
                '}';
    }
}
