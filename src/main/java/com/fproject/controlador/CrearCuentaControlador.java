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
        String usuario = FieldUsuario.getText().trim();
        String contrasena = FieldPassword.getText();

        if (usuario.isBlank() || contrasena.isBlank()) {
            mostrarError("Escribe usuario y contraseña.");
            return;
        }

        if (contrasena.length() < 4) {
            mostrarError("La contraseña debe tener al menos 4 caracteres.");
            return;
        }

        try {
            ManejadorDB4O db = ManejadorDB4O.getInstancia();

            if (db.buscarJugador(usuario) != null) {
                mostrarError("Ese usuario ya existe.");
                return;
            }

            JugadorPerfil jugador = new JugadorPerfil(usuario);
            jugador.setPasswordHash(Seguridad.hashSHA256(contrasena));
            db.guardarJugador(jugador);

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Crear cuenta");
            alerta.setHeaderText(null);
            alerta.setContentText("Cuenta creada. Ahora inicia sesión.");
            alerta.showAndWait();

            Cambiar(event, "/com/fproject/vista/IniciarSesion");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo guardar la cuenta.");
        }
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Crear cuenta");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    void PressIniciarSesion(MouseEvent event) {
        Cambiar(event,"/com/fproject/vista/IniciarSesion");
    }

}
