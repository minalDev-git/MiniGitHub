package com.minigithub.Views;

import com.minigithub.App;
import com.minigithub.controllers.User.RepositoryCellController;
import com.minigithub.model.RepositoryModel;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

//create a class in model folder whoose listcell you want to create
//we get each individual obj(e.g. repo) from the listview as a ItemsProperty.
public class RepositoryCellFactory extends ListCell<RepositoryModel> {
    @Override
    protected void updateItem(RepositoryModel item, boolean empty) {
        super.updateItem(item, empty);
        //if the listcell is empty, means no data added
        if (empty) {
            setText(null);
            setGraphic(null);
        } else{
            FXMLLoader loader = new FXMLLoader(App.class.getResource("FXML/User/RepositoryCell.fxml"));
            //This repository obj is passed to the controller of the cell we want to create.
            //The controller class is the controller of the FXML file that we have in loader. 
            //The controller contains an object of this model class. 
            RepositoryCellController controller = new RepositoryCellController(item,repo -> {
                // Remove user from ObservableList in the parent ListView
                getListView().getItems().remove(repo);
                getListView().refresh();
            });
            loader.setController(controller);
            //If the listcell has any text wriiten, it should get erased so that new data of a diff obj is added.
            setText(null);
            try {
                setGraphic(loader.load());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
