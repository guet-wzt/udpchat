package com.network.udpchat.common.controller;

import com.network.udpchat.client.RecieverThread;
import com.network.udpchat.common.StaticValue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class SendDataPackage {

    private DatagramSocket socket;
    public RecieverThread recieverThread;

    public SendDataPackage() throws SocketException {
        socket = new DatagramSocket();
        recieverThread = new RecieverThread(this.socket);
    }

    public RecieverThread getRecieverThread() {
        return recieverThread;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * 将信息发送给服务端
     * @param msg
     * @throws IOException
     */
    public void sendToServer(String msg) throws IOException {
        byte[] data = msg.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(data,data.length, InetAddress.getByName(StaticValue.SERVER_IP), StaticValue.DEFAULT_PORT);
        socket.send(datagramPacket);
    }

    /**
     * 监听客户端的端口，接受消息
     */
    public void recieverFromServer(){
        recieverThread.start();
    }

    /**
     * 停止线程
     */
    public void stopThread(){recieverThread.halt();}

}
