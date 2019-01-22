package com.db.project;

import java.io.IOException;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;
    private StackPane rootLayout;
    public static String u;
    public static int uid;
    @Override
    public void start(Stage primaryStage) throws SQLException {
        MainApp.primaryStage = primaryStage;
        MainApp.primaryStage.setTitle("Project");

        initRootLayout();
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/login.fxml"));
            rootLayout = loader.load();
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
            primaryStage.getIcons().add(new Image(MainApp.class.getResourceAsStream("icon.png")));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }   
    public void setUser(String u) {
    	MainApp.u= u;
    }
    public void setUID(int uid) {
    	MainApp.uid = uid;
    }
    public String getUser() {
    	return u;
    }
    public int getUID() {
    	return uid;
    }
    /**
     * Returns the main stage.dwda
     * @return
     */

    
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}