package com.fproject.datos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//clase para guardar los datos de la red a la que se conectó el cliente y enviar/recibir mensajes
public class RedCliente {
    private static Socket socket;
    private static ObjectInputStream entrada;
    private static ObjectOutputStream salida;

    public static synchronized void conectar(String ip, int puerto) throws IOException {
        if(socket != null && !socket.isClosed()){
            return;
        }
        socket = new Socket(ip, puerto);
        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(socket.getInputStream());
    }

    public static synchronized void enviar(Mensaje msg) throws IOException {
        salida.writeObject(msg);
        salida.flush();
        salida.reset();
    }

    public static Mensaje recibir() throws IOException, ClassNotFoundException {
        return (Mensaje) entrada.readObject();
    }

    public static void cerrar() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
