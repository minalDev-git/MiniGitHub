package com.minigithub.controllers.Admin;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import com.minigithub.database.DatabaseDriver;
import com.minigithub.model.Model;
import com.minigithub.model.UserModel;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CreateUserController implements Initializable{

    
    public TextField usrName_fld;
    public TextField description_fld;
    public TextField password_fld;
    public TextField email_fld;
    public TextField passkey_fld;
    public TextField jobtitle_fld;


    public Label error_lbl;

    public Button createUser_btn;

    private final String EMAIL_REGEX = "^[^@]+@[^@]+\\.[^@]+$";
    private final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
    //Allows only alphanumeric characters (uppercase letters, lowercase letters, and numbers).
    private final String PASSKEY_REGEX = "^[A-Za-z0-9]{8,12}$";
    private String year = (new Date().toString().substring(0,10)).substring(0,4);
    private boolean validPswd = false;
    private boolean validEmail = false;
    private boolean validPasskey = false;
    private boolean exists = false;
    private ObjectId id;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createUser_btn.setOnAction(event -> {
            id = createUser();
            if (id != null) {
                clearCredentials();
                error_lbl.setText("User Created Successfuly");
            }
            else if (isDataEmpty()) {
                error_lbl.setText("Please fill every Field");
            }
            else if(!validEmail){
                error_lbl.setText("Enter Valid Email");
            }
            else if(!validPswd){
                error_lbl.setText("Enter Valid Password");
            }
            else if (!validPasskey) {
                error_lbl.setText("Enter Valid Passkey");
            }
            else if (exists) {
                error_lbl.setText("User already Exists");
            }
            else{
                error_lbl.setText("User Not Created");
            }
        });
    }

    public ObjectId createUser(){
        if (!isUserExist(email_fld.getText(), password_fld.getText(),passkey_fld.getText()) && !isDataEmpty()) {
            UserModel newUser = new UserModel();
            newUser.setUsername(usrName_fld.getText());
            newUser.setCommits(0);
            newUser.setDateCreated(new Date());
            newUser.setEmail(email_fld.getText());
            newUser.setStreaks(0);
            newUser.setPassword(password_fld.getText());
            newUser.setDescription(description_fld.getText());
            newUser.setJobTitle(jobtitle_fld.getText());
            newUser.setPasskey(passkey_fld.getText());
            newUser.setLastCommitYear(year);
            Model.getInstance().addUser(newUser);
            return DatabaseDriver.insertUserData(newUser);
        }
        else {
            return null;
        }
    }
    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        validEmail = matcher.matches();
        return matcher.matches();
    }
    private boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile(PASSWORD_REGEX);
        Matcher matcher = pattern.matcher(password);
        validPswd = matcher.matches();
        return matcher.matches();
    }
    private boolean isValidPassKey(String passkey) {
        Pattern pattern = Pattern.compile(PASSKEY_REGEX);
        Matcher matcher = pattern.matcher(passkey);
        validPasskey = matcher.matches();
        return matcher.matches();
    }
    private boolean isUserExist(String email, String password, String passkey){
        if (isValidEmail(email) && isValidPassword(password) && isValidPassKey(passkey)) {
            if (DatabaseDriver.isUserExists(email, password,passkey)) {
                exists = false;
            }
            else{
                exists = true;
            }
        }
        return exists;
    }
    private boolean isDataEmpty(){
        if (usrName_fld.getText()!= "" && password_fld.getText() != "" && email_fld.getText() != "" && passkey_fld.getText() != "" && jobtitle_fld.getText() != "" ) {
            return false;
        }
        else {
            return true;
        }
    }
    private void clearCredentials(){
        usrName_fld.clear();
        email_fld.clear();
        password_fld.clear();
        description_fld.clear();
        jobtitle_fld.clear();
        passkey_fld.clear();
    }
}
