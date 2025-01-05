package com.minigithub.controllers.User;

import java.net.URL;
import java.util.ResourceBundle;

import com.minigithub.Views.AccountType;
import com.minigithub.model.Model;

import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class UserController implements Initializable {

    public BorderPane user_parent;

    // we want to change the center property of boarder pane, but how do we know
    // when to change?
    // we need a flag variable such that if user clicks a on from UserMenu,
    // we need to set of that flag in the ViewSenes and be able to update the view
    // in this Controller
    // we will add a listner to the StringProperty over here
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // this listner will listen to the button clicked on the UserMenu and load the
        // appropriate screen accordingly.
        // Everytime the value of the property changes,this listner set its new value
        // and act accordingly
        Model.getInstance().getViewScene().getUserMenuSelectedItem().addListener((ObservableValue, oldVal, newVal) -> {
            switch (newVal) {
                case REPOSITORIES -> user_parent.setCenter(Model.getInstance().getViewScene().getRepositoriesView());
                case PROFILE -> user_parent.setCenter(Model.getInstance().getViewScene().getProfileView());
                case CREATE_REPO -> user_parent.setCenter(Model.getInstance().getViewScene().CreateRepoWindow());
                case FILES -> user_parent.setCenter(Model.getInstance().getViewScene().getFilesWindow());
                case FILE_VIEW -> user_parent.setCenter(Model.getInstance().getViewScene().getOpenFileWindow());
                case LOGOUT -> {
                    Model.getInstance().reset(); // Clear all data
                    Model.getInstance().setUser(null);
                    Model.getInstance().setLoginAccountType(AccountType.USER);
                    Model.getInstance().getViewScene().showLoginWindow();
                    Model.getInstance().getViewScene().closeStage((Stage) user_parent.getScene().getWindow());
                }
                default -> user_parent.setCenter(Model.getInstance().getViewScene().getDashboardView());
            }
        });
    }
}
