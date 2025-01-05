package com.minigithub.controllers.User;

import java.net.URL;
import java.util.ResourceBundle;

import com.minigithub.Views.UserMenuOptions;
import com.minigithub.model.Model;

import javafx.fxml.Initializable;
// import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class UserMenuController implements Initializable{
    public Button dashboard_btn;
    public Button repositories_btn;
    public Button logout_btn;
    public Button profile_btn;
    public Text username;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        username.setText(Model.getInstance().getUser().getUsername());
        addListeners();
    }

    private void addListeners(){
        dashboard_btn.setOnAction(event -> onDashboard());
        repositories_btn.setOnAction(event -> onRepositories());
        profile_btn.setOnAction(event -> onProfile());
        logout_btn.setOnAction(event -> onLogout());
    }

    private void onDashboard(){
        Model.getInstance().getViewScene().getUserMenuSelectedItem().set(UserMenuOptions.DASHBOARD);
    }
    private void onRepositories(){
        Model.getInstance().getViewScene().getUserMenuSelectedItem().set(UserMenuOptions.REPOSITORIES);
    }
    private void onProfile(){
        Model.getInstance().getViewScene().getUserMenuSelectedItem().set(UserMenuOptions.PROFILE);
    }
    private void onLogout(){
        Model.getInstance().getViewScene().getUserMenuSelectedItem().set(UserMenuOptions.LOGOUT);
    }
}

