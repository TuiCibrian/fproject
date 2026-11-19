package com.fproject.datos;
import com.fproject.Main;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

/*
Utilidades Generales
- Cambiar de Ventana
 */
public class General {

    public General(){

    }


    //metodos generales para cambiar de ventana, se podrían combinar
    public static void Cambiar(ActionEvent evento, String nombre){
        try{
            Parent ruta = FXMLLoader.load(Main.class.getResource(nombre+".fxml"));
            Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();
            Scene scene = new Scene(ruta);

            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
            stage.setMaximized(false);
            stage.setMaximized(true);
            /*
            Puse que minimize y maximize ya que JavaFX tiene problemas para mantener ventana maximizada
            al cambiar de scene.
             */
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void Cambiar(MouseEvent evento, String nombre){
        try{
            Parent ruta = FXMLLoader.load(Main.class.getResource(nombre+".fxml"));
            Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();
            Scene scene = new Scene(ruta);

            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
            stage.setMaximized(false);
            stage.setMaximized(true);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
