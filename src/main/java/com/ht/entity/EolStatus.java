package com.ht.entity;

public  class  EolStatus {
    private String eolStatus = "Ready";

    public  synchronized String getEolStatus() {
        return eolStatus;
    }

    public synchronized void setEolStatus(String eolStatus) {
        this.eolStatus = eolStatus;
    }

}
