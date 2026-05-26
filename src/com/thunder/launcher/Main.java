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

        // una logica asi bien waos
        try {
            Path rutaCarpeta = Paths.get(".thunderlauncher_data");
            if (Files.notExists(rutaCarpeta)) {
                Files.createDirectory(rutaCarpeta);
                System.out.println("LA CARPETA JKDJAKFJA SE CREO SJFJKASNFKJSAXNFKASNCSLKA");
            } else {
                System.out.println("la carpeta ya existia en un rincon del sistema");
            }
        } catch (Exception e) {
            System.out.println("error de escritura no se pudo crear la carpeta porque la mosca nos la mojsuizjvuisdjviknvklvmvnkm" + e.getMessage());
        }

        // ==========================================================
        // ufff ojala cargue
        // ==========================================================
        System.out.println("[OK] cargando el fxml");

        // agregamos la / pa que no explote esta wa
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/bienvenida.fxml"));
        Parent root = loader.load();

        // creamos esta wa y la centramos sisisi
        Scene escena = new Scene(root, 500, 500);

        // ahora zi
        escena.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/bienvenida.css")).toExternalForm());

        ventana.setTitle("com.thunder.launcher.ThunderLauncher v1.0");
        ventana.setScene(escena); // se queda asi y nada mas csm
        ventana.show(); // abracadara
    }
}