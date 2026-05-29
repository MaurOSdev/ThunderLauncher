package com.thunder.launcher;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainFxmlController {

    @FXML
    private Button btnJugar;

    private String obtenerUsuarioDeSesion() {
        try {
            String carpetaHome = System.getProperty("user.home");
            Path rutaArchivo = Paths.get(carpetaHome, ".thunder", "user_session.txt");

            if (Files.exists(rutaArchivo) && Files.size(rutaArchivo) > 70) {
                String contenido = Files.readString(rutaArchivo, StandardCharsets.UTF_8).trim();

                int lenBasuraInicio = Integer.parseInt(contenido.substring(0, 3));
                int lenBasuraMedio = Integer.parseInt(contenido.substring(3, 6));

                String dataConBasura = contenido.substring(6);
                String desdeUsuario = dataConBasura.substring(lenBasuraInicio);

                String usuarioPuroConBasuraMedio = desdeUsuario.substring(0, desdeUsuario.length() - 64);
                String usuarioReal = usuarioPuroConBasuraMedio.substring(0, usuarioPuroConBasuraMedio.length() - lenBasuraMedio);

                System.out.println("[MOTOR] usuario recuperado de tu token: " + usuarioReal);
                return usuarioReal;
            }
        } catch (Exception e) {
            System.out.println("[WARN] No se pudo descifrar la sesion fallback " + e.getMessage());
        }
        return "ThunderPlayer";
    }

    // Engaño de UUID para cuentas offline
    private String generarUUIDOffline(String nickname) {
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + nickname).getBytes(StandardCharsets.UTF_8));
        return uuid.toString().replace("-", "");
    }

    @FXML
    private void lanzarMinecraft() {
        System.out.println("[MOTOR] INICIANDOOOOO");

        // 1. METEMOS TODO EN UN HILO SEPARADO para que descargue sin congelar la GUI
        new Thread(() -> {
            try {
                String urlJsonMojang = "https://piston-meta.mojang.com/v1/packages/7dfdbbdf9f50ad32650668bbb3897e58ef50abc5/26.1.1.json";

                // llamamos al fuego
                System.out.println("[MOTOR] DownloaderEngine AHORA");
                DownloaderEngine motorDescarga = new DownloaderEngine();
                motorDescarga.iniciarDescargaTotal(urlJsonMojang);

                // recuperamos cosas
                String carpetaHome = System.getProperty("user.home");

                // usamos un usuario XD
                String usuarioLogeado = obtenerUsuarioDeSesion();

                String rutaMinecraft = carpetaHome + File.separator + ".thunder";
                String rutaAssets = rutaMinecraft + File.separator + "assets";
                String rutaLibraries = rutaMinecraft + File.separator + "libraries";
                String rutaVersions = rutaMinecraft + File.separator + "versions" + File.separator + "26.1.1";

                List<String> comandos = new ArrayList<>();

                comandos.add("java");
                comandos.add("-Xmx2G");
                comandos.add("-XX:+UseG1GC");

                comandos.add("-Djava.library.path=" + rutaVersions + File.separator + "natives");

                comandos.add("-cp");
                comandos.add(rutaLibraries + File.separator + "*" + File.pathSeparator + rutaVersions + File.separator + "26.1.1.jar");

                comandos.add("net.minecraft.client.main.Main");

                // engao supremo
                comandos.add("--username");
                comandos.add(usuarioLogeado);

                comandos.add("--uuid");
                comandos.add(generarUUIDOffline(usuarioLogeado));

                comandos.add("--accessToken");
                comandos.add("00000000000000000000000000000000");

                comandos.add("--userType");
                comandos.add("mojang");

                comandos.add("--version");
                comandos.add("26.1.1");

                comandos.add("--gameDir");
                comandos.add(rutaMinecraft);

                comandos.add("--assetsDir");
                comandos.add(rutaAssets);

                comandos.add("--assetIndex");
                comandos.add("26.1.1");

                // AHORA YA
                ProcessBuilder pb = new ProcessBuilder(comandos);
                pb.directory(new File(rutaMinecraft));
                pb.inheritIO();

                System.out.println("[MOTOR] lanzando los procesinis");
                Process procesoMinecraft = pb.start();

                System.out.println("[OK] a viciar noma");

            } catch (Exception e) {
                System.out.println("[WARN] el proccesbuilder no encontro ni weas " + e.getMessage());
                e.printStackTrace();
            }
        }).start(); // FUEGO
    }

    @FXML
    public void initialize() {
        System.out.println("[OK] panel listo");
    }
}
