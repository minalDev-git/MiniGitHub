package com.minigithub.model;

import java.util.ArrayList;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

public class RepositoryModel{
    
    @BsonId
    private ObjectId id;
    private String owner;
    private String repositoryName;
    private String description;
    private java.util.Date Date;
    private ArrayList<ObjectId> files = new ArrayList<>();

    public RepositoryModel(){
        
    }

    public ObjectId getId() {
        return id;
    }
    public void setId(ObjectId id) {
        this.id = id;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public String getOwner() {
        return owner;
    }
    public ArrayList<ObjectId> getFiles() {
        return files;
    }
    public void setFiles(ArrayList<ObjectId> files) {
        this.files = files;
    }
    public String getRepositoryName() {
        return repositoryName;
    }
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public java.util.Date getDate() {
        return Date;
    }
    public void setDate(java.util.Date date) {
        Date = date;
    }
}
