package com.uspgdevteam.sonrisasana.servicio;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Named
@ApplicationScoped
public class FileStorageService {

    private final String BASE_DIR = System.getProperty("user.home") + "/sonrisasana_files/";

    public FileStorageService() {
        try {
            Files.createDirectories(Paths.get(BASE_DIR));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear directorio base", e);
        }
    }

    // ============================
    // GUARDAR ARCHIVO
    // ============================
    public String guardarArchivo(InputStream in, String fileName) {
        try {
            Path destino = Paths.get(BASE_DIR + fileName);
            Files.copy(in, destino);
            return destino.toString();

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar archivo", e);
        }
    }

    // ============================
    // LEER ARCHIVO
    // ============================
    public InputStream leerArchivo(String ruta) {
        try {
            return new FileInputStream(ruta);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer archivo", e);
        }
    }


    // ============================
    // DESCARGAR ARCHIVO
    // ============================
    public void descargarArchivo(String ruta) {
        try {
            Path p = Paths.get(ruta);
            File f = p.toFile();

            if (!f.exists()) return;

            FacesContext faces = FacesContext.getCurrentInstance();
            ExternalContext ext = faces.getExternalContext();

            ext.responseReset();
            ext.setResponseContentType(Files.probeContentType(p));
            ext.setResponseContentLength((int) f.length());
            ext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");

            OutputStream out = ext.getResponseOutputStream();
            Files.copy(p, out);

            faces.responseComplete();

        } catch (Exception e) {
            throw new RuntimeException("Error al descargar archivo", e);
        }
    }
}
