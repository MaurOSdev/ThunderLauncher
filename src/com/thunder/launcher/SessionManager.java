package com.thunder.launcher;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.MessageDigest;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

// SesionManager esta vez encriptado con AES
public class SessionManager {
    private static final String SALT = "ThunderLauncher|MaurOSdev|Seguro";

    private static byte[] generarClave() throws Exception {
        String semilla = System.getProperty("user.home") + "|" + SALT;
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha.digest(semilla.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOf(hash, 16); // AES-128 usa 16 bytes
    }

    public static String encriptar(String texto) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(generarClave(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cifrado);
        } catch (Exception e) {
            System.err.println("error encriptando: " + e.getMessage());
            return null;
        }
    }

    public static String desencriptar(String textoEncriptado) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(generarClave(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodificado = Base64.getDecoder().decode(textoEncriptado);
            byte[] original = cipher.doFinal(decodificado);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null; // si la clave no coincide directo al /dev/null
        }
    }
}
