package com.fproject.modelo;

import java.io.Serializable;

public class JugadorPerfil implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombreUsuario;
    private int partidasGanadas;
    private int partidasPerdidas;
    private int puntos;
    private String rango;
    private String passwordHash;
    private String Peleador;

    public JugadorPerfil() {
    }

    public JugadorPerfil(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        this.rango = "Novato";
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public int getPartidasGanadas() {
        return partidasGanadas;
    }

    public void setPartidasGanadas(int partidasGanadas) {
        this.partidasGanadas = partidasGanadas;
    }

    public int getPartidasPerdidas() {
        return partidasPerdidas;
    }

    public void setPartidasPerdidas(int partidasPerdidas) {
        this.partidasPerdidas = partidasPerdidas;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public String getRango() {
        return rango;
    }

    public void setRango(String rango) {
        this.rango = rango;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPeleador() {
        return Peleador;
    }

    public void setPeleador(String peleador) {
        Peleador = peleador;
    }
}
