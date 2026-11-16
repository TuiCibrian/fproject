package com.fproject.datos;

import java.util.ArrayList;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.fproject.modelo.HistorialPartida;
import com.fproject.modelo.JugadorPerfil;

public class ManejadorDB4O {
    private static final String BASE_DATOS = "cardjitsu.db4o";

    public JugadorPerfil buscarJugador(String nombreUsuario) {
        ObjectContainer db = Db4o.openFile(BASE_DATOS);

        try {
            for (JugadorPerfil jugador : db.query(JugadorPerfil.class)) {
                if (jugador.getNombreUsuario().equalsIgnoreCase(nombreUsuario)) {
                    return jugador;
                }
            }
            return null;
        } finally {
            db.close();
        }
    }

    public void guardarJugador(JugadorPerfil jugador) {
        ObjectContainer db = Db4o.openFile(BASE_DATOS);

        try {
            for (JugadorPerfil jugadorGuardado : db.query(JugadorPerfil.class)) {
                if (jugadorGuardado.getNombreUsuario().equalsIgnoreCase(jugador.getNombreUsuario())) {
                    db.delete(jugadorGuardado);
                    break;
                }
            }

            db.store(jugador);
            db.commit();
        } finally {
            db.close();
        }
    }

    public void guardarPartida(HistorialPartida partida) {
        ObjectContainer db = Db4o.openFile(BASE_DATOS);

        try {
            db.store(partida);
            db.commit();
        } finally {
            db.close();
        }
    }

    public ArrayList<HistorialPartida> obtenerHistorial() {
        ObjectContainer db = Db4o.openFile(BASE_DATOS);
        ArrayList<HistorialPartida> historial = new ArrayList<>();

        try {
            for (HistorialPartida partida : db.query(HistorialPartida.class)) {
                historial.add(partida);
            }
            return historial;
        } finally {
            db.close();
        }
    }
}
