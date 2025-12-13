package com.example.DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.Controlador.Conexion;

import com.example.Modelo.Carnet;

public class CarnetDAO {
    private Conexion conexion;

    public CarnetDAO() {
        this.conexion = new Conexion(); 
    }
public List<Carnet> listaFiltrada(String nombre, String apellido, String documento,
                                   String entidad, LocalDate fechaCreacion, LocalDate vigencia) throws Exception {

    List<Carnet> lista = new ArrayList<>();
    StringBuilder sql = new StringBuilder("SELECT * FROM carnet WHERE 1=1");
    List<Object> parametros = new ArrayList<>();

    // Filtros condicionales
    if (nombre != null && !nombre.isEmpty()) {
        sql.append(" AND LOWER(nombre) LIKE ?");
        parametros.add("%" + nombre.toLowerCase() + "%");
    }
    if (apellido != null && !apellido.isEmpty()) {
        sql.append(" AND LOWER(apellido) LIKE ?");
        parametros.add("%" + apellido.toLowerCase() + "%");
    }
    if (documento != null && !documento.isEmpty()) {
        sql.append(" AND documento LIKE ?");
        parametros.add("%" + documento + "%");
    }
    if (entidad != null && !entidad.isEmpty()) {
        sql.append(" AND LOWER(entidad) LIKE ?");
        parametros.add("%" + entidad.toLowerCase() + "%");
    }
    if (fechaCreacion != null) {
        sql.append(" AND fechaGeneracion = ?");
        parametros.add(java.sql.Date.valueOf(fechaCreacion));
    }
    if (vigencia != null) {
        sql.append(" AND fechaVigencia = ?");
        parametros.add(java.sql.Date.valueOf(vigencia));
    }

    try (Connection con = conexion.conectar();
         PreparedStatement pst = con.prepareStatement(sql.toString())) {

        // Asignar parámetros al PreparedStatement
        for (int i = 0; i < parametros.size(); i++) {
            pst.setObject(i + 1, parametros.get(i));
        }

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Carnet a = new Carnet(
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("documento"),
                        rs.getString("entidad"),
                        rs.getString("cargo"),
                        rs.getString("rh"),
                        rs.getDate("fechaVigencia").toLocalDate(),
                        rs.getDate("fechaGeneracion").toLocalDate(),
                        rs.getBytes("codigoPDF417"),
                        rs.getString("usuario"),
                        rs.getInt("id")
                );
                lista.add(a);
            }
        }
    }

    return lista;
}

    public List<Carnet> listarTodas() throws Exception {
        String sql = "SELECT * FROM carnet";
        List<Carnet> lista = new ArrayList<>();
        try (Connection con = conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Carnet a = new Carnet(
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("documento"),
                        rs.getString("entidad"),
                        rs.getString("cargo"),
                        rs.getString("rh"),
                        rs.getDate("fechaVigencia").toLocalDate(),
                        rs.getDate("fechaGeneracion").toLocalDate(),
                        rs.getBytes("codigoPDF417"),
                        rs.getString("usuario"),
                        rs.getInt("id")
                );
                lista.add(a);
            }
        }
        return lista;
    }
    public boolean guardarCodigoEnBD(Carnet codigo) {
        String sql = "INSERT INTO carnet(nombre, apellido, documento, entidad, cargo, rh, fechaGeneracion, fechaVigencia, codigoPDF417, usuario) VALUES (?,?,?,?,?,?,?,?,?,?)";
        Conexion conexion = new Conexion();
        try (Connection con = conexion.conectar()) {
            if (con == null) return false;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, codigo.getNombre());
                ps.setString(2, codigo.getApellido());
                ps.setString(3, codigo.getDocumento());
                ps.setString(4, codigo.getEntidad());
                ps.setString(5, codigo.getCargo());
                ps.setString(6, codigo.getRh());
                ps.setDate(7, Date.valueOf(codigo.getFechaCreacion()));
                ps.setDate(8, Date.valueOf(codigo.getFechaVigencia()));
                ps.setBytes(9, codigo.getCodigo());
                ps.setString(10, codigo.getUsername());
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al guardar código: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean EliminarCarnet(int id) throws Exception {
        String sql = "DELETE FROM carnet WHERE id = ?";
        try (Connection con = conexion.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1,id);
            return pst.executeUpdate() > 0;
        }
    }
    
public boolean actualizar(Carnet carnet) throws Exception {
    String sql = "UPDATE carnet SET nombre=?, apellido=?, documento=?, entidad=?, cargo=?, rh=?, " +
                 "fechaGeneracion=?, fechaVigencia=?, codigoPDF417=? WHERE id=?";

    try (Connection con = conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, carnet.getNombre());
        ps.setString(2, carnet.getApellido());
        ps.setString(3, carnet.getDocumento());
        ps.setString(4, carnet.getEntidad());
        ps.setString(5, carnet.getCargo());
        ps.setString(6, carnet.getRh());

        if (carnet.getFechaCreacion() != null)
            ps.setDate(7, java.sql.Date.valueOf(carnet.getFechaCreacion()));
        else
            ps.setNull(7, java.sql.Types.DATE);

        if (carnet.getFechaVigencia() != null)
            ps.setDate(8, java.sql.Date.valueOf(carnet.getFechaVigencia()));
        else
            ps.setNull(8, java.sql.Types.DATE);

        // PDF417 (BLOB)
        if (carnet.getCodigo() != null)
            ps.setBytes(9, carnet.getCodigo());
        else
            ps.setNull(9, java.sql.Types.BLOB);

        ps.setInt(10, carnet.getId());

        return ps.executeUpdate() > 0;
    }
}

}
