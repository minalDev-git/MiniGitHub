package com.minigithub.controllers.User;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.bson.types.ObjectId;

import com.minigithub.Views.UserMenuOptions;
import com.minigithub.database.DatabaseDriver;
import com.minigithub.database.GridFSUtility;
import com.minigithub.model.FileModel;
import com.minigithub.model.Model;
import com.minigithub.model.RepositoryModel;
import com.minigithub.model.UserModel;
import com.mongodb.client.gridfs.model.GridFSFile;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class FileViewController implements Initializable {

    public TextArea code_view;

    public Button edit_btn;
    public Button back_btn;
    public Button copycode_btn;
    public Button savefile_btn;
    public Button download_btn;
    public FontAwesomeIconView edit_icon;
    public Text filename;

    private int click = 0;
    private ObjectProperty<FileModel> file;
    private UserModel user;
    private ObjectProperty<RepositoryModel> repository;
    private String FileData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        repository = Model.getInstance().getRepository();
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
            file = Model.getInstance().getFile();
            // Add a listener to handle file updates
            file.addListener((obs, oldFile, newFile) -> {
                code_view.clear(); // Clear the previous content
                if (newFile != null) {
                    filename.setText(newFile.getFilename());
                    FileData = new String(GridFSUtility.getFileContents(newFile.getId()), StandardCharsets.UTF_8);
                    code_view.setText(FileData);
                    System.out.println("File updated in FileViewController: " + newFile.getId());
                }
            });
            // Set the initial file
            if (file.get() != null) {
                filename.setText(file.get().getFilename());
                FileData = new String(GridFSUtility.getFileContents(file.get().getId()), StandardCharsets.UTF_8);
                System.out.println("Initial File: " + file.get().getId());
            }
            savefile_btn.setDisable(true);
            code_view.setText(FileData);
            addListeners();
        }
    }

    private void addListeners() {
        edit_btn.setOnAction(event -> onEdit());
        copycode_btn.setOnAction(event -> onCopy());
        savefile_btn.setOnAction(event -> OnSave());
        download_btn.setOnAction(event -> onDownload());
        back_btn.setOnAction(event -> onBackPressed());
    }

    private void onEdit() {
        String oldData = code_view.getText();
        String newData = "";
        savefile_btn.setDisable(false);
        if (Model.getInstance().getAuthFlag()) {
            click++;
            if (code_view.isEditable() == false && click == 1) {
                code_view.setEditable(true);
                edit_btn.setText("Done");
                edit_icon.setVisible(false);
                savefile_btn.setDisable(false);

            } else if (code_view.isEditable() == true && click == 2) {
                code_view.setEditable(false);
                edit_btn.setText("Edit File");
                edit_icon.setVisible(true);
                click = 0;
            }
            if (!oldData.equals(newData)) {
                DatabaseDriver.incrementCommits(user.getId(), 1);
            }
        } else {
            savefile_btn.setDisable(true);
            Model.getInstance().getViewScene().showAuthenticationModal();
        }
    }

    private void onDownload() {
        FileModel file = Model.getInstance().getFile().get();
        File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
        try {
            GridFSUtility.downloadFile(file.getId(), downloadsDir.getAbsolutePath());
            showAutoDismissAlert("File downloaded Successfuly", 3);
        } catch (Exception e) {
            System.out.println("File not downloaded");
        }
        System.out.println(file);
    }

    private void onCopy() {
        String data = code_view.getText();
        if (data != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(data);
            clipboard.setContent(content);
            showAutoDismissAlert("File Copied to Clipboard", 3);
        }
    }

    private void OnSave() {
        savefile_btn.setDisable(true);
        code_view.setEditable(false);
        String newdata = code_view.getText();
        if (!newdata.isEmpty() && !newdata.equals(FileData)) {
            FileModel savedFile = new FileModel();
            try {
                InputStream inputStream = new ByteArrayInputStream(newdata.getBytes(StandardCharsets.UTF_8));
                savedFile.setType(file.get().getType());
                DatabaseDriver.deleteFileFromRepository(user.getId(), repository.get().getId(), file.get().getId());
                ObjectId newFileId = GridFSUtility.updateFile(file.get().getId(), file.get().getFilename(),
                        file.get().getType(), inputStream);
                DatabaseDriver.addFileToRepository(user.getId(), repository.get().getId(), newFileId);
                Model.getInstance().getFiles().remove(file.get());
                System.out.println("File saved with updated content: " + newFileId);
                GridFSFile newFile = GridFSUtility.getFile(newFileId);
                savedFile.setDate(newFile.getUploadDate());
                savedFile.setFilename(newFile.getFilename());
                savedFile.setId(newFileId);
                savedFile.setRepoName(repository.get().getRepositoryName());
                updateStreak(savedFile);
                Model.getInstance().getFiles().add(savedFile);
                Model.getInstance().getFile().set(savedFile);
                // repository.get().getFiles().add(newFileId);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("File not updated!");
            }
        }
    }

    private void onBackPressed() {
        Model.getInstance().getViewScene().getUserMenuSelectedItem().set(UserMenuOptions.FILES);
    }

    private void showAutoDismissAlert(String message, int duration) {
        // Create an alert
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Mini GitHub says...");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(edit_btn.getScene().getWindow());
        // Show the alert
        alert.show();

        // Create a timer to close the alert after a few seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(duration));
        pause.setOnFinished(event -> alert.close());
        pause.play();
    }

    private void updateStreak(FileModel file) {
        if (repository == null || repository.get() == null) {
            System.out.println("Repository is null, skipping streak update.");
            return;
        }
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
