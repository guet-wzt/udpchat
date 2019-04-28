package com.network.udpchat.server;

import com.network.udpchat.common.StaticValue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

public class UDPEchoServer extends UDPserver {

    public UDPEchoServer() throws SocketException {
        super(StaticValue.DEFAULT_PORT);
    }

    /**
     * 获取到用户数据包，将数据包转发出去
     * @param request
     */
    @Override
    public void respond(DatagramPacket request) {
        try {
            DatagramPacket outgoing = new DatagramPacket(request.getData(),request.getLength(),request.getAddress(),request.getPort());
            ds.send(outgoing);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("服务端启动....");
            UDPEchoServer server = new UDPEchoServer();
            server.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
