package com.example.Controlador;



import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

import com.example.DAO.IngresosAdministrativosDAO;
import com.example.Modelo.ingresoAdministrativos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;


public class AdministrativosController implements Initializable {
    @FXML private TableView<ingresoAdministrativos> IngresosAdmin;
    @FXML private TableColumn<ingresoAdministrativos, String> cedula, nombre, apellido, cargo, fecha, horaE, horaS;
    @FXML private Label TFcedula, TFnombre, TFapellido, TFcargo, TFfecha, TFhoraE, TFhoraS;
    @FXML private ImageView huellaE, huellaS, FotoPerfil;

    private ObservableList<ingresoAdministrativos> listaIngresoAdministrativos;
    IngresosAdministrativosDAO IngresosAdministrativosDAO = new IngresosAdministrativosDAO();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablaIngresos();
        cargarIngresos();
        configurarSeleccionTabla();
}
 private void configurarTablaIngresos(){
        cedula.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getCedula())));
        nombre.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));
        apellido.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getApellido()));
        cargo.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCargo()));
        fecha.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getFecha())));
        horaE.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getHoraIngreso())));
        horaS.setCellValueFactory(cellData -> {
        String horaSalida = cellData.getValue().getHoraSalida() != null? cellData.getValue().getHoraSalida().toString(): "PENDIENTE";
        return new javafx.beans.property.SimpleStringProperty(horaSalida);});
        listaIngresoAdministrativos=FXCollections.observableArrayList();
        IngresosAdmin.setItems(listaIngresoAdministrativos);
    }
 
 private void cargarIngresos() {
        try {
            listaIngresoAdministrativos.clear();
            listaIngresoAdministrativos.addAll(IngresosAdministrativosDAO.listarTodos());
        } catch (Exception e) {
            System.out.println("Error al cargar ingresos: " + e.getMessage());
        }
    }  

 private void configurarSeleccionTabla() {
    IngresosAdmin.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        if (newSelection != null) {
            ingresoAdministrativos seleccionado = newSelection;

            // Cargar datos en los Labels
            TFcedula.setText(seleccionado.getCedula());
            TFnombre.setText(seleccionado.getNombre());
            TFapellido.setText(seleccionado.getApellido());
            TFcargo.setText(seleccionado.getCargo());
            TFfecha.setText(seleccionado.getFecha() != null ? seleccionado.getFecha().toString() : "");
            TFhoraE.setText(seleccionado.getHoraIngreso() != null ? seleccionado.getHoraIngreso().toString() : "");
            TFhoraS.setText(seleccionado.getHoraSalida() != null ? seleccionado.getHoraSalida().toString() : "PENDIENTE");

            // Cargar la foto si existe
            byte[] fotoBytes = seleccionado.getFotoFuncionario();
            if (fotoBytes != null && fotoBytes.length > 0) {
                Image foto = new Image(new ByteArrayInputStream(fotoBytes));
                FotoPerfil.setImage(foto);

                // Hacer la imagen redonda
                double radio = Math.min(FotoPerfil.getFitWidth(), FotoPerfil.getFitHeight()) / 2;
                Circle clip = new Circle(radio);
                clip.setCenterX(FotoPerfil.getFitWidth() / 2);
                clip.setCenterY(FotoPerfil.getFitHeight() / 2);
                FotoPerfil.setClip(clip);
            } else {
                // Imagen por defecto
                Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/usuario.png"));
                FotoPerfil.setImage(defaultImage);
            }
        }
    });
}

}
