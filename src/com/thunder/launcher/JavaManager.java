package com.thunder.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class JavaManager {

    private static final String JAVA_DIR = System.getProperty("user.home") + "/.thunder/java/";

    /**
    obtiene la ruta del java pa la version
     */
    public static String getJavaPath(String minecraftVersion) {
        // logica dinamica
        int javaVersion = getRequiredJavaVersion(minecraftVersion);

        String folderName = "jdk-" + javaVersion;
        Path javaFolder = Paths.get(JAVA_DIR + folderName);
        Path javaBin = javaFolder.resolve("bin/java");

        // Si ya existe, lo usamos
        if (Files.exists(javaBin)) {
            System.out.println("[JAVA] Usando Java " + javaVersion + " existente.");
            return javaBin.toString();
        }

        // Si no, lo descargamos
        System.out.println("[JAVA] Descargando Java " + javaVersion + " para Minecraft " + minecraftVersion + "...");
        try {
            downloadAndExtract(javaVersion, javaFolder);
            return javaBin.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[JAVA] Error instalando Java. Usando 'java' del sistema como fallback.");
            return "java";
        }
    }

    /**
     * logica dinamica pa mapear la version
     */
    private static int getRequiredJavaVersion(String mcVersion) {
        // CASOS MUY PRECISOS
        if (mcVersion.equals("1.20.5") || mcVersion.equals("1.20.6")) {
            return 21;
        }

        // de 26.x puro java 25
        if (mcVersion.startsWith("26") || mcVersion.startsWith("27")) {
            return 25;
        }

        // de 1.21 puro java 21
        if (mcVersion.startsWith("1.21")) {
            return 21;
        }

        // de la 1.17 a 1.21.4 puro java 17
        if (mcVersion.startsWith("1.17") || mcVersion.startsWith("1.18") ||
                mcVersion.startsWith("1.19") || mcVersion.startsWith("1.20")) {
            return 17;
        }

        // de la 1.13 a 1.16 puro java 8
        if (mcVersion.startsWith("1.13") || mcVersion.startsWith("1.14") ||
                mcVersion.startsWith("1.15") || mcVersion.startsWith("1.16")) {
            return 8;
        }

        // todo anterior a la 1.13 usar java 8
        return 8;
    }

    private static void downloadAndExtract(int version, Path folder) throws Exception {
        folder.toFile().mkdirs();

        // urls con jdk no jre que la habia cgado JAJAJ
        String url = switch (version) {
            case 25 -> "https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.3%2B9/OpenJDK25U-jdk_x64_linux_hotspot_25.0.3_9.tar.gz";
            case 21 -> "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.2%2B13/OpenJDK21U-jdk_x64_linux_hotspot_21.0.2_13.tar.gz";
            case 17 -> "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.10_7.tar.gz";
            default -> "https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u492-b09/OpenJDK8U-jdk_x64_linux_hotspot_8u492b09.tar.gz";
        };

        Path tempFile = folder.resolve("java.tar.gz");

        // Descargar  esta wa
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));

        // Validar descarga
        if (Files.size(tempFile) == 0) {
            throw new IOException("Descarga vacía: " + url);
        }

        // Extraer
        System.out.println("[JAVA] Extrayendo...");
        ProcessBuilder pb = new ProcessBuilder(
                "tar", "-xzf", tempFile.toString(),
                "-C", folder.toString(),
                "--strip-components=1"
        );
        pb.inheritIO();
        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new IOException("Error al extraer: Exit code: " + exitCode);
        }

        // Validar extracción
        File binJava = folder.resolve("bin/java").toFile();
        if (!binJava.exists()) {
            throw new RuntimeException("bin/java no existe despues de extraer Estructura inesperada.");
        }

        Files.delete(tempFile);
        binJava.setExecutable(true);

        System.out.println("[JAVA] Instalación completada: " + binJava.getAbsolutePath());
    }
}