package com.minigithub.Views;

import com.minigithub.App;
import com.minigithub.controllers.User.FileCellController;
import com.minigithub.model.FileModel;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

public class FileCellFactory extends ListCell<FileModel>{
    @Override
    protected void updateItem(FileModel item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else{
            FXMLLoader loader = new FXMLLoader(App.class.getResource("FXML/User/FileCell.fxml"));
            FileCellController controller = new FileCellController(item, file -> {
                getListView().getItems().remove(file);
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
