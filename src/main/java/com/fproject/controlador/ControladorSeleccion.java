package com.fproject.controlador;

import com.fproject.datos.General;
import com.fproject.datos.Mensaje;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import static com.fproject.controlador.ControladorSalaEspera.enviarAlServidor;
import static com.fproject.datos.General.Cambiar;

public class ControladorSeleccion {

    @FXML
    private VBox BoxCard;

    @FXML
    private VBox KarateCard;

    @FXML
    private VBox SumoCard;

    @FXML
    void ClickBox(MouseEvent event) {
        General.getJugadorActual().setPeleador("Box");
        enviarAlServidor(new Mensaje(Mensaje.aviso.CAMBIAR_MONO,"Box"));
        Refrescar();
    }

    @FXML
    void ClickKarate(MouseEvent event) {
        General.getJugadorActual().setPeleador("Karate");
        enviarAlServidor(new Mensaje(Mensaje.aviso.CAMBIAR_MONO,"Karate"));
        Refrescar();
    }

    @FXML
    void ClickSumo(MouseEvent event) {
        General.getJugadorActual().setPeleador("Sumo");
        enviarAlServidor(new Mensaje(Mensaje.aviso.CAMBIAR_MONO,"Sumo"));
        Refrescar();
    }

    public void initialize(){
        Refrescar();
    }

    @FXML
    void cerrarSesion(ActionEvent event) {
        Cambiar(event,"/com/fproject/vista/SalaEspera");
    }

    private void Refrescar(){
        if(General.getJugadorActual().getPeleador()==null){
            General.getJugadorActual().setPeleador("Box");
        }

        BoxCard.getStyleClass().remove("cardMonoSelected");
        KarateCard.getStyleClass().remove("cardMonoSelected");
        SumoCard.getStyleClass().remove("cardMonoSelected");



        switch(General.getJugadorActual().getPeleador()){
            case "Box":{
                BoxCard.getStyleClass().add("cardMonoSelected");
                break;
            }
            case "Karate":{
                KarateCard.getStyleClass().add("cardMonoSelected");
                break;
            }
            case "Sumo":{
                SumoCard.getStyleClass().add("cardMonoSelected");
                break;
            }
        }
    }

}
