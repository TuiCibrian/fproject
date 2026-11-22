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
import static com.fproject.controlador.IniciarSesionControlador.servidor;

public class CrearCuentaControlador {

    @FXML
    private Button BTNCrear;

    @FXML
    private PasswordField FieldPassword;

    @FXML
    private TextField FieldUsuario;

    @FXML
    private Label IniciarSesion;

    @FXML
    void PressCrear(ActionEvent event) {
        if (servidor == null){
            mostrarError("No Se Encontró El Servidor");
            return;
        }
        //sacar datos de fields
        String usuario = FieldUsuario.getText().trim();
        String contrasena = FieldPassword.getText();

        JugadorPerfil datos = new JugadorPerfil();
        datos.setNombreUsuario(usuario);
        datos.setPasswordHash(contrasena);

        BTNCrear.setDisable(true);

        Thread hiloLogin = new Thread(() -> {
            try {
                RedCliente.conectar(servidor.ip(), servidor.puertoTCP());
                RedCliente.enviar(new Mensaje(Mensaje.aviso.CREAR_CUENTA, datos));
                Mensaje respuesta = RedCliente.recibir();

                Platform.runLater(() -> {
                    BTNCrear.setDisable(false);
                    if(respuesta.getAviso() == Mensaje.aviso.LOGIN_OK){
                        setJugadorActual(respuesta.getJugadorActual());
                        Cambiar(event, "/com/fproject/vista/SalaEspera");
                    }
                    else{
                        mostrarError(respuesta.getMensaje());
                    }
                });
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
