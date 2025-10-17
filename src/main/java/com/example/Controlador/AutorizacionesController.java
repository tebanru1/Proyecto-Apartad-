package com.example.Controlador;

import com.example.Modelo.Autorizaciones;

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
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AutorizacionesController implements Initializable {

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
    private Button btncargarArchivo;
    @FXML
    private Button btnDescargarArchivo;
    @FXML
    private Button btnEliminarArchivo;
    @FXML
    private Button btnSubirBD;   // <<--- Nuevo botón para subir a la BD
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
    private TableColumn<Autorizaciones, String> columnaFechaGeneracion;
    @FXML
    private DatePicker fechainicio;
    @FXML
    private DatePicker fechaterminacion;

    private Conexion conexion;
    private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;
    private ObservableList<Autorizaciones> listaAutorizaciones;

    // <<--- Variable para almacenar el PDF seleccionado
    private File PDF;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conexion = new Conexion();
        ConfigurarComboBox();
        configurarEventos();
        configurarTablaAutorizaciones();
        configurarEventoTabla();
        cargarAutorizacionesDesdeBaseDeDatos();
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
    String sql = "SELECT PDFs FROM Autorizaciones WHERE id = ?";

    try {
        con = conexion.conectar();
        pst = con.prepareStatement(sql);
        pst.setInt(1, idAutorizacion);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            byte[] pdfBytes = rs.getBytes("PDFs");

            String nombreArchivo = descripcion.replaceAll("[^a-zA-Z0-9\\-_\\.]", "_"); 
            PDF = File.createTempFile(nombreArchivo + "_",".pdf");
            try (FileOutputStream fos = new FileOutputStream(PDF)) {
                fos.write(pdfBytes);
            }

            archivonombre.setText(PDF.getName());
            System.out.println("PDF cargado: " + PDF.getAbsolutePath());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(PDF);
            } else {
                mostrarMensaje("No se puede abrir el PDF en este sistema.");
            }

        } else {
            mostrarMensaje("No se encontró PDF en la base de datos.");
        }

        rs.close();
    } catch (Exception e) {
        mostrarMensaje("Error al abrir PDF desde DB: " + e.getMessage());
    } finally {
        try {
            if (pst != null) pst.close();
            if (con != null) con.close();
        } catch (Exception e) {
            mostrarMensaje("Error al cerrar la conexión: " + e.getMessage());
        }
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

            Autorizaciones autorizaciones = new Autorizaciones(id,descripcion, area, tipo, fechaInicio,
                    fechaTerminacion, fechaGeneracion, PDFs);

            subirArchivoDB(autorizaciones);
            listaAutorizaciones.add(autorizaciones);
            mostrarMensaje("Archivo subido correctamente.");
            limpiarCampos();
        } catch (Exception e) {
            System.out.println("Error al subir autorización: " + e.getMessage());
        }
    }

    private void subirArchivoDB(Autorizaciones autorizaciones) {
        String sql = "INSERT INTO Autorizaciones(descripcion,area,tipo,fechaInicio,fechaTerminacion,fechaGeneracion,PDFs) VALUES(?,?,?,?,?,?,?)";
        try {
            con = conexion.conectar();
            pst = con.prepareStatement(sql);
            pst.setString(1, autorizaciones.getDescripcion());
            pst.setString(2, autorizaciones.getArea());
            pst.setString(3, autorizaciones.getTipo());
            pst.setDate(4, autorizaciones.getFechaInicio());
            pst.setDate(5, autorizaciones.getFechaTerminacion());
            pst.setDate(6, Date.valueOf(LocalDate.now()));
            pst.setBytes(7, autorizaciones.getPDFs());

            int filasAfectadas = pst.executeUpdate();
            if (filasAfectadas > 0) {
                mostrarMensaje("Archivo subido correctamente a la base de datos.");
            } else {
                mostrarMensaje("Error al subir el archivo a la base de datos.");
            }
        } catch (Exception e) {
            System.out.println("Error al subir el archivo: " + e.getMessage());
        } finally {
            try {
                if (pst != null) pst.close();
                if (con != null) con.close();
            } catch (Exception e) {
                System.out.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
private void filtrarAutorizaciones() {

    if (filtrar.getValue() == null || filtrar.getValue().isEmpty()) {
        mostrarMensaje("Por favor seleccione un área antes de filtrar.");
        return;
    }

    String areaSeleccionada = filtrar.getValue();
    String sql;
    if (areaSeleccionada.equalsIgnoreCase("TODAS")) {
        sql = "SELECT * FROM Autorizaciones";
    } else {
        sql = "SELECT * FROM Autorizaciones WHERE area = ?";
    }

    try {
        con = conexion.conectar();
        pst = con.prepareStatement(sql);

        if (!areaSeleccionada.equalsIgnoreCase("Todas")) {
            pst.setString(1, areaSeleccionada);
        }

        rs = pst.executeQuery();
        listaAutorizaciones.clear();

        boolean hayResultados = false;

        while (rs.next()) {
            Autorizaciones autorizacion = new Autorizaciones(
                    rs.getInt("id"),
                    rs.getString("descripcion"),
                    rs.getString("area"),
                    rs.getString("tipo"),
                    rs.getDate("fechaInicio"),
                    rs.getDate("fechaTerminacion"),
                    rs.getDate("fechaGeneracion").toString(),
                    rs.getBytes("PDFs")
            );
            listaAutorizaciones.add(autorizacion);
            hayResultados = true;
        }

        if (!hayResultados) {
            mostrarMensaje("No se encontraron autorizaciones para el área seleccionada.");
        }

    } catch (Exception e) {
        mostrarMensaje("Error al filtrar autorizaciones: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (con != null) con.close();
        } catch (Exception e) {
            mostrarMensaje("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
private void EliminarAutorizacion() {
        Autorizaciones autorizacionSeleccionada = tablaAutorizaciones.getSelectionModel().getSelectedItem();
        if (autorizacionSeleccionada == null) {
            mostrarMensaje("Por favor seleccione una autorización para eliminar.");
            return;
        }

        String sql = "DELETE FROM Autorizaciones WHERE id = ? ";

        try {
            con = conexion.conectar();
            pst = con.prepareStatement(sql);
            pst.setInt(1, autorizacionSeleccionada.getId());

            int filasAfectadas = pst.executeUpdate();
            if (filasAfectadas > 0) {
                listaAutorizaciones.remove(autorizacionSeleccionada);
                mostrarMensaje("Autorización eliminada correctamente.");
                limpiarCampos();
            } else {
                mostrarMensaje("Error al eliminar la autorización.");
            }
        } catch (Exception e) {
            mostrarMensaje("Error al eliminar autorización: " + e.getMessage());
        } finally {
            try {
                if (pst != null) pst.close();
                if (con != null) con.close();
            } catch (Exception e) {
                mostrarMensaje("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }   

    private void cargarAutorizacionesDesdeBaseDeDatos() {
        String sql = "SELECT * FROM Autorizaciones";
        try {
            con = conexion.conectar();
            pst = con.prepareStatement(sql);
            var rs = pst.executeQuery();
            listaAutorizaciones.clear();
            while (rs.next()) {
                Autorizaciones autorizacion = new Autorizaciones(
                        rs.getInt("id"),
                        rs.getString("descripcion"),
                        rs.getString("area"),
                        rs.getString("tipo"),
                        rs.getDate("fechaInicio"),
                        rs.getDate("fechaTerminacion"),
                        rs.getDate("fechaGeneracion").toString(),
                        rs.getBytes("PDFs")
                );
                listaAutorizaciones.add(autorizacion);
            }
        } catch (Exception e) {
            System.out.println("Error al cargar autorizaciones: " + e.getMessage());
        } finally {
            try {
                if (pst != null) pst.close();
                if (con != null) con.close();
            } catch (Exception e) {
                System.out.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }   
}
