package com.fproject.datos;

import java.security.MessageDigest;

/*
Utilidades de seguridad
- Hash SHA-256 para las contraseñas
 */
public class Seguridad {

    public static String hashSHA256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                String valor = Integer.toHexString(0xff & b);
                if (valor.length() == 1) {
                    hex.append("0");
                }
                hex.append(valor);
            }

            return hex.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
