package com.minigithub.model;

import org.bson.types.ObjectId;

public abstract class Person {
    private ObjectId id;
    private String username;
    private String password;

    Person(){}
    
    Person(String name, String password){
        this.username = name;
        this.password = password;
    }
    public ObjectId getId() {
        return id;
    }
    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
