package com.network.udpchat.common.controller;

import com.network.udpchat.common.StaticValue;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;

public class ChatController extends RepScene{

    @FXML
    public TextArea input;
    @FXML
    public VBox userListVBox;
    @FXML
    public Label showUsersLable;
    @FXML
    public Label recieverNameLabel;
    @FXML
    public ScrollPane msgDisplyPane;
    @FXML
    public Label logoutLabel;
    @FXML
    public Label sendAllLabel;


    private static HashMap<String,VBox> usersVBoxMap;
    private static HashMap<String,Label> userListLabelMap;
    private static DatagramSocket recieverDS;
    private static DatagramPacket recieverDP;
    private static String username;
    private static InetAddress inetAddress = null;
    private static int port;
    private static boolean flag = true;
    private static List<String> fri_name;
    //添加的好友
    private static String addFriList = "";


    private static String senderName = null;
    private static String message = null;
    private static boolean isGroupMsg = true;

    private static boolean  isGetGroup =true;

    private static boolean group = true;

    @FXML
    public Label addFriendLabel;
    @FXML
    public TextField addFriTextField;
    @FXML
    public ListView friendListView;
    @FXML
    public Label choiceFriLabel;
    @FXML
    public Label addGroupLabel;
    @FXML
    public TextField groupNameTField;
    @FXML
    public VBox groupVbox;
    @FXML
    public Label myGroupLabel;

    public ChatController() throws IOException {
        super();
        /**
         * 建立用户对应的界面hashmap
         */
        usersVBoxMap = new HashMap<>();

        userListLabelMap = new HashMap<>();
        fri_name = new ArrayList<>();

        /**
         * 用户接受信息的线程
         */
        new Thread(()->{
            try {
                recieverDS = getSocket();
                byte[] buffer = new byte[StaticValue.BUFFER_SIZE];
                recieverDP = new DatagramPacket(buffer,buffer.length);
                while (true) {
                    try {
                        recieverDS.receive(recieverDP);
                        String[] data = new String(recieverDP.getData(), 0, recieverDP.getLength()).split("\\|");
                        System.out.println(data[0]);
                        switch (data[0]){
                            case StaticValue.GET_ADDR_PORT:
                                String address = data[1];
                                inetAddress = InetAddress.getByName(address);
                                port = Integer.parseInt(data[2]);
                                System.out.println("客户端 监听端口：" + address + " port :" + port);
                                recieverDS = new DatagramSocket(port,inetAddress);
                                if (data.length > 3){
                                    userList(data);
                                }else {
                                    Platform.runLater(()->{
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                        alert.setContentText("提示：你目前还没添加好友...");
                                        alert.show();
                                    });
                                }
                                break;
                            case StaticValue.SEND_TO_ALL_USERS:
                                Platform.runLater(()->{
                                    message = data[1];
                                    senderName = data[2];
                                    Label ml =new Label(" " +senderName + " : " + message);
                                    ml.setAlignment(Pos.CENTER);
                                    ml.setPadding(new Insets(2,5,2,5));
                                    ml.setFont(new Font(17));
                                    ml.setBackground(new Background(new BackgroundFill(Color.rgb(255 ,218 ,185),null,null)));
                                    if (!recieverNameLabel.getText().equals("群发")){
                                        getLight("群发",0,0.78,0.55);
                                    }
                                    usersVBoxMap.get("群发").getChildren().add(ml);
                                });
                                break;
                            case StaticValue.MAKE_FRIENDS:
                                Platform.runLater(()->{
                                    String neg_fri = data[1];
                                    if (!StaticValue.LOGIN_UNACC.equals(neg_fri)){
                                        VBox newFri = new VBox();
                                        newFri.setFillWidth(true);
                                        newFri.setPrefHeight(2000);
                                        newFri.setSpacing(8);
                                        usersVBoxMap.put(neg_fri,newFri);

                                        fri_name.add(neg_fri);
                                        estaLabel(neg_fri);
                                    }else {
                                        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                                        warningAlert.setContentText("Waring:该用户不存在！");
                                        warningAlert.show();
                                    }
                                });
                                break;
                            case StaticValue.SINGLE_CHAT:
                                Platform.runLater(()->{
                                    int length = data.length;
                                    senderName = data[length-1];
                                    message = data[length-2];
                                    System.out.println("客户端接收消息：" + message + "from " + senderName + ":" + 1);
                                    Label msgLabel = new Label(" " +senderName + " : " + message);
                                    msgLabel.setAlignment(Pos.CENTER);
                                    msgLabel.setPadding(new Insets(2,5,2,5));
                                    msgLabel.setBackground(new Background(new BackgroundFill(Color.rgb(255,218 ,185),null,null)));
                                    msgLabel.setFont(new Font(17));
                                    try {
                                        if (usersVBoxMap.get(senderName) == null){//添加新好友
                                            addNewUser(msgLabel,senderName);
                                            getLight(senderName,0,0.78,0.55);
                                        }else {
                                            usersVBoxMap.get(senderName).getChildren().add(msgLabel);
                                            if (!senderName.equals(recieverNameLabel.getText())) {
                                                getLight(senderName,0,0.78,0.55);
                                            }
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                });
                                break;
                            case StaticValue.GROUP_CHAT:
                                Platform.runLater(()->{
                                    message = data[1];
                                    String reciverN = data[2];
                                    senderName = data[3];
                                    String userGroList = "";
                                    for (int i = 4;i < data.length;i++){
                                        userGroList = userGroList +"|" + data[i];
                                    }
                                    Label mll =new Label(" " +senderName + " : " + message);
                                    mll.setAlignment(Pos.CENTER);
                                    mll.setPadding(new Insets(2,5,2,5));
                                    mll.setFont(new Font(17));
                                    mll.setBackground(new Background(new BackgroundFill(Color.rgb(255 ,218 ,185),null,null)));

                                    /**
                                     * 有待处理是否是新建群聊......
                                     */
                                    if (userListLabelMap.get(reciverN) == null){
                                        isGroupMsg = true;
                                        isGetGroup = false;
                                        addNewUser(mll,reciverN);
                                        getLight(reciverN,0,0.78,0.55);
                                    }else {
                                        usersVBoxMap.get(reciverN).getChildren().add(mll);
                                        if (!reciverN.equals(recieverNameLabel.getText())) {
                                            getLight(reciverN,0,0.78,0.55);
                                        }
                                    }
                                });
                                break;
                            /**
                             * 获取用户的群
                             */
                            case StaticValue.MYGROUP:
                                Platform.runLater(()->{
                                    System.out.println(data[0]);
                                    if (StaticValue.LOGIN_ACCEPT.equals(data[1])){
                                        for (int i = 2;i < data.length;i++){
                                            Label groupNL =new Label(data[i]);
                                            isGroupMsg = false;
                                            isGetGroup = false;
                                            addNewUser(groupNL,data[i]);
                                        }
                                    }else {
                                        Alert inAl = new Alert(Alert.AlertType.INFORMATION);
                                        inAl.setContentText("提示：你还没加入任何群！");
                                        inAl.show();
                                    }
                                });
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    public void getLight(String un,double r,double g,double b){
        if (userListLabelMap.get(un)==null){
            System.out.println(un + " Label is null ");
        }else {
            System.out.println(un + " Label is not null");
            userListLabelMap.get(un).setBackground(new Background(new BackgroundFill(Color.color(r,g,b),null,null)));
        }
    }

    /**
     * 添加新好友
     * @param msgLabel
     */
    public void addNewUser(Label msgLabel,String sender){
        /**
         * 创建消息框，加入userVBOxMap
         */
        VBox userVBox = new VBox();
        userVBox.setFillWidth(true);
        userVBox.setPrefHeight(2000);
        userVBox.setSpacing(8);

        //isGroupMsg 开始为true
        if (isGroupMsg){
            userVBox.getChildren().add(msgLabel);
            isGroupMsg = false;
        }

        usersVBoxMap.put(sender,userVBox);
        /**
         * 加入好友列表
         */
        estaLabel(sender);
    }

    public void estaLabel(String name){

        Label userLabel = new Label(name);
        userLabel.setPrefWidth(183);
        userLabel.setPrefHeight(34);
        userLabel.setFont(Font.font(17));
        userLabel.setAlignment(Pos.CENTER);
        userLabel.setBackground(new Background(new BackgroundFill(Color.color(0.90,0.98,0.98),null,null)));

        System.out.println("isGetGroup = " + isGetGroup);
        if (!isGetGroup){
            System.out.println("Group: " + name);
            userLabel.setPrefWidth(142);
            groupVbox.getChildren().add(userLabel);
            groupVbox.setVisible(true);
            isGetGroup = true;
        }else {
            System.out.println("User: " + name);
            userListVBox.getChildren().add(userLabel);
            userListVBox.setVisible(true);
        }

        /**
         * 添加到用户对应的Label Map
         */
        userListLabelMap.put(name,userLabel);

        System.out.println(name + " : label ok");

        /**
         * 监听器，处理点击用户之后的界面切换
         */
        userLabel.setOnMouseClicked(event -> {
            String reciever = userLabel.getText().trim();
            recieverNameLabel.setText(reciever);
            msgDisplyPane.setContent(usersVBoxMap.get(reciever));
            msgDisplyPane.setVisible(true);
            getLight(reciever,0.90,0.98,0.98);
            System.out.println(reciever + ":" +usersVBoxMap.get(reciever).getChildren().size());
        });

    }

    public void userList(String[] user){
        Platform.runLater(()->{
            try {
                System.out.println("有好友 ："+ (user.length-3) + "人");
                for (int i = 3;i < user.length;i++){
                    String userName = user[i];
                    fri_name.add(userName);
                    System.out.println(userName);
                    /**
                     *创建每个好友对应的消息框VBox
                     */
                    VBox userVbox = new VBox();
                    userVbox.setPrefHeight(2000);
                    userVbox.setFillWidth(true);
                    userVbox.setSpacing(8);
                    usersVBoxMap.put(userName,userVbox);

                    /**
                     * 建立用户列表
                     */
                    if (username == null || userName.equals(username))continue;
                    estaLabel(userName);
                }
                //添加群聊的消息VBOx
                VBox vsVBox = new VBox();
                vsVBox.setPrefHeight(2000);
                vsVBox.setFillWidth(true);
                vsVBox.setSpacing(8);
                usersVBoxMap.put("群发",vsVBox);

                userListLabelMap.put("群发",sendAllLabel);

            }catch (Exception e){
                e.printStackTrace();
            }
        });

    }

    /**
     * 将数据信息发给服务器
     * @param actionEvent
     */
    @FXML
    public void toSend(ActionEvent actionEvent) throws IOException {
        String inputMsg = input.getText().trim();
        if(inputMsg==null || inputMsg.equals("")){
            Alert warningAlert = new Alert(Alert.AlertType.WARNING);
            warningAlert.setContentText("Wrong:输入框不能为空！");
            warningAlert.showAndWait();
            return;
        }
        String msg;
        String recieverName = recieverNameLabel.getText().trim();
        if (recieverName.equals("USER")){
            Alert warningAlert = new Alert(Alert.AlertType.WARNING,"Warning: 请选择聊天对象... ");
            warningAlert.showAndWait();
            return;
        }else if (recieverName.equals("群发")){
            msg = StaticValue.SEND_TO_ALL_USERS + "|" + inputMsg + "|" + username;
            if (fri_name.isEmpty()){
                Alert wa = new Alert(Alert.AlertType.WARNING);
                wa.setContentText("Warning: 请先添加好友！");
                wa.show();
                return;
            }
            for (String user : fri_name){
                msg = msg + "|" + user;
            }
        }else if (groupVbox.getChildren().contains(userListLabelMap.get(recieverName))){
            msg = StaticValue.GROUP_CHAT + "|" + inputMsg + "|"+  recieverName + "|" + username;
        }else {
            msg = StaticValue.CHAT + "|" + inputMsg + "|" + recieverName + "|" + username;
        }
        System.out.println(msg);
        sendToServer(msg);
        Platform.runLater(()->{
            String messg = " I : " + inputMsg;
            Label myLabel = new Label(messg);
            myLabel.setBackground(new Background(new BackgroundFill(Color.rgb(60,179, 113),null,null)));
            myLabel.setFont(new Font(17));
            myLabel.setTextFill(Color.color(1,1,1));
            myLabel.setPadding(new Insets(2,5,2,5));
            myLabel.setAlignment(Pos.CENTER);
            usersVBoxMap.get(recieverName).getChildren().add(myLabel);
            input.clear();
            for(Map.Entry<String, VBox> entry: usersVBoxMap.entrySet())
            {
                System.out.println("Key: "+ entry.getKey()+ " Value: "+entry.getValue().getChildren().size());
            }
        });
    }

    @FXML
    public void toCancel(ActionEvent actionEvent) {
        input.clear();
    }

    /**
     * 显示用户列表
     * @param mouseEvent
     * @throws IOException
     */
    @FXML
    public void showUsers(MouseEvent mouseEvent) throws IOException {
        if (flag){
            Stage chatStage = (Stage) ((Node)mouseEvent.getSource()).getScene().getWindow();
            String[] header = chatStage.getTitle().split(",");
            username = header[1].trim();

            String msg = StaticValue.GET_ADDR_PORT + "|" + username;
            sendToServer(msg);
            flag = false;
        }
    }

    @FXML
    public void logout(MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root);
        Stage loginStage = (Stage) logoutLabel.getScene().getWindow();
        loginStage.hide();
        flag = true;
        group = true;
        loginStage.setScene(scene);
        loginStage.setTitle("欢迎登录聊天系统");
        loginStage.show();
    }

    @FXML
    public void sendAll(MouseEvent mouseEvent) throws IOException {
        String reciever = "群发";
        recieverNameLabel.setText(reciever);

        msgDisplyPane.setContent(usersVBoxMap.get(reciever));
        msgDisplyPane.setVisible(true);
        getLight(reciever,0.90,0.90,0.98);
    }

    @FXML
    public void addFriend(MouseEvent mouseEvent) throws IOException {
        String negFri = addFriTextField.getText().trim();
        if (negFri == null || negFri.equals("")){
            Alert warningAL = new Alert(Alert.AlertType.WARNING);
            warningAL.setContentText("Warning:用户名不能为空");
            warningAL.show();
            return;
        }
        if (negFri.equals(username)||userListLabelMap.get(negFri)!=null){
            Alert w = new Alert(Alert.AlertType.WARNING);
            w.setContentText("Warning:该用户名无效！");
            w.show();
            addFriTextField.clear();
            return;
        }
        String sendFriMsg = StaticValue.MAKE_FRIENDS + "|" + username + "|" + negFri;
        sendToServer(sendFriMsg);
        addFriTextField.clear();
    }

    @FXML
    public void choiceFriToGroup(MouseEvent mouseEvent) {
        if (friendListView.isVisible()){
            friendListView.setVisible(false);
        }else {
            addFriList = "";
            friendListView.setVisible(true);
            ObservableList<String> observableList = FXCollections.observableArrayList(fri_name);
            friendListView.setItems(observableList);
            friendListView.setOnMouseClicked((event)->{
                addFriList = addFriList + "|" + friendListView.getSelectionModel().getSelectedItem().toString();
                friendListView.getItems().remove(friendListView.getSelectionModel().getSelectedItem());
                System.out.println(addFriList);
                if (friendListView.getItems().isEmpty()){
                    friendListView.setVisible(false);
                }
            });
        }
    }

    @FXML
    public void addGroup(MouseEvent mouseEvent) throws IOException {
        String groupName = groupNameTField.getText().trim();
        if (addFriList == null || groupName==null || groupName.equals("")||userListLabelMap.get(groupName) != null){
            Alert wa = new Alert(Alert.AlertType.WARNING);
            wa.setContentText("Warning: 群名称设置有误或未选择好友 ！");
            wa.show();
            addFriList = "";
            return;
        }
        /**
         * 添加到群名称 与 群好友对应的Map
         */
        addFriList = addFriList + "|" + username;

        if (addFriList.equals("|"+username)){
            Alert wa = new Alert(Alert.AlertType.WARNING);
            wa.setContentText("Warning:请选择好友...");
            wa.show();
            addFriList = "";
            return;
        }

        String message = StaticValue.CREATE_GROUP + "|" + username  +"|"+ groupName  + addFriList;
        System.out.println("创建群信息：" + message);
        sendToServer(message);

        System.out.println(groupName +":" + addFriList);

        Label groupNL =new Label(groupName);
        isGroupMsg = false;
        isGetGroup = false;

        addNewUser(groupNL,groupName);


        /**
         * 设置添加好友的listview不可见
         */
        addFriList = "";
        friendListView.setVisible(false);
        groupNameTField.clear();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(groupName + " 创建成功");
        alert.show();

    }

    @FXML
    public void searchMyGroup(MouseEvent mouseEvent) throws IOException {
        if (group){
            String msg = StaticValue.MYGROUP + "|" + username;
            sendToServer(msg);
            group = false;
        }
    }
}
