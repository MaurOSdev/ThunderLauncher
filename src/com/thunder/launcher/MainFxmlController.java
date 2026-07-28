package com.thunder.launcher;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import javafx.concurrent.Task;
import javafx.application.Platform;

import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;

public class MainFxmlController {

    @FXML private Button btnJugar;
    @FXML private Button btnInstancias;
    @FXML private Button btnAjustes;
    @FXML private ComboBox<String> cmbVersiones;
    @FXML private ProgressBar barraProgreso;
    @FXML private Label lblEstado;

    private DownloaderEngine motorDescarga;
    private String versionSeleccionada = "26.1.1";

    @FXML
    public void initialize() {
        System.out.println("[UI] panel principal iniciado");
        motorDescarga = new DownloaderEngine();

        btnJugar.setOnAction(e -> lanzarMinecraft());
        btnAjustes.setOnAction(e -> abrirAjustes());
        btnInstancias.setOnAction(e -> mostrarInstancias());

        cargarVersionesDesdeMojang();
    }

    private void cargarVersionesDesdeMojang() {
        lblEstado.setText("Cargando versiones...");
        cmbVersiones.setDisable(true);

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray versiones = json.getAsJsonArray("versions");

                List<String> listaLimpia = new ArrayList<>();
                for (var v : versiones) {
                    JsonObject obj = v.getAsJsonObject();
                    String id = obj.get("id").getAsString();
                    String tipo = obj.get("type").getAsString();
                    if (tipo.equals("release") || tipo.equals("snapshot")) {
                        listaLimpia.add(id + " (" + tipo + ")");
                    }
                }

                Platform.runLater(() -> {
                    cmbVersiones.getItems().setAll(listaLimpia);
                    if (!listaLimpia.isEmpty()) {
                        cmbVersiones.getSelectionModel().select(0);
                        versionSeleccionada = listaLimpia.get(0).split(" ")[0];
                    }
                    cmbVersiones.setDisable(false);
                    lblEstado.setText("[Ready] " + listaLimpia.size() + " versiones cargadas");
                });

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                Platform.runLater(() -> {
                    cmbVersiones.getItems().add("Error al cargar");
                    cmbVersiones.setDisable(false);
                    lblEstado.setText("Sin conexion");
                });
            }
        }).start();

        cmbVersiones.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null) {
                versionSeleccionada = nuevo.split(" ")[0];
                lblEstado.setText("📦 " + versionSeleccionada);
            }
        });
    }

    @FXML
    private void lanzarMinecraft() {
        // ... validaciones ...

        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() {
                try {
                    String versionId = versionSeleccionada.split(" ")[0];
                    String urlJson = obtenerUrlVersionJson(versionId);

                    // ESTO ES IMPORTANTISIMO
                    motorDescarga.setProgresoCallback((descargados, total) -> {
                        double progreso = (double) descargados / total;
                        updateProgress(progreso, 1.0);
                        updateMessage(descargados + "/" + total + " archivos");
                    });

                    // luego se llama la version dinamica
                    motorDescarga.iniciarDescargaTotal(urlJson, versionId);

                } catch (Exception e) {
                    System.err.println("[ERROR] " + e.getMessage());
                    updateMessage("Error: " + e.getMessage());
                    return null;
                }
                return null;
            }
        };

        barraProgreso.progressProperty().bind(tarea.progressProperty());
        tarea.messageProperty().addListener((obs, v, msg) -> {
            if (msg != null) lblEstado.setText("⬇️ " + msg);
        });
        tarea.setOnSucceeded(e -> {
            lblEstado.setText("Listo Iniciando!");
            btnJugar.setDisable(false);
            ejecutarMinecraft();
        });
        tarea.setOnFailed(e -> {
            lblEstado.setText("Error");
            btnJugar.setDisable(false);
            mostrarAlerta("Error: " + tarea.getException().getMessage(), Alert.AlertType.ERROR);
        });

        new Thread(tarea).start();
    }

    private void ejecutarMinecraft() {
        new Thread(() -> {
            try {
                String home = System.getProperty("user.home");
                String ruta = home + File.separator + ".thunder";
                String usuario = obtenerUsuarioDeSesion();

                String versionId = versionSeleccionada.split(" ")[0];
                String javaPath = JavaManager.getJavaPath(versionId);

                // ============================================================
                // LAS TRES VARIABLES QUE TE FALTABAN (las declaras aquí)
                // ============================================================
                String versions = ruta + File.separator + "versions" + File.separator + versionId;
                String assets = ruta + File.separator + "assets";
                StringBuilder cp = new StringBuilder();  // ← EL FALLO PRINCIPAL

                List<String> cmd = new ArrayList<>();

                System.out.println("[JAVA] Ruta usada: " + javaPath);
                cmd.add(javaPath);

                // RAM configurable
                int ram = 2048;
                try { ram = ConfigManager.getRamMB(); } catch (NoClassDefFoundError ignored) {}
                cmd.add("-Xmx" + ram + "M");
                cmd.add("-XX:+UseG1GC");

                // JVM args
                try {
                    String args = ConfigManager.getJvmArgs();
                    if (args != null && !args.isBlank()) {
                        for (String a : args.split(" ")) if (!a.isBlank()) cmd.add(a);
                    }
                } catch (NoClassDefFoundError ignored) {}

                cmd.add("-Djava.library.path=" + versions + File.separator + "natives");
                cmd.add("-cp");

                // ============================================================
                // VAMOS PC AAAAAAAAAAAAAA
                // ============================================================
                String libsPorVersion = versions + File.separator + "libraries";
                File dirLibs = new File(libsPorVersion);
                if (dirLibs.exists() && dirLibs.isDirectory()) {
                    for (File f : dirLibs.listFiles()) {
                        if (f.getName().endsWith(".jar")) {
                            cp.append(f.getAbsolutePath()).append(File.pathSeparator);
                        }
                    }
                }

                cp.append(versions).append(File.separator).append(versionId).append(".jar");
                cmd.add(cp.toString());

                cmd.add("net.minecraft.client.main.Main");
                cmd.add("--username"); cmd.add(usuario);
                cmd.add("--uuid"); cmd.add(generarUUIDOffline(usuario));
                cmd.add("--accessToken"); cmd.add("00000000000000000000000000000000");
                cmd.add("--userType"); cmd.add("mojang");
                cmd.add("--version"); cmd.add(versionId);
                cmd.add("--gameDir"); cmd.add(ruta);
                cmd.add("--assetsDir"); cmd.add(assets);

                String assetIndexId = obtenerAssetIndexParaVersion(versionId, versions);
                cmd.add("--assetIndex"); cmd.add(assetIndexId);

                System.out.println("[MOTOR] Comando: " + String.join(" ", cmd));

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(new File(ruta));
                pb.inheritIO();
                Process p = pb.start();
                p.waitFor();

            } catch (Exception e) {
                System.err.println("[ERROR] " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void abrirAjustes() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("⚙️ Ajustes");
        dialog.setHeaderText("Configura RAM y JVM args");

        // slider ram con fallback si ConfigManager no existe
        int ramActual = 2048;
        try { ramActual = ConfigManager.getRamMB(); } catch (NoClassDefFoundError ignored) {}
        Slider sliderRam = new Slider(512, 8192, ramActual);
        sliderRam.setShowTickLabels(true);
        sliderRam.setShowTickMarks(true);
        sliderRam.setMajorTickUnit(1024);

        // jvm args con fallback por si las moscas
        String argsActuales = "-XX:+UseG1GC";
        try { argsActuales = ConfigManager.getJvmArgs(); } catch (NoClassDefFoundError ignored) {}
        TextField txtArgs = new TextField(argsActuales);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.add(new Label("RAM maxima:"), 0, 0);
        grid.add(sliderRam, 1, 0);
        Label lblRam = new Label((int)sliderRam.getValue() + " MB");
        grid.add(lblRam, 2, 0);
        grid.add(new Label("JVM args:"), 0, 1);
        grid.add(txtArgs, 1, 2, 2, 1);

        sliderRam.valueProperty().addListener((o, v, n) -> lblRam.setText((int)n.doubleValue() + " MB"));

        dialog.getDialogPane().setContent(grid);
        ButtonType btnOk = new ButtonType("Guardar", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        dialog.setResultConverter(t -> {
            if (t == btnOk) {
                try {
                    ConfigManager.setRamMB((int)sliderRam.getValue());
                    ConfigManager.setJvmArgs(txtArgs.getText());
                    lblEstado.setText("Ajustes guardados");
                } catch (NoClassDefFoundError e) {
                    lblEstado.setText("[FATAL ERROR] ConfigManager no encontrado");
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void mostrarInstancias() {
        mostrarAlerta("instancias: proximamente jejeje", Alert.AlertType.INFORMATION);
    }

    private String obtenerUsuarioDeSesion() {
        try {
            Path ruta = Paths.get(System.getProperty("user.home"), ".thunder", "user_session.txt");
            if (Files.exists(ruta)) {
                String c = Files.readString(ruta).trim();
                if (c.startsWith("AES_V1:")) {
                    String datos = SessionManager.desencriptar(c.substring("AES_V1:".length()));
                    if (datos != null && datos.contains(":")) return datos.split(":")[0];
                }
            }
        } catch (Exception e) {
            System.err.println("[WARN] " + e.getMessage());
        }
        return "ThunderPlayer";
    }

    private String generarUUIDOffline(String nick) {
        return java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + nick).getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
    }

    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert a = new Alert(tipo);
        a.setTitle("ThunderLauncher");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    private String obtenerUrlVersionJson(String versionId) throws Exception {
        System.out.println("[MANIFEST] Buscando versión: " + versionId);

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        // 1. Descargar version_manifest.json
        java.net.http.HttpRequest manifestRequest = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json"))
                .build();

        java.net.http.HttpResponse<String> response = client.send(
                manifestRequest,
                java.net.http.HttpResponse.BodyHandlers.ofString()
        );

        com.google.gson.JsonObject manifest = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();

        // 2. Buscar la versión en la lista
        for (com.google.gson.JsonElement v : manifest.getAsJsonArray("versions")) {
            com.google.gson.JsonObject obj = v.getAsJsonObject();
            String id = obj.get("id").getAsString();

            if (id.equals(versionId)) {
                String url = obj.get("url").getAsString();
                System.out.println("[MANIFEST] URL encontrada: " + url);
                return url;
            }
        }

        throw new RuntimeException("Versión no encontrada: " + versionId);
    }
        private String obtenerAssetIndexParaVersion(String versionId, String rutaVersiones) {
            String assetIndexId = "legacy"; // fallback para versiones viejas
            try {
                Path assetIndexFile = Paths.get(rutaVersiones, "asset_index.txt");
                if (Files.exists(assetIndexFile)) {
                    assetIndexId = Files.readString(assetIndexFile).trim();
                    System.out.println("[LAUNCHER] Asset index para " + versionId + ": " + assetIndexId);
                }
            } catch (Exception e) {
                System.err.println("[WARN] No se pudo leer asset_index.txt: " + e.getMessage());
            }
            return assetIndexId;
        }

    }
