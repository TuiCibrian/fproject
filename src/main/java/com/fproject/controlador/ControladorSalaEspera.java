package com.fproject.controlador;

import java.util.ArrayList;

import com.fproject.datos.General;
import com.fproject.datos.Mensaje;
import com.fproject.datos.RedCliente;
import com.fproject.modelo.HistorialPartida;
import com.fproject.modelo.JugadorPerfil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import static com.fproject.datos.General.Cambiar;
import static com.fproject.controlador.IniciarSesionControlador.servidor;
import static com.fproject.datos.General.mostrarError;


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
                new ReadOnlyStringWrapper(c.getValue().getFecha() == null ? "" : c.getValue().getFecha()));

        Thread hiloHistorial = new Thread(() -> {
            try {
                RedCliente.enviar(new Mensaje(Mensaje.aviso.HISTORIAL, jugador));
                Mensaje respuesta = RedCliente.recibir();

                Platform.runLater(() -> {
                    if (respuesta != null && respuesta.getAviso() == Mensaje.aviso.HISTORIAL_OK) {
                        tablaHistorial.setItems(FXCollections.observableArrayList(respuesta.getHistorial()));
                    } else {
                        General.mostrarError(respuesta.getMensaje());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        hiloHistorial.setDaemon(true);
        hiloHistorial.start();
    }

    @FXML
    private void jugarOnline(ActionEvent event) {
        JugadorPerfil jugador = General.getJugadorActual();
        if (jugador == null) {
            General.mostrarError("Inicia Sesion Para Jugar Online");
            return;
        }

        final boolean[] buscando = {true};

        Alert alertaBusqueda = new Alert(Alert.AlertType.INFORMATION);
        alertaBusqueda.setTitle("Emparejamiento");
        alertaBusqueda.setHeaderText(null);
        alertaBusqueda.setContentText("Buscando Rival...");
        alertaBusqueda.getButtonTypes().setAll(ButtonType.CANCEL);
        alertaBusqueda.show();

        //al presionar boton cancelar le decimos al servidor que cancele todo
        alertaBusqueda.setOnCloseRequest(e -> {
            if (buscando[0]) {
                buscando[0] = false;
                Thread hiloCancelar = new Thread(() -> {
                    try {
                        RedCliente.enviar(new Mensaje(Mensaje.aviso.CANCELAR_BUSQUEDA, jugador));
                    } catch (Exception e2) {
                        mostrarError(e2.getMessage());
                    }
                });
                hiloCancelar.setDaemon(true);
                hiloCancelar.start();
            }
        });

        Thread hiloEmparejamiento = new Thread(() -> {
            try {
                //le dice al servidor que quiere buscar partida
                RedCliente.enviar(new Mensaje(Mensaje.aviso.BUSCAR_PARTIDA, jugador));

                //mientras sigamos buscando se ejecuta la espera
                while (buscando[0]) {
                    Mensaje respuesta = RedCliente.recibir();

                    //si recibimos respuesta nula o no estamos buscando, salimos
                    if(respuesta == null || !buscando[0]){
                        break;
                    }

                    //si el servidor cancela la busqueda salimos
                    if(respuesta.getAviso()==Mensaje.aviso.BUSQUEDA_CANCELADA){
                        break;
                    }

                    //si el servidor encontro partida dejamos de buscar partida y vamos a la pelea
                    if (respuesta.getAviso() == Mensaje.aviso.PARTIDA_OK) {
                        JugadorPerfil rival = respuesta.getJugadorActual();
                        Platform.runLater(() -> {
                            if(!buscando[0]){
                                alertaBusqueda.close();
                                return;
                            }
                            buscando[0] = false;
                            alertaBusqueda.close();
                            System.out.println("Partida Encontrada Contra: " + rival.getNombreUsuario());
                            Cambiar(event, "/com/fproject/vista/Inicio");
                        });
                        break;
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    alertaBusqueda.close();
                    if (buscando[0]) {
                        General.mostrarError("Error En Emparejamiento: " + e.getMessage());
                    }
                });
            }
        });

        hiloEmparejamiento.setDaemon(true);
        hiloEmparejamiento.start();
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
