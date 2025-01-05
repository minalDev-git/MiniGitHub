package com.minigithub.Views;

import com.minigithub.App;
import com.minigithub.controllers.Admin.UsersCellController;
import com.minigithub.model.UserModel;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

public class UsersCellFactory extends ListCell<UserModel>{
    @Override
    protected void updateItem(UserModel item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        }
        else{
            FXMLLoader loader = new FXMLLoader(App.class.getResource("FXML/Admin/UsersCell.fxml"));
            UsersCellController controller = new UsersCellController(item, user -> {
                // Remove user from ObservableList in the parent ListView
                getListView().getItems().remove(user);
                getListView().refresh();
            });
            loader.setController(controller);
            setText(null);
            try {
                setGraphic(loader.load());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
