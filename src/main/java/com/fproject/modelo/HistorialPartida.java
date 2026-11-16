package com.fproject.modelo;

import java.io.Serializable;

public class HistorialPartida implements Serializable {
    private static final long serialVersionUID = 1L;

    private String jugador1;
    private String jugador2;
    private String ganador;

    public HistorialPartida() {
    }

    public HistorialPartida(String jugador1, String jugador2, String ganador) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.ganador = ganador;
    }

    public String getJugador1() {
        return jugador1;
    }

    public void setJugador1(String jugador1) {
        this.jugador1 = jugador1;
    }

    public String getJugador2() {
        return jugador2;
    }

    public void setJugador2(String jugador2) {
        this.jugador2 = jugador2;
    }

    public String getGanador() {
        return ganador;
    }

    public void setGanador(String ganador) {
        this.ganador = ganador;
    }

}
