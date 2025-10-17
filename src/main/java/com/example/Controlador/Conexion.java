package com.example.Controlador;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Conexion {
    Connection con;

    public Connection conectar() {
        try {
            String url = "jdbc:mysql://localhost:3306/Apartado";
            String user = "root";
            String pass = "";

            con = DriverManager.getConnection(url, user, pass);
            return con;

        } catch (SQLException e) {
            System.out.println("Error al conectar: " + e.getMessage());
            return null;
        }
    }
}