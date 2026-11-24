package com.fproject.servidor;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Date;

import com.fproject.datos.ManejadorDB4O;
import com.fproject.datos.Mensaje;
import com.fproject.modelo.Carta;
import com.fproject.modelo.EstadoJuego;
import com.fproject.modelo.HistorialPartida;
import com.fproject.modelo.JugadorPerfil;
import com.fproject.modelo.ReglasJuego;
import com.fproject.modelo.ReglasPuntos;

//partida entre dos clientes; el servidor guarda el estado y decide cada ronda
public class SalaJuego {
    private final Servidor.Cliente jugador1;
    private final Servidor.Cliente jugador2;
    private final ReglasJuego reglas = new ReglasJuego();
    private EstadoJuego estado;
    private int indiceCartaJugador1 = -1;
    private int indiceCartaJugador2 = -1;
    private boolean partidaTerminada;
    private boolean revanchaJugador1;
    private boolean revanchaJugador2;
    private boolean salaCerrada;

    public SalaJuego(Servidor.Cliente jugador1, Servidor.Cliente jugador2) {
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
    }

    public synchronized void iniciar() {
        estado = reglas.iniciarPartida();
        indiceCartaJugador1 = -1;
        indiceCartaJugador2 = -1;
        partidaTerminada = false;
        revanchaJugador1 = false;
        revanchaJugador2 = false;

        enviarEstado(Mensaje.aviso.ESTADO_JUEGO, "La partida comenzó.");
    }

    public synchronized void seleccionarCarta(Servidor.Cliente cliente, int indice) {
        if (salaCerrada || partidaTerminada) {
            return;
        }

        //cada jugador elige una sola carta por ronda y debe estar en su mano
        if (cliente == jugador1) {
            if (indiceCartaJugador1 != -1 || indice < 0 || indice >= estado.getManoJugador1().size()) {
                return;
            }
            indiceCartaJugador1 = indice;
        } else if (cliente == jugador2) {
            if (indiceCartaJugador2 != -1 || indice < 0 || indice >= estado.getManoJugador2().size()) {
                return;
            }
            indiceCartaJugador2 = indice;
        } else {
            return;
        }

        if (indiceCartaJugador1 == -1 || indiceCartaJugador2 == -1) {
            Servidor.Cliente rival = rivalDe(cliente);
            cliente.enviar(new Mensaje(Mensaje.aviso.AVISO, "Esperando a que " + nombre(rival) + " elija..."));
            rival.enviar(new Mensaje(Mensaje.aviso.AVISO, nombre(cliente) + " ya eligió. Elige tu carta."));
            return;
        }

        resolverRonda();
    }

    private void resolverRonda() {
        Carta cartaJugador1 = estado.getManoJugador1().get(indiceCartaJugador1);
        Carta cartaJugador2 = estado.getManoJugador2().get(indiceCartaJugador2);

        System.out.println("Ronda: " + inspeccionar(cartaJugador1) + " vs " + inspeccionar(cartaJugador2));

        int resultado = reglas.resolverRonda(cartaJugador1, cartaJugador2);
        String textoResultado = "";

        if (resultado == ReglasJuego.JUGADOR1) {
            estado.getCartasGanadasJugador1().add(cartaJugador1);
            textoResultado += nombre(jugador1) + " Ganó La Ronda.";
        } else if (resultado == ReglasJuego.JUGADOR2) {
            estado.getCartasGanadasJugador2().add(cartaJugador2);
            textoResultado += nombre(jugador2) + " Ganó La Ronda.";
        } else {
            textoResultado += "La Ronda Terminó En Empate.";
        }

        estado.getManoJugador1().remove(indiceCartaJugador1);
        estado.getManoJugador2().remove(indiceCartaJugador2);
        indiceCartaJugador1 = -1;
        indiceCartaJugador2 = -1;

        if (reglas.tieneVictoria(estado.getCartasGanadasJugador1())) {
            terminarPartida(nombre(jugador1), textoResultado);
            return;
        }
        if (reglas.tieneVictoria(estado.getCartasGanadasJugador2())) {
            terminarPartida(nombre(jugador2), textoResultado);
            return;
        }

        reglas.reponerCarta(estado.getManoJugador1(), estado.getMazo());
        reglas.reponerCarta(estado.getManoJugador2(), estado.getMazo());

        estado.setUltimaCartaJugador1(cartaJugador1);
        estado.setUltimaCartaJugador2(cartaJugador2);

        if (estado.getMazo().isEmpty()
                && estado.getManoJugador1().isEmpty()
                && estado.getManoJugador2().isEmpty()) {
            terminarPartida("Empate", textoResultado + " No quedan cartas.");
            return;
        }

        enviarEstado(Mensaje.aviso.ESTADO_JUEGO, textoResultado);
    }

    private String inspeccionar(Object objeto) { //clase para inspeccionar cartas e imprimir sus datos en la temrinal
        //mediante instrospeccion
        String texto = objeto.getClass().getSimpleName() + ":";
        try {
            //recorre las propiedades y consigue sus nombres, con estos datos ya usa sus getters para imprimir en pantalla
            for (PropertyDescriptor p : Introspector.getBeanInfo(objeto.getClass(), Object.class).getPropertyDescriptors()) {
                texto += " " + p.getName() + "=" + p.getReadMethod().invoke(objeto);
            }
        } catch (Exception e) {
            texto += " no se pudo inspeccionar";
        }
        return texto;
    }

    private void terminarPartida(String ganador, String resultado) {
        partidaTerminada = true;
        estado.setGanador(ganador);
        registrarResultado(ganador);

        if (ganador.equals("Empate")) {
            enviarEstado(Mensaje.aviso.FIN_PARTIDA, resultado);
        } else {
            enviarEstado(Mensaje.aviso.FIN_PARTIDA, resultado + " " + ganador + " ganó la partida.");
        }
    }

    //solo el servidor abre la base de datos, los clientes reciben su perfil actualizado
    private void registrarResultado(String ganador) {
        try {
            ManejadorDB4O db = ManejadorDB4O.getInstancia();
            JugadorPerfil perfil1 = jugador1.getJugador();
            JugadorPerfil perfil2 = jugador2.getJugador();

            if (ganador.equals("Empate")) {
                ReglasPuntos.registrarEmpate(perfil1);
                ReglasPuntos.registrarEmpate(perfil2);
            } else if (ganador.equals(nombre(jugador1))) {
                ReglasPuntos.registrarVictoria(perfil1);
                ReglasPuntos.registrarDerrota(perfil2);
            } else {
                ReglasPuntos.registrarVictoria(perfil2);
                ReglasPuntos.registrarDerrota(perfil1);
            }

            db.guardarJugador(perfil1);
            db.guardarJugador(perfil2);
            db.guardarPartida(new HistorialPartida(
                    nombre(jugador1), nombre(jugador2), ganador, new Date().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void pedirRevancha(Servidor.Cliente cliente) {
        if (salaCerrada || !partidaTerminada) {
            return;
        }

        if (cliente == jugador1) {
            revanchaJugador1 = true;
        } else if (cliente == jugador2) {
            revanchaJugador2 = true;
        } else {
            return;
        }

        //la revancha empieza solo cuando los dos la aceptan
        if (revanchaJugador1 && revanchaJugador2) {
            iniciar();
            return;
        }

        Servidor.Cliente rival = rivalDe(cliente);
        cliente.enviar(new Mensaje(Mensaje.aviso.AVISO, "Esperando a que " + nombre(rival) + " acepte la revancha..."));
        rival.enviar(new Mensaje(Mensaje.aviso.AVISO, nombre(cliente) + " quiere la revancha. Presiona Revancha para aceptar."));
    }

    //se llama cuando un jugador vuelve al lobby o se desconecta
    public synchronized void salir(Servidor.Cliente cliente) {
        if (salaCerrada) {
            return;
        }
        salaCerrada = true;
        jugador1.sala = null;
        jugador2.sala = null;

        Servidor.Cliente rival = rivalDe(cliente);
        if (partidaTerminada) {
            rival.enviar(new Mensaje(Mensaje.aviso.RIVAL_DESCONECTADO, nombre(cliente) + " salió de la sala."));
        } else {
            rival.enviar(new Mensaje(Mensaje.aviso.RIVAL_DESCONECTADO, nombre(cliente) + " abandonó la partida."));
        }
    }

    private void enviarEstado(Mensaje.aviso aviso, String texto) {
        jugador1.enviar(crearMensaje(jugador1, aviso, texto));
        jugador2.enviar(crearMensaje(jugador2, aviso, texto));

    }

    //cada jugador recibe solo su mano; en la vista, jugador1 siempre es quien recibe el mensaje
    private Mensaje crearMensaje(Servidor.Cliente cliente, Mensaje.aviso aviso, String texto) {
        EstadoJuego vista = new EstadoJuego();

        if (cliente == jugador1) {
            vista.setManoJugador1(estado.getManoJugador1());
            vista.setCartasGanadasJugador1(estado.getCartasGanadasJugador1());
            vista.setCartasGanadasJugador2(estado.getCartasGanadasJugador2());
            vista.setUltimaCartaJugador1(estado.getUltimaCartaJugador1());
            vista.setUltimaCartaJugador2(estado.getUltimaCartaJugador2());
        } else {
            vista.setManoJugador1(estado.getManoJugador2());
            vista.setCartasGanadasJugador1(estado.getCartasGanadasJugador2());
            vista.setCartasGanadasJugador2(estado.getCartasGanadasJugador1());
            vista.setUltimaCartaJugador1(estado.getUltimaCartaJugador2());
            vista.setUltimaCartaJugador2(estado.getUltimaCartaJugador1());
        }
        vista.setGanador(estado.getGanador());

        Mensaje msg = new Mensaje(aviso, texto, Servidor.perfilPublico(cliente.getJugador()));
        msg.setEstado(vista);
        return msg;
    }

    private Servidor.Cliente rivalDe(Servidor.Cliente cliente) {
        if (cliente == jugador1) {
            return jugador2;
        }
        return jugador1;
    }

    private String nombre(Servidor.Cliente cliente) {
        return cliente.getJugador().getNombreUsuario();
    }

    private String describirCarta(Carta carta) {
        return carta.getElemento() + " " + carta.getValor() + " " + carta.getColor();
    }
}
