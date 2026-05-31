package com.thunder.launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.nio.charset.StandardCharsets;

// import pa usar AES
import com.thunder.launcher.SessionManager;

public class Main extends Application {

    @Override
    public void start(Stage ventana) throws Exception {
        System.out.println("======================================");
        System.out.println("creando las carpetas del bunker");
        System.out.println("======================================");

        try {
            String carpetaHome = System.getProperty("user.home");
            Path rutaCarpeta = Paths.get(carpetaHome, ".thunder");

            if (Files.notExists(rutaCarpeta)) {
                Files.createDirectory(rutaCarpeta);
                System.out.println("la carpeta se creo XD");
            } else {
                System.out.println("la carpeta .thunder ya existia de pana");
            }
        } catch (Exception e) {
            System.out.println("error de escritura CFFFFFFFFFFF " + e.getMessage());
        }

        // ==========================================================
        // lector de AES
        // ==========================================================
        String carpetaHome = System.getProperty("user.home");
        Path rutaArchivo = Paths.get(carpetaHome, ".thunder", "user_session.txt");

        String fxmlInicial = "bienvenida.fxml"; // fallback por si no hay sesion XD

        try {
            if (Files.exists(rutaArchivo)) {
                String contenido = Files.readString(rutaArchivo, StandardCharsets.UTF_8).trim();

                // detectar el coso viejo y pumba poner AES
                if (!contenido.startsWith("AES_V1:")) {
                    System.out.println("[WARN] sesion antigua detectada PROTOCOLO AUTODESTRUCCION BORRANDO SYSTEM32 na mentira XDXD");
                    Files.deleteIfExists(rutaArchivo);
                    // Forzar login nuevo
                } else {
                    // poner AES y desencriptar
                    String datosEncriptados = contenido.substring("AES_V1:".length());
                    String datos = SessionManager.desencriptar(datosEncriptados);

                    if (datos != null && datos.contains(":")) {
                        String[] partes = datos.split(":", 2);
                        String usuario = partes[0];
                        // String password = partes[1]; // Si lo necesito dsp

                        System.out.println("[AUTO-LOGIN] Sesión AES vvlida para " + usuario);
                        fxmlInicial = "MainFxml.fxml"; // exito al launcher principal ahora
                    } else {
                        System.out.println("[WARN] Sesión AES inválida o corrupta");
                        Files.deleteIfExists(rutaArchivo); // Limpiar y forzar login
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[WARN] Error leyendo sesion: " + e.getMessage());
            // fallback por si falla
        }

        System.out.println("[OK] cargando el fxml inteligente " + fxmlInicial);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlInicial));
        Parent root = loader.load();

        Scene escena = new Scene(root, 500, 500);

        // XD
        if (fxmlInicial.equals("bienvenida.fxml")) {
            escena.getStylesheets().add(Objects.requireNonNull(getClass().getResource("bienvenida.css")).toExternalForm());
        }

        ventana.setTitle("com.thunder.launcher.ThunderLauncher v1.0");
        ventana.setScene(escena);
        ventana.show();
    }
}