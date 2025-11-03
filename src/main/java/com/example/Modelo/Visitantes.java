package com.example.Modelo;

import java.sql.Date;
import java.sql.Time;

public class Visitantes {
  private long cedula;
  private String nombre;
  private String apellido;
  private String area;
  private String rol;
  private Date fecha;
  private Time horaIngreso;
  private Time horaSalida;
  private byte[] huella;

  public Visitantes(){}

    public Visitantes(long cedula, String nombre, String apellido, String area, String rol, Date fecha, Time horaIngreso, Time horaSalida, byte[] huella) {
      this.cedula = cedula;
      this.nombre = nombre;
      this.apellido = apellido;
      this.area = area;
      this.rol = rol;
      this.fecha = fecha;
      this.horaIngreso = horaIngreso;
      this.horaSalida = horaSalida;
      this.huella = huella;
    }

      public long getCedula() {
        return cedula;
      }
      public void setCedula(long cedula) {
        this.cedula = cedula;
      }

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

      public String getArea() {
        return area;
      }
      public void setArea(String area) {
        this.area = area;
      }

      public String getRol() {
        return rol;
      }
      public void setRol(String rol) {
        this.rol = rol;
      }

      public Date getFecha() {
        return fecha;
      }
      public void setFecha(Date fecha) {
        this.fecha = fecha;
      }

      public Time getHoraIngreso() {
        return horaIngreso;
      }
      public void setHoraIngreso(Time horaIngreso) {
        this.horaIngreso = horaIngreso;
      }

      public Time getHoraSalida() {
        return horaSalida;
      }
      public void setHoraSalida(Time horaSalida) {
        this.horaSalida = horaSalida;
      }

      public byte[] getHuella() {
        return huella;
      }
      public void setHuella(byte[] huella) {
        this.huella = huella;
      }
 
}
