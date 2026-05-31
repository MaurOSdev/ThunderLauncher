package com.thunder.launcher;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.util.Pair;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BienvenidaController {

    @FXML
    private Button btnOffline;

    @FXML
    private Button btnMicrosoft;

    @FXML
    private Label lblSubtitulo;

    @FXML
    private void manejarCuentaOffline() {
        System.out.println("[LOGS] abriendo el panel de credenciales");

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Crear Cuenta Offline");
        dialog.setHeaderText("Configure su acceso porfavor!");

        ButtonType btnGuardarType = new ButtonType("Guardar", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Tu Nickname");
        PasswordField txtContra = new PasswordField();
        txtContra.setPromptText("tu clave");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(txtUsuario, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(txtContra, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardarType) {
                return new Pair<>(txtUsuario.getText(), txtContra.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> resultado = dialog.showAndWait();

        // aqui esta el cambio AES mas validacion
        resultado.ifPresent(credenciales -> {
            String usuario = credenciales.getKey().trim();
            String password = credenciales.getValue();

            // reglas
            if (!usuario.matches("[a-zA-Z0-9_]{3,16}")) {
                mostrarError("Usuario: 3-16 letras/números/guion bajo (_)");
                return;
            }
            if (password.length() < 4) {
                mostrarError("Contraseña: mínimo 4 caracteres");
                return;
            }

            try {
                // esta wa usa el SessionManager pa configurar pa que quede asi bien fufuafuafuafuafuaf el AES
                String datos = usuario + ":" + password;
                String encriptado = SessionManager.encriptar(datos);

                if (encriptado == null) {
                    mostrarError("Error al procesar la sesión");
                    return;
                }

                // guardar el coso
                String carpetaHome = System.getProperty("user.home");
                Path rutaCarpeta = Paths.get(carpetaHome, ".thunder");
                Path rutaArchivo = rutaCarpeta.resolve("user_session.txt");

                Files.createDirectories(rutaCarpeta);
                Files.writeString(rutaArchivo, "AES_V1:" + encriptado, StandardCharsets.UTF_8);

                System.out.println("[OK] Sesión guardada con AES");
                lblSubtitulo.setText("Bienvenido " + usuario + "!");

                // cambiar escena
                cambiarEscenaMain();

            } catch (Exception e) {
                System.out.println("[ERROR] Falló el guardado: " + e.getMessage());
                e.printStackTrace();
                mostrarError("Error: " + e.getMessage());
            }
        });
    }

    @FXML
    private void manejarCuentaMicrosoft() {
        System.out.println("[logs] iniciar con microsoft");
        mostrarError("Microsoft login: proximamente😏😏😏 XDXDXDXD");
    }

    // metodo pa cambiar escena es reciclable btw
    private void cambiarEscenaMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainFxml.fxml"));
            Parent rootPrincipal = loader.load();
            Stage ventanaActual = (Stage) btnOffline.getScene().getWindow();
            Scene nuevaEscena = new Scene(rootPrincipal, 500, 500);
            ventanaActual.setScene(nuevaEscena);
            System.out.println("[LOGS] Mutando el chasis al panel principal");
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudo cambiar de escena: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // helper que muestra errores
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de inicio de sesión");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        System.out.println("[OK] Gracias por elegir ThunderLauncher!");
    }
}