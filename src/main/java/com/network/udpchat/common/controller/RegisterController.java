package com.network.udpchat.common.controller;

import com.network.udpchat.common.StaticValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.SocketException;


public class RegisterController extends RepScene{
    @FXML
    public TextField userName;
    @FXML
    public PasswordField passWord;
    @FXML
    public PasswordField conPassWord;

    public RegisterController() throws SocketException {
        super();
    }

    @FXML
    public void toLogin(ActionEvent actionEvent) throws IOException {
        String usename = userName.getText().trim();
        String password = passWord.getText();
        String conpassword = conPassWord.getText();
        if (usename.equals("")||usename==null||password.equals("")||password==null||conpassword.equals("")||conpassword==null){
            Alert er =  errorAlert();
            er.setContentText("Error：用户名或密码为空！");
            er.showAndWait();
        }else if (!password.equals(conpassword)){
            Alert er1 = errorAlert();
            er1.setContentText("Error：请重新确认密码！");
            er1.showAndWait();
        }else{
            /**
             * 接收服务器的确定消息的线程
             */
            String url = "/fxml/chat.fxml";
            recieverFromServerMsg(url,actionEvent,getClass(),usename);

            String msg = StaticValue.REGISTER_LOGIN+ "|" + usename + "|" + password;
            sendToServer(msg);
        }
    }

    @FXML
    public void toGoBack(ActionEvent actionEvent) throws IOException {
        String url = "/fxml/login.fxml";
        changeScene(url,actionEvent,RegisterController.class,"登录...");
    }
}
