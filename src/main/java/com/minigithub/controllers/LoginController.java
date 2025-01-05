package com.minigithub.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.minigithub.Views.AccountType;
import com.minigithub.database.DatabaseDriver;
import com.minigithub.model.AdminModel;
import com.minigithub.model.Model;
import com.minigithub.model.UserModel;

import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController implements Initializable {
    public ChoiceBox<AccountType> acc_selector;
    public Label username_lbl;
    public TextField username_fld;
    public TextField password_fld;
    public Button login_btn;
    public Label error_lbl;

    // when the user clicks the login btn, we want to be able to come to viewScene
    // and show the user window.
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // This provides the options on the choicebox to allow us to switch b/w accounts
        acc_selector.setItems(FXCollections.observableArrayList(AccountType.USER, AccountType.ADMIN));
        acc_selector.setValue(Model.getInstance().getViewScene().getLoginAccountType());// By defalut "USER" is set in ViewScenes constructor
        // We will add a listner to the choicebox,so everytime the value changes, we set
        // the loginAccountType variable in ViewScene
        acc_selector.valueProperty().addListener(
                observable -> Model.getInstance().getViewScene().setLoginAccountType(acc_selector.getValue()));
        login_btn.setOnAction(event -> onLogin());
    }

    public void onLogin() {
        // Accessing the current Login stage to close Login window
        // we take any one of the fields and use its getScene method to get the Scene it
        // belongs to.
        Stage stage = (Stage) error_lbl.getScene().getWindow();
        // Load the appropriate login window based on the value of loginAccountType
        if (Model.getInstance().getViewScene().getLoginAccountType() == AccountType.USER) {
            // 1st check if login is successful. Evaluate Login Credentials
            evaluateUserCred(username_fld.getText(), password_fld.getText());
            // Check whether the userLoginFlag = true
            if (Model.getInstance().getUserLoginFlag()) {
                Model.getInstance().getViewScene().showUserWindow();
                // Close the login stage
                Model.getInstance().getViewScene().closeStage(stage);
                // updateStreakOnLogin();
            } else {
                username_fld.setText(null);
                password_fld.setText(null);
                error_lbl.setText("Login Not Successful");
            }
        } else {
            Model.getInstance().setLoginAccountType(AccountType.ADMIN);
            evaluateAdminCred(username_fld.getText(), password_fld.getText());
            if (Model.getInstance().getAdminLoginFlag()) {
                Model.getInstance().getViewScene().showAdminWindow();
                Model.getInstance().getViewScene().closeStage(stage);
            } else {
                username_fld.setText(null);
                password_fld.setText(null);
                error_lbl.setText("Login Not Successful");
            }
        }
    }
    // Login Evaluation
    private boolean evaluateUserCred(String username, String password) {
        Model.getInstance().reset(); // Reset before loading new user data
        try {
            UserModel newUser = DatabaseDriver.getUserData(username,password);
            if (newUser != null) {
                Model.getInstance().setUser(newUser);
                DatabaseDriver.updateLastCommitYear(newUser.getId());
                Model.getInstance().setUserLoginFlag(true);
                Model.getInstance().setRepositories(DatabaseDriver.getAllRepos(newUser.getId()));
                System.out.println(Model.getInstance().getUser().getUsername());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public void evaluateAdminCred(String username, String password){
        Model.getInstance().reset();
        AdminModel admin = DatabaseDriver.getAdmin(username, password);
        try {
            if (username.equals(admin.getUsername()) && password.equals(admin.getPassword())) {
                Model.getInstance().setAdminLoginFlag(true);
                Model.getInstance().deleteAdmin();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
