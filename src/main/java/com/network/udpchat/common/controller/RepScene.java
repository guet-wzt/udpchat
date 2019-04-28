package com.network.udpchat.common.controller;

import com.network.udpchat.common.factory.BaseSessionFactory;
import com.network.udpchat.common.StaticValue;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RepScene extends SendDataPackage{

    protected BaseSessionFactory baseSessionFactory;

    public RepScene() throws SocketException {
        super();
    }

    public Alert errorAlert(){
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Error Dialog");
        error.setHeaderText("Look, an Error Dialog");
        return error;
    }

    /**
     * 切换界面
     * @param url
     * @param actionEvent
     * @param c
     * @param user
     * @throws IOException
     */
    public void changeScene(String url, ActionEvent actionEvent,Class c,String user) throws IOException {
        System.out.println("path :" + c.getResource(url));
        Parent root = FXMLLoader.load(c.getResource(url));
        Scene scene  = new Scene(root);
        Stage registerStage = (Stage)(((Node)actionEvent.getSource())).getScene().getWindow();
        registerStage.hide();
        registerStage.setScene(scene);
        registerStage.setTitle("欢迎," + user);
        registerStage.show();
    }

    /**
     * 接收服务器的确定消息的线程
     */
    public void recieverFromServerMsg(String url, ActionEvent actionEvent,Class c,String usename){
        Platform.runLater(()-> {
            DatagramSocket recieverDS = getSocket();
            byte[] buffer = new byte[StaticValue.BUFFER_SIZE];
            DatagramPacket recieverDP = new DatagramPacket(buffer, buffer.length);
            while (true) {
                try {
                    recieverDS.receive(recieverDP);
                    String[] data = new String(recieverDP.getData(), 0, recieverDP.getLength()).split("\\|");
                    if (StaticValue.LOGIN_ACCEPT.equals(data[0])) {
                        System.out.println(usename + " :登录....");
                        try {
                            changeScene(url, actionEvent, c, usename);
                            recieverDS.close();
//                            recieverDS.disconnect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {
                            break;
                        }
                    } else {
                        Alert erNamePw = errorAlert();
                        erNamePw.setContentText("Error：用户名或密码错误！");
                        erNamePw.showAndWait();
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
