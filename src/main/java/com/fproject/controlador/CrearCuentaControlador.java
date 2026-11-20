package com.fproject.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
    //le dice al usuario que cree una cuenta, tiene que tener nombre unico
    }

    @FXML
    void PressIniciarSesion(MouseEvent event) {
        Cambiar(event,"/com/fproject/vista/IniciarSesion");
    }

}
