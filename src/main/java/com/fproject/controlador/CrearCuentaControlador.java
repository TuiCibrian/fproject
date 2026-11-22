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

import static com.fproject.datos.General.*;
import static com.fproject.controlador.IniciarSesionControlador.obtenerServidor;

public class CrearCuentaControlador {

    @FXML
    private Button BTNCrear;

    @FXML
    private PasswordField FieldPassword;

    @FXML
    private TextField FieldUsuario;

    @FXML
    private Label IniciarSesion;

    public void initialize() {
        RedCliente.setOyente(msg -> procesarMensaje(msg));
    }

    private void procesarMensaje(Mensaje msg) {
        if (msg.getAviso() == Mensaje.aviso.LOGIN_OK) {
            setJugadorActual(msg.getJugadorActual());
            Cambiar(BTNCrear, "/com/fproject/vista/SalaEspera");
        } else if (msg.getAviso() == Mensaje.aviso.LOGIN_ERROR || msg.getAviso() == Mensaje.aviso.CONEXION_PERDIDA) {
            BTNCrear.setDisable(false);
            mostrarError(msg.getMensaje());
        }
    }

    @FXML
    void PressCrear(ActionEvent event) {
        //sacar datos de fields
        String usuario = FieldUsuario.getText().trim();
        String contrasena = FieldPassword.getText();

        JugadorPerfil datos = new JugadorPerfil();
        datos.setNombreUsuario(usuario);
        datos.setPasswordHash(contrasena);

        BTNCrear.setDisable(true);

        //la respuesta del servidor llega a procesarMensaje
        Thread hiloLogin = new Thread(() -> {
            try {
                BuscadorServidor.ServidorEncontrado datosServidor = obtenerServidor();
                RedCliente.conectar(datosServidor.ip(), datosServidor.puertoTCP());
                RedCliente.enviar(new Mensaje(Mensaje.aviso.CREAR_CUENTA, datos));
            }
            catch(Exception e) {
                Platform.runLater(() -> {
                    BTNCrear.setDisable(false);
                    mostrarError("Error de Conexión: "+e.getMessage());
                });
            }
        });
        hiloLogin.setDaemon(true);
        hiloLogin.start();
    }

    @FXML
    void PressIniciarSesion(MouseEvent event) {
        Cambiar(event,"/com/fproject/vista/IniciarSesion");
    }

}
