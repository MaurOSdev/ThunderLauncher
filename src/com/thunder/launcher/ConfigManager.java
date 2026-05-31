package com.thunder.launcher;

import java.nio.file.*;
import java.util.Properties;

// guarda ram maxima y minima y modifica tu jvm como quieras
public class ConfigManager {
    private static final Path CONFIG_PATH = Paths.get(
            System.getProperty("user.home"), ".thunder", "config.properties");
    private static Properties props = new Properties();

    // Cargar config al iniciar la clase
    static {
        if (Files.exists(CONFIG_PATH)) {
            try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
                props.load(reader);
            } catch (Exception ignored) {}
        }
    }

    // ram default en 2gb
    public static int getRamMB() {
        return Integer.parseInt(props.getProperty("ram_mb", "2048"));
    }
    public static void setRamMB(int mb) {
        props.setProperty("ram_mb", String.valueOf(mb));
        guardar();
    }

    // jvm por default
    public static String getJvmArgs() {
        return props.getProperty("jvm_args", "-XX:+UseG1GC -XX:+UnlockExperimentalVMOptions");
    }
    public static void setJvmArgs(String args) {
        props.setProperty("jvm_args", args);
        guardar();
    }

    // guardar en el disco
    private static void guardar() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (var writer = Files.newBufferedWriter(CONFIG_PATH)) {
                props.store(writer, "ThunderLauncher Config - NO EDITAR MANUALMENTE BAJO NINGUNA CIRSCUNSTANCIA");
            }
        } catch (Exception e) {
            System.err.println("error guardando config: " + e.getMessage());
        }
    }
}