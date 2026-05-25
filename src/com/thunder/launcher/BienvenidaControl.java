package com.thunder.launcher;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class BienvenidaController {

    @FXML private Button btnOffline;
    @FXML private Button btnMicrosoft;

    @FXML
    public void initialize() {
        // esta wea ejecuta una wbada que imprime esto
        System.out.println("[ThunderLauncher] aca esta esto");
    }

    @FXML
    private void handleCrearOffline() {
        System.out.println("[ThunderLauncher] elegiste ser jack sparrow argh");
        // aca lanzas tu contra
    }

    @FXML
    private void handleCrearMicrosoft() {
        System.out.println("[ThunderLauncher] ahhh muy vip eh");
        // XD
    }
}