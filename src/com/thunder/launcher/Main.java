package com.thunder.launcher;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        // ejecutar la interfaz en awjsdsddas o nose que kaka
        SwingUtilities.invokeLater(() -> {
            // twin
            JFrame ventana = new JFrame("com.thunder.launcher.ThunderLauncher v1.0 - Creando Directorios");
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.setSize(500, 500);
            ventana.setLocationRelativeTo(null);
            ventana.getContentPane().setBackground(new Color(15, 15, 15));

            System.out.println("======================================");
            System.out.println("creando las carpetas");
            System.out.println("======================================");

            // ==========================================
            // dale bro ya casi
            // ==========================================
            try {
                // deinimos la ruta de el proyecto
                // usamos el . asi se hace bien escondidita asi bien waos
                Path rutaCarpeta = Paths.get(".thunderlauncher_data");

                // veamos si la carpeta existe o valemos monda
                if (Files.notExists(rutaCarpeta)) {
                    // hagase la luz
                    Files.createDirectory(rutaCarpeta);
                    System.out.println("LA CARPTEMASJKDJAKFJA SE CREO SJFJKASNFKJSAXNFKASNCSLKA");
                } else {
                    System.out.println("la carpeta ya existia en un rincon del sistema");
                }
            } catch (Exception e) {
                System.out.println("error de escritura no se pudo crear la carpeta " + e.getMessage());
            }
            // ==========================================

            // duke
            try {
                java.net.URI uri = new java.net.URI("https://pbs.twimg.com/profile_images/1182586235744710656/zYfBUJhU_400x400.jpg");
                URL url = uri.toURL();

                ImageIcon iconolisto = new ImageIcon(url);
                Image img = iconolisto.getImage().getScaledInstance(480, 480, Image.SCALE_SMOOTH);
                JLabel etiquetaImagen = new JLabel(new ImageIcon(img));
                etiquetaImagen.setHorizontalAlignment(JLabel.CENTER);

                ventana.add(etiquetaImagen, BorderLayout.CENTER);
                System.out.println("[OK] El duke de Twitter cargo gigante XIJSLDAKFASGJ");

            } catch (Exception e) {
                JLabel errorLabel = new JLabel("error de carga del duke pero ta joya la careta");
                errorLabel.setForeground(Color.RED);
                errorLabel.setHorizontalAlignment(JLabel.CENTER);
                ventana.add(errorLabel);
                System.out.println("     [WARN] fallo la imagen pero ta joya");
            }

            ventana.setVisible(true);
        });
    }
}