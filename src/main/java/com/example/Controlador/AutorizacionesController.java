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
import java.util.List;
import java.util.ResourceBundle;

public class AutorizacionesController implements Initializable {
    @FXML Label lbIngresadas,lblTotalAutorizaciones,lblVencidas,lblPendientes;
    @FXML
    private TextField txtDescripcion,Descripcion;
    @FXML
    private TextField archivonombre;
    @FXML
    private ComboBox<String> AREA;
    @FXML
    private ComboBox<String> TIPO;
    @FXML
    private ComboBox<String> Area,Tipo; 
    @FXML
    private Button btnSubirBD,btnEliminarArchivo,btnDescargarArchivo,btncargarArchivo,btnAutorizacionIngresada,btnLimpiar; 
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
    private DatePicker fechainicio,fechaterminacion,FechaIni,FechaFin;
 

    private AutorizacionesDAO autorizacionesDAO;
    private ObservableList<Autorizaciones> listaAutorizaciones;
    private int idSeleccionado; 

    // <<--- Variable para almacenar el PDF seleccionado
    private File PDF;

    /**
     * Inicializa el controlador y configura la vista de autorizaciones.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        autorizacionesDAO = new AutorizacionesDAO();
        ConfigurarComboBox();
        configurarEventos();
        configurarTablaAutorizaciones();
        configurarEventoTabla();
        cargarAutorizacionesDesdeBaseDeDatos();
        DashboardAutorizaciones();
        ConfigurarTextField();
    }
    /**
     * Interfaz para recibir el usuario en controladores.
     */
    public interface UsuarioReceptor {
        void setUsuario(Usuario usuario);
    }
    /**
     * Actualiza los contadores del dashboard de autorizaciones.
     */
    private void DashboardAutorizaciones(){
        AutorizacionesIngresadas();
        AutorizacionesVencidas();
        AutorizacionesRecibidas();
        AutorizacionesPendientes();
}
    /**
     * Configura los eventos de los botones y combos principales.
     */
    private void configurarEventos() {
        btncargarArchivo.setOnAction(event -> cargarArchivo((Stage) btncargarArchivo.getScene().getWindow()));
        btnSubirBD.setOnAction(event -> subir());
        Area.setOnAction(event -> filtrarAutorizaciones());
        btnEliminarArchivo.setOnAction(event -> EliminarAutorizacion());
        btnLimpiar.setOnAction(event -> limpiarFiltros());
    }
    /**
     * Configura los eventos de la tabla de autorizaciones (selección y doble clic).
     */
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

    /**
     * Abre el PDF asociado a una autorización desde la base de datos.
     * @param idAutorizacion ID de la autorización
     * @param descripcion Descripción para el nombre del archivo
     */
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


    /**
     * Configura los ComboBox de área y tipo para la vista y filtros.
     */
    private void ConfigurarComboBox() {
        AREA.getItems().addAll("ATENCIÓN Y TRATAMIENTO", "COMANDO DE VIGILANCIA", "DIRECCIÓN", "DOMICILIARIA",
                "EXPENDIO", "JURIDICA", "PAGADURIA", "PABELLONES", "POLICIA JUDICIAL", "RANCHO",
                "RESEÑA", "SANIDAD", "OTRO");
        TIPO.getItems().addAll("OCASIONAL", "PERIODICO", "PERMANENTE");
        Area.getItems().addAll("ATENCIÓN Y TRATAMIENTO", "COMANDO DE VIGILANCIA", "DIRECCIÓN", "DOMICILIARIA",
                "EXPENDIO", "JURIDICA", "PAGADURIA", "PABELLONES", "POLICIA JUDICIAL", "RANCHO",
                "RESEÑA", "SANIDAD");
        Tipo.getItems().addAll("OCASIONAL", "PERIODICO", "PERMANENTE");
        AREA.setPromptText("SELECCIONE ÁREA");
        Tipo.setPromptText("FILTRAR POR TIPO");
        TIPO.setPromptText("SELECCIONE TIPO");
        Area.setPromptText("FILTRAR POR ÁREA");
    }

    /**
     * Permite seleccionar un archivo PDF desde el sistema de archivos.
     * @param stage Ventana principal
     */
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

    /**
     * Configura un TextField para aceptar solo mayúsculas y caracteres válidos.
     * @param textField Campo de texto a configurar
     */
    public void TextFieldMayusculas(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(newValue.toUpperCase())) {
                textField.setText(newValue.toUpperCase());
            }
            if(textField.getText().length()>50){
                String s = textField.getText().substring(0, 50);
                textField.setText(s);
            }
            if(!newValue.matches("[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]*")){
                textField.setText(newValue.replaceAll("[^a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]", ""));

            }
        });
    }
    /**
     * Configura los campos de texto para validaciones y formato.
     */
    public void ConfigurarTextField() {
    TextFieldMayusculas(Descripcion);
    TextFieldMayusculas(txtDescripcion);
    
}

    /**
     * Muestra un mensaje informativo en una alerta.
     * @param mensaje Mensaje a mostrar
     */
    private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("AUTORIZACIONES");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Limpia los campos del formulario de registro de autorizaciones.
     */
    private void limpiarCampos() {
        txtDescripcion.clear();
        AREA.getSelectionModel().clearSelection();
        TIPO.getSelectionModel().clearSelection();
        fechainicio.getEditor().clear();
        fechaterminacion.getEditor().clear();
        archivonombre.clear();
        PDF = null;
    }
        /**
         * Limpia los filtros de búsqueda y recarga la tabla de autorizaciones.
         */
        private void limpiarFiltros() {

    Descripcion.clear();

    resetComboBox(Area, "FILTRAR POR ÁREA");
    resetComboBox(Tipo, "FILTRAR POR TIPO");

    FechaIni.setValue(null);
    FechaFin.setValue(null);

    Descripcion.requestFocus();
}

    /**
     * Reinicia un ComboBox a su estado inicial con texto personalizado.
     * @param combo ComboBox a reiniciar
     * @param texto Texto a mostrar como prompt
     */
    private <T> void resetComboBox(ComboBox<T> combo, String texto) {
    combo.setValue(null);
    combo.getSelectionModel().clearSelection();

    combo.setButtonCell(new ListCell<>() {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null || empty ? texto : item.toString());
        }
    });
}


    /**
     * Configura las columnas y eventos de la tabla de autorizaciones.
     */
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
                Descripcion.setOnKeyReleased(event -> filtrarAutorizaciones());
                Area.setOnAction(event -> filtrarAutorizaciones());
                Tipo.setOnAction(event -> filtrarAutorizaciones());
                FechaIni.setOnAction(event -> filtrarAutorizaciones());
                FechaFin.setOnAction(event -> filtrarAutorizaciones());
    }

    /**
     * Sube una nueva autorización con PDF a la base de datos.
     */
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
                cargarAutorizacionesDesdeBaseDeDatos();
                DashboardAutorizaciones();
            } else {
                mostrarMensaje("Error al subir el archivo a la base de datos.");
            }
        } catch (Exception e) {
            System.out.println("Error al subir autorización: " + e.getMessage());
        }
    }

    /**
     * Filtra la lista de autorizaciones según los criterios seleccionados.
     */
    private void filtrarAutorizaciones() {
        try {
          AutorizacionesDAO dao = new AutorizacionesDAO();
            String descr=Descripcion.getText().trim();
            String areaSeleccionada = Area.getValue();
            String tipoSeleccionado = Tipo.getValue();
            Date fechaInicio = (FechaIni.getValue() != null) ? Date.valueOf(FechaIni.getValue()) : null;
            Date fechaFin = (FechaFin.getValue() != null) ? Date.valueOf(FechaFin.getValue()) : null;

            List<Autorizaciones> list = dao.filtrar(descr,areaSeleccionada, tipoSeleccionado, fechaInicio, fechaFin);
            listaAutorizaciones.setAll(list);
    }catch (Exception e) {
            System.out.println("Error al filtrar autorizaciones: " + e.getMessage());
        }
    }

    /**
     * Elimina la autorización seleccionada de la base de datos.
     */
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
                DashboardAutorizaciones();
            } else {
                mostrarMensaje("Error al eliminar la autorización.");
            }
        } catch (Exception e) {
            mostrarMensaje("Error al eliminar autorización: " + e.getMessage());
        }
    }

    /**
     * Carga todas las autorizaciones desde la base de datos.
     */
    private void cargarAutorizacionesDesdeBaseDeDatos() {
        try {
            listaAutorizaciones.clear();
            listaAutorizaciones.addAll(autorizacionesDAO.listarTodas());
        } catch (Exception e) {
            System.out.println("Error al cargar autorizaciones: " + e.getMessage());
        }
    }  
    /**
     * Actualiza el contador de autorizaciones ingresadas.
     */
    private void AutorizacionesIngresadas(){
        try {
            int total=autorizacionesDAO.AutorizacionesIngresadas();
            lblTotalAutorizaciones.setText(String.valueOf(total));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    } 
    /**
     * Actualiza el contador de autorizaciones vencidas.
     */
    private void AutorizacionesVencidas(){
        try {
            int total=autorizacionesDAO.AutorizacionesVencidas();
            lblVencidas.setText(String.valueOf(total));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }  
    /**
     * Actualiza el contador de autorizaciones recibidas.
     */
    private void AutorizacionesRecibidas(){
        try {
            int total=autorizacionesDAO.AutorizacionesRecibidas();
            lbIngresadas.setText(String.valueOf(total));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    } 
    /**
     * Actualiza el contador de autorizaciones pendientes.
     */
    private void AutorizacionesPendientes(){
        try {
            int total=autorizacionesDAO.AutorizacionesPendientes();
            lblPendientes.setText(String.valueOf(total));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    /**
     * Actualiza el estado de la autorización seleccionada a 'INGRESADA'.
     */
    @FXML
    private void ActualizarEstado() {

    String nuevoEstado = "INGRESADA"; // Estado que deseas asignar

    boolean actualizado=autorizacionesDAO.actualizarEstadoAutorizacion(idSeleccionado, nuevoEstado);
    if(idSeleccionado == 0){
        mostrarMensaje("Seleccione una autorización para actualizar su estado.");
        return;
    }
    DashboardAutorizaciones();
    cargarAutorizacionesDesdeBaseDeDatos();
}
}
