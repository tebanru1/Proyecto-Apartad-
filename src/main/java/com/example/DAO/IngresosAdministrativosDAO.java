package com.example.DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.example.Controlador.Conexion;
import com.example.Modelo.ingresoAdministrativos;

public class IngresosAdministrativosDAO {
    PreparedStatement ps;
    Conexion cn = new Conexion();
    Connection con;
    private static Conexion cne = new Conexion();
public void registrar(ingresoAdministrativos in) {
    String sql = "INSERT INTO administrativos(cedula,nombre,apellido,cargo,fotoFuncionario) VALUES (?,?,?,?,?)";
    try {
        con = cn.conectar();
        if (con == null) {
            JOptionPane.showMessageDialog(null, "No se pudo establecer conexión con la base de datos.");
            return;
        }
        ps = con.prepareStatement(sql);
        ps.setString(1, in.getCedula());
        ps.setString(2, in.getNombre());
        ps.setString(3, in.getApellido());
        ps.setString(4, in.getCargo());
        ps.setBytes(5, in.getFotoFuncionario());
        ps.executeUpdate(); 
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, e.toString());
    } finally {
        try {
            if (ps != null) ps.close();
            if (con != null) con.close(); 
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }
}
public void registrarSalida(ingresoAdministrativos administrativo) {
    String sqlActualizar = """
        UPDATE RegistrosAsistencia
        SET horaSalida = ?, huellaSalida = ?
        WHERE id = (
            SELECT r.id
            FROM RegistrosAsistencia r
            INNER JOIN Administrativos a ON r.administrativo_id = a.id
            WHERE a.cedula = ? AND r.horaSalida IS NULL
            ORDER BY r.fecha DESC, r.horaIngreso DESC
            LIMIT 1
        )
    """;

    try (Connection con = cn.conectar();
         PreparedStatement psActualizar = con.prepareStatement(sqlActualizar)) {

        psActualizar.setTime(1, administrativo.getHoraSalida());
        psActualizar.setBytes(2, administrativo.getHuellaSalida());
        psActualizar.setString(3, administrativo.getCedula());

        int filasActualizadas = psActualizar.executeUpdate();

        if (filasActualizadas > 0) {
            System.out.println("✅ Salida registrada correctamente para la cédula: " + administrativo.getCedula());
        } else {
            System.out.println("⚠️ No se encontró un registro pendiente para la cédula: " + administrativo.getCedula());
        }

    } catch (SQLException e) {
        System.err.println("❌ Error al registrar salida: " + e.getMessage());
        e.printStackTrace();
    }
}
public static ingresoAdministrativos buscarUltimoIngresoPendiente(String cedula) throws SQLException {
    String sql = """
        SELECT r.id, r.horaIngreso, r.horaSalida, r.huellaIngreso, r.administrativo_id, a.nombre, a.apellido, a.cargo
        FROM RegistrosAsistencia r
        INNER JOIN Administrativos a ON r.administrativo_id = a.id
        WHERE a.cedula = ? AND r.horaSalida IS NULL
        ORDER BY r.fecha DESC, r.horaIngreso DESC
        LIMIT 1
    """;

    try (
        Connection con = cne.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, cedula);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            ingresoAdministrativos ingreso = new ingresoAdministrativos(
                cedula,
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("cargo"),
                null, // fecha
                rs.getTime("horaIngreso"),
                null, // horaSalida
                rs.getBytes("huellaIngreso"),
                null, // huellaSalida
                null, // foto
                null  // usuario
            );
            return ingreso;
        }
    }catch(SQLException e){
        System.err.println("❌ Error al buscar último ingreso pendiente: " + e.getMessage());
        e.printStackTrace();
    }

    return null; // no hay ingreso pendiente
}


public void ingreso(ingresoAdministrativos administrativo) {
    String sqlBuscarId = "SELECT id FROM Administrativos WHERE cedula = ?";
    String sqlActualizar = "INSERT INTO RegistrosAsistencia (fecha,horaIngreso,huellaIngreso,administrativo_id,usuario) values (?, ?, ?, ?, ?)";

    try (Connection con = cn.conectar();
         PreparedStatement psBuscar = con.prepareStatement(sqlBuscarId)) {

        // 1️⃣ Buscar el id del administrativo
        psBuscar.setString(1, administrativo.getCedula());
        ResultSet rs = psBuscar.executeQuery();

        if (rs.next()) {
            int idAdministrativo = rs.getInt("id");

            // 2️⃣ Actualizar registro usando el id
            try (PreparedStatement psActualizar = con.prepareStatement(sqlActualizar)) {
                psActualizar.setDate(1, administrativo.getFecha());
                psActualizar.setTime(2, administrativo.getHoraIngreso());
                psActualizar.setBytes(3, administrativo.getHuellaIngreso());
                psActualizar.setInt(4, idAdministrativo);
                psActualizar.setString(5, administrativo.getUsuario());

                int filasActualizadas = psActualizar.executeUpdate();

                if (filasActualizadas > 0) {
                    System.out.println("✅ Ingreso actualizado correctamente para la cédula: " + administrativo.getCedula());
                } else {
                    System.out.println("⚠️ No se encontró un registro para el administrativo con id: " + idAdministrativo);
                }
            }

        } else {
            System.out.println("⚠️ No se encontró un administrativo con la cédula: " + administrativo.getCedula());
        }

    } catch (SQLException e) {
        System.err.println("❌ Error al actualizar ingreso: " + e.getMessage());
        e.printStackTrace();
    }
}

public ingresoAdministrativos buscarIngreso(ingresoAdministrativos administrativo) {
    String sqlAdmin = "SELECT * FROM Administrativos WHERE cedula = ?";
    String sqlIngreso = "SELECT * FROM RegistrosAsistencia WHERE administrativo_id = ? ORDER BY fecha DESC, horaIngreso DESC LIMIT 1";
    ingresoAdministrativos ingre = null;

    try (Connection con = cn.conectar()) {

        // --- 1. Buscar administrativo ---
        try (PreparedStatement ps = con.prepareStatement(sqlAdmin)) {
            ps.setString(1, administrativo.getCedula());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ingre = new ingresoAdministrativos();
                ingre.setCedula(rs.getString("cedula"));
                ingre.setNombre(rs.getString("nombre"));
                ingre.setApellido(rs.getString("apellido"));
                ingre.setCargo(rs.getString("cargo"));
                ingre.setFotoFuncionario(rs.getBytes("fotoFuncionario"));

                int idAdministrativo = rs.getInt("id");

                // --- 2. Buscar último registro de asistencia ---
                try (PreparedStatement pst = con.prepareStatement(sqlIngreso)) {
                    pst.setInt(1, idAdministrativo);
                    ResultSet rsIngreso = pst.executeQuery();

                    if (rsIngreso.next()) {
                        ingre.setHoraIngreso(rsIngreso.getTime("horaIngreso"));
                        ingre.setHoraSalida(rsIngreso.getTime("horaSalida"));
                        ingre.setHuellaIngreso(rsIngreso.getBytes("huellaIngreso"));
                        ingre.setHuellaSalida(rsIngreso.getBytes("huellaSalida"));
                    }
                }
            }
        }

    } catch (SQLException e) {
        System.out.println("Error al buscar ingreso: " + e.toString());
    }

    return ingre;
}

    public ingresoAdministrativos buscarPorCedula(String cedula) {
    String sql = "SELECT * FROM administrativos WHERE cedula = ?";
    ingresoAdministrativos in = null;

    try (Connection con = cn.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, cedula);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            // Crear objeto con los datos encontrados
            in = new ingresoAdministrativos(
                    rs.getString("cedula"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("cargo"),
                    rs.getBytes("fotoFuncionario")
            );
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error al buscar: " + e.toString());
    }

    return in; // Retorna null si no encontró nada
}

public boolean existeCedula(String cedula) {
    String sql = "SELECT * FROM administrativos WHERE cedula = ?";
    try (Connection con = cn.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, cedula);
        ResultSet rs = ps.executeQuery();
        return rs.next(); // Devuelve true si existe al menos una fila

    } catch (SQLException e) {
        System.err.println("Error al verificar cédula: " + e.getMessage());
        return false;
    }
}
public List<ingresoAdministrativos> listarTodos() throws Exception {
    String sql = """
        SELECT r.id AS registroId, r.administrativo_id, a.cedula, a.nombre, a.apellido, a.cargo, a.fotoFuncionario,
               r.fecha, r.horaIngreso, r.horaSalida, r.huellaIngreso, r.huellaSalida, r.usuario
        FROM RegistrosAsistencia r
        INNER JOIN Administrativos a ON r.administrativo_id = a.id
        ORDER BY r.fecha DESC, r.horaIngreso DESC
    """;

    List<ingresoAdministrativos> lista = new ArrayList<>();
    try (Connection con = cn.conectar();
         PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            ingresoAdministrativos a = new ingresoAdministrativos(
                    rs.getString("cedula"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("cargo"),
                    rs.getDate("fecha"),
                    rs.getTime("horaIngreso"),
                    rs.getTime("horaSalida"),
                    rs.getBytes("huellaIngreso"),
                    rs.getBytes("huellaSalida"),
                    rs.getBytes("fotoFuncionario"),
                    rs.getString("usuario")
            );
            lista.add(a);
        }
    }
    return lista;
}

public int contarAdmin() throws Exception {
    int total = 0;
    String sql = "SELECT COUNT(*) FROM RegistrosAsistencia WHERE fecha = CURRENT_DATE AND horaSalida IS NULL";

    try (Connection con = cn.conectar();
         PreparedStatement pstmt = con.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            total = rs.getInt(1);
        }
    }

    return total;
}
public List<ingresoAdministrativos> FiltrarAvanzado(LocalDate fecha, String documento, String nombre) throws Exception {
    StringBuilder sql = new StringBuilder("""
        SELECT r.id AS registroId, r.administrativo_id, a.cedula, a.nombre, a.apellido, a.cargo, a.fotoFuncionario,
               r.fecha, r.horaIngreso, r.horaSalida, r.huellaIngreso, r.huellaSalida, r.usuario
        FROM RegistrosAsistencia r
        INNER JOIN Administrativos a ON r.administrativo_id = a.id
        WHERE 1=1
    """);

    List<Object> parametros = new ArrayList<>();

    if (fecha != null) {
        sql.append(" AND r.fecha = ?");
        parametros.add(Date.valueOf(fecha));
    }
    if (documento != null && !documento.isEmpty()) {
        sql.append(" AND a.cedula LIKE ?");
        parametros.add("%" + documento + "%");
    }
    if (nombre != null && !nombre.isEmpty()) {
        sql.append(" AND LOWER(a.nombre) LIKE ?");
        parametros.add("%" + nombre.toLowerCase() + "%");
    }
    sql.append(" ORDER BY r.fecha DESC, r.horaIngreso DESC ");
    List<ingresoAdministrativos> lista = new ArrayList<>();

    try (Connection con = cn.conectar();
         PreparedStatement pst = con.prepareStatement(sql.toString())) {

        for (int i = 0; i < parametros.size(); i++) {
            pst.setObject(i + 1, parametros.get(i));
        }

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                ingresoAdministrativos a = new ingresoAdministrativos(
                        rs.getString("cedula"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("cargo"),
                        rs.getDate("fecha"),
                        rs.getTime("horaIngreso"),
                        rs.getTime("horaSalida"),
                        rs.getBytes("huellaIngreso"),
                        rs.getBytes("huellaSalida"),
                        rs.getBytes("fotoFuncionario"),
                        rs.getString("usuario")
                );
                lista.add(a);
            }
        }
    }

    return lista;
}

}

