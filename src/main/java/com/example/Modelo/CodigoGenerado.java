package com.example.Modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Modelo para representar códigos PDF417 generados
 */
public class CodigoGenerado {
    private String nombre;
    private String cedula;
    private String ciudad;
    private String pabellon;
    private LocalDate fechaInicio;
    private LocalDate fechaTerminacion;
    private String codigoPDF417;
    private String fechaGeneracion;
    private byte[] imagenCodigo; // Para almacenar la imagen del código como BLOB
    
    public CodigoGenerado() {}
    
    public CodigoGenerado(String nombre, String cedula, String ciudad, String pabellon, 
                         LocalDate fechaInicio, LocalDate fechaTerminacion, String codigoPDF417) {
        this.nombre = nombre;
        this.cedula = cedula;
        this.ciudad = ciudad;
        this.pabellon = pabellon;
        this.fechaInicio = fechaInicio;
        this.fechaTerminacion = fechaTerminacion;
        this.codigoPDF417 = codigoPDF417;
        this.fechaGeneracion = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getCedula() {
        return cedula;
    }
    
    public void setCedula(String cedula) {
        this.cedula = cedula;
    }
    
    public String getCiudad() {
        return ciudad;
    }
    
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
    
    public String getPabellon() {
        return pabellon;
    }
    
    public void setPabellon(String pabellon) {
        this.pabellon = pabellon;
    }
    
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public LocalDate getFechaTerminacion() {
        return fechaTerminacion;
    }
    
    public void setFechaTerminacion(LocalDate fechaTerminacion) {
        this.fechaTerminacion = fechaTerminacion;
    }
    
    public String getCodigoPDF417() {
        return codigoPDF417;
    }
    
    public void setCodigoPDF417(String codigoPDF417) {
        this.codigoPDF417 = codigoPDF417;
    }
    
    public String getFechaGeneracion() {
        return fechaGeneracion;
    }
    
    public void setFechaGeneracion(String fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }
    
    public byte[] getImagenCodigo() {
        return imagenCodigo;
    }
    
    public void setImagenCodigo(byte[] imagenCodigo) {
        this.imagenCodigo = imagenCodigo;
    }
    
    /**
     * Genera el texto que se codificará en PDF417
     */
    public String generarTextoParaCodigo() {
        return String.format("NOMBRE:%s|CEDULA:%s|CIUDAD:%s|PABELLON:%s|INICIO:%s|TERMINACION:%s|FECHA_GEN:%s",
            nombre, cedula, ciudad, pabellon, 
            fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            fechaTerminacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            fechaGeneracion);
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s", nombre, cedula);
    }
}
