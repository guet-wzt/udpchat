package com.network.udpchat.client;

import com.network.udpchat.common.StaticValue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class RecieverThread extends Thread {
    private DatagramSocket reciever;
    private boolean stoped = false;
    public static String senderName = null;
    public static String message = null;


    public RecieverThread(DatagramSocket datagramSocket){
        this.reciever = datagramSocket;
    }
    public void halt(){
        stoped = true;
    }

    @Override
    public void run() {
        super.run();
        byte[] buffer = new byte[StaticValue.BUFFER_SIZE];

        while (true){
            if (stoped)return;
            DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length);
            try {
                /**
                 * 从服务端接收消息有待处理......
                 */
                reciever.receive(datagramPacket);
                String[] data = new String(datagramPacket.getData(),0,datagramPacket.getLength()).split("\\|");
                int length = data.length;
                senderName = data[length-1];
                message = data[length-2];
                System.out.println("客户端接收消息：" + message + "from " + senderName + ":" + 1);
                Thread.yield();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
