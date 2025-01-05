package com.minigithub.controllers.User;

import java.net.URL;
import java.util.ResourceBundle;

import com.minigithub.database.DatabaseDriver;
import com.minigithub.model.Model;
import com.minigithub.model.UserModel;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileController implements Initializable{
    public Label name_lbl;
    public Label description_lbl;
    public Label jobtitle_lbl;
    public Label email_lbl;
    public Label error_lbl;

    public TextField username_fld;
    public TextField description_fld;
    public TextField jobtitle_fld;

    public Button updateprofile_btn;

    private UserModel user;
    // private ObjectProperty<UserModel> currentuser = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user = Model.getInstance().getUser();
        Model.getInstance().getUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser == null) {
                // User logged out: Reset the UI
                user = oldUser;
            } else {
                // User logged in: Load the new user's data
                user = newUser;
            }
        });
        if (user != null) {
            setData(user);
            updateprofile_btn.setOnAction(event -> onUpdateBtn(user,username_fld.getText(), description_fld.getText(),jobtitle_fld.getText()));
        }
    }

    private void setData(UserModel user){
        name_lbl.setText(user.getUsername());
        description_lbl.setText(user.getDescription());
        jobtitle_lbl.setText(user.getJobTitle());
        email_lbl.setText(user.getEmail());
    }
    
    private void onUpdateBtn(UserModel user, String newName, String newDes, String newTitle){
        if (newDes.equals("")) {
            newDes = user.getDescription();
        }
        if (newTitle.equals("")) {
            newTitle = user.getJobTitle();
        }
        if(newName.equals("")){
            newName = user.getUsername();
        }
        name_lbl.setText(newName);
        description_lbl.setText(newDes);
        jobtitle_lbl.setText(newTitle);
        error_lbl.setText("Updated Successfuly");
        clearCredentials();
        DatabaseDriver.updateUserData(user.getId(), newName,user.getPassword(), newDes, newTitle);
    }
    private void clearCredentials(){
        username_fld.setText(null);
        description_fld.setText(null);
        jobtitle_fld.setText(null);
    }
}
