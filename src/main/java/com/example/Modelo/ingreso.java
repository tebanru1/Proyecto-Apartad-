package com.example.Modelo;

import java.sql.Date;
import java.sql.Time;

public class ingreso{
    private String cedula;
    private Date fecha;
    private Time hora;

    public ingreso(){}
    
    public ingreso(String cedula, Date fecha, Time hora){
        this.cedula=cedula;
        this.fecha=fecha;
        this.hora=hora;
    }
    
    // Constructor para compatibilidad con código existente
    public ingreso(String cedula, Date fecha){
        this.cedula=cedula;
        this.fecha=fecha;
        this.hora=null;
    }
public Date getFecha(){
    return fecha;
}
public void setFecha(Date fecha){
    this.fecha=fecha;
}
public String getCedula(){
    return cedula;
}
public void setCedula(String cedula){
    this.cedula=cedula;
}

public Time getHora(){
    return hora;
}

public void setHora(Time hora){
    this.hora=hora;
}
}