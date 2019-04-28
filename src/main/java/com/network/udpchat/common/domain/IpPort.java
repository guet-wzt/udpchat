package com.network.udpchat.common.domain;

public class IpPort {
    private String ip;
    private int port;

    public IpPort(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
