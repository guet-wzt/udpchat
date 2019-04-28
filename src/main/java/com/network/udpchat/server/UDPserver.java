package com.network.udpchat.server;

import com.network.udpchat.common.StaticValue;
import com.network.udpchat.common.domain.UserPort;
import com.network.udpchat.server.service.ChatServiceImp;


import java.io.IOException;
import java.net.*;


abstract class UDPserver extends Thread {

    private int bufferSize;
    protected DatagramSocket ds;
    private ChatServiceImp service;

    public UDPserver(int port, int bufferSize) throws SocketException {
        this.bufferSize = bufferSize;
        this.ds = new DatagramSocket(port);
    }

    /**
     * 绑定服务端的端口
     * @param port
     * @throws SocketException
     */
    public UDPserver(int port) throws SocketException {
        this(port, StaticValue.BUFFER_SIZE);
    }

    @Override
    public void run() {
        super.run();
        byte[] buffer = new byte[StaticValue.BUFFER_SIZE];
        try {
            service = new ChatServiceImp();
            while (true){
                DatagramPacket incoming = new DatagramPacket(buffer,buffer.length);
                try {
                    ds.receive(incoming);
                    String[] data = new String(incoming.getData(),0,incoming.getLength()).split("\\|");
                    int length = data.length;
                    String execute = data[0];
                    String username = data[1];

                    /*服务端分情况处理用户的请求*/
                    switch (execute){

                        /*处理用户的登录*/
                        case StaticValue.JUST_LOGIN :
                            String password = data[2];
                            System.out.println("服务器处理用户登录："+username);
                            new Thread(()->{
                                if (service.login(incoming,username,password) == 1){
                                    incoming.setData(StaticValue.LOGIN_ACCEPT.getBytes());
                                }else{
                                    incoming.setData(StaticValue.LOGIN_UNACC.getBytes());
                                }
                                this.respond(incoming);
                            }).start();
                            break;

                            /*处理用户的注册登录*/
                        case StaticValue.REGISTER_LOGIN:
                            String passwordd = data[2];
                            System.out.println("服务器处理用户注册并登录..."+username);
                            new Thread(()->{
                                if (service.register(incoming,username,passwordd) == 1){
                                    incoming.setData(StaticValue.LOGIN_ACCEPT.getBytes());
                                }else{
                                    incoming.setData(StaticValue.LOGIN_UNACC.getBytes());
                                }
                                this.respond(incoming);
                            }).start();
                            break;

                            /*转发用户的信息给另一个用户*/
                        case StaticValue.CHAT:
                            String msg = data[1];
                            String recieverName = data[2];
                            String sender = data[3];
                            System.out.println("服务器接收信息 : "+msg+" from: "+sender);
                            new Thread(()-> {
                                try {
                                    service.chat(msg,recieverName,sender);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                            break;
                        /**
                         * 获取用户的ip和端口号
                         */
                        case StaticValue.GET_ADDR_PORT:
                            new Thread(()->{
                                try {
                                    System.out.println("查询端口的用户名 ： " + username);
                                    String ipPortUserMsg = service.getUserList(username);
                                    if (ipPortUserMsg == null){
                                        System.out.println("Error:用户端口和好友查询出错！");
                                    }else{
                                        System.out.println(ipPortUserMsg);
                                        incoming.setData(ipPortUserMsg.getBytes());
                                        this.respond(incoming);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }).start();
                            break;

                        case StaticValue.SEND_TO_ALL_USERS:
                            String sendAllMsg = data[1];
                            String senderName = data[2];
                            System.out.println(data);
                            new Thread(()-> {
                                try {
                                    service.sendAll(sendAllMsg,senderName,data);
                                } catch (SocketException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                            break;

                        case StaticValue.MAKE_FRIENDS:
                            String pos_fri = data[1];
                            String nef_fri = data[2];
                            new Thread(()->{
                                System.out.println(pos_fri+ " -->" + nef_fri);
                                String retMsg = StaticValue.MAKE_FRIENDS + "|" + service.makeFriend(pos_fri,nef_fri);
                                System.out.println("服务端发给客户端：" + retMsg);
                                try {
                                    UserPort userPort = service.getSingleUserPort(pos_fri);
                                    InetAddress ip = InetAddress.getByName(userPort.getAddress());
                                    incoming.setData(retMsg.getBytes());
                                    incoming.setAddress(ip);
                                    incoming.setPort(userPort.getPort());
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }
                                this.respond(incoming);
                            }).start();
                            break;
                        case StaticValue.GROUP_CHAT:
                            new Thread(()-> service.sengGroupMsg(data)).start();
                            break;

                        case StaticValue.MYGROUP:
                            String userName = data[1];
                            System.out.println("查看表用户："+userName);
                            new Thread(()->{
                                UserPort userPort = service.getSingleUserPort(userName);
                                String ip = userPort.getAddress();
                                int port = userPort.getPort();
                                try {
                                    incoming.setPort(port);
                                    incoming.setAddress(InetAddress.getByName(ip));
                                    String retMESG = service.getGroupName(userName);
                                    System.out.println(retMESG);
                                    System.out.println("服务端发到 ip: " + ip +" port :" + port);
                                    incoming.setData(retMESG.getBytes());
                                    this.respond(incoming);
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }

                            }).start();
                            break;

                        case StaticValue.CREATE_GROUP:
                            new Thread(()-> service.addGroup(data)).start();
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public abstract void respond(DatagramPacket request);
}
