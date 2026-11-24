package com.fproject.datos;

import com.fproject.Main;
import com.fproject.modelo.JugadorPerfil;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.IOException;

public class General {

    private static JugadorPerfil jugadorActual;
    private static JugadorPerfil rivalActual;
    private static MediaPlayer bgmPlayer;

    public General() {
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

    public static void Cambiar(Event evento, String nombre) {
        Cambiar((Node) evento.getSource(), nombre);
    }

    public static void Cambiar(Node nodo, String nombre) {
        try {
            Parent ruta = FXMLLoader.load(Main.class.getResource(nombre + ".fxml"));
            Stage stage = (Stage) nodo.getScene().getWindow();
            stage.getScene().setRoot(ruta);
        } catch (IOException e) {
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

    public static void reproducirMusica(String nombre) {
        detenerMusica();
        Media bgm = new Media(General.class.getResource("/com/fproject/audio/" + nombre + ".wav").toExternalForm());
        bgmPlayer = new MediaPlayer(bgm);
        bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        bgmPlayer.play();
    }

    public static void detenerMusica() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
    }

    public static void reproducirEfecto(String nombre) {
        AudioClip sfx = new AudioClip(General.class.getResource("/com/fproject/audio/" + nombre + ".wav").toExternalForm());
        sfx.play();
    }
}
