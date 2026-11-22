package com.fproject.servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class DescubrimientoServidor {
    /*
    las respuestas unicas de nuestro juego son:
    - MONKEY_BUSCAR (algun jugador aqui?)
    - MONKEY_AQUI (hola, soy un jugador!)
    asi sabrán que se comunican entre si
    */

    private final int puertoUDP;
    private final int puertoTCP;
    private volatile DatagramSocket socketUDP;
    private Thread hiloBuscar;

    public DescubrimientoServidor(int puertoUDP, int puertoTCP) {
        this.puertoUDP = puertoUDP;
        this.puertoTCP = puertoTCP;
    }

    public void iniciar() {
        hiloBuscar = new Thread(this::buscar, "DescubrimientoUDP");
        hiloBuscar.start();
    }

    private void buscar() {
        try (DatagramSocket socket = new DatagramSocket(puertoUDP)) {
            //configuramos socket
            this.socketUDP = socket;
            byte[] buffer = new byte[256];

            //esperamos a que llegue algun paquete
            while (true) {
                DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
                socket.receive(peticion);

                String mensaje = new String(peticion.getData(), 0, peticion.getLength());
                //si nos estan buscando le decimos que estamos aqui (que somos del mismo juego)
                if (mensaje.equals("MONKEY_BUSCAR")) {
                    byte[] respuesta = ("MONKEY_AQUI" + puertoTCP).getBytes(StandardCharsets.UTF_8);
                    socket.send(new DatagramPacket(respuesta, respuesta.length, peticion.getAddress(), peticion.getPort()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error en UDP: " + e.getMessage());
        }
    }

    public void detener(){
        DatagramSocket socket = this.socketUDP;
        if(socket != null && !socket.isClosed()){
            socket.close();
        }
    }
}

