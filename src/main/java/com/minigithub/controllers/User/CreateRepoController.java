package com.minigithub.controllers.User;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import org.bson.types.ObjectId;

import com.minigithub.Views.UserMenuOptions;
import com.minigithub.database.DatabaseDriver;
import com.minigithub.database.GridFSUtility;
import com.minigithub.model.FileModel;
import com.minigithub.model.Model;
import com.minigithub.model.RepositoryModel;
import com.minigithub.model.UserModel;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CreateRepoController implements Initializable{
    public TextField owner_fld;
    public TextField reponame_fld;
    public TextField description_fld;

    public Button createrepo_btn;

    public Label error_lbl;
    public CheckBox readme_box;

    private UserModel user;

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
            owner_fld.setText(user.getUsername());
            createrepo_btn.setOnAction(event -> onCreateRepo());
        }
    }
    
    private void onCreateRepo(){
        RepositoryModel repository = new RepositoryModel();
        if (reponame_fld.getText().equals("")) {
            error_lbl.setText("Enter a valid repo name");
        }
        else if (validateRepoName(reponame_fld.getText())) {
            error_lbl.setText("Repository Name is available");
            repository.setOwner(owner_fld.getText());
            repository.setDescription(description_fld.getText());
            repository.setRepositoryName(reponame_fld.getText());
            repository.setDate(new Date());
            repository.setId(new ObjectId());
            if (readme_box.isSelected() == true) {
                FileModel readmeFile = GridFSUtility.createREADME(user.getId(), reponame_fld.getText());
                System.out.println(readmeFile.getType());
                Model.getInstance().addFiles(readmeFile);
                repository.getFiles().add(readmeFile.getId());
            }
            Model.getInstance().addRepository(repository);
            DatabaseDriver.addRepository(user.getId(), repository);
            DatabaseDriver.incrementCommits(user.getId(), 1);
            updateStreak(repository);
            Model.getInstance().getViewScene().getUserMenuSelectedItem().set(UserMenuOptions.REPOSITORIES);
        }
        else{
            clearCredentials();
            error_lbl.setText("Repository Name must be unique");
        }
    }
    private boolean validateRepoName(String repoName){
        if (repoName == "" || DatabaseDriver.getRepoByName(user.getId(), repoName) != null) {
            return false;
        }
        else{
            return true;
        }
    }
    private void updateStreak(RepositoryModel repo){
        ArrayList<RepositoryModel> repositories = DatabaseDriver.getAllRepos(user.getId());
        RepositoryModel lastRepo = repositories.get(repositories.size()-1);
        LocalDate lastRepoDate = lastRepo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentRepoDate = repo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Check if the current file is uploaded on the next day
        if (lastRepoDate.plusDays(1).isEqual(currentRepoDate)) {
            if (!Model.getInstance().getStreakFlag()) {
                Model.getInstance().setStreakFlag(true);
                DatabaseDriver.UpdateStreaks(user.getId(), user.getStreaks() + 1);
            }
        }
        // Handle end-of-month and year transitions naturally
        else if (!lastRepoDate.isEqual(currentRepoDate)) {
            // Reset streak or clear the flag if needed
            Model.getInstance().setStreakFlag(false);
            DatabaseDriver.UpdateStreaks(user.getId(), 0);
        }
    }
    private void clearCredentials(){
        reponame_fld.clear();
        description_fld.clear();
    }
}
