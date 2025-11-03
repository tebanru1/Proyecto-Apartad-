package com.example.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.example.Controlador.Conexion;
import com.example.Modelo.ingreso;

public class IngresosDAO {
    PreparedStatement ps;
    Conexion cn = new Conexion();
    Connection con;
    


      public boolean Ingreso(ingreso in) {
        String sql = "INSERT INTO administrativos(cedula, fecha, hora) VALUES (?,?,?)";
        try {
            con = cn.conectar();
            if (con == null) {
                JOptionPane.showMessageDialog(null, "No se pudo establecer conexión con la base de datos.");
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, in.getCedula());
            ps.setDate(2, in.getFecha());
            ps.setTime(3, in.getHora());
            ps.executeUpdate(); 
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.toString());
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close(); 
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
    }
}
