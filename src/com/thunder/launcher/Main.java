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
            System.out.println("error de escritura cf " + e.getMessage());
        }

        // ==========================================================
        // ahora EL DESFOBUSCADOR
        // ==========================================================
        String carpetaHome = System.getProperty("user.home");
        Path rutaArchivo = Paths.get(carpetaHome, ".thunder", "user_session.txt");

        String fxmlInicial = "bienvenida.fxml"; // fallback por si no existe token por si las moscas XD

        try {
            // minimo mas de 70 caracteres ahora
            if (Files.exists(rutaArchivo) && Files.size(rutaArchivo) > 70) {
                String contenido = Files.readString(rutaArchivo, StandardCharsets.UTF_8).trim();

                System.out.println("[sistema] leyendo los tokens");

                // desempaquetamos los indices de control XD
                int lenBasuraInicio = Integer.parseInt(contenido.substring(0, 3));
                int lenBasuraMedio = Integer.parseInt(contenido.substring(3, 6));

                // solo quedarnos con la data
                String dataConBasura = contenido.substring(6);

                // rebanamos la basura como un pepino sisisiii
                String desdeUsuario = dataConBasura.substring(lenBasuraInicio);

                // siempre mide 1 stack de caracteres XD
                String hashGuardado = desdeUsuario.substring(desdeUsuario.length() - 64);

                // ahora esto
                String usuarioPuroConBasuraMedio = desdeUsuario.substring(0, desdeUsuario.length() - 64);
                String usuarioGuardado = usuarioPuroConBasuraMedio.substring(0, usuarioPuroConBasuraMedio.length() - lenBasuraMedio);

                if (!usuarioGuardado.isEmpty() && hashGuardado.length() == 64) {
                    System.out.println("[AUTO-LOGIN] ¡Descifrado de mapa de 3 digitos exitoso!");
                    System.out.println("[AUTO-LOGIN] Bienvenido " + usuarioGuardado);

                    fxmlInicial = "MainFxml.fxml"; // esto es lo shidori
                }
            }
        } catch (Exception e) {
            System.out.println("[WARN] token corrupto seguramente " + e.getMessage());
        }

        System.out.println("[OK] cargando el fxml inteligente: " + fxmlInicial);

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