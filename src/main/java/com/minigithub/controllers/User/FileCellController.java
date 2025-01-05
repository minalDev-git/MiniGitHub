package com.minigithub.controllers.User;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import com.minigithub.Views.UserMenuOptions;
import com.minigithub.database.DatabaseDriver;
import com.minigithub.database.GridFSUtility;
import com.minigithub.model.FileModel;
import com.minigithub.model.Model;
import com.minigithub.model.RepositoryModel;
import com.minigithub.model.UserModel;

import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class FileCellController implements Initializable{

    private FileModel file;
    private ObjectProperty<RepositoryModel> repository;
    private UserModel user;
    private final Consumer<FileModel> onDeleteCallback;

    public Button delete_btn;
    public Label date_lbl;
    public Label name_lbl;
    public Label msg_lbl;

    public FileCellController(FileModel fileModel, Consumer<FileModel> onDeleteCallback){
        this.file = fileModel;
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
            repository = Model.getInstance().getRepository();
            setData();
            name_lbl.setOnMouseClicked(event -> onFileClicked());
            delete_btn.setOnAction(event -> deleteFile());
        }
    }

    private void setData(){
        date_lbl.setText(file.getDate().toString().substring(0,10));
        name_lbl.setText(file.getFilename());
        msg_lbl.setText("File from " + repository.get().getRepositoryName());
    }
    private void deleteFile(){
        if (Model.getInstance().getAuthFlag()) {
            onDeleteCallback.accept(file);
            repository.get().getFiles().remove(file.getId());
            Model.getInstance().getFiles().remove(file);
            GridFSUtility.deleteFile(file.getId());
            DatabaseDriver.deleteFileFromRepository(user.getId(), repository.get().getId(), file.getId());
        }
        else{
            Model.getInstance().getViewScene().showAuthenticationModal();
        }
    }
    private void onFileClicked(){
        if (!isAllowedFile(file)) {
            showAutoDismissAlert("This File cannot be Viewed",5);
        }
        else{
            Model.getInstance().getFile().set(file);
            Model.getInstance().getViewScene().getUserMenuSelectedItem().set(UserMenuOptions.FILE_VIEW);
        }
    }
    private boolean isAllowedFile(FileModel file) {
        String[] restrictedExtensions = { "exe", "dll", "bat", "iso" ,"sh", "cmd","mp4","png","jpeg","gif"};
        for (String extension : restrictedExtensions) {
            if (file.getType().equals(extension)) {
                return false;
            }
        }
        return true;
    }
    private void showAutoDismissAlert(String message, int duration) {
        // Create an alert
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(name_lbl.getScene().getWindow());
        // Show the alert
        alert.show();

        // Create a timer to close the alert after a few seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(duration));
        pause.setOnFinished(event -> alert.close());
        pause.play();
    }
}
