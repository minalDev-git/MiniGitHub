package com.minigithub.controllers.User;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import org.bson.types.ObjectId;

import com.minigithub.Views.DashboardCellFactory;
import com.minigithub.database.DatabaseDriver;
import com.minigithub.model.FileModel;
import com.minigithub.model.Model;
import com.minigithub.model.RepositoryModel;
import com.minigithub.model.UserModel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

public class DashboardController implements Initializable{

    public Text dashboard_user;
    public Text longest_streak;
    public Text current_streak;

    public Label total_repos;
    public Label total_commits;
    public Label dashboard_email;
    public Label dashboard_job_title;
    public Label streak_dates;
    public Label login_date;
    public Label userlevel_lbl;

    public ListView<RepositoryModel> recent_repos_listview;
    private ObjectProperty<UserModel> user = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user = Model.getInstance().getUserProperty();
        Model.getInstance().getUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser == null) {
                // User logged out: Reset the UI
                user = null;
            } else {
                // User logged in: Load the new user's data
                user.set(newUser);
            }
        });
        if (user != null) {
            recent_repos_listview.setCellFactory(repo -> new DashboardCellFactory());
            getRecentRepos(user.get().getId());
            setData();
            assignUserLevel();
            setLongestStreakDates();
        }
    }

    private void setData(){
        dashboard_user.setText(dashboard_user.getText() + " " + user.get().getUsername());
        dashboard_email.setText(dashboard_email.getText() + " " + user.get().getEmail());
        dashboard_job_title.setText(dashboard_job_title.getText() + " " + user.get().getJobTitle());
        login_date.setText(new Date().toString().substring(0,10));
        total_repos.setText(total_repos.getText() + " " + getTotalRepos().toString());
        total_commits.setText(total_commits.getText() + "(" + user.get().getLastCommitYear() + ") " + getTotalCommits().toString());
        current_streak.setText(user.get().getStreaks().toString());
        // streak_dates.setText(Model.getInstance().getStreakDates());
        // longest_streak.setText(Model.getInstance().getLongestStreak().toString());
    }
    private Integer getTotalRepos(){
        return user.get().getRepositories().size();
    }
    private Integer getTotalCommits(){
        return user.get().getCommits();
    }
    private void assignUserLevel(){
        if (user.get().getStreaks() >= 50) {
            userlevel_lbl.setText("Active User");
        }
        else{
            userlevel_lbl.setText("Casual User");
        }
    }
    private void getRecentRepos(ObjectId userId){
        ArrayList<RepositoryModel> recentRepos = DatabaseDriver.getRecentRepos(userId);
        if (recentRepos != null) {
            if (recentRepos.size() < 5) {
                for (RepositoryModel repository : recentRepos) {
                    recent_repos_listview.getItems().add(repository);
                }
            }
            else{
                for (int index = 0; index < 5; index++) {
                    recent_repos_listview.getItems().add(recentRepos.get(index));
                }
            }
        }
    }

    private void setLongestStreakDates() {
        UserModel user = Model.getInstance().getUser();
        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalDate storedStartDate = null;
        LocalDate storedEndDate = null;
        int count = 0;
        int largestCount = 0;
        // Retrieve the last uploaded repository
        ArrayList<RepositoryModel> repositories = user.getRepositories();
        if (repositories.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-dd-yyyy");
            streak_dates.setText(LocalDate.now().format(formatter));
            longest_streak.setText("0");
            return; // No repositories
        }
        for (RepositoryModel repository : repositories) {
            // Retrieve all files from the repository
            ArrayList<FileModel> files = DatabaseDriver.getAllFilesFromRepo(user.getId(), repository.getId());
            if (files.isEmpty()) {
                continue; // Skip repositories with no files
            }
            for (int i = 0; i < files.size()-1; i++) {
                // Convert upload date and today's date to LocalDate
                LocalDate fileDate = files.get(i).getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (i == 0 || (startDate == null && endDate == null)) {
                    startDate = fileDate;
                    endDate = fileDate;
                    count = 1;
                }
                else{
                    if (startDate.plusDays(1).isEqual(fileDate)) {
                        endDate = fileDate;
                        count++;
                    }
                    else{
                        if (largestCount < count && startDate!= null && endDate != null) {
                            largestCount = count;
                            storedStartDate = startDate;
                            storedEndDate = endDate;
                        }
                        // Reset streak tracking
                        startDate = fileDate;
                        endDate = fileDate;
                        count = 1;
                    }
                }
            }
            if (largestCount < count && startDate != null && endDate != null) {
                largestCount = count; // Include the last streak
                storedStartDate = startDate;
                storedEndDate = endDate;
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-dd");
        if (storedStartDate != null && storedEndDate != null) {
            String startDateStr = storedStartDate.format(formatter);
            String endDateStr = storedEndDate.format(formatter);
            longest_streak.setText(Integer.toString(largestCount));
            if (startDateStr.equals(endDateStr)) {
                streak_dates.setText(startDateStr);
            }
            else{
                streak_dates.setText(startDateStr + " -> " + endDateStr);
            }
            Model.getInstance().setStreakDates(streak_dates.getText());
            Model.getInstance().setLongestStreak(largestCount);
        } else {
            // No streak found
            streak_dates.setText(LocalDate.now().format(formatter));
            longest_streak.setText("0");
        }
    }
}
