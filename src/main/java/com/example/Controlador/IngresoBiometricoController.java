package com.example.Controlador;


import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import com.example.DAO.IngresosAdministrativosDAO;
import com.example.Modelo.Usuario;
import com.example.Modelo.ingresoAdministrativos;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;




public class IngresoBiometricoController implements Initializable {
    @FXML
    private Button btnRegistrar;
    @FXML private TextField fechaActual, horaIngreso, horaSalida;
    @FXML private ImageView huellaIngreso, huellaSalida;
    private String cedula;
    private Usuario usuario;
    private ingresoAdministrativos ingreso;
    private final IngresosAdministrativosDAO IngresosAdministrativosDAO = new IngresosAdministrativosDAO();
    SimpleDateFormat formato12Horas = new SimpleDateFormat("hh:mm a");

@Override
public void initialize (URL location, ResourceBundle resources) {
    Date fecha=Date.valueOf(LocalDate.now());
    fechaActual.setText(fecha.toString());
    ConfigurarEventos();
  
}
public void setCedula(String cedula) {
    this.cedula = cedula;
}

public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
}
public void ConfigurarEventos(){
    btnRegistrar.setOnAction(event -> registrarEntradaOSalida());
}

private void registrarEntradaOSalida() {
    try {
        String cedula = this.cedula;
        String usuario1 = usuario.getUsuario();

        // Verificar si hay un ingreso pendiente
        ingresoAdministrativos ultimoIngreso = IngresosAdministrativosDAO.buscarUltimoIngresoPendiente(cedula);

        if (ultimoIngreso != null) {
            // Ya hay un ingreso pendiente -> registrar salida
            Time horaSalida1 = Time.valueOf(LocalTime.now());
            byte[] huellaSalida = null; // Captura real de huella si la tienes

            ingresoAdministrativos salidaAdmin = new ingresoAdministrativos(
                cedula,
                null, null, null,
                null,
                ultimoIngreso.getHoraIngreso(),
                horaSalida1,
                ultimoIngreso.getHuellaIngreso(),
                huellaSalida,
                null,
                usuario1
            );

            IngresosAdministrativosDAO.registrarSalida(salidaAdmin);
            mostrarMensaje("Salida registrada correctamente.");

            // Convertir Time a Date y formatear
            Date horaSalidaDate = new Date(horaSalida1.getTime());
            horaSalida.setText(formato12Horas.format(horaSalidaDate));

        } else {
            // No hay ingreso pendiente -> registrar nuevo ingreso
            Date fecha = Date.valueOf(LocalDate.now());
            Time horaIngreso1 = Time.valueOf(LocalTime.now());
            byte[] huellaIngreso = null; // Captura real de huella si la tienes

            ingresoAdministrativos ingresoAdmin = new ingresoAdministrativos(
                cedula,
                null, null, null,
                fecha,
                horaIngreso1,
                null,
                huellaIngreso,
                null,
                null,
                usuario1
            );

            IngresosAdministrativosDAO.ingreso(ingresoAdmin);
            mostrarMensaje("Ingreso registrado correctamente.");

            // Convertir Time a Date y formatear
            Date horaIngresoDate = new Date(horaIngreso1.getTime());
            horaIngreso.setText(formato12Horas.format(horaIngresoDate));
        }

    } catch (Exception e) {
        e.printStackTrace();
        mostrarMensaje("Error al registrar ingreso o salida: " + e.getMessage());
    }
}


  private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    public void setIngreso(ingresoAdministrativos ingreso) {
    this.ingreso = ingreso;
     Date horaIngresoDate = new Date(ingreso.getHoraIngreso().getTime());
    horaIngreso.setText(formato12Horas.format(horaIngresoDate));
   
}
}