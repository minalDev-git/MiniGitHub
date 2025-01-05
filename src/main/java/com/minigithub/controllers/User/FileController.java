package com.minigithub.controllers.User;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import org.bson.types.ObjectId;

import com.minigithub.Views.FileCellFactory;
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
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;

//The page where user can see all the files in a specific repo
public class FileController implements Initializable {

    public ListView<FileModel> files_listview;

    public Button upload_btn;
    public Button download_btn;

    public Text repoName;
    private ObjectProperty<RepositoryModel> repository;
    private RepositoryModel currentRepo = new RepositoryModel();
    private UserModel user;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        repository = Model.getInstance().getRepository();
        user = Model.getInstance().getUser();
        Model.getInstance().getUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser == null) {
                // User logged out: Reset the UI
                user = oldUser;
            } else {
                // User logged in: Load the new user's data
                loadUserData(newUser);
            }
        });
        if (user != null) {
            // Add a listener to handle repository updates
            repository.addListener((obs, oldRepo, newRepo) -> {
                if (newRepo != null) {
                    repoName.setText(newRepo.getRepositoryName());
                    currentRepo = newRepo;
                    repository.set(newRepo);
                    System.out.println("Repository updated in FileController: " + newRepo.getId());
                }
            });
            // Set the initial repository
            if (repository.get() != null) {
                repoName.setText(repository.get().getRepositoryName());
                currentRepo = repository.get();
                System.out.println("Initial repository: " + repository.get().getId());
            }
            files_listview.setItems(Model.getInstance().getFiles());
            files_listview.setCellFactory(file -> new FileCellFactory());
            files_listview.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Model.getInstance().getFile().set(newValue);
                    files_listview.refresh();
                }
            });
            addListeners();
        }
    }

    // private void resetUI() {
    //     filename.setText("");
    //     code_view.clear();
    //     code_view.setEditable(false);
    //     savefile_btn.setDisable(true);
    // }
    private void loadUserData(UserModel user) {
        // Update the UI based on the new user
        System.out.println("Logged in user: " + user.getId());
        repository = Model.getInstance().getRepository();
        Model.getInstance().setUser(user);
    }
    private void addListeners() {
        upload_btn.setOnAction(event -> uploadFile());
        download_btn.setOnAction(event -> downloadFile());
    }

    private void uploadFile() {
        FileModel file = new FileModel();
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) {
            String fileExtension = "";
            int dotIndex = selectedFile.getName().lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < selectedFile.getName().length() - 1) {
                fileExtension = selectedFile.getName().substring(dotIndex + 1); // Extract extension
            }
            try {
                if (user == null || repository.get() == null) {
                    System.out.println("User or repository is null.");
                    return;
                }
                String newFileName = getUniqueFileName(user.getId(), repository.get().getId(), selectedFile.getName());
                ObjectId fileId = GridFSUtility.uploadFile(selectedFile.getAbsolutePath(), newFileName,
                        fileExtension);
                System.out.println("File uploaded successfully. File ID: " + fileId);
                file.setFilename(newFileName);
                file.setDate(new Date());
                file.setId(fileId);
                file.setRepoName(repository.get().getRepositoryName());
                file.setType(fileExtension);
                Model.getInstance().getFiles().add(file);
                DatabaseDriver.addFileToRepository(user.getId(), currentRepo.getId(), fileId);
                repository.get().getFiles().add(fileId);
                DatabaseDriver.incrementCommits(user.getId(), 1);
                updateStreak(file);

            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("File not uploaded");
            }
        }
    }

    private String getUniqueFileName(ObjectId userId, ObjectId repoId, String fileName) {
        String name = fileName;
        String uniqueName = fileName;
        int counter = 1;
        ArrayList<FileModel> files = DatabaseDriver.getAllFilesFromRepo(userId, repoId);
        for (FileModel file : files) {
            if (file != null && file.getFilename().equals(uniqueName)) {
                uniqueName = name + "(" + counter + ")";
                counter++;
            }
        }
        return uniqueName;
    }

    private void downloadFile() {
        FileModel file = files_listview.getSelectionModel().getSelectedItem();
        if (file != null) {
            File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
            try {
                GridFSUtility.downloadFile(file.getId(), downloadsDir.getAbsolutePath());
                showAutoDismissAlert("File Downloaded Successfully", 3);
            } catch (Exception e) {
                System.out.println("File not downloaded");
            }
        }
    }
    private void showAutoDismissAlert(String message, int duration) {
        // Create an alert
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Mini GitHub says...");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(upload_btn.getScene().getWindow());
        // Show the alert
        alert.show();

        // Create a timer to close the alert after a few seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(duration));
        pause.setOnFinished(event -> alert.close());
        pause.play();
    }
    private void updateStreak(FileModel file) {
        // Retrieve all files from the repository
        ArrayList<FileModel> files = DatabaseDriver.getAllFilesFromRepo(user.getId(), repository.get().getId());
        if (files.isEmpty()) {
            return; // No previous files, nothing to compare
        }
        // Get the date of the last uploaded file
        FileModel lastFile = files.get(files.size() - 1);
        LocalDate lastFileDate = lastFile.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentFileDate = file.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Check if the current file is uploaded on the next day
        if (lastFileDate.plusDays(1).isEqual(currentFileDate)) {
            if (!Model.getInstance().getStreakFlag()) {
                Model.getInstance().setStreakFlag(true);
                DatabaseDriver.UpdateStreaks(user.getId(), user.getStreaks() + 1);
            }
        }
        // Handle end-of-month and year transitions naturally
        else if (!lastFileDate.isEqual(currentFileDate)) {
            // Reset streak or clear the flag if needed
            Model.getInstance().setStreakFlag(false);
            DatabaseDriver.UpdateStreaks(user.getId(), 0);
        }
    }
}
