package com.minigithub.controllers.User;

import java.net.URL;
import java.util.ResourceBundle;

import com.minigithub.model.RepositoryModel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class DashboardCellController implements Initializable {
    private ObjectProperty<RepositoryModel> repository = new SimpleObjectProperty<>();

    public DashboardCellController(RepositoryModel repositoryModel){
        this.repository.set(repositoryModel);
    }

    public Label name_lbl;
    public Label date_lbl;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        name_lbl.setText(repository.get().getRepositoryName());
        date_lbl.setText(repository.get().getDate().toString().substring(0,10));
    }
}
