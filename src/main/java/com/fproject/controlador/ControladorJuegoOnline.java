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

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

//el cliente solo muestra el estado y manda la carta elegida; el servidor resuelve la ronda
public class ControladorJuegoOnline {
    private int ganadasYo = 0;
    private int ganadasRival = 0;

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
    private VBox zonaRonda;

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
        General.reproducirMusica("bgm");
        manoJugador.setSpacing(20);
        JugadorPerfil jugador = General.getJugadorActual();
        JugadorPerfil rival = General.getRivalActual();

        aplicarPeleador(imgYo, jugador, false);
        aplicarPeleador(imgRival, rival, true);

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
                revisarGolpe();
                break;
            case AVISO:
                lblEstado.setText(msg.getMensaje());
                break;
            case FIN_PARTIDA:
                General.detenerMusica();
                estado = msg.getEstado();
                puedeElegir = false;
                General.setJugadorActual(msg.getJugadorActual());
                btnRevancha.setDisable(false);
                lblRonda.setText(msg.getMensaje());
                lblEstado.setText("Partida terminada. Pide la revancha o vuelve al lobby.");
                mostrarEstado();
                revisarGolpe();
                String ganador = estado.getGanador();
                if (ganador == null || ganador.equals("Empate")) {
                    return; //no sonar nada
                }
                String yo = General.getJugadorActual().getNombreUsuario();
                General.reproducirEfecto(ganador.equals(yo) ? "victoria" : "derrota");
                break;
            case RIVAL_DESCONECTADO:
                puedeElegir = false;
                btnRevancha.setDisable(true);
                lblEstado.setText(msg.getMensaje() + " Regresa al lobby.");
                mostrarEstado();
                General.detenerMusica();
                break;
            case CONEXION_PERDIDA:
                General.setJugadorActual(null);
                General.setRivalActual(null);
                mostrarError(msg.getMensaje());
                Cambiar(lblEstado, "/com/fproject/vista/IniciarSesion");
                General.detenerMusica();
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
        General.detenerMusica();
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
            Button boton = new Button();
            boton.setGraphic(imagenCarta(mano.get(i), 60));
            boton.setStyle(estiloCarta(mano.get(i)));
            boton.setOnAction(event -> seleccionarCarta(indice));
            boton.setDisable(!puedeElegir);
            boton.getStyleClass().add("cartaReal");
            manoJugador.getChildren().add(boton);
        }

        cartasYo.getChildren().clear();
        cartasYo.getChildren().add(describirGanadas(estado.getCartasGanadasJugador1()));
        cartasRival.getChildren().clear();
        cartasRival.getChildren().add(describirGanadas(estado.getCartasGanadasJugador2()));
        mostrarRonda();
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
        cartita.setPadding(new Insets(0,5,0,5));
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

    private ImageView imagenCarta(Carta carta, double ancho) {
        String ruta = "/com/fproject/vista/images/Cartas/" + carta.getValor() + carta.getElemento() + ".png";
        if (getClass().getResource(ruta) == null) {
            ruta = "/com/fproject/vista/images/CartaFront.png";
        }
        ImageView imagen = new ImageView(new Image(ruta));
        imagen.setFitWidth(ancho);
        imagen.setPreserveRatio(true);
        return imagen;
    }

    private String estiloCarta(Carta carta) {
        String color = switch (carta.getColor()) {
            case "Rojo" -> "#ff5252";
            case "Azul" -> "#52b7ff";
            case "Verde" -> "#77ff52";
            default -> "#ffd54f";
        };
        return "-fx-background-color: transparent; -fx-padding: 0; -fx-cursor: hand; "
                + "-fx-border-color: " + color + "; -fx-border-width: 4; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 10, 0.2, 4, 4);";
    }

    private String peleadorDe(JugadorPerfil jugador) {
        if (jugador == null || jugador.getPeleador() == null) return "Box";
        return switch (jugador.getPeleador()) {
            case "Box", "Karate", "Sumo" -> jugador.getPeleador();
            default -> "Box";
        };
    }

    //metodo simple para decidir tamaño si es que es sumo
    private void aplicarPeleador(ImageView img, JugadorPerfil jugador, boolean esRival) {
        String peleador = peleadorDe(jugador);
        //ajustar tamaño o direccion...
        int tam = peleador.equals("Sumo") ? 290 : 180;

        if (peleador.equals("Karate")) {
            esRival = !esRival;
        }

        img.setScaleX(esRival ? -1.0 : 1.0);
        img.setFitWidth(tam);
        img.setFitHeight(tam);
        img.setImage(new Image("/com/fproject/vista/images/" + peleador + "1.png"));
    }

    //cartas para mostrar quien gano
    private void mostrarRonda() {
        zonaRonda.getChildren().clear();
        if (estado == null || estado.getUltimaCartaJugador1() == null) {
            zonaRonda.setVisible(false);
            zonaRonda.setManaged(false);
            return;
        }
        zonaRonda.getChildren().addAll(
                cartaRonda("Rival", estado.getUltimaCartaJugador2()),
                cartaRonda("Tú", estado.getUltimaCartaJugador1()));
        zonaRonda.setVisible(true);
        zonaRonda.setManaged(true);
    }

    private VBox cartaRonda(String titulo, Carta carta) {
        Label nombre = new Label(titulo);
        nombre.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label cuerpo = new Label();
        cuerpo.setGraphic(imagenCarta(carta, 70));
        cuerpo.setStyle(estiloCarta(carta));

        VBox caja = new VBox(4, nombre, cuerpo);
        caja.setAlignment(Pos.CENTER);
        return caja;
    }

    //animaciones, ayuda pls


    //esto es para ver quien golpea a quien (quien gano la ronda)
    private void revisarGolpe() {
        int ganadasNewYo = estado.getCartasGanadasJugador1().size();
        int ganadasNewRival = estado.getCartasGanadasJugador2().size();

        if (ganadasNewYo > ganadasYo) {
            Golpe(imgYo, General.getJugadorActual());
        } else if (ganadasNewRival > ganadasRival) {
            Golpe(imgRival, General.getRivalActual());
        }

        ganadasYo = ganadasNewYo;
        ganadasRival = ganadasNewRival;
    }

    private void Golpe(ImageView img, JugadorPerfil jugador) {
        int total;

        if (jugador == null || jugador.getPeleador() == null) {
            return;
        }

        String peleador = peleadorDe(jugador);

        //si es sumo tiene 2 fotogramas, si no es pues tiene 3
        if (peleador.equals("Sumo")) {
            total = 2;
        } else {
            total = 3;
        }

        //creamos la animacion y su temporizador
        Timeline animacion = new Timeline();
        int duracion = 0;

        //pasa por los fotogramas
        for (int i = 1; i <= total; i++) {
            String ruta = "/com/fproject/vista/images/" + peleador + i + ".png";
            animacion.getKeyFrames().add(new KeyFrame(Duration.millis(duracion), e -> {
                img.setImage(new Image(ruta));
            }));
            duracion += 150;
        }

        General.reproducirEfecto("golpe");

        //regresar a pose inicial
        animacion.getKeyFrames().add(new KeyFrame(Duration.millis(duracion), e -> {
            img.setImage(new Image("/com/fproject/vista/images/" + peleador + "1.png"));
        }));

        animacion.play();
    }
}
