package com.minigithub.controllers.Admin;

import java.net.URL;
import java.util.ResourceBundle;

import com.minigithub.Views.AdminMenuOptions;
import com.minigithub.model.Model;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public class AdminMenuController implements Initializable{

    public Button create_usr_btn;
    public Button users_btn;
    public Button logout_btn;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addListeners();
    }

    private void addListeners(){
        create_usr_btn.setOnAction(event -> onCreateUser());
        users_btn.setOnAction(event -> onUsers());
        logout_btn.setOnAction(event -> onLogout());
    }
    private void onCreateUser(){
        Model.getInstance().getViewScene().getAdminMenuSelectedItem().set(AdminMenuOptions.CREATE_USER);
    }
    private void onUsers(){
        Model.getInstance().getViewScene().getAdminMenuSelectedItem().set(AdminMenuOptions.USERS);
    }
    private void onLogout(){
        Model.getInstance().getViewScene().getAdminMenuSelectedItem().set(AdminMenuOptions.LOGOUT);
    }
}
