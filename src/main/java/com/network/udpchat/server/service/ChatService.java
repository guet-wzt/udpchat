package com.network.udpchat.server.service;

import com.network.udpchat.common.domain.User;
import com.network.udpchat.common.domain.UserPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TransferQueue;

public interface ChatService {

    /**
     * when user login, update user's information
     * 用户登录前，查找用户是否存在（0 登录失败，1 成功 ）
     * @param datagramPacket
     * @param username
     */
    int login(DatagramPacket datagramPacket, String username,String password);

    /**
     * when user register,update the table
     * 用户注册前，查找用户是否存在（0 注册失败，1 成功）
     * @param datagramPacket
     * @param username
     * @param password
     */
    int register(DatagramPacket datagramPacket,String username,String password);

    /**
     * When user login out
     */
    void loginOut();


    /**
     * communicate with another one
     * translate the message
     * @param message
     * @param recieverName
     */
    void chat(String message, String recieverName, String sender) throws IOException;

    /**
     * get all users information
     * @return
     */
    List<UserPort> getUsersList();

    /**
     * 通过用户名获取单独的用户
     * @param username
     * @return
     */
    UserPort getSingleUserPort(String username);

    /**
     *
     * @param username
     * @return
     */
    String getUserList(String username);

    /**
     * 群聊
     * @param message
     * @param sender
     * @throws SocketException
     */
    void sendAll(String message, String sender, String[] userList) throws SocketException;

    /**
     * 提交好友
     * @param pos_friend
     * @param neg_friend
     * @return
     */
    String makeFriend(String pos_friend,String neg_friend);

    /**
     * 转发群聊消息给特定好友
     * @param data
     */
    void sengGroupMsg(String[] data);

    /**
     * 获取用户的群组
     * @param username
     * @return
     */
    String getGroupName(String username);

    /**
     * 创建新的群组
     * @param data
     */
    void addGroup(String[] data);

}
