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
    public static BuscadorServidor.ServidorEncontrado servidor;

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

    @FXML
    private void PressIngresar(ActionEvent event) {
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

        BTNIngresar.setDisable(true);

        Thread hiloLogin = new Thread(() -> {
            try {
                RedCliente.conectar(servidor.ip(), servidor.puertoTCP());
                RedCliente.enviar(new Mensaje(Mensaje.aviso.LOGIN, datos));
                Mensaje respuesta = RedCliente.recibir();

                Platform.runLater(() -> {
                    BTNIngresar.setDisable(false);
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
                   BTNIngresar.setDisable(false);
                   mostrarError("Error de Conexión: "+e.getMessage());
                });
            }
        });
        hiloLogin.setDaemon(true);
        hiloLogin.start();
    }


}
