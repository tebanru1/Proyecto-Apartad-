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
        String sql = """
                        SELECT id, descripcion, area, tipo,
                        fechaInicio, fechaTerminacion, fechaGeneracion, PDFs,
                        CASE
                            WHEN estado IS NULL AND fechaTerminacion < CURRENT_DATE THEN 'VENCIDA'
                            WHEN estado IS NULL AND fechaTerminacion >= CURRENT_DATE THEN 'PENDIENTE'
                            ELSE estado
                        END AS estado_calculado
                    FROM Autorizaciones
                    """;
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
                        rs.getBytes("PDFs"),
                        rs.getString("estado_calculado")
                );
                lista.add(a);
            }
        }
        return lista;
    }

    // Filtrar por área (si area == "TODAS" devuelve todas)
    public List<Autorizaciones> filtrar(String descripcion,String area,String tipo,Date fechainicio,Date fechafin) throws Exception {
       StringBuilder sql = new StringBuilder(
        "SELECT id, descripcion, area, tipo, fechaInicio, fechaTerminacion, fechaGeneracion, PDFs, " +
        "CASE " +
        " WHEN estado IS NULL AND fechaTerminacion < CURRENT_DATE THEN 'VENCIDA' " +
        " WHEN estado IS NULL AND fechaTerminacion >= CURRENT_DATE THEN 'PENDIENTE' " +
        " ELSE estado " +
        "END AS estado_calculado " +
        "FROM Autorizaciones WHERE 1=1"
    );
        List<Autorizaciones> lista = new ArrayList<>();
       List<Object> parametros = new ArrayList<>();
         if (descripcion != null && !descripcion.isEmpty()) {
              sql.append(" AND descripcion LIKE ?");
              parametros.add("%" + descripcion + "%");
            }
        if(area != null && !area.isEmpty()) {
            sql.append(" AND area = ?");
            parametros.add(area);
        }
        if(tipo != null && !tipo.isEmpty()) {
            sql.append(" AND tipo = ?");
            parametros.add(tipo);
        }
        if(fechainicio != null) {
            sql.append(" AND fechaInicio = ?");
            parametros.add(fechainicio);
        }
        if(fechafin != null) {
            sql.append(" AND fechaTerminacion = ?");
            parametros.add(fechafin);
        }
        
        try (Connection con = conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametros.size(); i++) {
            pst.setObject(i + 1, parametros.get(i));}
            
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
                            rs.getBytes("PDFs"),
                            rs.getString("estado_calculado")
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

    public int AutorizacionesIngresadas() throws Exception {
    int total = 0;
    String sql = "SELECT COUNT(*) FROM Autorizaciones";

    try (Connection con = conexion.conectar();
         PreparedStatement pstmt = con.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            total = rs.getInt(1);
        }
    }

    return total;
    }
    public int AutorizacionesVencidas() throws Exception {
    int total = 0;
    String sql = "SELECT COUNT(*) FROM Autorizaciones WHERE fechaTerminacion < CURRENT_DATE AND estado IS NULL";

    try (Connection con = conexion.conectar();
         PreparedStatement pstmt = con.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            total = rs.getInt(1);
        }
    }

    return total;
}
  public int AutorizacionesRecibidas() throws Exception {
    int total = 0;
    String sql = "SELECT COUNT(*) FROM Autorizaciones WHERE estado= 'INGRESADA'";

    try (Connection con = conexion.conectar();
         PreparedStatement pstmt = con.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            total = rs.getInt(1);
        }
    }

    return total;
}
  public int AutorizacionesPendientes() throws Exception {
    int total = 0;
    String sql = "SELECT COUNT(*) FROM Autorizaciones WHERE fechaTerminacion >= CURRENT_DATE AND estado IS NULL";

    try (Connection con = conexion.conectar();
         PreparedStatement pstmt = con.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            total = rs.getInt(1);
        }
    }

    return total;
}

public boolean actualizarEstadoAutorizacion(int idAutorizacion, String nuevoEstado) {
    String sql = "UPDATE Autorizaciones SET estado = ? WHERE id = ? AND estado IS NULL";
    // Retorna true si se actualizó, false si no se hizo nada

    try (Connection con = conexion.conectar();
         PreparedStatement pstmt = con.prepareStatement(sql)) {

        pstmt.setString(1, nuevoEstado);       // El estado que queremos asignar
        pstmt.setInt(2, idAutorizacion);       // ID de la autorización

        int filas = pstmt.executeUpdate();
        return filas > 0; // Si filas > 0, se actualizó
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

}