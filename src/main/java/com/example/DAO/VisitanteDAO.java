package com.example.DAO;

import com.example.Controlador.Conexion;
import com.example.Modelo.Visitantes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class VisitanteDAO {
    private final Conexion conexion;

    public VisitanteDAO() {
        this.conexion = new Conexion();
    }

    public void GuardarVisitante(Visitantes visitante) throws Exception {
        String sql = "INSERT INTO visitantes (cedula, nombre, apellido, area, rol, fecha, horaIngreso, horaSalida, huella) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = conexion.conectar();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, visitante.getCedula());
            pstmt.setString(2, visitante.getNombre());
            pstmt.setString(3, visitante.getApellido());
            pstmt.setString(4, visitante.getArea());
            pstmt.setString(5, visitante.getRol());
            pstmt.setDate(6, (Date) visitante.getFecha());
            pstmt.setTime(7, visitante.getHoraIngreso());
            pstmt.setTime(8, visitante.getHoraSalida());
            pstmt.setBytes(9, visitante.getHuella());
            pstmt.executeUpdate();
        }
    }

    public void SalidaVisitanteDB(Visitantes visitante) throws Exception {
        String sql = "UPDATE visitantes SET horaSalida = ? WHERE cedula = ? ";
        try (Connection con = conexion.conectar();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setTime(1, visitante.getHoraSalida());
            pstmt.setLong(2, visitante.getCedula());
            pstmt.executeUpdate();
        }
    }
    public List<Visitantes> CargarVisitantesDB() throws Exception {
        String sql = "SELECT * FROM visitantes";
        List<Visitantes> lista = new ArrayList<>();
        try (Connection con = conexion.conectar();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                long cedula = rs.getLong("cedula");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String area = rs.getString("area");
                String rol = rs.getString("rol");
                Date fecha = rs.getDate("fecha");
                Time horaIngreso = rs.getTime("horaIngreso");
                Time horaSalida = rs.getTime("horaSalida");
                byte[] huella = rs.getBytes("huella");
                Visitantes v = new Visitantes(cedula, nombre, apellido, area, rol, fecha, horaIngreso, horaSalida, huella);
                lista.add(v);
            }
        }
        return lista;
    }
    public List<Visitantes> buscarVisitantes(String busqueda) throws Exception {
    List<Visitantes> lista = new ArrayList<>();
    String sql;

    // Si la búsqueda es un número, buscar por cédula
    if (busqueda.matches("\\d+")) {
        sql = "SELECT * FROM visitantes WHERE cedula = ?";
    } else {
        // Si no, buscar por nombre o apellido (con coincidencias parciales)
        sql = "SELECT * FROM visitantes WHERE nombre LIKE ? OR apellido LIKE ?";
    }

    try (Connection con = conexion.conectar();
         PreparedStatement pstmt = con.prepareStatement(sql)) {

        if (busqueda.matches("\\d+")) {
            pstmt.setLong(1, Long.parseLong(busqueda));
        } else {
            pstmt.setString(1, "%" + busqueda + "%");
            pstmt.setString(2, "%" + busqueda + "%");
        }

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                long cedula = rs.getLong("cedula");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String area = rs.getString("area");
                String rol = rs.getString("rol");
                Date fecha = rs.getDate("fecha");
                Time horaIngreso = rs.getTime("horaIngreso");
                Time horaSalida = rs.getTime("horaSalida");
                byte[] huella = rs.getBytes("huella");

                Visitantes v = new Visitantes(cedula, nombre, apellido, area, rol, fecha, horaIngreso, horaSalida, huella);
                lista.add(v);
            }
        }
    }
    return lista;
}
public int contarVisitasHoy() throws Exception {
    int total = 0;
    String sql = "SELECT COUNT(*) FROM visitantes WHERE fecha = CURRENT_DATE";

    try (Connection con = conexion.conectar();
         PreparedStatement pstmt = con.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            total = rs.getInt(1);
        }
    }

    return total;
}
public int VisitasFaltante() throws Exception {
    int total = 0;
    String sql = "SELECT COUNT(*) FROM visitantes WHERE fecha = CURRENT_DATE AND horaSalida IS NULL";

    try (Connection con = conexion.conectar();
         PreparedStatement pstmt = con.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            total = rs.getInt(1);
        }
    }

    return total;
}
}