package com.minigithub.Views;

import java.io.IOException;

import com.minigithub.App;
import com.minigithub.controllers.Admin.AdminController;
import com.minigithub.controllers.User.UserController;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ViewScenes {

    private AccountType loginAccountType;
    //User View
    private AnchorPane dashboardView;
    private AnchorPane repositoriesView;
    private AnchorPane profileView;
    private AnchorPane createRepositoryView;
    private AnchorPane fileView;
    private AnchorPane showFilesView;

    /*we used a string Propety first to navigate b/w screens but to avoid eroors we created 
    an enum "UserMenuOptions" and add all the options on the user menu. so we have to change
    the type to ObjectProperty<UserMenuOptions>
    */
    private final ObjectProperty<UserMenuOptions> userMenuSelectedItem;

    //Admin View
    private AnchorPane createUserView;
    private AnchorPane usersView;
    private final ObjectProperty<AdminMenuOptions> adminMenuSelectedItem;

    public ViewScenes(){
        //Initially this will be displayed on the ChoiceBox
        this.loginAccountType = AccountType.USER;
        this.userMenuSelectedItem = new SimpleObjectProperty<>();
        this.adminMenuSelectedItem = new SimpleObjectProperty<>();
    }

    public void setLoginAccountType(AccountType loginAccountType) {
        this.loginAccountType = loginAccountType;
    }
    public AccountType getLoginAccountType() {
        return loginAccountType;
    }

    private void createStage(FXMLLoader loader){
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        //adding logo to our app screen
        stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/com/minigithub/Images/github.jpeg"))));
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Mini Github");
        stage.show();
    }

    /*
     * User Views Section
    */
    public AnchorPane getDashboardView(){
        if (dashboardView == null) {
            try {
                dashboardView = new FXMLLoader(App.class.getResource("FXML/User/Dashboard.fxml")).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dashboardView;
    }

    public AnchorPane getRepositoriesView() {
        if (repositoriesView == null) {
            try {
                repositoriesView = new FXMLLoader(App.class.getResource("FXML/User/Repositories.fxml")).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return repositoriesView;
    }
    public AnchorPane getProfileView(){
        if (profileView == null) {
            try {
                profileView = new FXMLLoader(App.class.getResource("FXML/User/ProfilePage.fxml")).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return profileView;
    }

    //This property will be set from the UserMenuController according to the button clicked.
    public ObjectProperty<UserMenuOptions> getUserMenuSelectedItem() {
        return userMenuSelectedItem;
    }

    public void showUserWindow(){
        FXMLLoader loader = new FXMLLoader(App.class.getResource("FXML/User/user.fxml"));
        UserController userController = new UserController();
        loader.setController(userController);
        createStage(loader);
    }
    public AnchorPane getOpenFileWindow(){
        if (fileView == null) {
            try {
                fileView = new FXMLLoader(App.class.getResource("FXML/User/FileView.fxml")).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileView;
    }
    public AnchorPane getFilesWindow(){
        if (showFilesView == null) {
            try {
                showFilesView  = new FXMLLoader(App.class.getResource("FXML/User/Files.fxml")).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return showFilesView;
    }
    public AnchorPane CreateRepoWindow(){
        if (createRepositoryView == null) {
            try {
                createRepositoryView = new FXMLLoader(App.class.getResource("FXML/User/CreateRepo.fxml")).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return createRepositoryView;
    }
    public void showAuthenticationModal() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("FXML/User/AuthBox.fxml"));
            Parent modalRoot = loader.load();
            Stage modalStage = new Stage();
            modalStage.setTitle("Authentication Required");
            modalStage.initModality(Modality.APPLICATION_MODAL); // Block interaction with parent window
            modalStage.setScene(new Scene(modalRoot));
            modalStage.showAndWait(); // Wait until the modal is closed
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /*
     * Admin Views Section
    */

    public ObjectProperty<AdminMenuOptions> getAdminMenuSelectedItem() {
        return adminMenuSelectedItem;
    }

    public AnchorPane getCreateUserView() {
        if (createUserView == null) {
            try {
                createUserView = new FXMLLoader(App.class.getResource("FXML/Admin/CreateUser.fxml")).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return createUserView;
    }
    public AnchorPane getUsersView(){
        if (usersView == null) {
            try {
                usersView = new FXMLLoader(App.class.getResource("FXML/Admin/Users.fxml")).load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return usersView;
    }
    public void showAdminWindow(){
        FXMLLoader loader = new FXMLLoader(App.class.getResource("FXML/Admin/admin.fxml"));
        AdminController adminController = new AdminController();
        loader.setController(adminController);
        createStage(loader);
    }

    //Common View for both Sections
    public void showLoginWindow(){
        FXMLLoader loader = new FXMLLoader(App.class.getResource("FXML/Login.fxml"));
        createStage(loader);
    }
    public void closeStage(Stage stage){
        stage.close();
    }
}
