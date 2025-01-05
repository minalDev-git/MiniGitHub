package com.minigithub.model;

import java.util.ArrayList;

public class UserModel extends Person{
    //binding the fields of a model class as an observable properties is essential 
    //for enabling data binding and property change listening.
    //Data binding is a powerful feature in JavaFX that allows UI components to
    //automatically update when the underlying data model changes
    //These properties make this possible by being observable.
    private String email;
    private String description;
    private String jobTitle;
    private String passkey;
    private Integer streaks;
    private Integer commits;
    private String lastCommitYear;
    private java.util.Date dateCreated;
    private ArrayList<RepositoryModel> repositories = new ArrayList<>();

    public UserModel(){
        
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPasskey() {
        return passkey;
    }
    public void setPasskey(String passkey) {
        this.passkey = passkey;
    }
    public java.util.Date getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(java.util.Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    public ArrayList<RepositoryModel> getRepositories() {
        return repositories;
    }
    public void setRepositories(ArrayList<RepositoryModel> repositories) {
        this.repositories = repositories;
    }
    public String getLastCommitYear() {
        return lastCommitYear;
    }

    public void setLastCommitYear(String lastCommitYear) {
        this.lastCommitYear = lastCommitYear;
    }
    public Integer getCommits() {
        return commits;
    }
    public Integer getStreaks() {
        return streaks;
    }
    public void setCommits(Integer commits) {
        this.commits = commits;
    }
    public void setStreaks(Integer streaks) {
        this.streaks = streaks;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    public String getJobTitle() {
        return jobTitle;
    }
}
