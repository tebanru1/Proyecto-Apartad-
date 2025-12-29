package com.example.Modelo;

public class Usuario {
    private Integer id;
    private String cedula;
    private String nombre;
    private String apellido;   
    private String usuario;
    private String contrasena; 
    private String grado;
    private String rol;

    public Usuario() {
    }
    
    public Usuario(String cedula,String nombre, String apellido, String usuario, String contrasena, String grado, String rol) {
        this.cedula=cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario=usuario;
        this.contrasena=contrasena;
        this.grado = grado;
        this.rol = rol;
    }
    public Usuario(Integer id,String nombre, String apellido, String grado, String rol,String usuario) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.grado = grado;
        this.rol = rol;
        this.usuario=usuario;
    }
    public String getCedula(){
        return cedula;
    }
    public String getNombre() {
        return nombre;
    }
    public String getApellido() {
        return apellido;
    }
    public String getGrado() {
        return grado;
    }
    public String getRol() {
        return rol;
    }
    public void setCedula(String cedula){
        this.cedula=cedula;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    public void setGrado(String grado) {
        this.grado = grado;
    }
    public void setRol(String rol) {
        this.rol = rol;
    }
    public void setUsuario(String usuario){
        this.usuario = usuario;
    }
    public String getUsuario() {
        return usuario;
    }
        public void setContrasena(String contrasena){
        this.contrasena = contrasena;
    }
    public String getContrasena() {
        return contrasena;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

}
