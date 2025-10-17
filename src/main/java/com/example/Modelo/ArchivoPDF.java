package com.example.Modelo;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ArchivoPDF {
    private String nombre;
    private String ruta;
    private String fechaModificacion;
    private File archivo;
    
    public ArchivoPDF() {}
    
    public ArchivoPDF(File archivo) {
        this.archivo = archivo;
        this.nombre = archivo.getName();
        this.ruta = archivo.getAbsolutePath();
        this.fechaModificacion = formatearFecha(archivo.lastModified());
    }
    
    public ArchivoPDF(String nombre, String ruta, String fechaModificacion) {
        this.nombre = nombre;
        this.ruta = ruta;
        this.fechaModificacion = fechaModificacion;
    }
    
    private String formatearFecha(long timestamp) {
        LocalDateTime fecha = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp), 
            java.time.ZoneId.systemDefault()
        );
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getRuta() {
        return ruta;
    }
    
    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
    
    public String getFechaModificacion() {
        return fechaModificacion;
    }
    
    public void setFechaModificacion(String fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
    
    public File getArchivo() {
        return archivo;
    }
    
    public void setArchivo(File archivo) {
        this.archivo = archivo;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}
