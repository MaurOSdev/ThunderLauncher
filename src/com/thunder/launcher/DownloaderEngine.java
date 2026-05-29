package com.thunder.launcher;

import com.google.gson.Gson;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloaderEngine {

    private final String rutaMinecraft;
    private final HttpClient client;
    private final Gson gson;

    public DownloaderEngine() {
        String carpetaHome = System.getProperty("user.home");
        this.rutaMinecraft = carpetaHome + File.separator + ".thunder";
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // descarga archivos base
    public void descargarArchivoInmediato(String urlDescarga, String rutaDestino) {
        try {
            File archivo = new File(rutaDestino);
            if (archivo.exists()) {
                // si el archivo existe ps no lo descargamos
                return;
            }

            // crear carpeta si no existen aunque lo mas probable esq existan
            archivo.getParentFile().mkdirs();

            System.out.println("[DOWNLOADER] Descargando: " + urlDescarga + " -> " + archivo.getName());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlDescarga))
                    .build();

            // descarga al disco
            client.send(request, HttpResponse.BodyHandlers.ofFile(archivo.toPath()));

        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Fallo al descargar " + urlDescarga + ": " + e.getMessage());
        }
    }

    // aqui la magia d1
    public void iniciarDescargaTotal(String urlJsonVersion) {
        try {
            System.out.println("[DOWNLOADER] iniciando descarga 1.. 2... 3.. YA");

            // Rutas base en el bunker .thunder
            String rutaJsonLocal = rutaMinecraft + File.separator + "versions" + File.separator + "26.1.1" + File.separator + "26.1.1.json";
            String rutaJarCliente = rutaMinecraft + File.separator + "versions" + File.separator + "26.1.1" + File.separator + "26.1.1.jar";
            String carpetaLibraries = rutaMinecraft + File.separator + "libraries";

            // bajar el json si no lo tenes
            descargarArchivoInmediato(urlJsonVersion, rutaJsonLocal);

            // PASO B: Leer el JSON con GSON
            System.out.println("[GSON] desarmando el json");
            java.io.Reader reader = Files.newBufferedReader(Paths.get(rutaJsonLocal), StandardCharsets.UTF_8);
            MinecraftVersionJson versionData = gson.fromJson(reader, MinecraftVersionJson.class);
            reader.close();

            // PASO C: Descargar el 26.1.1.jar principal del cliente
            if (versionData.downloads != null && versionData.downloads.client != null) {
                System.out.println("[DOWNLOADER] validando el ejecutable del juego");
                descargarArchivoInmediato(versionData.downloads.client.url, rutaJarCliente);
            }

            // descargar todas las librerias
            System.out.println("[DOWNLOADER] Analizando librerias del sistema");
            for (MinecraftVersionJson.Library lib : versionData.libraries) {

                // descargando librerias estandar
                if (lib.downloads != null && lib.downloads.artifact != null) {
                    String urlLib = lib.downloads.artifact.url;
                    // simplificamos esto
                    String nombreArchivoLib = urlLib.substring(urlLib.lastIndexOf('/') + 1);
                    String destinoFinalLib = carpetaLibraries + File.separator + nombreArchivoLib;

                    descargarArchivoInmediato(urlLib, destinoFinalLib);
                }

                // 2. Filtro Pro para tu VM de Arch Linux (Natives .so)
                if (lib.natives != null && lib.natives.containsKey("linux") && lib.downloads != null && lib.downloads.classifiers != null) {
                    String claveNativa = lib.natives.get("linux"); // Da "natives-linux"
                    MinecraftVersionJson.DownloadItem itemLinux = lib.downloads.classifiers.get(claveNativa);

                    if (itemLinux != null) {
                        String urlNative = itemLinux.url;
                        String nombreNative = urlNative.substring(urlNative.lastIndexOf('/') + 1);
                        String destinoNative = rutaMinecraft + File.separator + "versions" + File.separator + "26.1.1" + File.separator + "natives" + File.separator + nombreNative;

                        System.out.println("[SISTEMA] encontrando librerias nativas");
                        descargarArchivoInmediato(urlNative, destinoNative);
                    }
                }
            }

            System.out.println("[DOWNLOADER] Todo listo");

        } catch (Exception e) {
            System.out.println("[DOWNLOADER] algo fallo en la descarga " + e.getMessage());
            e.printStackTrace();
        }
    }
}
