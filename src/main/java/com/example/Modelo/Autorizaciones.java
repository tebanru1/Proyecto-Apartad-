package com.example.Modelo;
import java.sql.Date;


public class Autorizaciones {
    private int id; 
    private String descripcion;
    private String area;
    private String tipo;
    private Date fechaInicio;
    private Date fechaTerminacion;
    private String fechaGeneracion;
    private byte[] PDFs; // Para almacenar el PDF como BLOB

    public Autorizaciones() {}

    public Autorizaciones(int id,String descripcion, String area, String tipo, Date fechaInicio,
                          Date fechaTerminacion, String fechaGeneracion, byte[] PDFs) {
        this.id = id;
        this.descripcion = descripcion;
        this.area = area;
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.fechaTerminacion = fechaTerminacion;
        this.fechaGeneracion = fechaGeneracion;
        this.PDFs = PDFs; 
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaTerminacion() {
        return fechaTerminacion;
    }

    public void setFechaTerminacion(Date fechaTerminacion) {
        this.fechaTerminacion = fechaTerminacion;
    }

    public String getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(String fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public byte[] getPDFs() {
        return PDFs;
    }

    public void setPDFs(byte[] PDFs) {
        this.PDFs = PDFs; 
    }
}
