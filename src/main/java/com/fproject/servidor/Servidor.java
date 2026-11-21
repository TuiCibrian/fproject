package com.fproject.servidor;

import com.fproject.datos.ManejadorDB4O;
import com.fproject.datos.Mensaje;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Servidor {
    private static final int PUERTO_PREDETERMINADO = 12345;
    private static final List<Cliente> CLIENTES = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        //recibe puerto de argumentos o tomamos predeterminado
        int puerto = args.length == 0 ? PUERTO_PREDETERMINADO : Integer.parseInt(args[0]);

        /*
        El servidor manejará la base de datos
        los clientes solo se comunican con el servidor y reciben info
        */
        ManejadorDB4O.getInstancia();
        //Si se cierra el servidor también cerramos la base de datos
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
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

        private Cliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            //codigo para recibir datos de clientes
        }

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
    }
}