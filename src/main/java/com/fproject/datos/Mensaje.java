package com.fproject.datos;

import com.fproject.modelo.Carta;
import com.fproject.modelo.EstadoJuego;
import com.fproject.modelo.HistorialPartida;
import com.fproject.modelo.JugadorPerfil;

import java.io.Serializable;
import java.util.ArrayList;

public class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum aviso {
        LOGIN,
        CREAR_CUENTA,
        LOGIN_OK,
        LOGIN_ERROR,
        HISTORIAL,
        HISTORIAL_OK,
        HISTORIAL_ERROR,
        BUSCAR_PARTIDA,
        CANCELAR_BUSQUEDA,
        BUSQUEDA_CANCELADA,
        PARTIDA_OK,
        AVISO,
        ESTADO_JUEGO,
        SELECCIONAR_CARTA,
        FIN_PARTIDA,
        REVANCHA,
        SALIR_PARTIDA,
        RIVAL_DESCONECTADO,
        CONEXION_PERDIDA,
        CAMBIAR_MONO
    }

    private aviso aviso;

    private String mensaje;

    private JugadorPerfil jugadorActual;

    private Carta carta;

    private ArrayList<HistorialPartida> historial;

    private EstadoJuego estado;

    private int indice;

    public Mensaje() {

    }

    public Mensaje(aviso aviso, String mensaje) {
        this.aviso = aviso;
        this.mensaje = mensaje;
    }

    public Mensaje(aviso aviso, String mensaje, JugadorPerfil jugadorActual) {
        this.aviso = aviso;
        this.mensaje = mensaje;
        this.jugadorActual = jugadorActual;
    }

    public Mensaje(aviso aviso, String mensaje, Carta carta) {
        this.aviso = aviso;
        this.mensaje = mensaje;
        this.carta = carta;
    }

    public Mensaje(aviso aviso, JugadorPerfil jugadorActual) {
        this.aviso = aviso;
        this.jugadorActual = jugadorActual;
    }

    public Mensaje(aviso aviso, Carta carta) {
        this.aviso = aviso;
        this.carta = carta;
    }

    public Mensaje(aviso aviso, ArrayList<HistorialPartida>  historialPartida) {
        this.aviso = aviso;
        this.historial = historialPartida;
    }

    public Mensaje(aviso aviso, int indice) {
        this.aviso = aviso;
        this.indice = indice;
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

    public ArrayList<HistorialPartida> getHistorial() {
        return historial;
    }

    public void setHistorial(ArrayList<HistorialPartida> historial) {
        this.historial = historial;
    }

    public EstadoJuego getEstado() {
        return estado;
    }

    public void setEstado(EstadoJuego estado) {
        this.estado = estado;
    }

    public int getIndice() {
        return indice;
    }

    public void setIndice(int indice) {
        this.indice = indice;
    }
}
