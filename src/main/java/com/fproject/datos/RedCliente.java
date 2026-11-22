package com.fproject.datos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javafx.application.Platform;

//clase para guardar los datos de la red a la que se conectó el cliente y enviar/recibir mensajes
public class RedCliente {
    private static Socket socket;
    private static ObjectOutputStream salida;
    private static OyenteRed oyente;

    //cada pantalla se registra como oyente para recibir lo que manda el servidor
    public interface OyenteRed {
        void recibir(Mensaje msg);
    }

    public static void setOyente(OyenteRed nuevoOyente) {
        oyente = nuevoOyente;
    }

    public static synchronized void conectar(String ip, int puerto) throws IOException {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        Socket nuevoSocket = new Socket(ip, puerto);
        salida = new ObjectOutputStream(nuevoSocket.getOutputStream());
        salida.flush();
        ObjectInputStream entrada = new ObjectInputStream(nuevoSocket.getInputStream());
        socket = nuevoSocket;

        //un solo hilo lee todo lo del servidor, asi no se mezclan los mensajes entre pantallas
        Thread hiloLector = new Thread(() -> leer(nuevoSocket, entrada));
        hiloLector.setDaemon(true);
        hiloLector.start();
    }

    private static void leer(Socket socketLector, ObjectInputStream entrada) {
        try {
            while (true) {
                Mensaje msg = (Mensaje) entrada.readObject();
                Platform.runLater(() -> entregar(msg));
            }
        } catch (IOException | ClassNotFoundException e) {
            //si se cerró con cerrar() no hay que avisar nada
            if (cerrarSocket(socketLector)) {
                Mensaje perdida = new Mensaje(Mensaje.aviso.CONEXION_PERDIDA, "Se perdió la conexión con el servidor");
                Platform.runLater(() -> entregar(perdida));
            }
        }
    }

    //se lee el oyente al momento de entregar, porque la pantalla pudo cambiar
    private static void entregar(Mensaje msg) {
        if (oyente != null) {
            oyente.recibir(msg);
        }
    }

    private static synchronized boolean cerrarSocket(Socket socketLector) {
        if (socket != socketLector) {
            return false;
        }
        cerrar();
        return true;
    }

    public static synchronized void enviar(Mensaje msg) throws IOException {
        if (salida == null) {
            throw new IOException("No hay conexión con el servidor");
        }
        salida.writeObject(msg);
        salida.flush();
        salida.reset();
    }

    public static synchronized void cerrar() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
        salida = null;
    }
}
