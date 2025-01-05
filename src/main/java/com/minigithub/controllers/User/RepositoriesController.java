package com.minigithub.controllers.User;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import org.bson.types.ObjectId;

import com.minigithub.Views.RepositoryCellFactory;
import com.minigithub.Views.UserMenuOptions;
import com.minigithub.database.DatabaseDriver;
import com.minigithub.database.GridFSUtility;
import com.minigithub.model.Model;
import com.minigithub.model.RepositoryModel;
import com.minigithub.model.UserModel;

import javafx.animation.PauseTransition;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

//The page where the user can see his repos.
public class RepositoriesController implements Initializable {

    public ListView<RepositoryModel> repository_listview;
    public Button uploadrepo_btn;
    public Button downloadrepo_btn;
    public Button newrepo_btn;

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
            repository_listview.setItems(Model.getInstance().getRepositories());
            repository_listview.setCellFactory(repo -> new RepositoryCellFactory());
            // repository_listview.getItems().addAll(DatabaseDriver.getAllRepos(user.getId()));
            repository_listview.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    repository_listview.refresh();
                    Model.getInstance().getRepository().set(newValue);
                }
                else{
                    Model.getInstance().getRepository().set(oldValue);
                }
            });
            addListeners();
        }
    }

    private void addListeners() {
        newrepo_btn.setOnAction(event -> onNewRepo());
        uploadrepo_btn.setOnAction(event -> uploadRepository());
        downloadrepo_btn.setOnAction(event -> downloadRepository());
    }

    public void uploadRepository() {
        RepositoryModel repo = new RepositoryModel();
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(System.getProperty("user.home")));
        File dir = dc.showDialog(null);
        boolean flag = true;
        if (dir != null && validateRepoName(dir.getName())) {
            repo.setRepositoryName(dir.getName());
            repo.setDate(new Date());
            repo.setOwner(user.getUsername());
            repo.setDescription("Added Repository from System");
            // Creating a File object for directory
            File directoryPath = new File(dir.getAbsolutePath());
            // List of all files and directories
            File filesList[] = directoryPath.listFiles();
            for (File file : filesList) {
                if (file.isFile()) {
                    String fileExtension = "";
                    int dotIndex = file.getName().lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < file.getName().length() - 1) {
                        fileExtension = file.getName().substring(dotIndex + 1); // Extract extension
                    }
                    ObjectId id = GridFSUtility.uploadFile(file.getAbsolutePath(), file.getName(), fileExtension);
                    repo.getFiles().add(id);
                    repo.setId(new ObjectId());
                } else {
                    flag = false;
                    ArrayList<ObjectId> ids = repo.getFiles();
                    for (ObjectId id : ids) {
                        GridFSUtility.deleteFile(id);
                    }
                    showAutoDismissAlert("Please choose a directory, containing only files", 5);
                    break;
                }
            }
            if (flag) {
                Model.getInstance().addRepository(repo);
                DatabaseDriver.addRepository(user.getId(), repo);
                DatabaseDriver.incrementCommits(user.getId(), 1);
                updateStreak(repo);
            }
        } else {
            showAutoDismissAlert("Repository either already exists or is not selected", 3);
        }
    }

    private void onNewRepo() {
        Model.getInstance().getViewScene().getUserMenuSelectedItem().set(UserMenuOptions.CREATE_REPO);
    }

    private void showAutoDismissAlert(String message, int duration) {
        // Create an alert
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Mini GitHub says...");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(uploadrepo_btn.getScene().getWindow());
        // Show the alert
        alert.show();

        // Create a timer to close the alert after a few seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(duration));
        pause.setOnFinished(event -> alert.close());
        pause.play();
    }

    private void downloadRepository() {
        if (Model.getInstance().getAuthFlag()) {
            RepositoryModel repository = repository_listview.getSelectionModel().getSelectedItem();
            if (repository != null) {
                File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
                File newDirectory = new File(downloadsDir, repository.getRepositoryName());
                if (!newDirectory.exists()) {
                    boolean created = newDirectory.mkdir();
                    if (created) {
                        System.out.println("Directory created successfully: " + newDirectory.getAbsolutePath());
                        ArrayList<ObjectId> fileIds = repository.getFiles();
                        for (ObjectId objectId : fileIds) {
                            GridFSUtility.downloadFile(objectId, newDirectory.getAbsolutePath());
                        }
                        showAutoDismissAlert("Repository Downloaded Successfully " + newDirectory.getAbsolutePath(), 3);
                    } else {
                        showAutoDismissAlert("Failed to create the directory.", 3);
                    }
                } else {
                    showAutoDismissAlert("Directory already exists: " + newDirectory.getAbsolutePath(), 3);
                }
            }
        } else {
            Model.getInstance().getViewScene().showAuthenticationModal();
        }
    }

    private boolean validateRepoName(String repoName) {
        if (repoName == "" || DatabaseDriver.getRepoByName(user.getId(), repoName) != null) {
            return false;
        } else {
            return true;
        }
    }

    private void updateStreak(RepositoryModel repo) {
        ArrayList<RepositoryModel> repositories = DatabaseDriver.getAllRepos(user.getId());
        RepositoryModel lastRepo = repositories.get(repositories.size() - 1);
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
}
