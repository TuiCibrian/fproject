package com.fproject.controlador;

import java.util.ArrayList;

import com.fproject.datos.General;
import com.fproject.datos.ManejadorDB4O;
import com.fproject.modelo.HistorialPartida;
import com.fproject.modelo.JugadorPerfil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import static com.fproject.datos.General.Cambiar;

public class ControladorSalaEspera {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblEstadisticas;

    @FXML
    private TableView<HistorialPartida> tablaHistorial;

    @FXML
    private TableColumn<HistorialPartida, String> colRival;

    @FXML
    private TableColumn<HistorialPartida, String> colGanador;

    @FXML
    private TableColumn<HistorialPartida, String> colFecha;

    @FXML
    public void initialize() {
        JugadorPerfil jugador = General.getJugadorActual();

        if (jugador == null) {
            lblBienvenida.setText("Inicia sesión para jugar.");
            return;
        }

        lblBienvenida.setText("Bienvenido, " + jugador.getNombreUsuario());
        lblEstadisticas.setText(
                "Ganadas: " + jugador.getPartidasGanadas()
                + " | Perdidas: " + jugador.getPartidasPerdidas()
                + " | Puntos: " + jugador.getPuntos()
                + " | Rango: " + jugador.getRango());

        colRival.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(rival(c.getValue(), jugador.getNombreUsuario())));
        colGanador.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getGanador()));
        colFecha.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(
                        c.getValue().getFecha() == null ? "" : c.getValue().getFecha()));

        try {
            ArrayList<HistorialPartida> historial = ManejadorDB4O.getInstancia()
                    .obtenerHistorialJugador(jugador.getNombreUsuario());
            tablaHistorial.setItems(FXCollections.observableArrayList(historial));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void jugarLocal(ActionEvent event) {
        Cambiar(event, "/com/fproject/vista/inicio");
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        General.setJugadorActual(null);
        Cambiar(event, "/com/fproject/vista/IniciarSesion");
    }

    private String rival(HistorialPartida partida, String usuario) {
        if (partida.getJugador1() != null && partida.getJugador1().equalsIgnoreCase(usuario)) {
            return partida.getJugador2();
        }
        return partida.getJugador1();
    }
}
