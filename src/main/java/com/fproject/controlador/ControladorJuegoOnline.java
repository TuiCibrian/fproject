package com.fproject.controlador;

import java.io.IOException;
import java.util.ArrayList;

import com.fproject.datos.General;
import com.fproject.datos.Mensaje;
import com.fproject.datos.RedCliente;
import com.fproject.modelo.Carta;
import com.fproject.modelo.EstadoJuego;
import com.fproject.modelo.JugadorPerfil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static com.fproject.datos.General.Cambiar;
import static com.fproject.datos.General.mostrarError;

//el cliente solo muestra el estado y manda la carta elegida; el servidor resuelve la ronda
public class ControladorJuegoOnline {
    @FXML
    private Button btnRevancha;

    @FXML
    private HBox cartasRival;

    @FXML
    private HBox cartasYo;

    @FXML
    private ImageView imgRival;

    @FXML
    private ImageView imgYo;

    @FXML
    private Label lblCartasGanadas;

    @FXML
    private Label lblCartasGanadasRival;

    @FXML
    private Label lblEstado;

    @FXML
    private Label lblJugador;

    @FXML
    private Label lblRival;

    @FXML
    private Label lblRonda;

    @FXML
    private HBox manoJugador;

    //en el estado que manda el servidor, jugador1 siempre es este cliente
    private EstadoJuego estado;
    private boolean puedeElegir;

    @FXML
    public void initialize() {
        JugadorPerfil jugador = General.getJugadorActual();
        JugadorPerfil rival = General.getRivalActual();

        if (jugador != null) {
            lblJugador.setText(jugador.getNombreUsuario() + " (tú)");
            if (jugador.getPeleador() != null) {
                imgYo.setImage(new Image("/com/fproject/vista/images/" + jugador.getPeleador() + "1.png"));
                if ("Sumo".equals(jugador.getPeleador())) {
                    imgYo.setFitWidth(290);
                    imgYo.setFitHeight(290);
                } else {
                    imgYo.setFitWidth(180);
                    imgYo.setFitHeight(180);
                }
            }
        }

        if (rival != null) {
            lblRival.setText(rival.getNombreUsuario() + " - " + rival.getRango());
            if (rival.getPeleador() != null) {
                imgRival.setImage(new Image("/com/fproject/vista/images/" + rival.getPeleador() + "1.png"));
                if ("Sumo".equals(rival.getPeleador())) {
                    imgRival.setFitWidth(290);
                    imgRival.setFitHeight(290);
                } else {
                    imgRival.setFitWidth(180);
                    imgRival.setFitHeight(180);
                }
            }
        }

        RedCliente.setOyente(msg -> Platform.runLater(() -> procesarMensaje(msg)));
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

            boton.setOnAction(event -> seleccionarCarta(indice));
            boton.setPrefWidth(105);
            boton.setPrefHeight(92);
            boton.setWrapText(true);
            boton.setStyle(estiloCarta(mano.get(i)));
            boton.setDisable(!puedeElegir);
            manoJugador.getChildren().add(boton);
        }

        cartasYo.getChildren().clear();
        cartasYo.getChildren().add(describirGanadas(estado.getCartasGanadasJugador1()));
        cartasRival.getChildren().clear();
        cartasRival.getChildren().add(describirGanadas(estado.getCartasGanadasJugador2()));
    }

    private HBox describirGanadas(ArrayList<Carta> cartas) {
        HBox contenedor = new HBox();
        contenedor.setSpacing(10);
        for (Carta carta : cartas) {
            contenedor.getChildren().add(describirCarta(carta));
        }
        return contenedor;
    }

    private VBox describirCarta(Carta carta) {
        VBox cartita = new VBox();
        cartita.setAlignment(Pos.CENTER);
        cartita.setStyle("-fx-background-radius: 10px;");
        switch (carta.getColor()) {
            case "Rojo":
                cartita.setStyle("-fx-background-color: #ff5252;");
                break;
            case "Azul":
                cartita.setStyle("-fx-background-color: #52b7ff;");
                break;
            case "Verde":
                cartita.setStyle("-fx-background-color: #77ff52;");
                break;
        }
        ImageView imagen = new ImageView("/com/fproject/vista/images/" + carta.getElemento() + ".png");
        imagen.setFitWidth(25);
        imagen.setFitHeight(25);
        cartita.getChildren().add(imagen);
        return cartita;
    }

    private String estiloCarta(Carta carta) {
        String elemento = carta.getElemento() == null ? "" : carta.getElemento().toLowerCase();
        String borde = switch (elemento) {
            case "fuego" -> "#ff5252";
            case "agua" -> "#40c4ff";
            case "hielo" -> "#e0f7fa";
            default -> "#ffd54f";
        };
        return "-fx-background-color: #232026; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 10; -fx-border-color: " + borde + "; "
                + "-fx-border-width: 2.5; -fx-border-radius: 10; -fx-cursor: hand;";
    }
}
