package com.fproject.modelo;

public class ReglasPuntos {
    public static final int PUNTOS_VICTORIA = 3;
    public static final int PUNTOS_EMPATE = 1;

    public static void registrarVictoria(JugadorPerfil jugador) {
        jugador.setPartidasGanadas(jugador.getPartidasGanadas() + 1);
        jugador.setPuntos(jugador.getPuntos() + PUNTOS_VICTORIA);
        jugador.setRango(calcularRango(jugador.getPuntos()));
    }

    public static void registrarDerrota(JugadorPerfil jugador) {
        jugador.setPartidasPerdidas(jugador.getPartidasPerdidas() + 1);
        jugador.setRango(calcularRango(jugador.getPuntos()));
    }

    public static void registrarEmpate(JugadorPerfil jugador) {
        jugador.setPuntos(jugador.getPuntos() + PUNTOS_EMPATE);
        jugador.setRango(calcularRango(jugador.getPuntos()));
    }

    private static String calcularRango(int puntos) {
        if (puntos >= 100) {
            return "Maestro";
        }
        if (puntos >= 50) {
            return "Profesional";
        }
        if (puntos >= 25) {
            return "Avanzado";
        }
        if (puntos >= 10) {
            return "Amateur";
        }
        return "Novato";
    }
}
