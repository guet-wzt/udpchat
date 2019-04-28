package com.network.udpchat.server.service;

import com.network.udpchat.common.dao.FriendsMapper;
import com.network.udpchat.common.dao.UserGroupMapper;
import com.network.udpchat.common.dao.UserMapper;
import com.network.udpchat.common.dao.UserPortMapper;
import com.network.udpchat.common.domain.*;
import com.network.udpchat.common.factory.BaseSessionFactory;
import com.network.udpchat.common.StaticValue;
import org.apache.ibatis.session.SqlSession;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServiceImp implements ChatService {
    //访问数据库的工厂
    private BaseSessionFactory baseSessionFactory;
    /**
     * 每个用户对应一个Ip 和 port
     */
    public static Map<String,IpPort> us_port;

    public ChatServiceImp() throws IOException {
        baseSessionFactory = new BaseSessionFactory();
        us_port = new HashMap<>();
        try (SqlSession session = baseSessionFactory.getSqlSession()){
            UserPortMapper userPortMapper = session.getMapper(UserPortMapper.class);
            List<UserPort> userPorts = userPortMapper.selectAll();
            if (userPorts.isEmpty()){
                System.out.println("无用户存在");
            }else {
                for (UserPort userPort : userPorts){
                    us_port.put(userPort.getUsername(),new IpPort(userPort.getAddress(),userPort.getPort()));
                }
            }
        }
    }

    /**
     * 通过datagramPacket和username获取 发送端 的端口信息
     * @param datagramPacket
     * @param username
     * @return
     */
    public UserPort getUserInf(DatagramPacket datagramPacket,String username){
        InetSocketAddress userSocket  = (InetSocketAddress) datagramPacket.getSocketAddress();
        InetAddress userIP = userSocket.getAddress();
        int port =  userSocket.getPort();
        String address =  userIP.getHostAddress();

        UserPort userPort = new UserPort();
        userPort.setUsername(username);
        userPort.setAddress(address);
        userPort.setPort(port);
        return userPort;
    }

    public User getUser(String username, String password){
        return new User(username,password);
    }

    /**
     * 用户登录更新端口表
     * 0 登录失败，1 成功
     * @param datagramPacket
     * @param username
     */
    @Override
    public int login(DatagramPacket datagramPacket,String username,String password) {
        try(SqlSession session = baseSessionFactory.getSqlSession()){

            UserMapper userMapper = session.getMapper(UserMapper.class);
            User user = userMapper.selectByPrimaryKey(username);
            if (user == null){
                return 0;
            }else{
                if (!password.equals(user.getPassword())){
                    return 0;
                }else{
                    /**
                     * 用户名和密码均正确的情况下，更新用户端口表
                     */
                    UserPortMapper userPortMapper = session.getMapper(UserPortMapper.class);

                    String beforeIP = userPortMapper.selectByPrimaryKey(username).getAddress();
                    int beforePort = userPortMapper.selectByPrimaryKey(username).getPort();
                    System.out.println("begore login ip :" + beforeIP + "  port :" + beforePort);

                    UserPort userPort = getUserInf(datagramPacket,username);
                    userPortMapper.updateByPrimaryKey(userPort);
                    session.commit();

                    String afterIP = userPortMapper.selectByPrimaryKey(username).getAddress();
                    int afterPort = userPortMapper.selectByPrimaryKey(username).getPort();
                    System.out.println("after login ip :" + afterIP + "  port :" + afterPort);
                    System.out.println(username + " 登录ip:" + userPort.getAddress());

                    us_port.get(username).setIp(afterIP);
                    us_port.get(username).setPort(afterPort);
                    System.out.println("登录后IP:" + us_port.get(username).getIp() + " port:" + us_port.get(username).getPort());
                    System.out.println("insert into user_port is ok!");

                    return 1;
                }
            }
        }

    }

    /**
     * 用户注册时，插入用户表和端口表关于用户的信息
     * 0 注册登录失败，1成功
     * @param datagramPacket
     * @param username
     * @param password
     */
    @Override
    public int register(DatagramPacket datagramPacket, String username,String password) {
        try (SqlSession session = baseSessionFactory.getSqlSession()){
            UserMapper userMapper = session.getMapper(UserMapper.class);


            User temp = userMapper.selectByPrimaryKey(username);

            if (temp == null){
                /**
                 * 用户id不存在，可以插入
                 */
                try {
                    UserPortMapper userPortMapper = session.getMapper(UserPortMapper.class);

                    UserPort userPort = getUserInf(datagramPacket,username);
                    User user = getUser(username,password);

                    userPortMapper.insert(userPort);
                    System.out.println(username + " 登录ip:" + userPort.getAddress());
                    session.commit();
                    userMapper.insert(user);
                    session.commit();
                    us_port.put(username,new IpPort(userPort.getAddress(),userPort.getPort()));

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    return 1;
                }
            }else{
                return 0;
            }
        }
    }

    @Override
    public void loginOut() {

    }

    /**
     * 用户聊天时服务器转发消息给另一个用户
     * @param message
     * @param recieverName
     * @param sender
     */
    @Override
    public void chat(String message, String recieverName,String sender) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        String msg = StaticValue.SINGLE_CHAT + "|" + message + "|" + sender;
        byte[] data = msg.getBytes();

        UserPort userPort = getSingleUserPort(recieverName);
        if (userPort == null){
            System.out.println(recieverName + " 不存在 ！");
        }
        String address = userPort.getAddress();
        int port = userPort.getPort();

        InetAddress inetAddress = InetAddress.getByName(address);
        DatagramPacket datagramPacket = new DatagramPacket(data,data.length,inetAddress,port);
        socket.send(datagramPacket);
        System.out.println("send to IP :" + address + "  port :" + port);
        System.out.println("服务器转发消息：" +message+" from :"+ sender);
        Thread.yield();
    }

    /**
     * 获取用户端口表中所有的用户的信息
     * @return
     */
    @Override
    public List<UserPort> getUsersList() {
        try (SqlSession session = baseSessionFactory.getSqlSession()){
            UserPortMapper userPortMapper = session.getMapper(UserPortMapper.class);
            List<UserPort> userPortList = userPortMapper.selectAll();
            return userPortList;
        }
    }

    @Override
    public UserPort getSingleUserPort(String username) {
        try (SqlSession session = baseSessionFactory.getSqlSession()){
            UserPortMapper userPortMapper = session.getMapper(UserPortMapper.class);
            return userPortMapper.selectByPrimaryKey(username);
        }
    }

    @Override
    public String getUserList(String username) {
        UserPort up = getSingleUserPort(username);
        String ip = up.getAddress();
        int port = up.getPort();
        String returnMsg = null;
        if (up == null){
            System.out.println(username + " : 用户不存在 !");
            return returnMsg;
        }
        returnMsg = StaticValue.GET_ADDR_PORT + "|" + ip + "|" + port;
        try (SqlSession session = baseSessionFactory.getSqlSession()){
            FriendsMapper friendsMapper = session.getMapper(FriendsMapper.class);
            List<Friends> friendsList = friendsMapper.selectFriendsList(username);
            if (friendsList.isEmpty()){
                System.out.println(username + " :无好友 !");
                return returnMsg;
            }
            for (Friends friend : friendsList){
                if (friend.getPosFriend().equals(username)){
                    returnMsg = returnMsg + "|" + friend.getNegFriend();
                }else {
                    returnMsg = returnMsg + "|" + friend.getPosFriend();
                }
            }
        }
        return returnMsg;
    }

    @Override
    public void sendAll(String message, String sender,String[] userList) throws SocketException {
        List<UserPort> userPortList = getUsersList();
        DatagramSocket socket = new DatagramSocket();
        String msg = StaticValue.SINGLE_CHAT + "|" + message + "|" + sender;
        byte[] data = msg.getBytes();
        System.out.println("............................");
        try {
            if (userPortList.isEmpty()){
                System.out.println(sender + " : 用户不存在 !或无好友");
            }else {
                for (int i = 3;i < userList.length;i++){
                    System.out.println("群聊好友：" + userList[i]);
                    IpPort ipPort = us_port.get(userList[i]);
                    System.out.println(ipPort.getIp()+ ":" + ipPort.getPort());
                    InetAddress ip = InetAddress.getByName(ipPort.getIp());
                    int port = ipPort.getPort();
                    DatagramPacket datagramPacket = new DatagramPacket(data,data.length,ip,port);
                    socket.send(datagramPacket);
                    Thread.yield();
                }
                socket.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加好友
     * @param neg_friend
     * @return
     */
    @Override
    public String makeFriend(String pos_friend,String neg_friend) {
        try (SqlSession session = baseSessionFactory.getSqlSession()){
            UserPortMapper userPortMapper =session.getMapper(UserPortMapper.class);
            UserPort userPort = userPortMapper.selectByPrimaryKey(neg_friend);
            if (userPort == null){
                return StaticValue.LOGIN_UNACC;
            }
            FriendsMapper friendsMapper = session.getMapper(FriendsMapper.class);
            Friends friends = new Friends();
            friends.setPosFriend(pos_friend);
            friends.setNegFriend(neg_friend);
            int result = friendsMapper.insertSelective(friends);
            session.commit();
            if (result != 1){
                System.out.println("添加好友失败");
                return StaticValue.LOGIN_UNACC;
            }
            System.out.println("服务器插入好友表................");
            return neg_friend;
        }
    }

    @Override
    public void sengGroupMsg(String[] data) {
        String msg = data[1];
        String reciver = data[2];
        String  sender= data[3];
        String transMsg = StaticValue.GROUP_CHAT + "|" + msg +"|" + reciver + "|" + sender;
        try (SqlSession session = baseSessionFactory.getSqlSession()){
            UserGroupMapper userGroupMapper = session.getMapper(UserGroupMapper.class);
            List<UserGroup> userGroups = userGroupMapper.selectAll();
            DatagramSocket socket = new DatagramSocket();
            System.out.println("服务器转发消息：" + transMsg + " from :" + sender);
            byte[] da = transMsg.getBytes();
            for (UserGroup userGroup : userGroups){
                if (userGroup.getMemberName().contains(sender) && userGroup.getGroupName().equals(reciver)){
                    String[] memberList = userGroup.getMemberName().split("\\|");
                    System.out.println(userGroup.getMemberName());
                    for (String name : memberList){
                        if (name==null||name.equals("")||sender.equals(name))continue;
                        IpPort ipPort = us_port.get(name);
                        InetAddress ip = InetAddress.getByName(ipPort.getIp());
                        int port = ipPort.getPort();
                        DatagramPacket datagramPacket = new DatagramPacket(da,da.length,ip,port);
                        socket.send(datagramPacket);
                    }
                    socket.close();
                    break;
                }
                Thread.yield();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getGroupName(String username) {
        try (SqlSession session = baseSessionFactory.getSqlSession()){
            String restr;
            int count = 0;
            UserGroupMapper userGroupMapper = session.getMapper(UserGroupMapper.class);
            List<UserGroup> userGroups = userGroupMapper.selectAll();
            if (userGroups.isEmpty()){
                return StaticValue.MYGROUP + "|" + StaticValue.LOGIN_UNACC;
            }
            restr = StaticValue.MYGROUP + "|" + StaticValue.LOGIN_ACCEPT;
            for (UserGroup userGroup : userGroups){
                if (userGroup.getMemberName().contains(username)) {
                    restr = restr + "|" + userGroup.getGroupName();
                    count = 1;
                }
            }
            if (count == 1){
                return restr;
            }else {
                return StaticValue.MYGROUP + "|" + StaticValue.LOGIN_UNACC;
            }
        }
    }

    @Override
    public void addGroup(String[] data) {
        String username = data[1];
        String groupName = data[2];
        String memberList="";
        for (int i = 3;i < data.length;i++){
            if (data[i].equals("") || data[i] == null)continue;
            memberList = memberList + "|" + data[i];
        }
        UserGroup userGroup = new UserGroup();
        userGroup.setUsername(username);
        userGroup.setGroupName(groupName);
        userGroup.setMemberName(memberList);
        try (SqlSession session = baseSessionFactory.getSqlSession()){
            UserGroupMapper userGroupMapper = session.getMapper(UserGroupMapper.class);
            int result = userGroupMapper.insertSelective(userGroup);
            if (result != 1){
                System.out.println(groupName +" 创建失败");
                return;
            }
            System.out.println("服务器插入群聊表................");
            session.commit();
        }
    }
}
