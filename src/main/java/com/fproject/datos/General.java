package com.fproject.datos;
import com.fproject.Main;
import com.fproject.modelo.JugadorPerfil;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/*
Utilidades Generales
- Cambiar de Ventana
 */
public class General {

    //jugador que inició sesión
    private static JugadorPerfil jugadorActual;

    public General(){

    }

    public static JugadorPerfil getJugadorActual() {
        return jugadorActual;
    }

    public static void setJugadorActual(JugadorPerfil jugador) {
        jugadorActual = jugador;
    }

    public static void Cambiar(Event evento, String nombre){
        try{
            Parent ruta = FXMLLoader.load(Main.class.getResource(nombre+".fxml"));
            Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();

            stage.getScene().setRoot(ruta);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
