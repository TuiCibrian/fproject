package com.fproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


//HOLA SOY CIBRIAN MAÑANA (14-NOV) VOY A INTENTAR TERMINAR LA PRACTICA 9 Y YA LE METO MANO AQUÍ SORRY

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //Ubicación del Archivo FXML de Inicio
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/fproject/vista/inicio.fxml"));
        // Tamaño de Ventana
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        stage.setTitle("Titulo Ventana");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}