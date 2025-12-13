package com.example.Modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Carnet {
    private String nombre;
    private String apellido;
    private String documento;
    private String entidad;
    private String cargo;
    private String rh;
    private LocalDate fechaVigencia;
    private LocalDate fechaCreacion;
    private byte[] codigo;
    private String codigoPDF417;
    private String username;
    private int id;
    public Carnet() {
    }
    public Carnet(String nombre, String apellido, String documento, String entidad,String cargo, String rh,
            LocalDate fechaVigencia, LocalDate fechaCreacion,byte[] codigo, String username, int id) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.entidad = entidad;
        this.rh = rh;
        this.cargo=cargo;
        this.fechaVigencia = fechaVigencia;
        this.fechaCreacion = fechaCreacion;
        this.codigo = codigo;
        this.username = username;
        this.id = id;
    }
    public Carnet(String nombre, String apellido, String documento, String entidad, String cargo, String rh,
            LocalDate fechaVigencia, LocalDate fechaCreacion, String username,String codigoPDF417) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.entidad = entidad;
        this.cargo = cargo;
        this.rh = rh;
        this.fechaVigencia = fechaVigencia;
        this.fechaCreacion = fechaCreacion;
        this.username=username;
        this.codigoPDF417 = codigoPDF417;
    }
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getApellido() {
        return apellido;
    }
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    public String getDocumento() {
        return documento;
    }
    public void setDocumento(String documento) {
        this.documento = documento;
    }
    public String getEntidad() {
        return entidad;
    }
    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }
    public String getCargo() {
        return cargo;
    }
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
    public String getRh() {
        return rh;
    }
    public void setRh(String rh) {
        this.rh = rh;
    }
    public LocalDate getFechaVigencia() {
        return fechaVigencia;
    }
    public void setFechaVigencia(LocalDate fechaVigencia) {
        this.fechaVigencia = fechaVigencia;
    }
    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }
    
    public byte[] getCodigo() {
        return codigo;
    }
    public void setCodigo(byte[] codigo) {
        this.codigo = codigo;
    }
    public String getCodigoPDF417() {
        return codigoPDF417;
    }
    public void setCodigoPDF417(String codigoPDF417) {
        this.codigoPDF417 = codigoPDF417;
    }
    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String generarTextoParaCodigo() {
        return String.format("NOMBRES:%s|APELLIDOS:%s|DOCUMENTO:%s|ENTIDAD:%s||CARGO:%s|RH:%s|FECHA_CREACION:%s|FECHA_VIGENCIA:%s|USUARIO:%s",
            nombre, apellido, documento, entidad,cargo,rh,
            fechaCreacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            fechaVigencia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            username
        );
    }
        @Override
    public String toString() {
        return String.format("%s - %s", nombre, documento);
    }
}
