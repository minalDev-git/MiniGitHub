module com.minigithub {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires transitive javafx.web;
    requires transitive org.mongodb.driver.sync.client;
    requires transitive org.mongodb.bson;
    requires de.jensd.fx.glyphs.commons;
    requires de.jensd.fx.glyphs.materialdesignicons;
    requires transitive de.jensd.fx.glyphs.fontawesome;
    requires transitive org.mongodb.driver.reactivestreams;
    requires transitive org.reactivestreams;
    requires transitive org.mongodb.driver.core;
    requires transitive java.desktop;

    opens com.minigithub.controllers to javafx.fxml;
    opens com.minigithub to javafx.graphics;
    exports com.minigithub.controllers;
    exports com.minigithub.controllers.Admin;
    exports com.minigithub.controllers.User;
    exports com.minigithub.model;
    exports com.minigithub.Views;
    exports com.minigithub.database;
}
