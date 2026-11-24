package com.fproject.controlador;

import java.io.IOException;

import com.fproject.datos.General;
import com.fproject.datos.Mensaje;
import com.fproject.datos.RedCliente;
import com.fproject.modelo.HistorialPartida;
import com.fproject.modelo.JugadorPerfil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import static com.fproject.datos.General.Cambiar;
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

    private Alert alertaBusqueda;
    private boolean buscando;

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

        //las respuestas del servidor llegan a procesarMensaje
        RedCliente.setOyente(msg -> procesarMensaje(msg));
        enviarAlServidor(new Mensaje(Mensaje.aviso.HISTORIAL, jugador));
    }

    private void procesarMensaje(Mensaje msg) {
        switch (msg.getAviso()) {
            case HISTORIAL_OK:
                tablaHistorial.setItems(FXCollections.observableArrayList(msg.getHistorial()));
                break;
            case PARTIDA_OK:
                //el servidor ya creó la sala, asi que se entra aunque se haya presionado cancelar
                cerrarBusqueda();
                General.setRivalActual(msg.getJugadorActual());
                System.out.println("Partida Encontrada Contra: " + msg.getJugadorActual().getNombreUsuario());
                Cambiar(lblBienvenida, "/com/fproject/vista/JuegoOnline");
                break;
            case BUSQUEDA_CANCELADA:
                cerrarBusqueda();
                break;
            case CONEXION_PERDIDA:
                cerrarBusqueda();
                General.setJugadorActual(null);
                mostrarError(msg.getMensaje());
                Cambiar(lblBienvenida, "/com/fproject/vista/IniciarSesion");
                break;
            case LOGIN_ERROR, HISTORIAL_ERROR, CAMBIAR_ERROR:
                mostrarError(msg.getMensaje());
                break;
            case CAMBIAR_OK:
                General.getJugadorActual().setPeleador(msg.getJugadorActual().getPeleador());
                break;
            default:
                break;
        }
    }

    @FXML
    private void jugarOnline(ActionEvent event) {
        JugadorPerfil jugador = General.getJugadorActual();
        if (jugador == null) {
            General.mostrarError("Inicia Sesion Para Jugar Online");
            return;
        }
        if (buscando) {
            return;
        }
        buscando = true;

        alertaBusqueda = new Alert(Alert.AlertType.INFORMATION);
        alertaBusqueda.setTitle("Emparejamiento");
        alertaBusqueda.setHeaderText(null);
        alertaBusqueda.setContentText("Buscando Rival...");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(40, 40);
        alertaBusqueda.setGraphic(progressIndicator);
        alertaBusqueda.getButtonTypes().setAll(ButtonType.CANCEL);

        //si el usuario cierra la alerta mientras se busca, le decimos al servidor que cancele
        alertaBusqueda.setOnHidden(e -> {
            if (buscando) {
                buscando = false;
                enviarAlServidor(new Mensaje(Mensaje.aviso.CANCELAR_BUSQUEDA, jugador));
            }
        });
        alertaBusqueda.show();

        enviarAlServidor(new Mensaje(Mensaje.aviso.BUSCAR_PARTIDA, jugador));
    }

    private void cerrarBusqueda() {
        buscando = false;
        if (alertaBusqueda != null) {
            alertaBusqueda.close();
        }
    }

    public static void enviarAlServidor(Mensaje msg) {
        try {
            RedCliente.enviar(msg);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo comunicar con el servidor: " + e.getMessage());
        }
    }

    @FXML
    private void jugarLocal(ActionEvent event) {
        //el juego local no usa la red
        RedCliente.setOyente(null);
        Cambiar(event, "/com/fproject/vista/inicio");
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        //al cerrar el socket el servidor lo saca de la busqueda
        RedCliente.setOyente(null);
        RedCliente.cerrar();
        General.setJugadorActual(null);
        General.setRivalActual(null);
        Cambiar(event, "/com/fproject/vista/IniciarSesion");
    }

    private String rival(HistorialPartida partida, String usuario) {
        if (partida.getJugador1() != null && partida.getJugador1().equalsIgnoreCase(usuario)) {
            return partida.getJugador2();
        }
        return partida.getJugador1();
    }

    @FXML
    private void PressCambiarPeleador(ActionEvent event){
        Cambiar(event, "/com/fproject/vista/Seleccion");
    }


}
