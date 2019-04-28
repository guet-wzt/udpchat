package com.network.udpchat.common.controller;

import com.network.udpchat.common.StaticValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.SocketException;

public class LoginController  extends RepScene{

    @FXML
    public TextField userName;
    @FXML
    public PasswordField passWord;
    @FXML
    public Button login;
    @FXML
    public Button register;
    @FXML
    public Label userLabel;
    @FXML
    public Label password;


    public LoginController() throws SocketException {
        super();
    }



    @FXML
    public void toLogin(ActionEvent actionEvent) throws IOException {
        String user_name = userName.getText().trim();
        String pass_word = passWord.getText().trim();
        if (user_name.equals("")||user_name==null||pass_word.equals("")||pass_word==null){
            Alert er = errorAlert();
            er.setContentText("Error：用户名或密码为空！");
            er.showAndWait();
        }else{
            /**
             * 接收服务器的确定消息的线程
             */
            String url = "/fxml/chat.fxml";
            recieverFromServerMsg(url,actionEvent,getClass(),user_name);
            String msg = StaticValue.JUST_LOGIN+ "|" + user_name + "|" + pass_word;
            sendToServer(msg);
        }
    }

    @FXML
    public void toRegister(ActionEvent actionEvent) throws IOException {
        String url = "/fxml/register.fxml";
        changeScene(url,actionEvent,getClass(),"注册...");
    }
}
