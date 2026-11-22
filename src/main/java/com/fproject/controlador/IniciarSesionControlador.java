package com.fproject.controlador;

import com.fproject.datos.*;
import com.fproject.modelo.JugadorPerfil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import static com.fproject.datos.General.Cambiar;
import static com.fproject.datos.General.setJugadorActual;

import static com.fproject.datos.General.mostrarError;
import static com.fproject.datos.BuscadorServidor.ServidorEncontrado;


public class IniciarSesionControlador {
    //aqui estan los datos del servidor encontrado, es estatico para usarlo en todos los controladores
    public static volatile BuscadorServidor.ServidorEncontrado servidor;

    @FXML
    private Button BTNIngresar;

    @FXML
    private Label CrearCuenta;

    @FXML
    private PasswordField FieldPassword;

    @FXML
    private TextField FieldUsuario;

    @FXML
    void PressCrearCuenta(MouseEvent event) {
        Cambiar(event, "/com/fproject/vista/CrearCuenta");
    }

    public void initialize() {
        RedCliente.setOyente(msg -> procesarMensaje(msg));

        //si ya esta el servidor no necesitamos asignarlo otra vez
        if (servidor != null) {
            return;
        }
        Thread hiloBusqueda = new Thread(() -> {
            servidor = BuscadorServidor.buscar(12346, 2000);

            Platform.runLater(() -> {
                if (servidor != null) {
                    System.out.println("Servidor encontrado en ip: " + servidor.ip() + " y puerto " + servidor.puertoTCP());
                } else {
                    System.out.println("No se encontró el servidor por UDP");
                }
            });
        });
        hiloBusqueda.setDaemon(true);
        hiloBusqueda.start();
    }

    //si la busqueda UDP no ha terminado o falló se intenta otra vez; si tampoco, se usa este equipo
    public static BuscadorServidor.ServidorEncontrado obtenerServidor() {
        if (servidor == null) {
            servidor = BuscadorServidor.buscar(12346, 2000);
        }
        if (servidor == null) {
            return new BuscadorServidor.ServidorEncontrado("localhost", 12345);
        }
        return servidor;
    }

    private void procesarMensaje(Mensaje msg) {
        if (msg.getAviso() == Mensaje.aviso.LOGIN_OK) {
            setJugadorActual(msg.getJugadorActual());
            Cambiar(BTNIngresar, "/com/fproject/vista/SalaEspera");
        } else if (msg.getAviso() == Mensaje.aviso.LOGIN_ERROR || msg.getAviso() == Mensaje.aviso.CONEXION_PERDIDA) {
            BTNIngresar.setDisable(false);
            mostrarError(msg.getMensaje());
        }
    }

    @FXML
    private void PressIngresar(ActionEvent event) {
        //sacar datos de fields
        String usuario = FieldUsuario.getText().trim();
        String contrasena = FieldPassword.getText();

        JugadorPerfil datos = new JugadorPerfil();
        datos.setNombreUsuario(usuario);
        datos.setPasswordHash(contrasena);

        BTNIngresar.setDisable(true);

        //la respuesta del servidor llega a procesarMensaje
        Thread hiloLogin = new Thread(() -> {
            try {
                ServidorEncontrado datosServidor = obtenerServidor();
                RedCliente.conectar(datosServidor.ip(), datosServidor.puertoTCP());
                RedCliente.enviar(new Mensaje(Mensaje.aviso.LOGIN, datos));
            }
            catch(Exception e) {
                Platform.runLater(() -> {
                   BTNIngresar.setDisable(false);
                   mostrarError("Error de Conexión: "+e.getMessage());
                });
            }
        });
        hiloLogin.setDaemon(true);
        hiloLogin.start();
    }


}
