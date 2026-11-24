package com.fproject.servidor;

import com.fproject.datos.ManejadorDB4O;
import com.fproject.datos.Mensaje;
import com.fproject.datos.Seguridad;
import com.fproject.modelo.HistorialPartida;
import com.fproject.modelo.JugadorPerfil;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;


public class Servidor {
    private static final int PUERTO_PREDETERMINADO = 12345;
    private static final List<Cliente> CLIENTES = new CopyOnWriteArrayList<>();
    private static ManejadorDB4O db = ManejadorDB4O.getInstancia();

    private static final List<Cliente> SALA_ESPERA = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        //recibe puerto de argumentos o tomamos predeterminado
        int puerto = args.length == 0 ? PUERTO_PREDETERMINADO : Integer.parseInt(args[0]);

        /*
        El servidor manejará la base de datos
        los clientes solo se comunican con el servidor y reciben info
        */
        DescubrimientoServidor descubrimiento = new DescubrimientoServidor(puerto + 1, puerto);
        descubrimiento.iniciar();

        //Si se cierra el servidor también cerramos la base de datos y dejamos de buscar clientes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            descubrimiento.detener();
            ManejadorDB4O.cerrarBaseDatos();
        }));

        iniciar(puerto);
    }

    private static void iniciar(int puerto) throws IOException {
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor Iniciado En Puerto " + puerto);
            while (true) {
                Socket socket = servidor.accept();
                Thread hilo = new Thread(new Cliente(socket), "Cliente-" + socket.getPort());
                hilo.start();
            }
        }
    }

    //copia del perfil sin el passwordHash para mandarla a los clientes
    static JugadorPerfil perfilPublico(JugadorPerfil jugador) {
        JugadorPerfil perfil = new JugadorPerfil();
        perfil.setNombreUsuario(jugador.getNombreUsuario());
        perfil.setRango(jugador.getRango());
        perfil.setPartidasGanadas(jugador.getPartidasGanadas());
        perfil.setPartidasPerdidas(jugador.getPartidasPerdidas());
        perfil.setPuntos(jugador.getPuntos());
        perfil.setPeleador(jugador.getPeleador());
        return perfil;
    }

    private static synchronized void difundir(Mensaje mensaje) {
        for (Cliente cliente : CLIENTES) {
            cliente.enviar(mensaje);
        }
    }

    static final class Cliente implements Runnable {
        private final Socket socket;
        private ObjectOutputStream salida;
        private volatile JugadorPerfil jugadorAutenticado;
        volatile SalaJuego sala;

        private Cliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                //al recibir un cliente hace esto:
                salida = new ObjectOutputStream(socket.getOutputStream());
                salida.flush();
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

                //añadimos cliente a la lista
                CLIENTES.add(this);

                //va a recibir mensajes en un ciclo
                while (true) {
                    Object obj = entrada.readObject();
                    if (obj instanceof Mensaje) {
                        //si recibe un mensaje lo procesamos
                        Mensaje msg = (Mensaje) obj;
                        procesarMensaje(msg);
                    }
                }
            } catch (EOFException | SocketException e) {
                System.out.println("Cliente Desconectado: " + nombreCliente());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error del Cliente " + socket.getPort() + ": " + e.getMessage());
            } finally {
                CLIENTES.remove(this);
                quitarDeEspera(this);

                //si estaba en partida se le avisa al rival
                SalaJuego salaActual = sala;
                if (salaActual != null) {
                    salaActual.salir(this);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("No se pudo cerrar el socket");
                }
            }
        }


        JugadorPerfil getJugador() {
            return jugadorAutenticado;
        }

        private String nombreCliente() {
            if (jugadorAutenticado == null) {
                return "sin sesión (" + socket.getPort() + ")";
            }
            return jugadorAutenticado.getNombreUsuario();
        }

        //metodo para que el servidor sepa que tipo de mensaje es
        private void procesarMensaje(Mensaje msg) {
            //fuera de login y registro, todo requiere haber iniciado sesión
            if (jugadorAutenticado == null
                    && msg.getAviso() != Mensaje.aviso.LOGIN
                    && msg.getAviso() != Mensaje.aviso.CREAR_CUENTA) {
                this.enviar(new Mensaje(Mensaje.aviso.LOGIN_ERROR, "Inicia Sesión Primero"));
                return;
            }

            SalaJuego salaActual = sala;
            switch (msg.getAviso()) {
                case LOGIN: {
                    //validaciones
                    JugadorPerfil datosLogin = msg.getJugadorActual();
                    if (datosLogin == null || datosLogin.getNombreUsuario().isEmpty() || datosLogin.getPasswordHash().isEmpty()) {
                        this.enviar(new Mensaje(Mensaje.aviso.LOGIN_ERROR, "Escribe Usuario y Contraseña"));
                        return;
                    }

                    String usuario = datosLogin.getNombreUsuario().trim();
                    String password = datosLogin.getPasswordHash();
                    String passwordHash = Seguridad.hashSHA256(password);

                    JugadorPerfil jugador = db.buscarJugador(usuario);

                    if (jugador == null) {
                        System.out.println("Usuario " + msg.getJugadorActual().getNombreUsuario() + " no encontrado");
                        this.enviar(new Mensaje(Mensaje.aviso.LOGIN_ERROR, "Usuario " + usuario + " no encontrado"));
                        return;
                    }

                    if (jugador.getPasswordHash() == null || !jugador.getPasswordHash().equals(passwordHash)) {
                        System.out.println("Usuario " + msg.getJugadorActual().getNombreUsuario() + " envio contraseña incorrecta");
                        this.enviar(new Mensaje(Mensaje.aviso.LOGIN_ERROR, "Contraseña Incorrecta"));
                        return;
                    }

                    //si paso validaciones enviamos un mensaje de exito
                    this.jugadorAutenticado = jugador;
                    this.enviar(new Mensaje(Mensaje.aviso.LOGIN_OK, "Inicio de Sesión Existoso", perfilPublico(jugador)));
                    System.out.println("Usuario Conectado :"+this.jugadorAutenticado.getNombreUsuario());
                    return;
                }
                case CREAR_CUENTA: {
                    JugadorPerfil datosLogin = msg.getJugadorActual();
                    if (datosLogin == null || datosLogin.getNombreUsuario().isEmpty() || datosLogin.getPasswordHash().isEmpty() || datosLogin.getNombreUsuario() == null || datosLogin.getPasswordHash() == null) {
                        this.enviar(new Mensaje(Mensaje.aviso.LOGIN_ERROR, "Escribe Usuario y Contraseña"));
                        return;
                    }
                    String usuario = datosLogin.getNombreUsuario().trim();
                    String password = datosLogin.getPasswordHash();

                    if (password.length() < 4) {
                        this.enviar(new Mensaje(Mensaje.aviso.LOGIN_ERROR, "La Contraseña Debe Tener Al Menos 4 Caracteres"));
                        return;
                    }


                    if (db.buscarJugador(usuario) != null) {
                        this.enviar(new Mensaje(Mensaje.aviso.LOGIN_ERROR, "Ese Usuario Ya Existe"));
                        return;
                    }

                    datosLogin.setNombreUsuario(usuario.trim());
                    datosLogin.setPasswordHash(Seguridad.hashSHA256(password));
                    datosLogin.setRango("Novato");
                    datosLogin.setPeleador("Box");
                    db.guardarJugador(datosLogin);

                    //si paso validaciones enviamos un mensaje de exito y guardamos sus datos
                    this.jugadorAutenticado = datosLogin;

                    this.enviar(new Mensaje(Mensaje.aviso.LOGIN_OK, "Cuenta Creada Correctamente", perfilPublico(datosLogin)));
                    System.out.println("Usuario Registrado y Conectado: "+this.jugadorAutenticado.getNombreUsuario());
                    return;
                }
                case HISTORIAL: {
                    //se usa el jugador que inició sesión, no el que manda el cliente
                    String nombre = jugadorAutenticado.getNombreUsuario();

                    ArrayList<HistorialPartida> lista = db.obtenerHistorialJugador(nombre);

                    this.enviar(new Mensaje(Mensaje.aviso.HISTORIAL_OK, lista));
                    return;
                }
                case BUSCAR_PARTIDA: {
                    procesarBusqueda(this);
                    return;
                }
                case CANCELAR_BUSQUEDA:{
                    //si ya lo emparejaron, el cliente recibe PARTIDA_OK y entra a la partida
                    if (cancelarBusqueda(this)) {
                        this.enviar(new Mensaje(Mensaje.aviso.BUSQUEDA_CANCELADA,"Busqueda Cancelada"));
                    }
                    return;
                }
                case SELECCIONAR_CARTA: {
                    if (salaActual != null) {
                        salaActual.seleccionarCarta(this, msg.getIndice());
                    }
                    return;
                }
                case REVANCHA: {
                    if (salaActual != null) {
                        salaActual.pedirRevancha(this);
                    }
                    return;
                }
                case SALIR_PARTIDA: {
                    if (salaActual != null) {
                        salaActual.salir(this);
                    }
                    return;
                }
                case CAMBIAR_MONO: {
                    if(!Objects.equals(msg.getMensaje(), "Box") && !Objects.equals(msg.getMensaje(), "Karate") && !Objects.equals(msg.getMensaje(), "Sumo")) {
                        this.enviar(new Mensaje(Mensaje.aviso.CAMBIAR_ERROR,"Peleador Invalido"));
                        return;
                    }
                    this.getJugador().setPeleador(msg.getMensaje());
                    db.guardarJugador(this.getJugador());
                    this.enviar(new Mensaje(Mensaje.aviso.CAMBIAR_OK,this.getJugador()));
                    return;
                }
                default:
                    System.err.println("Mensaje Desconocido " + msg.getAviso());
            }
        }

        private static synchronized void procesarBusqueda(Cliente solicitante) {
            //si ya esta esperando o jugando no lo agregamos
            if (solicitante.sala != null || SALA_ESPERA.contains(solicitante)) {
                return;
            }

            //si es el primero en llegar lo agregamos a la espera
            if (SALA_ESPERA.isEmpty()) {
                SALA_ESPERA.add(solicitante);
                System.out.println("Jugador Esperando Partida: " + solicitante.jugadorAutenticado.getNombreUsuario());
                return;
            }

            //el rival es el otro que espera, y el rival del rival es el solicitante
            Cliente rival = SALA_ESPERA.remove(0);
            if (rival == solicitante) {
                SALA_ESPERA.add(solicitante);
                return;
            }

            SalaJuego salaNueva = new SalaJuego(rival, solicitante);
            solicitante.sala = salaNueva;
            rival.sala = salaNueva;
            System.out.println("Partida: " + rival.nombreCliente() + " vs " + solicitante.nombreCliente());

            //le enviamos al rival y al primer usuario en espera los datos del otro
            solicitante.enviar(new Mensaje(Mensaje.aviso.PARTIDA_OK, perfilPublico(rival.jugadorAutenticado)));
            rival.enviar(new Mensaje(Mensaje.aviso.PARTIDA_OK, perfilPublico(solicitante.jugadorAutenticado)));

            //despues de PARTIDA_OK se manda la primera mano de cada uno
            salaNueva.iniciar();
        }

        //metodo para enviar mensajes a un cliente
        synchronized void enviar(Mensaje mensaje) {
            try {
                salida.writeObject(mensaje);
                salida.flush();
                salida.reset();
            } catch (IOException e) {
                System.err.println("No Se Pudo Enviar El Mensaje");
                try {
                    socket.close();
                } catch (IOException closeError) {
                    System.err.println("No Se Pudo Cerrar El Socket");
                }
            }
        }

        private static synchronized boolean cancelarBusqueda(Cliente solicitante) {
            boolean estaba = SALA_ESPERA.remove(solicitante);
            System.out.println("Jugador cancelo búsqueda: " + solicitante.nombreCliente());
            return estaba;
        }

        private static synchronized void quitarDeEspera(Cliente cliente) {
            SALA_ESPERA.remove(cliente);
        }
    }
}