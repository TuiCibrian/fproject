package com.fproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

//HOLA SOY CIBRIAN MAÑANA (14-NOV) VOY A INTENTAR TERMINAR LA PRACTICA 9 Y YA LE METO MANO AQUÍ SORRY

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/fproject/vista/inicio.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        stage.setTitle("Pokémon Card-Jitsu");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}
