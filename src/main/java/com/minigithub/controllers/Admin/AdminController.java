package com.minigithub.controllers.Admin;

import java.net.URL;
import java.util.ResourceBundle;

import com.minigithub.Views.AccountType;
import com.minigithub.model.Model;

import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AdminController implements Initializable{

    public BorderPane admin_parent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Model.getInstance().getViewScene().getAdminMenuSelectedItem().addListener((Observable,oldVal,newVal)->{
            switch (newVal) {
                case USERS -> admin_parent.setCenter(Model.getInstance().getViewScene().getUsersView());
                case LOGOUT -> {
                    Model.getInstance().setAdminLoginFlag(false);
                    Model.getInstance().setLoginAccountType(AccountType.USER);
                    Model.getInstance().getViewScene().showLoginWindow();
                    Model.getInstance().getViewScene().closeStage((Stage)admin_parent.getScene().getWindow());
                }
                default -> admin_parent.setCenter(Model.getInstance().getViewScene().getCreateUserView());
            }
        }); 
    }
    
}
