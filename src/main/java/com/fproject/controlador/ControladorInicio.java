package com.fproject.controlador;

import java.util.ArrayList;

import com.fproject.modelo.Carta;
import com.fproject.modelo.EstadoJuego;
import com.fproject.modelo.ReglasJuego;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ControladorInicio {
    @FXML
    private TextField txtJugador1;

    @FXML
    private TextField txtJugador2;

    @FXML
    private HBox manoJugador1;

    @FXML
    private HBox manoJugador2;

    @FXML
    private Label lblCartasGanadasJugador1;

    @FXML
    private Label lblCartasGanadasJugador2;

    @FXML
    private Label lblEstado;

    @FXML
    private Button btnIniciar;

    private ReglasJuego reglas = new ReglasJuego();
    private EstadoJuego estado;
    private String nombreJugador1;
    private String nombreJugador2;
    private int turnoSeleccion = 1;
    private int indiceCartaJugador1 = -1;
    private int indiceCartaJugador2 = -1;
    private boolean partidaTerminada;

    @FXML
    public void initialize() {
        lblEstado.setText("Escribe los nombres y presiona Iniciar partida.");
    }

    @FXML
    private void iniciarPartida(ActionEvent event) {
        if (txtJugador1.getText().isBlank() || txtJugador2.getText().isBlank()) {
            lblEstado.setText("Escribe el nombre de los dos jugadores.");
            return;
        }

        nombreJugador1 = txtJugador1.getText().trim();
        nombreJugador2 = txtJugador2.getText().trim();
        estado = reglas.iniciarPartida();
        turnoSeleccion = 1;
        indiceCartaJugador1 = -1;
        indiceCartaJugador2 = -1;
        partidaTerminada = false;

        txtJugador1.setDisable(true);
        txtJugador2.setDisable(true);
        btnIniciar.setDisable(true);
        btnIniciar.setText("Partida en curso");

        actualizarCartasGanadas();
        lblEstado.setText("Turno de " + nombreJugador1 + ". Elige una carta.");
        mostrarManos();
    }

    private void seleccionarCarta(int jugador, int indice) {
        if (partidaTerminada || jugador != turnoSeleccion) {
            return;
        }

        if (jugador == ReglasJuego.JUGADOR1) {
            indiceCartaJugador1 = indice;
            turnoSeleccion = ReglasJuego.JUGADOR2;
            lblEstado.setText(nombreJugador1 + " eligió. Pasa el turno a " + nombreJugador2 + ".");
            mostrarManos();
            return;
        }

        indiceCartaJugador2 = indice;
        resolverRonda();
    }

    private void resolverRonda() {
        Carta cartaJugador1 = estado.getManoJugador1().get(indiceCartaJugador1);
        Carta cartaJugador2 = estado.getManoJugador2().get(indiceCartaJugador2);
        int resultado = reglas.resolverRonda(cartaJugador1, cartaJugador2);
        String textoResultado = nombreJugador1 + " jugó " + describirCarta(cartaJugador1)
                + ". " + nombreJugador2 + " jugó " + describirCarta(cartaJugador2) + ". ";

        if (resultado == ReglasJuego.JUGADOR1) {
            estado.getCartasGanadasJugador1().add(cartaJugador1);
            textoResultado += nombreJugador1 + " ganó la ronda.";
        } else if (resultado == ReglasJuego.JUGADOR2) {
            estado.getCartasGanadasJugador2().add(cartaJugador2);
            textoResultado += nombreJugador2 + " ganó la ronda.";
        } else {
            textoResultado += "La ronda terminó en empate.";
        }

        estado.getManoJugador1().remove(indiceCartaJugador1);
        estado.getManoJugador2().remove(indiceCartaJugador2);
        indiceCartaJugador1 = -1;
        indiceCartaJugador2 = -1;
        actualizarCartasGanadas();

        if (reglas.tieneVictoria(estado.getCartasGanadasJugador1())) {
            terminarPartida(nombreJugador1, textoResultado);
            return;
        }
        if (reglas.tieneVictoria(estado.getCartasGanadasJugador2())) {
            terminarPartida(nombreJugador2, textoResultado);
            return;
        }

        reglas.reponerCarta(estado.getManoJugador1(), estado.getMazo());
        reglas.reponerCarta(estado.getManoJugador2(), estado.getMazo());

        if (estado.getMazo().isEmpty()
                && estado.getManoJugador1().isEmpty()
                && estado.getManoJugador2().isEmpty()) {
            terminarPartida("Empate", textoResultado + " No quedan cartas.");
            return;
        }

        turnoSeleccion = ReglasJuego.JUGADOR1;
        if (estado.getMazo().isEmpty()) {
            lblEstado.setText(textoResultado + " El mazo se agotó; jueguen las cartas restantes.");
        } else {
            lblEstado.setText(textoResultado + " Turno de " + nombreJugador1 + ".");
        }
        mostrarManos();
    }

    private void terminarPartida(String ganador, String resultado) {
        partidaTerminada = true;
        estado.setGanador(ganador);

        if (ganador.equals("Empate")) {
            lblEstado.setText(resultado);
        } else {
            lblEstado.setText(resultado + " " + ganador + " ganó la partida.");
        }

        txtJugador1.setDisable(false);
        txtJugador2.setDisable(false);
        btnIniciar.setDisable(false);
        btnIniciar.setText("Nueva partida");
        mostrarManos();
    }

    private void mostrarManos() {
        mostrarMano(estado.getManoJugador1(), manoJugador1, ReglasJuego.JUGADOR1);
        mostrarMano(estado.getManoJugador2(), manoJugador2, ReglasJuego.JUGADOR2);
    }

    private void mostrarMano(ArrayList<Carta> mano, HBox contenedor, int jugador) {
        contenedor.getChildren().clear();

        for (int i = 0; i < mano.size(); i++) {
            int indice = i;
            Carta carta = mano.get(i);
            Button boton = new Button(describirCarta(carta));

            boton.setPrefWidth(100);
            boton.setPrefHeight(80);
            boton.setWrapText(true);
            boton.setDisable(partidaTerminada || jugador != turnoSeleccion);
            boton.setOnAction(event -> seleccionarCarta(jugador, indice));
            contenedor.getChildren().add(boton);
        }
    }

    private void actualizarCartasGanadas() {
        lblCartasGanadasJugador1.setText(
                "Cartas ganadas: " + estado.getCartasGanadasJugador1().size());
        lblCartasGanadasJugador2.setText(
                "Cartas ganadas: " + estado.getCartasGanadasJugador2().size());
    }

    private String describirCarta(Carta carta) {
        return carta.getElemento() + "\n" + carta.getValor() + "\n" + carta.getColor();
    }
}
