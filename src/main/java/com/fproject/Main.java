package com.fproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

//HOLA SOY CIBRIAN MAÑANA (14-NOV) VOY A INTENTAR TERMINAR LA PRACTICA 9 Y YA LE METO MANO AQUÍ SORRY
//HOLA SOY AXEL, YO VOY A CHAMBEAR HOY (17-NOV)

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/fproject/vista/IniciarSesion.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Pokémon Card-Jitsu");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}
