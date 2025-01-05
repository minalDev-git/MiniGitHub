package com.minigithub.Views;

import com.minigithub.App;
import com.minigithub.controllers.User.DashboardCellController;
import com.minigithub.model.RepositoryModel;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

public class DashboardCellFactory extends ListCell<RepositoryModel>{
    @Override
    protected void updateItem(RepositoryModel item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        }
        else{
            FXMLLoader loader = new FXMLLoader(App.class.getResource("FXML/User/DashboardCell.fxml"));
            DashboardCellController controller = new DashboardCellController(item);
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
