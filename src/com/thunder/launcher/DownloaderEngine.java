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
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;

public class DownloaderEngine {

    private final String rutaMinecraft;
    private final HttpClient client;
    private final Gson gson;

    // callback para reportar el progreso a la ui
    public interface ProgresoCallback {
        void onProgreso(int descargados, int total);
    }
    private ProgresoCallback callbackProgreso;

    public DownloaderEngine() {
        String carpetaHome = System.getProperty("user.home");
        this.rutaMinecraft = carpetaHome + File.separator + ".thunder";
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // setter para registrar el callback en la ui
    public void setProgresoCallback(ProgresoCallback cb) {
        this.callbackProgreso = cb;
    }

    // Descarga archivos base al disco
    public void descargarArchivoInmediato(String urlDescarga, String rutaDestino) {
        try {
            File archivo = new File(rutaDestino);
            if (archivo.exists()) {
                return;
            }

            archivo.getParentFile().mkdirs();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlDescarga))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofFile(archivo.toPath()));

        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Fallo: " + e.getMessage());
        }
    }
    // ahora esta wa
    private void extraerJar(String jarPath, String destinoDir) throws Exception {
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                File destFile = new File(destinoDir, entry.getName());

                if (entry.isDirectory()) {
                    destFile.mkdirs();
                } else {
                    destFile.getParentFile().mkdirs();
                    try (java.io.InputStream is = jar.getInputStream(entry);
                         java.io.FileOutputStream fos = new java.io.FileOutputStream(destFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    // acepta version dinamica y reporta progreso esta wa
    public void iniciarDescargaTotal(String urlJsonVersion, String version) {
        try {
            System.out.println("[DOWNLOADER] Descargando versión: " + version);

            // rutas dinamicas segun la version
            String rutaVersiones = rutaMinecraft + File.separator + "versions" + File.separator + version;
            String rutaJsonLocal = rutaVersiones + File.separator + version + ".json";
            String rutaJarCliente = rutaVersiones + File.separator + version + ".jar";
            String carpetaLibraries = rutaVersiones + File.separator + "libraries";
            Files.createDirectories(Paths.get(carpetaLibraries));
            String rutaNatives = rutaVersiones + File.separator + "natives";

            // descargar json de la version
            descargarArchivoInmediato(urlJsonVersion, rutaJsonLocal);

            // leer json con gson
            java.io.Reader reader = Files.newBufferedReader(Paths.get(rutaJsonLocal), StandardCharsets.UTF_8);
            MinecraftVersionJson versionData = gson.fromJson(reader, MinecraftVersionJson.class);
            reader.close();

// aca sin redeclarar versionData
            String assetIndexId = (versionData.assetIndex != null && versionData.assetIndex.id != null)
                    ? versionData.assetIndex.id
                    : "legacy";

            Path assetIndexFile = Paths.get(rutaVersiones, "asset_index.txt");
            Files.writeString(assetIndexFile, assetIndexId);
            System.out.println("[DOWNLOADER] Asset index guardado: " + assetIndexId);

            // Descargar cliente jar
            if (versionData.downloads != null && versionData.downloads.client != null) {
                descargarArchivoInmediato(versionData.downloads.client.url, rutaJarCliente);
            }

            // descargar librerias
            System.out.println("[DOWNLOADER] Procesando librerias...");
            int totalLibs = versionData.libraries.size();
            int libsDescargadas = 0;

            for (MinecraftVersionJson.Library lib : versionData.libraries) {
                // Librerias estándar
                if (lib.downloads != null && lib.downloads.artifact != null) {
                    String urlLib = lib.downloads.artifact.url;
                    String nombreArchivoLib = urlLib.substring(urlLib.lastIndexOf('/') + 1);
                    String destinoFinalLib = carpetaLibraries + File.separator + nombreArchivoLib;
                    descargarArchivoInmediato(urlLib, destinoFinalLib);
                }

                // Librerias nativas para Linux
                if (lib.natives != null && lib.natives.containsKey("linux")
                        && lib.downloads != null && lib.downloads.classifiers != null) {
                    String claveNativa = lib.natives.get("linux");
                    MinecraftVersionJson.DownloadItem itemLinux = lib.downloads.classifiers.get(claveNativa);
                    if (itemLinux != null) {
                        String urlNative = itemLinux.url;
                        String nombreNative = urlNative.substring(urlNative.lastIndexOf('/') + 1);

                        // Descargar el jar de natives
                        String jarDestino = rutaNatives + File.separator + nombreNative;
                        descargarArchivoInmediato(urlNative, jarDestino);

                        // extraer los .jar XD
                        if (nombreNative.endsWith(".jar")) {
                            System.out.println("[DOWNLOADER] Extrayendo natives-linux de: " + nombreNative);
                            extraerJar(jarDestino, rutaNatives);
                        }
                    }
                }

                // Reportar progreso de librerias
                libsDescargadas++;
                if (callbackProgreso != null && libsDescargadas % 5 == 0) {
                    callbackProgreso.onProgreso(libsDescargadas, totalLibs + 100); // +100 por assets aprox
                }
            }

            // descargar assets
            if (versionData.assetIndex != null) {
                System.out.println("[DOWNLOADER] Descargando assets...");

                String rutaAssetIndexes = rutaMinecraft + File.separator + "assets" + File.separator + "indexes";
                String rutaAssetObjects = rutaMinecraft + File.separator + "assets" + File.separator + "objects";

                new File(rutaAssetIndexes).mkdirs();
                new File(rutaAssetObjects).mkdirs();

                // Descargar asset index JSON
                String rutaAssetIndexJson = rutaAssetIndexes + File.separator + versionData.assetIndex.id + ".json";
                descargarArchivoInmediato(versionData.assetIndex.url, rutaAssetIndexJson);

                // Leer asset index
                java.io.Reader assetReader = Files.newBufferedReader(Paths.get(rutaAssetIndexJson), StandardCharsets.UTF_8);
                AssetIndexJson assetIndex = gson.fromJson(assetReader, AssetIndexJson.class);
                assetReader.close();

                // Descargar cada asset
                int totalAssets = assetIndex.objects.size();
                int contador = 0;

                for (Map.Entry<String, AssetIndexJson.AssetObject> entry : assetIndex.objects.entrySet()) {
                    String hash = entry.getValue().hash;
                    String hashPrefix = hash.substring(0, 2);
                    String urlObjeto = "https://resources.download.minecraft.net/" + hashPrefix + "/" + hash;
                    String destinoObjeto = rutaAssetObjects + File.separator + hashPrefix + File.separator + hash;

                    descargarArchivoInmediato(urlObjeto, destinoObjeto);
                    contador++;

                    // Reportar progreso cada 10 assets
                    if (callbackProgreso != null && contador % 10 == 0) {
                        callbackProgreso.onProgreso(totalLibs + contador, totalLibs + totalAssets);
                    }
                }
                System.out.println("[OK] " + contador + " assets descargados");
            }

            // Finalizar progreso
            if (callbackProgreso != null) {
                callbackProgreso.onProgreso(100, 100);
            }
            System.out.println("[DOWNLOADER] Todo listo para " + version);

        } catch (Exception e) {
            System.out.println("[DOWNLOADER] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // sobrecarga pa mantener el code viejo
    public void iniciarDescargaTotal(String urlJsonVersion) {
        iniciarDescargaTotal(urlJsonVersion, "26.1.1"); // fallback a version por defecto
    }

    // ==========================================================
    // POJOs para gson XD
    // ==========================================================
    public static class MinecraftVersionJson {
        public Downloads downloads;
        public List<Library> libraries;
        public AssetIndexInfo assetIndex;

        public static class Downloads { public DownloadItem client; }
        public static class DownloadItem { public String url; }
        public static class AssetIndexInfo { public String id; public String url; }
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