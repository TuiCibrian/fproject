package com.fproject.controlador;

import java.io.IOException;
import java.util.ArrayList;

import com.fproject.datos.General;
import com.fproject.datos.Mensaje;
import com.fproject.datos.RedCliente;
import com.fproject.modelo.Carta;
import com.fproject.modelo.EstadoJuego;
import com.fproject.modelo.JugadorPerfil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import static com.fproject.datos.General.Cambiar;
import static com.fproject.datos.General.mostrarError;

//el cliente solo muestra el estado y manda la carta elegida; el servidor resuelve la ronda
public class ControladorJuegoOnline {
    @FXML
    private Label lblJugador;

    @FXML
    private Label lblRival;

    @FXML
    private Label lblCartasGanadas;

    @FXML
    private Label lblCartasGanadasRival;

    @FXML
    private HBox manoJugador;

    //resultado de la ultima ronda; lblEstado es lo que hay que hacer ahora
    @FXML
    private Label lblRonda;

    @FXML
    private Label lblEstado;

    @FXML
    private Button btnRevancha;

    //en el estado que manda el servidor, jugador1 siempre es este cliente
    private EstadoJuego estado;
    private boolean puedeElegir;

    @FXML
    public void initialize() {
        JugadorPerfil jugador = General.getJugadorActual();
        JugadorPerfil rival = General.getRivalActual();

        if (jugador != null) {
            lblJugador.setText(jugador.getNombreUsuario() + " (tú)");
        }
        if (rival != null) {
            lblRival.setText(rival.getNombreUsuario() + " - " + rival.getRango());
        }

        RedCliente.setOyente(msg -> procesarMensaje(msg));
    }

    private void procesarMensaje(Mensaje msg) {
        switch (msg.getAviso()) {
            case ESTADO_JUEGO:
                estado = msg.getEstado();
                puedeElegir = true;
                btnRevancha.setDisable(true);
                lblRonda.setText(msg.getMensaje());
                lblEstado.setText("Elige una carta.");
                mostrarEstado();
                break;
            case AVISO:
                lblEstado.setText(msg.getMensaje());
                break;
            case FIN_PARTIDA:
                estado = msg.getEstado();
                puedeElegir = false;
                General.setJugadorActual(msg.getJugadorActual());
                btnRevancha.setDisable(false);
                lblRonda.setText(msg.getMensaje());
                lblEstado.setText("Partida terminada. Pide la revancha o vuelve al lobby.");
                mostrarEstado();
                break;
            case RIVAL_DESCONECTADO:
                puedeElegir = false;
                btnRevancha.setDisable(true);
                lblEstado.setText(msg.getMensaje() + " Regresa al lobby.");
                mostrarEstado();
                break;
            case CONEXION_PERDIDA:
                General.setJugadorActual(null);
                General.setRivalActual(null);
                mostrarError(msg.getMensaje());
                Cambiar(lblEstado, "/com/fproject/vista/IniciarSesion");
                break;
            default:
                break;
        }
    }

    private void seleccionarCarta(int indice) {
        if (!puedeElegir) {
            return;
        }

        //solo se manda el indice; la carta del rival no se conoce hasta la resolucion
        puedeElegir = false;
        lblEstado.setText("Elegiste " + describirCarta(estado.getManoJugador1().get(indice)) + ". Esperando...");
        mostrarEstado();
        enviarAlServidor(new Mensaje(Mensaje.aviso.SELECCIONAR_CARTA, indice));
    }

    @FXML
    private void pedirRevancha(ActionEvent event) {
        btnRevancha.setDisable(true);
        enviarAlServidor(new Mensaje(Mensaje.aviso.REVANCHA, "Revancha"));
    }

    @FXML
    private void volverLobby(ActionEvent event) {
        enviarAlServidor(new Mensaje(Mensaje.aviso.SALIR_PARTIDA, "Salir"));
        General.setRivalActual(null);
        Cambiar(event, "/com/fproject/vista/SalaEspera");
    }

    private void enviarAlServidor(Mensaje msg) {
        try {
            RedCliente.enviar(msg);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo comunicar con el servidor: " + e.getMessage());
        }
    }

    private void mostrarEstado() {
        if (estado == null) {
            return;
        }

        manoJugador.getChildren().clear();
        ArrayList<Carta> mano = estado.getManoJugador1();

        for (int i = 0; i < mano.size(); i++) {
            int indice = i;
            Button boton = new Button(mano.get(i).getElemento() + "\n" + mano.get(i).getValor() + "\n" + mano.get(i).getColor());

            boton.setPrefWidth(100);
            boton.setPrefHeight(80);
            boton.setWrapText(true);
            boton.setDisable(!puedeElegir);
            boton.setOnAction(event -> seleccionarCarta(indice));
            manoJugador.getChildren().add(boton);
        }

        lblCartasGanadas.setText(describirGanadas(estado.getCartasGanadasJugador1()));
        lblCartasGanadasRival.setText(describirGanadas(estado.getCartasGanadasJugador2()));
    }

    private String describirGanadas(ArrayList<Carta> cartas) {
        String texto = "Cartas ganadas: " + cartas.size();
        for (Carta carta : cartas) {
            texto += "\n" + describirCarta(carta);
        }
        return texto;
    }

    private String describirCarta(Carta carta) {
        return carta.getElemento() + " " + carta.getValor() + " " + carta.getColor();
    }
}
