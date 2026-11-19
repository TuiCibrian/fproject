package com.fproject.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import static com.fproject.datos.General.Cambiar;

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

    }

}
