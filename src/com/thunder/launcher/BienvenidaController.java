package com.thunder.launcher;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.util.Pair;
import java.util.Optional;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

// importamos estas cacas pa que mi IDE no parezca sangre de tanto code en rojo
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
    private Label lblSubtitulo; // esto es importante lo juro

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

        resultado.ifPresent(credenciales -> {
            String usuario = credenciales.getKey();
            String contraPlana = credenciales.getValue();

            System.out.println("[logs] Gracias por elegir ThunderLauncher!: " + usuario);

            String contraHasheada = encriptarClave(contraPlana);
            System.out.println("aca esta el ..." + contraHasheada);

            lblSubtitulo.setText("Gracias por elegir ThunderLauncher " + usuario + "!");

            // ========================================================
            // ahora si PUM
            // ========================================================
            try {
                System.out.println("[LOGS] Mutando el chasis al panel principal...");

                // cargamos esto pa que no explote
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/thunder/launcher/MainFxml.fxml"));
                Parent rootPrincipal = loader.load();

                // Hacemos palanca con el boton de offline XD
                Stage ventanaActual = (Stage) btnOffline.getScene().getWindow();

                // armamos como lego esto
                Scene nuevaEscena = new Scene(rootPrincipal, 500, 500);


                // nuevaEscena.getStylesheets().add(getClass().getResource("principal.css").toExternalForm());
//por si las moscas no o añado aun no tengo preparado el css para esto
                // ahora si
                ventanaActual.setScene(nuevaEscena);

            } catch (Exception e) {
                System.out.println("[ERROR] no se pudo cambiar la escena: " + e.getMessage());
                e.printStackTrace();
            }
            // ========================================================
        });
    }

    @FXML
    private void manejarCuentaMicrosoft() {
        System.out.println("[logs] inciar con microsoft");
    }

    private String encriptarClave(String claveOriginal) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(claveOriginal.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("[ERROR] hay un error", ex);
        }
    }

    @FXML
    public void initialize() {
        System.out.println("[OK] Gracias por elegir ThunderLauncher!");
    }
}