package com.example.Controlador;

import com.example.DAO.AutorizacionesDAO;
import com.example.Modelo.Autorizaciones;
import com.example.Modelo.Usuario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AutorizacionesController implements Initializable {
    @FXML Label lbIngresadas,lblTotalAutorizaciones,lblVencidas;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private TextField archivonombre;
    @FXML
    private ComboBox<String> AREA;
    @FXML
    private ComboBox<String> TIPO;
    @FXML
    private ComboBox<String> filtrar; 
    @FXML
    private Button btnSubirBD,btnEliminarArchivo,btnDescargarArchivo,btncargarArchivo,btnAutorizacionIngresada; 
    @FXML
    private TableView<Autorizaciones> tablaAutorizaciones;
    @FXML
    private TableColumn<Autorizaciones, String> id;
    @FXML
    private TableColumn<Autorizaciones, String> columnaDescripcion;
    @FXML
    private TableColumn<Autorizaciones, String> columnaArea;
    @FXML
    private TableColumn<Autorizaciones, String> columnaTipo;
    @FXML
    private TableColumn<Autorizaciones, String> columnaFecha;
    @FXML
    private TableColumn<Autorizaciones, String> columnaFechaTerminacion;
    @FXML
    private TableColumn<Autorizaciones, String> columnaFechaGeneracion,columnaEstado;
    @FXML
    private DatePicker fechainicio;
    @FXML
    private DatePicker fechaterminacion;

    private AutorizacionesDAO autorizacionesDAO;
    private ObservableList<Autorizaciones> listaAutorizaciones;
    private int idSeleccionado; 

    // <<--- Variable para almacenar el PDF seleccionado
    private File PDF;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        autorizacionesDAO = new AutorizacionesDAO();
        ConfigurarComboBox();
        configurarEventos();
        configurarTablaAutorizaciones();
        configurarEventoTabla();
        cargarAutorizacionesDesdeBaseDeDatos();
        AutorizacionesIngresadas();
        AutorizacionesVencidas();
        AutorizacionesRecibidas();
    }
        public interface UsuarioReceptor {
            void setUsuario(Usuario usuario);
        }

    private void configurarEventos() {
        btncargarArchivo.setOnAction(event -> cargarArchivo((Stage) btncargarArchivo.getScene().getWindow()));
        btnSubirBD.setOnAction(event -> subir());
        filtrar.setOnAction(event -> filtrarAutorizaciones());
        btnEliminarArchivo.setOnAction(event -> EliminarAutorizacion());
    }
private void configurarEventoTabla() {
    tablaAutorizaciones.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        if (newSelection != null) {
            txtDescripcion.setText(newSelection.getDescripcion());
            AREA.setValue(newSelection.getArea());
            TIPO.setValue(newSelection.getTipo());
            fechainicio.setValue(newSelection.getFechaInicio().toLocalDate());
            fechaterminacion.setValue(newSelection.getFechaTerminacion().toLocalDate());
            idSeleccionado=newSelection.getId();
            archivonombre.setText("Archivo cargado"); 
        }
    });

    // Evento para abrir PDF al hacer doble clic
    tablaAutorizaciones.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2) { // doble clic
            Autorizaciones seleccion = tablaAutorizaciones.getSelectionModel().getSelectedItem();
            if (seleccion != null) {
                abrirPDFDesdeDB(seleccion.getId(), seleccion.getDescripcion());
            }
        }
    });
}

private void abrirPDFDesdeDB(int idAutorizacion, String descripcion) {
    try {
        byte[] pdfBytes = autorizacionesDAO.obtenerPDFPorId(idAutorizacion);
        if (pdfBytes == null || pdfBytes.length == 0) {
            mostrarMensaje("No se encontró PDF en la base de datos.");
            return;
        }

        String nombreArchivo = descripcion.replaceAll("[^a-zA-Z0-9\\-_\\.]", "_");
        PDF = File.createTempFile(nombreArchivo + "_", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(PDF)) {
            fos.write(pdfBytes);
        }

        archivonombre.setText(PDF.getName());

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(PDF);
        } else {
            mostrarMensaje("No se puede abrir el PDF en este sistema.");
        }
    } catch (Exception e) {
        mostrarMensaje("Error al abrir PDF desde DB: " + e.getMessage());
    }
}


    private void ConfigurarComboBox() {
        AREA.getItems().addAll("ATENCIÓN Y TRATAMIENTO", "COMANDO DE VIGILANCIA", "DIRECCIÓN", "DOMICILIARIA",
                "EXPENDIO", "JURIDICA", "PAGADURIA", "PABELLONES", "POLICIA JUDICIAL", "RANCHO",
                "RESEÑA", "SANIDAD", "OTRO");
        TIPO.getItems().addAll("OCASIONAL", "PERIODICO", "PERMANENTE");
        AREA.setPromptText("SELECCIONE ÁREA");
        TIPO.setPromptText("SELECCIONE TIPO");
        filtrar.setPromptText("SELECCIONE FILTRO");
        filtrar.getItems().addAll("TODAS","ATENCIÓN Y TRATAMIENTO", "COMANDO DE VIGILANCIA", "DIRECCIÓN", "DOMICILIARIA",
                "EXPENDIO", "JURIDICA", "PAGADURIA", "PABELLONES", "POLICIA JUDICIAL", "RANCHO",
                "RESEÑA", "SANIDAD");
    }

    private void cargarArchivo(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);
        if (archivoSeleccionado != null) {
            PDF = archivoSeleccionado;
            archivonombre.setText(archivoSeleccionado.getName());
            System.out.println("Archivo seleccionado: " + PDF.getAbsolutePath());
        } else {
            System.out.println("No se seleccionó ningún archivo.");
        }
    }

    private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("AUTORIZACIONES");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarCampos() {
        txtDescripcion.clear();
        AREA.getSelectionModel().clearSelection();
        TIPO.getSelectionModel().clearSelection();
        fechainicio.getEditor().clear();
        fechaterminacion.getEditor().clear();
        archivonombre.clear();
        PDF = null;
    }

    private void configurarTablaAutorizaciones() {
        id.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        columnaDescripcion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescripcion()));
        columnaArea.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getArea()));
        columnaTipo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTipo()));
        columnaFecha.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFechaInicio().toString()));
        columnaFechaTerminacion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFechaTerminacion().toString()));
        columnaFechaGeneracion.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFechaGeneracion()));
        columnaEstado.setCellValueFactory(cellData->
             new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEstado()));
                listaAutorizaciones=FXCollections.observableArrayList();
                tablaAutorizaciones.setItems(listaAutorizaciones);

    }

    private void subir() {
        try {
            int id = 0;
            String descripcion = txtDescripcion.getText().toUpperCase();
            String area = AREA.getValue();
            String tipo = TIPO.getValue();
            Date fechaInicio = Date.valueOf(fechainicio.getValue());
            Date fechaTerminacion = Date.valueOf(fechaterminacion.getValue());
            String fechaGeneracion = LocalDate.now().toString();

            if (PDF == null) {
                mostrarMensaje("Debe seleccionar un archivo PDF antes de subir.");
                return;
            }

            byte[] PDFs = java.nio.file.Files.readAllBytes(PDF.toPath());

            Autorizaciones autorizaciones = new Autorizaciones(id, descripcion, area, tipo, fechaInicio,
                    fechaTerminacion, fechaGeneracion, PDFs,null);

            int nuevoId = autorizacionesDAO.guardar(autorizaciones);
            if (nuevoId > 0) {
                autorizaciones.setId(nuevoId);
                listaAutorizaciones.add(autorizaciones);
                mostrarMensaje("Archivo subido correctamente.");
                limpiarCampos();
            } else {
                mostrarMensaje("Error al subir el archivo a la base de datos.");
            }
        } catch (Exception e) {
            System.out.println("Error al subir autorización: " + e.getMessage());
        }
    }

    private void filtrarAutorizaciones() {
        try {
            String areaSeleccionada = filtrar.getValue();
            if (areaSeleccionada == null || areaSeleccionada.isEmpty()) {
                mostrarMensaje("Por favor seleccione un área antes de filtrar.");
                return;
            }
            listaAutorizaciones.clear();
            listaAutorizaciones.addAll(autorizacionesDAO.filtrarPorArea(areaSeleccionada));
            if (listaAutorizaciones.isEmpty()) mostrarMensaje("No se encontraron autorizaciones para el área seleccionada.");
        } catch (Exception e) {
            mostrarMensaje("Error al filtrar autorizaciones: " + e.getMessage());
        }
    }

    private void EliminarAutorizacion() {
        Autorizaciones autorizacionSeleccionada = tablaAutorizaciones.getSelectionModel().getSelectedItem();
        if (autorizacionSeleccionada == null) {
            mostrarMensaje("Por favor seleccione una autorización para eliminar.");
            return;
        }
        try {
            boolean ok = autorizacionesDAO.eliminar(autorizacionSeleccionada.getId());
            if (ok) {
                listaAutorizaciones.remove(autorizacionSeleccionada);
                mostrarMensaje("Autorización eliminada correctamente.");
                limpiarCampos();
            } else {
                mostrarMensaje("Error al eliminar la autorización.");
            }
        } catch (Exception e) {
            mostrarMensaje("Error al eliminar autorización: " + e.getMessage());
        }
    }

    private void cargarAutorizacionesDesdeBaseDeDatos() {
        try {
            listaAutorizaciones.clear();
            listaAutorizaciones.addAll(autorizacionesDAO.listarTodas());
        } catch (Exception e) {
            System.out.println("Error al cargar autorizaciones: " + e.getMessage());
        }
    }  
    private void AutorizacionesIngresadas(){
        try {
            int total=autorizacionesDAO.AutorizacionesIngresadas();
            lblTotalAutorizaciones.setText(String.valueOf(total));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    } 
       private void AutorizacionesVencidas(){
        try {
            int total=autorizacionesDAO.AutorizacionesVencidas();
            lblVencidas.setText(String.valueOf(total));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }  
     private void AutorizacionesRecibidas(){
        try {
            int total=autorizacionesDAO.AutorizacionesRecibidas();
            lbIngresadas.setText(String.valueOf(total));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    } 
@FXML
private void ActualizarEstado() {

    String nuevoEstado = "INGRESADA"; // Estado que deseas asignar

    boolean actualizado=autorizacionesDAO.actualizarEstadoAutorizacion(idSeleccionado, nuevoEstado);

}
}
