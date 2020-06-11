package com.ht.entity;

public  class  EolStatus {
    private static EolStatus eolStatusObject;
    private String eolStatus = "Ready";

    public static EolStatus getInstance() {
        if (eolStatusObject == null) {
            eolStatusObject = new EolStatus();
        }
        return eolStatusObject;
    }

    public synchronized String getEolStatus() {
        return eolStatus;
    }

    public synchronized void setEolStatus(String eolStatus) {
        this.eolStatus = eolStatus;
    }
}
