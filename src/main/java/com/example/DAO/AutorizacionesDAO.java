package com.example.DAO;

import com.example.Controlador.Conexion;
import com.example.Modelo.Autorizaciones;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AutorizacionesDAO {
    private final Conexion conexion;

    public AutorizacionesDAO() {
        this.conexion = new Conexion();
    }

    // Guardar una nueva autorización. Devuelve el id generado (o -1 si no se obtuvo).
    public int guardar(Autorizaciones a) throws Exception {
        String sql = "INSERT INTO Autorizaciones(descripcion,area,tipo,fechaInicio,fechaTerminacion,fechaGeneracion,PDFs) VALUES(?,?,?,?,?,?,?)";
        try (Connection con = conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, a.getDescripcion());
            pst.setString(2, a.getArea());
            pst.setString(3, a.getTipo());
            pst.setDate(4, a.getFechaInicio());
            pst.setDate(5, a.getFechaTerminacion());
            pst.setString(6, a.getFechaGeneracion());
            pst.setBytes(7, a.getPDFs());

            int filas = pst.executeUpdate();
            if (filas == 0) return -1;
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        }
    }

    // Eliminar por id
    public boolean eliminar(int id) throws Exception {
        String sql = "DELETE FROM Autorizaciones WHERE id = ?";
        try (Connection con = conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        }
    }

    // Listar todas las autorizaciones
    public List<Autorizaciones> listarTodas() throws Exception {
        String sql = "SELECT * FROM Autorizaciones";
        List<Autorizaciones> lista = new ArrayList<>();
        try (Connection con = conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Autorizaciones a = new Autorizaciones(
                        rs.getInt("id"),
                        rs.getString("descripcion"),
                        rs.getString("area"),
                        rs.getString("tipo"),
                        rs.getDate("fechaInicio"),
                        rs.getDate("fechaTerminacion"),
                        rs.getString("fechaGeneracion"),
                        rs.getBytes("PDFs")
                );
                lista.add(a);
            }
        }
        return lista;
    }

    // Filtrar por área (si area == "TODAS" devuelve todas)
    public List<Autorizaciones> filtrarPorArea(String area) throws Exception {
        if (area == null || area.isBlank() || area.equalsIgnoreCase("TODAS")) {
            return listarTodas();
        }
        String sql = "SELECT * FROM Autorizaciones WHERE area = ?";
        List<Autorizaciones> lista = new ArrayList<>();
        try (Connection con = conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, area);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Autorizaciones a = new Autorizaciones(
                            rs.getInt("id"),
                            rs.getString("descripcion"),
                            rs.getString("area"),
                            rs.getString("tipo"),
                            rs.getDate("fechaInicio"),
                            rs.getDate("fechaTerminacion"),
                            rs.getString("fechaGeneracion"),
                            rs.getBytes("PDFs")
                    );
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    // Obtener bytes del PDF por id (retorna null si no existe)
    public byte[] obtenerPDFPorId(int id) throws Exception {
        String sql = "SELECT PDFs FROM Autorizaciones WHERE id = ?";
        try (Connection con = conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getBytes("PDFs");
            }
        }
        return null;
    }
}