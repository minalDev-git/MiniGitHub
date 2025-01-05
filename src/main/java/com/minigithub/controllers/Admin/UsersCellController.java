package com.minigithub.controllers.Admin;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import org.bson.types.ObjectId;

import com.minigithub.database.DatabaseDriver;
import com.minigithub.database.GridFSUtility;
import com.minigithub.model.RepositoryModel;
import com.minigithub.model.UserModel;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class UsersCellController implements Initializable{

    public Label usercell_lbl;
    public Label passkeycell_lbl;
    public Label pswdcell_lbl;
    public Label date_lbl;

    // public FontAwesomeIconView delete_icon;
    public Button delete_btn;

    private final UserModel user;
    private final Consumer<UserModel> onDeleteCallback;

    public UsersCellController(UserModel user, Consumer<UserModel> onDeleteCallback){
        this.user = user;
        this.onDeleteCallback = onDeleteCallback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setData();
        delete_btn.setOnAction(event -> deleteUser());
    }

    private void setData(){
        usercell_lbl.setText(user.getUsername());
        passkeycell_lbl.setText(user.getPasskey());
        pswdcell_lbl.setText(user.getPassword());
        date_lbl.setText(user.getDateCreated().toString().substring(0, 10));
    }
    private void deleteUser(){
        onDeleteCallback.accept(user); // Notify controller
        usercell_lbl.setText(null);
        passkeycell_lbl.setText(null);
        date_lbl.setText(null);
        delete_btn.setVisible(false);
        //Deleting repos and files associated with the user
        ArrayList<ObjectId> files = new ArrayList<>();
        ArrayList<RepositoryModel> userRepos = new ArrayList<>();
        userRepos = user.getRepositories();
        for (RepositoryModel repo : userRepos) {
            files = repo.getFiles();
            for (ObjectId fileId : files) {
                GridFSUtility.deleteFile(fileId);
            }
            DatabaseDriver.deleteRepository(user.getId(), repo.getId());
        }
        DatabaseDriver.deleteUser(user.getId());
    }
}