package com.example.Modelo;

import java.sql.Date;
import java.sql.Time;

public class ingresoAdministrativos{
    private String cedula;
    private String nombre;
    private String apellido;
    private String cargo;
    private Date fecha;
    private Time horaIngreso;
    private Time horaSalida;
    private byte[] huellaIngreso;
    private byte[] huellaSalida;
    private byte[] fotoFuncionario;
    private String usuario;

    public ingresoAdministrativos(){}
    public ingresoAdministrativos(Date fecha, Time horaIngreso, Time horaSalida, byte[] huellaIngreso, byte[] huellaSalida,String usuario){
        this.horaIngreso = horaIngreso;
        this.horaSalida = horaSalida;
        this.huellaIngreso = huellaIngreso;
        this.huellaSalida = huellaSalida;
        this.usuario = usuario;
    }
    public ingresoAdministrativos(String cedula, String nombre, String apellido, String cargo,byte[] fotoFuncionario) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cargo = cargo;
        this.fotoFuncionario = fotoFuncionario;
    }
    public ingresoAdministrativos(String cedula, String nombre, String apellido, String cargo, Date fecha, Time horaIngreso, Time horaSalida, byte[] huellaIngreso, byte[] huellaSalida, byte[] fotoFuncionario, String usuario) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cargo = cargo;
        this.fecha = fecha;
        this.horaIngreso = horaIngreso;
        this.horaSalida = horaSalida;
        this.huellaIngreso = huellaIngreso;
        this.huellaSalida = huellaSalida;
        this.fotoFuncionario = fotoFuncionario;
        this.usuario = usuario;
    }
    public String getCedula() {
        return cedula;
    }
    public void setCedula(String cedula) {
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
    public String getCargo() {
        return cargo;
    }
    public void setCargo(String cargo) {
        this.cargo = cargo;
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
    public byte[] getHuellaIngreso() {
        return huellaIngreso;
    }   
    public void setHuellaIngreso(byte[] huellaIngreso) {
        this.huellaIngreso = huellaIngreso;
    }
    public byte[] getHuellaSalida() {
        return huellaSalida;
    }
    public void setHuellaSalida(byte[] huellaSalida) {
        this.huellaSalida = huellaSalida;
    }
    public byte[] getFotoFuncionario() {
        return fotoFuncionario;
    }
    public void setFotoFuncionario(byte[] fotoFuncionario) {
        this.fotoFuncionario = fotoFuncionario;
    }
    public String getUsuario() {
        return usuario;
    }
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}