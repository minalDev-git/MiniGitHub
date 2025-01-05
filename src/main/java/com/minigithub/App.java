package com.minigithub;

import com.minigithub.model.Model;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    // private static Scene scene;

    @Override
    public void start(Stage stage){
        Model.getInstance().createAdmin();
        Model.getInstance().getViewScene().showLoginWindow();
    }

    // public static void setRoot(String fxml) throws IOException {
    //     scene.setRoot(loadFXML(fxml));
    // }
    // private static Parent loadFXML(String fxml) throws IOException {
    //     FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    //     return fxmlLoader.load();
    // }
    public static void main(String[] args) {
        launch(args);
    }
}
