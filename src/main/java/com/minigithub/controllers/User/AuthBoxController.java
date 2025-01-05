package com.minigithub.controllers.User;

import java.net.URL;
import java.util.ResourceBundle;

import com.minigithub.model.Model;
import com.minigithub.model.UserModel;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AuthBoxController implements Initializable{
    
    public Button cancel_btn;
    public Button unlock_btn;
    public Label error_lbl;
    public TextField authpasskey_fld;

    private UserModel user;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user = Model.getInstance().getUser();
        cancel_btn.setOnAction(event -> closeModal());
        unlock_btn.setOnAction(event -> validatePasskey());
    }
    
    private void closeModal() {
        // Close the modal window
        Stage stage = (Stage) cancel_btn.getScene().getWindow();
        stage.close();
    }

    private void validatePasskey(){
        String passkey = authpasskey_fld.getText();
        if (passkey.equals(user.getPasskey())) {
            Model.getInstance().setAuthflag(true);
            closeModal();
        }
        else{
            error_lbl.setText("Invalid Passkey");
        }
    }
}
