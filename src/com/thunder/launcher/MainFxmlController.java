package com.thunder.launcher;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFxmlController {

    // el id del boton XD
    @FXML
    private Button btnJugar;

    // El método que se ejecutará cuando presiones el botón (debe tener onAction="#lanzarMinecraft")
    @FXML
    private void lanzarMinecraft() {
        System.out.println("[MOTOR] iniciando el proccesbuilder.");

        try {
            // recuperamos las cosas base del sistema
            String carpetaHome = System.getProperty("user.home");

            // nota pa mi mismo: esta cosa pone el usuario dsp
            String usuarioLogeado = "KernelKid";

            // apuntamos al .thunder
            String rutaMinecraft = carpetaHome + File.separator + ".thunder";
            String rutaAssets = rutaMinecraft + File.separator + "assets";
            String rutaLibraries = rutaMinecraft + File.separator + "libraries";

            // armamos la lista de comandos a esta wa
            List<String> comandos = new ArrayList<>();

            // invocad al DUKE
            comandos.add("java");

            // le asignamos 2gb de ram al minecraft pa que corra de pana
            comandos.add("-Xmx2G");
            comandos.add("-XX:+UseG1GC"); // ESTO ESTA OPTIMIZADISISISIISISISISMO

            // configuracion de los classpaths de
            comandos.add("-Djava.library.path=" + rutaMinecraft + File.separator + "versions" + File.separator + "26.1.1" + File.separator + "natives");
            comandos.add("-cp");
            comandos.add(rutaLibraries + File.separator + "*;" + rutaMinecraft + File.separator + "versions" + File.separator + "26.1.1" + File.separator + "26.1.1.jar");

            // La clase maestra de Mojang que levanta el juego
            comandos.add("net.minecraft.client.main.Main");

            // ARGUMENTOS FORMALES DE MINECRAFT
            comandos.add("--username");
            comandos.add(usuarioLogeado); // aca el nickname se pone

            comandos.add("--version");
            comandos.add("26.1.1");

            comandos.add("--gameDir");
            comandos.add(rutaMinecraft);

            comandos.add("--assetsDir");
            comandos.add(rutaAssets);

            comandos.add("--assetIndex");
            comandos.add("26.1.1");

            // ACA LA PUMBA
            ProcessBuilder pb = new ProcessBuilder(comandos);

            // establecemos la carpeta en el .thunder
            pb.directory(new File(rutaMinecraft));

            // ESTO ES CALVISIMO
            pb.inheritIO();

            System.out.println("[MOTOR] lanzando los procesinis");

            // arranca en un hilo SEPARADO
            Process procesoMinecraft = pb.start();

            System.out.println("[OK] a viciar noma");

        } catch (Exception e) {
            // si aca falta un .jar ps el juego se queda con coma inducido
            System.out.println("[WARN] el proccesbuilder no encontro ni weas " + e.getMessage());
        }
    }

    // Método que se ejecuta al cargar la pantalla principal
    @FXML
    public void initialize() {
        System.out.println("[OK] panel listo");
    }
}
