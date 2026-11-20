package com.fproject.datos;

import java.util.ArrayList;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;
import com.fproject.modelo.HistorialPartida;
import com.fproject.modelo.JugadorPerfil;

public class ManejadorDB4O {
    private static final String BASE_DATOS = "cardjitsu.db4o";

    private static ManejadorDB4O instancia;
    private ObjectContainer db;

    private ManejadorDB4O() {
        db = Db4o.openFile(BASE_DATOS);
    }

    public static synchronized ManejadorDB4O getInstancia() {
        if (instancia == null) {
            instancia = new ManejadorDB4O();
        }
        return instancia;
    }

    public static synchronized void cerrarBaseDatos() {
        if (instancia != null) {
            instancia.cerrar();
            instancia = null;
        }
    }

    private synchronized void cerrar() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    public synchronized JugadorPerfil buscarJugador(String nombreUsuario) {
        for (JugadorPerfil jugador : db.query(new Predicate<JugadorPerfil>() {
            @Override
            public boolean match(JugadorPerfil jugador) {
                return jugador.getNombreUsuario().equalsIgnoreCase(nombreUsuario);
            }
        })) {
            return jugador;
        }
        return null;
    }

    public synchronized void guardarJugador(JugadorPerfil jugador) {
        JugadorPerfil jugadorGuardado = buscarJugador(jugador.getNombreUsuario());

        if (jugadorGuardado != null) {
            jugadorGuardado.setPasswordHash(jugador.getPasswordHash());
            jugadorGuardado.setPartidasGanadas(jugador.getPartidasGanadas());
            jugadorGuardado.setPartidasPerdidas(jugador.getPartidasPerdidas());
            jugadorGuardado.setPuntos(jugador.getPuntos());
            jugadorGuardado.setRango(jugador.getRango());
            db.store(jugadorGuardado);
        } else {
            db.store(jugador);
        }

        db.commit();
    }

    public synchronized void guardarPartida(HistorialPartida partida) {
        db.store(partida);
        db.commit();
    }

    public synchronized ArrayList<HistorialPartida> obtenerHistorial() {
        ArrayList<HistorialPartida> historial = new ArrayList<>();

        for (HistorialPartida partida : db.query(HistorialPartida.class)) {
            historial.add(partida);
        }

        return historial;
    }

    public synchronized ArrayList<HistorialPartida> obtenerHistorialJugador(String nombreUsuario) {
        ArrayList<HistorialPartida> historial = new ArrayList<>();

        for (HistorialPartida partida : db.query(new Predicate<HistorialPartida>() {
            @Override
            public boolean match(HistorialPartida partida) {
                return partida.getJugador1().equalsIgnoreCase(nombreUsuario)
                        || partida.getJugador2().equalsIgnoreCase(nombreUsuario);
            }
        })) {
            historial.add(partida);
        }

        return historial;
    }
}
