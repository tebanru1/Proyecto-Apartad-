package com.example.DAO;

import com.example.Controlador.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.example.Modelo.Visitantes;

public class IngresosDiaDAO {
    private final Conexion conexion;
    private PreparedStatement ps;
    private Connection con; 
    private ResultSet rs;
    public IngresosDiaDAO() {
        this.conexion = new Conexion();
    }
public List<Visitantes> obtenerVisitantes() {
    String sql="SELECT nombre,apellido,fecha,horaIngreso,horaSalida,rol " +
                 "FROM visitantes " +
                 "WHERE fecha = CURRENT_DATE " +
                 "ORDER BY horaIngreso";
    List<Visitantes> lista=new ArrayList<>(); 
    try {
        con=conexion.conectar();
        ps=con.prepareStatement(sql);
        rs=ps.executeQuery();
        while (rs.next()) {
            Visitantes v=new Visitantes();
            v.setNombre(rs.getString("nombre"));
            v.setApellido(rs.getString("apellido"));
            v.setHoraIngreso(rs.getTime("horaIngreso"));
            v.setHoraSalida(rs.getTime("horaSalida"));
            v.setRol(rs.getString("rol"));
            lista.add(v);
        }
    } catch (Exception e) {
        System.out.println(e.toString());
    }return lista;
}
}
