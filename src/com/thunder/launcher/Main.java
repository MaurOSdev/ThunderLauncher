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

// heredamos la wa clasica a javafx
public class Main extends Application {

    @Override
    public void start(Stage ventana) throws Exception {
        System.out.println("======================================");
        System.out.println("creando las carpetas");
        System.out.println("======================================");

        // esta wa crea las carpeta JSIJFAHJIKFS
        try {
            String carpetaHome = System.getProperty("user.home");
            Path rutaCarpeta = java.nio.file.Paths.get(carpetaHome, ".thunder");

            if (java.nio.file.Files.notExists(rutaCarpeta)) {
                java.nio.file.Files.createDirectory(rutaCarpeta);
                System.out.println("la carpeta se creo XD");
            } else {
                System.out.println("la carpeta .thunder existia en... bueno algun lado");
            }
        } catch (Exception e) {
            System.out.println("error de escritura porque la mosca nos las djkansfiuAJcIJhYUAXCHDSJZHXBDHKJJ jh " + e.getMessage());
        }

        // ==========================================================
        // ojala cargue de pana
        // ==========================================================
        String carpetaHome = System.getProperty("user.home");
        Path rutaCarpeta = Paths.get(carpetaHome, ".thunder");
        Path rutaArchivo = rutaCarpeta.resolve("user_session.txt");

        String fxmlInicial = "bienvenida.fxml"; // por defecto la bienveida cffffff
        String cssInicial = "bienvenida.css";   // Su CSS por si las moscas

        try {
            // ahora cambiamos la sesion
            if (Files.exists(rutaArchivo) && Files.size(rutaArchivo) > 0) {
                String usuarioGuardado = Files.readString(rutaArchivo, java.nio.charset.StandardCharsets.UTF_8).trim();
                if (!usuarioGuardado.isEmpty()) {
                    System.out.println("[AUTO-LOGIN] Detectada cuenta de: " + usuarioGuardado + ". Saltando directo al panel principal!");
                    fxmlInicial = "MainFxml.fxml"; // redirigimos a Main
                    cssInicial = "principal.css";  // cambiamos el css
                }
            }
        } catch (Exception e) {
            System.out.println("[WARN] no se pudo encontrar la sesion " + e.getMessage());
        }

        System.out.println("[OK] cargando el fxml " + fxmlInicial);

        // Cargamos la wa
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlInicial));
        Parent root = loader.load();

        // creamos esta wa y la centramos sisisi
        Scene escena = new Scene(root, 500, 500);

        // C
        try {
            if (getClass().getResource(cssInicial) != null) {
                escena.getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssInicial)).toExternalForm());
            } else {
                System.out.println("[WARN] no se encontro el css " + cssInicial + "iniciando normal");
            }
        } catch (Exception e) {
            System.out.println("[WARN] Fallo la wea de estilos JAJAJA " + e.getMessage());
        }

        ventana.setTitle("com.thunder.launcher.ThunderLauncher v1.0");
        ventana.setScene(escena); // se queda asi y nada mas csm
        ventana.show(); // abracadara
    }
}