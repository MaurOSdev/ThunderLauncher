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
        System.out.println("[OK] Cargando el fxml del futuro...");

        // cargamos el fxml asi bien fuafufaufaufaf
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/bienvenida.fxml"));
        Parent root = loader.load();

        // creamos esta wa y la centramos sisisi
        Scene escena = new Scene(root, 500, 500);

        // Si quieres meterle el CSS desde Java, se hace así de una:
        escena.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/bienvenida.css")).toExternalForm());

        ventana.setTitle("com.thunder.launcher.ThunderLauncher v1.0");
        ventana.setScene(escena);
        ventana.setScene(null); // nota mia pa que no se me olvide esta wa En javafx puro esto no existe igual se centra solo o se maneja por el os
        ventana.show(); // abracadara
    }

}