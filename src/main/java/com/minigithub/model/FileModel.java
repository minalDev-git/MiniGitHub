package com.minigithub.model;

import org.bson.types.ObjectId;

public class FileModel{

    private ObjectId id;
    private String repoName;
    private String filename;
    private String type;
    private java.util.Date Date;

    public FileModel(){
    
    }
    public ObjectId getId() {
        return id;
    }
    public void setId(ObjectId id) {
        this.id = id;
    }
    public String getRepoName() {
        return repoName;
    }
    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public java.util.Date getDate() {
        return Date;
    }
    public void setDate(java.util.Date date) {
        Date = date;
    }
}
