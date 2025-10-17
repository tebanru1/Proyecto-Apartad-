
package com.example.Controlador;

import com.example.Modelo.ArchivoPDF;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class InicioController implements Initializable {

    // FXML UI Components
    @FXML private Button btnIngreso;
    @FXML private Button btnSalida;
    @FXML private TextField TFCedula;
    @FXML private TableView<ArchivoPDF> table_view;
    @FXML private TableColumn<ArchivoPDF, String> Descripcion;
    @FXML private TableColumn<ArchivoPDF, String> columnaFecha;

    // Data
    private ObservableList<ArchivoPDF> listaPDFs;
    private IngresoController ingresoController;

    // Inicialización
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ingresoController = new IngresoController();
        configurarTablaPDFs();
        cargarPDFs();
        configurarEventos();
    }

    // Configuración de la tabla de PDFs
    private void configurarTablaPDFs() {
        Descripcion.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fechaModificacion"));
        listaPDFs = FXCollections.observableArrayList();
        table_view.setItems(listaPDFs);
        TFCedula.textProperty().addListener((observable, oldValue, newValue) -> validarCedulaEnTiempoReal());
    }

    // Configuración de eventos
    private void configurarEventos() {
        table_view.setOnMouseClicked(this::manejarClicTabla);
        btnIngreso.setOnAction(event -> manejarIngreso());
        btnSalida.setOnAction(event -> manejarSalida());
    }

    // Maneja el evento de ingreso
    @FXML
    private void manejarIngreso() {
        String cedula = TFCedula.getText().trim();
        if (cedula.isEmpty()) {
            mostrarMensaje("Por favor ingrese su número de cédula", Alert.AlertType.WARNING);
            TFCedula.requestFocus();
            return;
        }
        if (!ingresoController.esCedulaValida(cedula)) {
            mostrarMensaje("Cédula inválida. Debe contener entre 7 y 10 dígitos numéricos.", Alert.AlertType.ERROR);
            TFCedula.requestFocus();
            return;
        }
        boolean resultado = ingresoController.registrarIngreso(cedula);
        if (resultado) {
            mostrarMensaje("¡INGRESO registrado exitosamente!", Alert.AlertType.INFORMATION);
            limpiarCampo();
        } else {
            mostrarMensaje("Error al registrar el ingreso. Verifique la conexión a la base de datos.", Alert.AlertType.ERROR);
        }
    }

    // Maneja el evento de salida
    @FXML
    private void manejarSalida() {
        String cedula = TFCedula.getText().trim();
        if (cedula.isEmpty()) {
            mostrarMensaje("Por favor ingrese su número de cédula", Alert.AlertType.WARNING);
            TFCedula.requestFocus();
            return;
        }
        if (!ingresoController.esCedulaValida(cedula)) {
            mostrarMensaje("Cédula inválida. Debe contener entre 7 y 10 dígitos numéricos.", Alert.AlertType.ERROR);
            TFCedula.requestFocus();
            return;
        }
        boolean resultado = ingresoController.registrarIngreso(cedula);
        if (resultado) {
            mostrarMensaje("¡SALIDA registrada exitosamente!", Alert.AlertType.INFORMATION);
            limpiarCampo();
        } else {
            mostrarMensaje("Error al registrar la salida. Verifique la conexión a la base de datos.", Alert.AlertType.ERROR);
        }
    }

    // Acceso al controlador de ingreso
    public IngresoController getIngresoController() {
        return ingresoController;
    }

    // Cargar PDFs desde la carpeta de documentos
    private void cargarPDFs() {
        try {
            String rutaDocumentos = System.getProperty("user.home") + File.separator + "Documents";
            File carpetaDocumentos = new File(rutaDocumentos);
            if (!carpetaDocumentos.exists()) {
                mostrarMensaje("No se encontró la carpeta de documentos", Alert.AlertType.WARNING);
                return;
            }
            listaPDFs.clear();
            File[] archivos = carpetaDocumentos.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (archivos != null && archivos.length > 0) {
                for (File archivo : archivos) {
                    ArchivoPDF pdf = new ArchivoPDF(archivo);
                    listaPDFs.add(pdf);
                }
                listaPDFs.sort((a, b) -> b.getArchivo().lastModified() > a.getArchivo().lastModified() ? 1 : -1);
                System.out.println("Se cargaron " + listaPDFs.size() + " archivos PDF");
            } else {
                mostrarMensaje("No se encontraron archivos PDF en la carpeta de documentos", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            mostrarMensaje("Error al cargar los PDFs: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Maneja el evento de doble clic en la tabla
    private void manejarClicTabla(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ArchivoPDF pdfSeleccionado = table_view.getSelectionModel().getSelectedItem();
            if (pdfSeleccionado != null) {
                abrirPDF(pdfSeleccionado);
            }
        }
    }

    // Abre un archivo PDF con el programa predeterminado del sistema
    private void abrirPDF(ArchivoPDF pdf) {
        try {
            if (pdf.getArchivo().exists()) {
                java.awt.Desktop.getDesktop().open(pdf.getArchivo());
                System.out.println("Abriendo PDF: " + pdf.getNombre());
            } else {
                mostrarMensaje("El archivo no existe: " + pdf.getNombre(), Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarMensaje("Error al abrir el PDF: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Recargar la lista de PDFs
    public void recargarPDFs() {
        cargarPDFs();
    }

    // Mostrar mensajes en la interfaz
    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Obtener la lista de PDFs
    public ObservableList<ArchivoPDF> getListaPDFs() {
        return listaPDFs;
    }

    // Limpiar el campo de cédula
    private void limpiarCampo() {
        TFCedula.clear();
        TFCedula.setStyle("-fx-border-color: transparent;");
        TFCedula.requestFocus();
    }

    // Validar la cédula en tiempo real
    private void validarCedulaEnTiempoReal() {
        String cedula = TFCedula.getText().trim();
        if (cedula.isEmpty()) {
            return;
        }
        if (ingresoController.esCedulaValida(cedula)) {
            TFCedula.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        } else {
            TFCedula.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }
    }
}

