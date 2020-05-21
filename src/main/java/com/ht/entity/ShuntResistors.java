package com.ht.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

// @Data
@Entity
@Table(name = "shuntresistors")
public class ShuntResistors {

    @Id
    private String id;
    @Column(name = "rvalue")
    private float rValue;
    @Column(name = "rValid")
    private boolean rValid;
    @Column(name = "usedBy")
    private String usedBy;

    public String getID() {
        return id;
    }

    public void setID(String resistorID) {
        this.id = resistorID;
    }

    public float getRValue() { return rValue; }

    public void setRValue(float resistorValue) {
        this.rValue = resistorValue;
    }

    public boolean getRValid() { return rValid;   }

    public void setRValue(boolean resistorValid) { this.rValid = resistorValid;  }

    public String getUsedBy() {
        return this.usedBy;
    }

    public void setUsedBy(String resistorUsedBy) {
        this.usedBy = resistorUsedBy;
    }
}
