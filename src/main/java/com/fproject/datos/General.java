package com.fproject.datos;
import com.fproject.Main;
import com.fproject.modelo.JugadorPerfil;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

/*
Utilidades Generales
- Cambiar de Ventana
 */
public class General {

    //jugador que inició sesión
    private static JugadorPerfil jugadorActual;

    //rival de la partida en línea actual
    private static JugadorPerfil rivalActual;

    public General(){

    }

    public static JugadorPerfil getJugadorActual() {
        return jugadorActual;
    }

    public static void setJugadorActual(JugadorPerfil jugador) {
        jugadorActual = jugador;
    }

    public static JugadorPerfil getRivalActual() {
        return rivalActual;
    }

    public static void setRivalActual(JugadorPerfil rival) {
        rivalActual = rival;
    }

    public static void Cambiar(Event evento, String nombre){
        Cambiar((Node) evento.getSource(), nombre);
    }

    //para cambiar de ventana cuando no hay evento, por ejemplo al recibir un mensaje del servidor
    public static void Cambiar(Node nodo, String nombre){
        try{
            Parent ruta = FXMLLoader.load(Main.class.getResource(nombre+".fxml"));
            Stage stage = (Stage) nodo.getScene().getWindow();

            stage.getScene().setRoot(ruta);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Iniciar sesión");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
