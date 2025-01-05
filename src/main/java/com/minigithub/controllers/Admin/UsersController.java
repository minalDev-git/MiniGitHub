package com.minigithub.controllers.Admin;

import java.net.URL;
import java.util.ResourceBundle;

import com.minigithub.Views.UsersCellFactory;
import com.minigithub.database.DatabaseDriver;
import com.minigithub.model.UserModel;

import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

public class UsersController implements Initializable{


    public ListView<UserModel> users_listview;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // users_listview.setItems(Model.getInstance().getUsers());
        users_listview.setCellFactory(user -> new UsersCellFactory());
        users_listview.getItems().addAll(DatabaseDriver.getAllUsers());
    }
}
