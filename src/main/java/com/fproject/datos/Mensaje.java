package com.fproject.datos;

import com.fproject.modelo.Carta;
import com.fproject.modelo.JugadorPerfil;

import java.io.Serializable;

public class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum aviso {
        LOGIN,
        CREAR_CUENTA,
        RESPUESTA_LOGIN,
        AVISO
    }

    private aviso aviso;

    private String mensaje;

    private JugadorPerfil jugadorActual;

    private Carta carta;

    public Mensaje(){

    }

    public Mensaje(aviso aviso, String mensaje){
        this.aviso = aviso;
        this.mensaje = mensaje;
    }

    public Mensaje(aviso aviso, String mensaje, JugadorPerfil jugadorActual){
        this.aviso = aviso;
        this.mensaje = mensaje;
        this.jugadorActual = jugadorActual;
    }

    public Mensaje(aviso aviso, String mensaje, Carta carta){
        this.aviso = aviso;
        this.mensaje = mensaje;
        this.carta = carta;
    }

    public aviso getAviso() {
        return aviso;
    }

    public void setAviso(aviso aviso) {
        this.aviso = aviso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public JugadorPerfil getJugadorActual() {
        return jugadorActual;
    }

    public void setJugadorActual(JugadorPerfil jugadorActual) {
        this.jugadorActual = jugadorActual;
    }

    public Carta getCarta() {
        return carta;
    }

    public void setCarta(Carta carta) {
        this.carta = carta;
    }
}
