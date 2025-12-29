package com.example.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.example.Controlador.Conexion;
import com.example.Modelo.Usuario;

public class LoginDAO {

    // Método público para validar usuario
    public boolean validarUsuario(String usuario, String contraseña) {
        String sql = "SELECT * FROM usuarios WHERE usuario = ? AND contraseña = ?";

        try (Connection con = new Conexion().conectar();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            pstmt.setString(2, contraseña);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); 
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Usuario obtenerDatosUsuario(String usuario) {
    String sql = "SELECT id, nombre, apellido, grado, rol, usuario FROM usuarios WHERE usuario = ?";

    try (Connection con = new Conexion().conectar();
         PreparedStatement pstmt = con.prepareStatement(sql)) {

        pstmt.setString(1, usuario);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("grado"),
                    rs.getString("rol"),
                    rs.getString("usuario")
                );
            } else {
                return null; // usuario no encontrado
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
public void registrarUsuario(Usuario usuario){
    String sql="INSERT INTO usuarios (cedula,nombre,apellido,usuario,contraseña,grado,rol) VALUES (?,?,?,?,?,?,?)";
    try {
        Connection cn=new Conexion().conectar();
        PreparedStatement pst=cn.prepareStatement(sql);
        pst.setString(1, usuario.getCedula());
        pst.setString(2, usuario.getNombre());
        pst.setString(3, usuario.getApellido());
        pst.setString(4, usuario.getUsuario());
        pst.setString(5, usuario.getContrasena());
        pst.setString(6, usuario.getGrado());
        pst.setString(7, usuario.getRol());
        pst.executeUpdate();

    } catch (Exception e) {
        System.out.println(e.toString());
    }
}

}
