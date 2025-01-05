package com.minigithub.controllers.User;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import org.bson.types.ObjectId;

import com.minigithub.Views.UserMenuOptions;
import com.minigithub.database.DatabaseDriver;
import com.minigithub.database.GridFSUtility;
import com.minigithub.model.Model;
import com.minigithub.model.RepositoryModel;
import com.minigithub.model.UserModel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class RepositoryCellController implements Initializable{

    public Label date_lbl;
    public Label name_lbl;
    public Label msg_lbl;

    public Button delete_btn;

    private ObjectProperty<RepositoryModel> repository = new SimpleObjectProperty<>();
    private UserModel user;
    private final Consumer<RepositoryModel> onDeleteCallback;

    //constructor of this controller class takes the object of model class and initializes
    //the repository attribute here. We now have access to the data from the RepositoryModel obj.
    public RepositoryCellController(RepositoryModel repositoryModel, Consumer<RepositoryModel> onDeleteCallback){
        this.repository.set(repositoryModel);
        this.onDeleteCallback = onDeleteCallback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user = Model.getInstance().getUser();
        Model.getInstance().getUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser == null) {
                // User logged out: Reset the UI
                user = null;
            } else {
                // User logged in: Load the new user's data
                user = newUser;
            }
        });
        if (user != null) {
            setData();
            delete_btn.setOnAction(event -> onDeleteRepo());
            name_lbl.setOnMouseClicked(event -> onRepoClicked());
        }
    }
    
    private void onDeleteRepo(){
        if (Model.getInstance().getAuthFlag()) {
            onDeleteCallback.accept(repository.get());
            ArrayList<ObjectId> fileIds = repository.get().getFiles();
            for (ObjectId fileId : fileIds) {
                GridFSUtility.deleteFile(fileId);
                DatabaseDriver.deleteFileFromRepository(user.getId(), repository.get().getId(), fileId);
            }
            DatabaseDriver.deleteRepository(user.getId(), repository.get().getId());
        }
        else{
            Model.getInstance().getViewScene().showAuthenticationModal();
        }
    }
    private void setData(){
        date_lbl.setText(repository.get().getDate().toString().substring(0,10));
        name_lbl.setText(repository.get().getRepositoryName());
        msg_lbl.setText(repository.get().getDescription());
    }
    private void onRepoClicked(){
        Model.getInstance().getRepository().set(repository.get());
        Model.getInstance().setFiles(DatabaseDriver.getAllFilesFromRepo(user.getId(), repository.get().getId()));
        Model.getInstance().getViewScene().getUserMenuSelectedItem().set(UserMenuOptions.FILES);
    }
}
