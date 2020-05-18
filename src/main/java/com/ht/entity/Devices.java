package com.ht.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//@Data
@Entity
@Table(name = "devices")
public class Devices {
    @Column(name = "device")
    private String device;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "port_number")
    private String portNumber;

    @Id
    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(String port) {
        this.portNumber = port;
    }

    @Override
    public String toString() {
        return "Devices{" +
                "device='" + device + '\'' +
                ", IP_Address='" + ipAddress + '\'' +
                ", PortNumber='" + portNumber + '\'' +
                '}';
    }
}
