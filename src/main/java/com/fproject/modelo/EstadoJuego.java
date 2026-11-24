package com.fproject.modelo;

import java.io.Serializable;
import java.util.ArrayList;

public class EstadoJuego implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Carta> mazo;
    private ArrayList<Carta> manoJugador1;
    private ArrayList<Carta> manoJugador2;
    private ArrayList<Carta> cartasGanadasJugador1;
    private ArrayList<Carta> cartasGanadasJugador2;
    private String ganador;
    private Carta ultimaCartaJugador1;
    private Carta ultimaCartaJugador2;

    public EstadoJuego() {
        mazo = new ArrayList<>();
        manoJugador1 = new ArrayList<>();
        manoJugador2 = new ArrayList<>();
        cartasGanadasJugador1 = new ArrayList<>();
        cartasGanadasJugador2 = new ArrayList<>();
    }

    public ArrayList<Carta> getMazo() {
        return mazo;
    }

    public void setMazo(ArrayList<Carta> mazo) {
        this.mazo = mazo;
    }

    public ArrayList<Carta> getManoJugador1() {
        return manoJugador1;
    }

    public void setManoJugador1(ArrayList<Carta> manoJugador1) {
        this.manoJugador1 = manoJugador1;
    }

    public ArrayList<Carta> getManoJugador2() {
        return manoJugador2;
    }

    public void setManoJugador2(ArrayList<Carta> manoJugador2) {
        this.manoJugador2 = manoJugador2;
    }

    public ArrayList<Carta> getCartasGanadasJugador1() {
        return cartasGanadasJugador1;
    }

    public void setCartasGanadasJugador1(ArrayList<Carta> cartasGanadasJugador1) {
        this.cartasGanadasJugador1 = cartasGanadasJugador1;
    }

    public ArrayList<Carta> getCartasGanadasJugador2() {
        return cartasGanadasJugador2;
    }

    public void setCartasGanadasJugador2(ArrayList<Carta> cartasGanadasJugador2) {
        this.cartasGanadasJugador2 = cartasGanadasJugador2;
    }

    public String getGanador() {
        return ganador;
    }

    public void setGanador(String ganador) {
        this.ganador = ganador;
    }

    public Carta getUltimaCartaJugador1() {
        return ultimaCartaJugador1;
    }

    public void setUltimaCartaJugador1(Carta ultimaCartaJugador1) {
        this.ultimaCartaJugador1 = ultimaCartaJugador1;
    }

    public Carta getUltimaCartaJugador2() {
        return ultimaCartaJugador2;
    }

    public void setUltimaCartaJugador2(Carta ultimaCartaJugador2) {
        this.ultimaCartaJugador2 = ultimaCartaJugador2;
    }
}
