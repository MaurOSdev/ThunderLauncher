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
import java.util.List;
import java.util.Map;

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

    // Descarga archivos base al disco
    public void descargarArchivoInmediato(String urlDescarga, String rutaDestino) {
        try {
            File archivo = new File(rutaDestino);
            if (archivo.exists()) {
                // aplicamos la de yo no lo descargo porque ya lo tengo
                return;
            }

            archivo.getParentFile().mkdirs();

            System.out.println("[DOWNLOADER] Descargando: " + urlDescarga + " -> " + archivo.getName());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlDescarga))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofFile(archivo.toPath()));

        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Fallo al descargar " + urlDescarga + ": " + e.getMessage());
        }
    }

    // Aquí corre la magia total
    public void iniciarDescargaTotal(String urlJsonVersion) {
        try {
            System.out.println("[DOWNLOADER] iniciando descarga 1.. 2... 3.. YA");

            // Rutas base en el bunker .thunder
            String rutaJsonLocal = rutaMinecraft + File.separator + "versions" + File.separator + "26.1.1" + File.separator + "26.1.1.json";
            String rutaJarCliente = rutaMinecraft + File.separator + "versions" + File.separator + "26.1.1" + File.separator + "26.1.1.jar";
            String carpetaLibraries = rutaMinecraft + File.separator + "libraries";

            // Bajar el json maestro si no lo tenes
            descargarArchivoInmediato(urlJsonVersion, rutaJsonLocal);

            // PASO B: Leer el JSON con GSON
            System.out.println("[GSON] desarmando el json de la version");
            java.io.Reader reader = Files.newBufferedReader(Paths.get(rutaJsonLocal), StandardCharsets.UTF_8);
            MinecraftVersionJson versionData = gson.fromJson(reader, MinecraftVersionJson.class);
            reader.close();

            // PASO C: Descargar el 26.1.1.jar principal del cliente
            if (versionData.downloads != null && versionData.downloads.client != null) {
                System.out.println("[DOWNLOADER] validando el ejecutable del juego");
                descargarArchivoInmediato(versionData.downloads.client.url, rutaJarCliente);
            }

            // Descargar todas las librerías del sistema
            System.out.println("[DOWNLOADER] Analizando librerias del sistema");
            for (MinecraftVersionJson.Library lib : versionData.libraries) {

                // Descargando librerías estándar
                if (lib.downloads != null && lib.downloads.artifact != null) {
                    String urlLib = lib.downloads.artifact.url;
                    String nombreArchivoLib = urlLib.substring(urlLib.lastIndexOf('/') + 1);
                    String destinoFinalLib = carpetaLibraries + File.separator + nombreArchivoLib;

                    descargarArchivoInmediato(urlLib, destinoFinalLib);
                }

                // filtro pro
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

            // ==========================================================
            // descargar la version
            // ==========================================================
            if (versionData.assetIndex != null) {
                System.out.println("[DOWNLOADER] Iniciando descarga automatica de assets");

                String rutaAssetIndexes = rutaMinecraft + File.separator + "assets" + File.separator + "indexes";
                String rutaAssetObjects = rutaMinecraft + File.separator + "assets" + File.separator + "objects";

                new File(rutaAssetIndexes).mkdirs();
                new File(rutaAssetObjects).mkdirs();

                // Descargar el asset index JSON oficial
                String rutaAssetIndexJson = rutaAssetIndexes + File.separator + versionData.assetIndex.id + ".json";
                descargarArchivoInmediato(versionData.assetIndex.url, rutaAssetIndexJson);

                // Desarmar el mapa con GSON
                System.out.println("[GSON] desarmando el mapa de hashes");
                java.io.Reader assetReader = Files.newBufferedReader(Paths.get(rutaAssetIndexJson), StandardCharsets.UTF_8);
                AssetIndexJson assetIndex = gson.fromJson(assetReader, AssetIndexJson.class);
                assetReader.close();

                System.out.println("[DOWNLOADER] Descargando " + assetIndex.objects.size() + " sonidos y texturas");
                int contador = 0;
                for (Map.Entry<String, AssetIndexJson.AssetObject> entry : assetIndex.objects.entrySet()) {
                    String hash = entry.getValue().hash;
                    String hashPrefix = hash.substring(0, 2);
                    String urlObjeto = "https://resources.download.minecraft.net/" + hashPrefix + "/" + hash;
                    String destinoObjeto = rutaAssetObjects + File.separator + hashPrefix + File.separator + hash;

                    descargarArchivoInmediato(urlObjeto, destinoObjeto);
                    contador++;

                    // Log silencioso industrial cada 100 archivos para no laguear la GUI
                    if (contador % 100 == 0) {
                        System.out.println("[DOWNLOADER] " + contador + "/" + assetIndex.objects.size() + " assets procesados de pana");
                    }
                }

                System.out.println("[OK] listo " + contador + " recursos inyectados");
            }

            System.out.println("[DOWNLOADER] Todo listo");

        } catch (Exception e) {
            System.out.println("[DOWNLOADER] algo fallo en la descarga " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================================
    // maquetas pojo (porfavor funciona)
    // ==========================================================
    public static class MinecraftVersionJson {
        public Downloads downloads;
        public List<Library> libraries;
        public AssetIndexInfo assetIndex;

        public static class Downloads {
            public DownloadItem client;
        }

        public static class DownloadItem {
            public String url;
        }

        public static class AssetIndexInfo {
            public String id;
            public String url;
        }

        public static class Library {
            public LibraryDownloads downloads;
            public Map<String, String> natives;
        }

        public static class LibraryDownloads {
            public DownloadItem artifact;
            public Map<String, DownloadItem> classifiers;
        }
    }

    public static class AssetIndexJson {
        public Map<String, AssetObject> objects;

        public static class AssetObject {
            public String hash;
            public long size;
        }
    }
}