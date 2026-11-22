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
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
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

    private static synchronized void difundir(Mensaje mensaje) {
        for (Cliente cliente : CLIENTES) {
            cliente.enviar(mensaje);
        }
    }

    private static final class Cliente implements Runnable {
        private final Socket socket;
        private ObjectOutputStream salida;
        private JugadorPerfil jugadorAutenticado;
        private Cliente rival;

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
                System.out.println("Cliente Desconectado: " + this.jugadorAutenticado.getNombreUsuario());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error del Cliente " + socket.getPort() + ": " + e.getMessage());
            } finally {
                CLIENTES.remove(this);
                SALA_ESPERA.remove(this);
                if (this.rival != null) {
                    this.rival.rival = null;
                    this.rival = null;
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("No se pudo cerrar el socket");
                }
            }
        }


        //metodo para que el servidor sepa que tipo de mensaje es
        private void procesarMensaje(Mensaje msg) {
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
                    this.enviar(new Mensaje(Mensaje.aviso.LOGIN_OK, "Inicio de Sesión Existoso", jugador));
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
                    db.guardarJugador(datosLogin);

                    //si paso validaciones enviamos un mensaje de exito y guardamos sus datos
                    this.jugadorAutenticado = datosLogin;

                    JugadorPerfil perfilCliente = new JugadorPerfil();
                    perfilCliente.setNombreUsuario(datosLogin.getNombreUsuario());
                    perfilCliente.setRango(datosLogin.getRango());
                    perfilCliente.setPartidasGanadas(datosLogin.getPartidasGanadas());
                    perfilCliente.setPartidasPerdidas(datosLogin.getPartidasPerdidas());
                    perfilCliente.setPuntos(datosLogin.getPuntos());

                    this.enviar(new Mensaje(Mensaje.aviso.LOGIN_OK, "Cuenta Creada Correctamente",perfilCliente));
                    System.out.println("Usuario Registrado y Conectado: "+this.jugadorAutenticado.getNombreUsuario());
                    return;
                }
                case HISTORIAL: {
                    JugadorPerfil datosLogin = msg.getJugadorActual();
                    String nombre = datosLogin.getNombreUsuario();

                    ArrayList<HistorialPartida> lista = db.obtenerHistorialJugador(nombre);

                    this.enviar(new Mensaje(Mensaje.aviso.HISTORIAL_OK, lista));
                    return;
                }
                case BUSCAR_PARTIDA: {
                    if (msg.getJugadorActual() != null && msg.getJugadorActual().getNombreUsuario() != null) {
                        this.jugadorAutenticado = msg.getJugadorActual();
                    }
                    procesarBusqueda(this);
                    return;
                }
                case CANCELAR_BUSQUEDA:{
                    cancelarBusqueda(this);
                    this.enviar(new Mensaje(Mensaje.aviso.BUSQUEDA_CANCELADA,"Busqueda Cancelada"));
                    return;
                }
                default:
                    System.err.println("Mensaje Desconocido " + msg.getAviso());
            }
        }

        private static synchronized void procesarBusqueda(Cliente solicitante) {
            //si ya esta esperando no lo agregamos
            if (SALA_ESPERA.contains(solicitante)) {
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

            solicitante.rival = rival;
            rival.rival = solicitante;

            String nombreSolicitante = solicitante.jugadorAutenticado.getNombreUsuario();
            String nombreRival = rival.jugadorAutenticado.getNombreUsuario();

            //le enviamos al rival y al primer usuario en espera los datos del otro
            solicitante.enviar(new Mensaje(Mensaje.aviso.PARTIDA_OK, rival.jugadorAutenticado));
            rival.enviar(new Mensaje(Mensaje.aviso.PARTIDA_OK, solicitante.jugadorAutenticado));

        }

        //metodo para enviar mensajes a un cliente
        private synchronized void enviar(Mensaje mensaje) {
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

        private static synchronized void cancelarBusqueda(Cliente solicitante) {
            SALA_ESPERA.remove(solicitante);
            System.out.println("Jugador cancelo búsqueda: " + solicitante.jugadorAutenticado.getNombreUsuario());
        }
    }
}