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
    @Column(name = "r_value")
    private Double rValue;
    @Column(name = "r_valid")
    private Boolean rValid;
    @Column(name = "comments")
    private String comments;

    public String getID() {
        return id;
    }

    public void setID(String resistorID) {
        this.id = resistorID;
    }

    public Double getRValue() { return rValue; }

    public void setRValue(Double resistorValue) {
        this.rValue = resistorValue;
    }

    public Boolean getRValid() { return rValid;   }

    public void setRValue(Boolean resistorValid) { this.rValid = resistorValid;  }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String resistorUsedBy) {
        this.comments = resistorUsedBy;
    }
}
