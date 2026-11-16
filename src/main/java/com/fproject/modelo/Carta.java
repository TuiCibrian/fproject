package com.fproject.modelo;

import java.io.Serializable;

public class Carta implements Serializable {
    private static final long serialVersionUID = 1L;

    private String elemento;
    private int valor;
    private String color;

    public Carta() {
    }

    public Carta(String elemento, int valor, String color) {
        this.elemento = elemento;
        this.valor = valor;
        this.color = color;
    }

    public String getElemento() {
        return elemento;
    }

    public void setElemento(String elemento) {
        this.elemento = elemento;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
