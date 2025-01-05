package com.minigithub.model;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import com.minigithub.Views.AccountType;
import com.minigithub.Views.ViewScenes;
import com.minigithub.database.DatabaseDriver;
import com.minigithub.database.MongoDBConnection;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
    //A singleton pattern ensures that you always get back the same instance of whatever type you are retrieving,
    //example: when the user clicks login btn, we will navigate him to the User window so the
    //instance of user window is created, but after it has been created, we want the same instance
    //to be used throughout the app till the user logs out.
    private static Model model;
    private final ViewScenes viewScene;
    private final MongoDBConnection connection;
    //We will need to track whether the user is trying to login or admin.
    private AccountType loginAccountType = AccountType.USER;
    // User Data section
    private final ObjectProperty<UserModel> user = new SimpleObjectProperty<>();
    private final ObjectProperty<RepositoryModel> repository = new SimpleObjectProperty<>();
    private final ObjectProperty<FileModel>file = new SimpleObjectProperty<>();
    private ObservableList<FileModel> files = FXCollections.observableArrayList();
    private ObservableList<RepositoryModel> repositories = FXCollections.observableArrayList();

    private boolean userLoginFlag;
    private boolean authflag;
    private boolean streakFlag;

    private Integer longestStreak;
    private String streakDates;
    
    // Admin Data Section
    private boolean adminLoginFlag;
    private ObjectId adminId;
    private final ObservableList<UserModel> users = FXCollections.observableArrayList();

    private Model(){
        this.viewScene = new ViewScenes();
        this.connection = new MongoDBConnection();
        // Admin Data Section
    }

    //This methods checks whether the object has been created or not. 
    //it allows to get the same instance, where ever we call this method.
    public static synchronized Model getInstance(){
        if (model == null) {
            model = new Model();
        }
        return model;
    }
    public void reset() {
        setUser(null);
        this.repository.set(null);
        this.file.set(null);
        this.files.clear();
        this.repositories.clear();
        this.userLoginFlag = false;
        this.authflag = false;
        this.streakFlag = false;
        this.adminLoginFlag = false;
    }
    public ViewScenes getViewScene() {
        return viewScene;
    }
    public MongoDBConnection getConnection() {
        return connection;
    }
    public AccountType getLoginAccountType() {
        return loginAccountType;
    }
    public void setLoginAccountType(AccountType loginAccountType) {
        this.loginAccountType = loginAccountType;
    }
    /*
     * Client Method Section
    */
    public ObjectProperty<UserModel> getUserProperty() {
        return user;
    }
    public UserModel getUser() {
        return user.get();
    }
    public void setUser(UserModel newUser) {
        user.set(newUser);
    }
    public ObjectProperty<RepositoryModel> getRepository() {
        return repository;
    }
    public void setRepository(RepositoryModel repository) {
        this.repository.set(repository);
    }
    public ObjectProperty<FileModel> getFile() {
        return file;
    }
    public void setFile(FileModel file) {
        this.file.set(file);
    }
    public boolean getUserLoginFlag(){
        return userLoginFlag;
    }
    public void setUserLoginFlag(boolean flag) {
        this.userLoginFlag = flag;
    }
    public boolean getAuthFlag(){
        return authflag;
    }
    public void setAuthflag(boolean authflag) {
        this.authflag = authflag;
    }
    public boolean getStreakFlag(){
        return streakFlag;
    }
    public void setStreakFlag(boolean streakFlag) {
        this.streakFlag = streakFlag;
    }
    public Integer getLongestStreak() {
        return longestStreak;
    }
    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }
    public String getStreakDates() {
        return streakDates;
    }
    public void setStreakDates(String streakDates) {
        this.streakDates = streakDates;
    }
    public ObservableList<FileModel> getFiles() {
        return files;
    }
    public void addFiles(FileModel file){
        this.files.add(file);
    }
    public void setFiles(ArrayList<FileModel> files) {
        clearFiles();
        for (FileModel file : files) {
            addFiles(file);
        }
    }
    public void clearFiles() {
        files.clear(); 
    }
    public ObservableList<RepositoryModel> getRepositories() {
        return repositories;
    }
    public void setRepositories(ArrayList<RepositoryModel> repositories) {
        this.repositories.clear();
        for (RepositoryModel repository : repositories) {
            this.repositories.add(repository);
        }
    }
    public void addRepository(RepositoryModel repository){
        this.repositories.add(repository);
    }
    /*
     * Admin Method Section
    */
    public void createAdmin(){
        try {
            adminId = DatabaseDriver.createAdmin();
        } catch (Exception e) {
            System.out.println("Admin not created");
        }
    }
    public void deleteAdmin(){
        try {
            DatabaseDriver.deleteAdmin(adminId);
        } catch (Exception e) {
            System.out.println("Admin not created");
        }
    }
    public boolean getAdminLoginFlag(){
        return adminLoginFlag;
    }
    public void setAdminLoginFlag(boolean adminLoginFlag) {
        this.adminLoginFlag = adminLoginFlag;
    }
    public ObservableList<UserModel> getUsers() {
        return users;
    }

    public void addUser(UserModel user) {
        users.add(user);
    }
}
