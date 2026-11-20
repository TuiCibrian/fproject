package com.fproject.controlador;

import com.fproject.datos.ManejadorDB4O;
import com.fproject.datos.Seguridad;
import com.fproject.modelo.JugadorPerfil;
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

public class IniciarSesionControlador {

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
        Cambiar(event,"/com/fproject/vista/CrearCuenta");
    }

    @FXML
    void PressIngresar(ActionEvent event) {
        String usuario = FieldUsuario.getText().trim();
        String contrasena = FieldPassword.getText();

        if (usuario.isBlank() || contrasena.isBlank()) {
            mostrarError("Escribe usuario y contraseña.");
            return;
        }

        try {
            ManejadorDB4O db = ManejadorDB4O.getInstancia();
            JugadorPerfil jugador = db.buscarJugador(usuario);

            if (jugador == null) {
                mostrarError("El usuario no existe. Crea una cuenta.");
                return;
            }

            if (jugador.getPasswordHash() == null
                    || !jugador.getPasswordHash().equals(Seguridad.hashSHA256(contrasena))) {
                mostrarError("Contraseña incorrecta.");
                return;
            }

            setJugadorActual(jugador);
            Cambiar(event, "/com/fproject/vista/SalaEspera");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir la base de datos.");
        }
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Iniciar sesión");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
