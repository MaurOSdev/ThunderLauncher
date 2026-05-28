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
        // ufff ojala cargue
        // ==========================================================
        System.out.println("[OK] cargando el fxml");

        // agregamos la barrita pa que no explote esta mrd
        FXMLLoader loader = new FXMLLoader(getClass().getResource("bienvenida.fxml"));
        Parent root = loader.load();

        // creamos esta wa y la centramos sisisi
        Scene escena = new Scene(root, 500, 500);

        // ahora zi
        escena.getStylesheets().add(Objects.requireNonNull(getClass().getResource("bienvenida.css")).toExternalForm());

        ventana.setTitle("com.thunder.launcher.ThunderLauncher v1.0");
        ventana.setScene(escena); // se queda asi y nada mas csm
        ventana.show(); // abracadara
    }
}
