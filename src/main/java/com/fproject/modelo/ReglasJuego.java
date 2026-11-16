package com.fproject.modelo;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ReglasJuego {
    public static final int EMPATE = 0;
    public static final int JUGADOR1 = 1;
    public static final int JUGADOR2 = 2;

    private static final String FUEGO = "Fuego";
    private static final String AGUA = "Agua";
    private static final String PLANTA = "Planta";

    public EstadoJuego iniciarPartida() {
        EstadoJuego estado = new EstadoJuego();
        estado.setMazo(crearMazo());
        repartirManos(estado);
        return estado;
    }

    public ArrayList<Carta> crearMazo() {
        ArrayList<Carta> mazo = new ArrayList<>();
        ArrayList<String> elementos = new ArrayList<>();
        ArrayList<String> colores = new ArrayList<>();

        elementos.add(FUEGO);
        elementos.add(AGUA);
        elementos.add(PLANTA);

        colores.add("Rojo");
        colores.add("Azul");
        colores.add("Verde");

        for (String elemento : elementos) {
            for (String color : colores) {
                for (int valor = 1; valor <= 10; valor++) {
                    mazo.add(new Carta(elemento, valor, color));
                }
            }
        }

        return mazo;
    }

    public void repartirManos(EstadoJuego estado) {
        for (int i = 0; i < 5; i++) {
            estado.getManoJugador1().add(robarCarta(estado.getMazo()));
            estado.getManoJugador2().add(robarCarta(estado.getMazo()));
        }
    }

    public void reponerCarta(ArrayList<Carta> mano, ArrayList<Carta> mazo) {
        if (!mazo.isEmpty()) {
            mano.add(robarCarta(mazo));
        }
    }

    public int resolverRonda(Carta carta1, Carta carta2) {
        if (carta1.getElemento().equals(carta2.getElemento())) {
            if (carta1.getValor() > carta2.getValor()) {
                return JUGADOR1;
            }
            if (carta2.getValor() > carta1.getValor()) {
                return JUGADOR2;
            }
            return EMPATE;
        }

        if (ganaElemento(carta1.getElemento(), carta2.getElemento())) {
            return JUGADOR1;
        }
        if (ganaElemento(carta2.getElemento(), carta1.getElemento())) {
            return JUGADOR2;
        }
        return EMPATE;
    }

    public boolean tieneVictoria(ArrayList<Carta> cartas) {
        return tieneTresElementos(cartas) || tieneTresColores(cartas);
    }

    private Carta robarCarta(ArrayList<Carta> mazo) {
        int indice = ThreadLocalRandom.current().nextInt(0, mazo.size());
        return mazo.remove(indice);
    }

    private boolean ganaElemento(String elemento1, String elemento2) {
        return (FUEGO.equals(elemento1) && PLANTA.equals(elemento2))
                || (PLANTA.equals(elemento1) && AGUA.equals(elemento2))
                || (AGUA.equals(elemento1) && FUEGO.equals(elemento2));
    }

    private boolean tieneTresElementos(ArrayList<Carta> cartas) {
        boolean tieneFuego = false;
        boolean tieneAgua = false;
        boolean tienePlanta = false;

        for (Carta carta : cartas) {
            if (FUEGO.equals(carta.getElemento())) {
                tieneFuego = true;
            }
            if (AGUA.equals(carta.getElemento())) {
                tieneAgua = true;
            }
            if (PLANTA.equals(carta.getElemento())) {
                tienePlanta = true;
            }
        }

        return tieneFuego && tieneAgua && tienePlanta;
    }

    private boolean tieneTresColores(ArrayList<Carta> cartas) {
        for (int i = 0; i < cartas.size(); i++) {
            for (int j = i + 1; j < cartas.size(); j++) {
                for (int k = j + 1; k < cartas.size(); k++) {
                    Carta carta1 = cartas.get(i);
                    Carta carta2 = cartas.get(j);
                    Carta carta3 = cartas.get(k);

                    boolean mismoElemento = carta1.getElemento().equals(carta2.getElemento())
                            && carta1.getElemento().equals(carta3.getElemento());
                    boolean distintoColor = !carta1.getColor().equals(carta2.getColor())
                            && !carta1.getColor().equals(carta3.getColor())
                            && !carta2.getColor().equals(carta3.getColor());

                    if (mismoElemento && distintoColor) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
