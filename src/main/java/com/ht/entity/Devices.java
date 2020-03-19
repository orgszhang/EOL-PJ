package com.ht.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//@Data
@Entity
@Table(name = "Devices")
public class Devices {
    @Column(name = "Device")
    private String device;
    @Column(name = "IP_Address")
    private String ip;
    @Column(name = "PortNumber")
    private String port;

    @Id
    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Devices{" +
                "device='" + device + '\'' +
                ", IP_Address='" + ip + '\'' +
                ", PortNumber='" + port + '\'' +
                '}';
    }
}
