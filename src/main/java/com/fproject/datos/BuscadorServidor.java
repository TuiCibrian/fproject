package com.fproject.datos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class BuscadorServidor {
    public record ServidorEncontrado(String ip, int puertoTCP){}

    public static ServidorEncontrado buscar(int puertoUDP, int espera){
        try(DatagramSocket socket = new DatagramSocket()){
            //configuramos socket
            socket.setBroadcast(true);
            socket.setSoTimeout(espera);

            byte[] envio = "MONKEY_BUSCAR".getBytes(StandardCharsets.UTF_8);
            //255.255.255.255 se lo envia a todos los de la misma red local
            InetAddress difundir = InetAddress.getByName("255.255.255.255");
            socket.send(new DatagramPacket(envio,envio.length,difundir,puertoUDP));
            byte[] buffer = new byte[256];
            DatagramPacket respuesta = new DatagramPacket(buffer,buffer.length);
            socket.receive(respuesta);

            //si encuentra algun jugador/cliente guardamos sus datos
            String texto = new String(respuesta.getData(),0,respuesta.getLength(),StandardCharsets.UTF_8);
            if(texto.startsWith("MONKEY_AQUI")){
                int puertoTCP = Integer.parseInt(texto.substring("MONKEY_AQUI".length()).trim());
                return new ServidorEncontrado(respuesta.getAddress().getHostAddress(),puertoTCP);
            }
        }
        catch(SocketTimeoutException e){
            System.err.println("Tiempo de Espera Agotado");
            return null;
        }
        catch(IOException | NumberFormatException e){
            System.err.println("Error en busqueda UDP: "+e.getMessage());
        }
        return null;
    }
}
