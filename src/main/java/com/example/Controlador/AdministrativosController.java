package com.example.Controlador;

import com.example.DAO.IngresosAdministrativosDAO;
import com.example.Modelo.ingresoAdministrativos;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AdministrativosController implements Initializable {

    @FXML private TableView<ingresoAdministrativos> IngresosAdmin;
    @FXML private TableColumn<ingresoAdministrativos, String> cedula, nombre, apellido, cargo, fecha, horaE, horaS;

    @FXML private Label TFcedula, TFnombre, TFapellido, TFcargo, TFfecha, TFhoraE, TFhoraS;
    @FXML private ImageView FotoPerfil;

    @FXML private TextField Tnombre, TFdocumento;
    @FXML private DatePicker filtroFecha;

    @FXML private Button btnReporte, btnlimpiar, btnlimpiarDatos;

    private ObservableList<ingresoAdministrativos> listaIngresoAdministrativos;
    private final IngresosAdministrativosDAO dao = new IngresosAdministrativosDAO();

    // ======================================================
    // INITIALIZE
    // ======================================================
    /**
     * Inicializa el controlador y configura la vista al cargar la pantalla.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        configurarEventos();
        cargarIngresos();
        configurarSeleccionTabla();
        configurarTextField();
    }

    // ======================================================
    // TABLA
    // ======================================================
    /**
     * Configura las columnas y eventos de la tabla de ingresos administrativos.
     */
    private void configurarTabla() {

        cedula.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCedula()));
        nombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));
        apellido.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getApellido()));
        cargo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCargo()));
        fecha.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getFecha())));
        horaE.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getHoraIngreso())));
        horaS.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getHoraSalida() != null ? d.getValue().getHoraSalida().toString() : "PENDIENTE"
        ));

        listaIngresoAdministrativos = FXCollections.observableArrayList();
        IngresosAdmin.setItems(listaIngresoAdministrativos);

        filtroFecha.setOnAction(e -> cargarIngresos());
        TFdocumento.setOnKeyReleased(e -> cargarIngresos());
        Tnombre.setOnKeyReleased(e -> cargarIngresos());
    }

    // ======================================================
    // CARGA CON FILTRO EN PANTALLA
    // ======================================================
    /**
     * Carga los ingresos administrativos aplicando los filtros de búsqueda.
     */
    private void cargarIngresos() {
        try {
            LocalDate fecha = filtroFecha.getValue();
            String doc = TFdocumento.getText().trim();
            String nom = Tnombre.getText().trim();

            List<ingresoAdministrativos> lista = dao.FiltrarAvanzado(fecha, doc, nom);
            listaIngresoAdministrativos.setAll(lista);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ======================================================
    // EVENTOS
    // ======================================================
    /**
     * Configura los eventos de los botones principales de la vista.
     */
    private void configurarEventos() {
        btnReporte.setOnAction(e -> preguntarCriterioReporte());
        btnlimpiar.setOnAction(e -> limpiarFiltros());
        btnlimpiarDatos.setOnAction(e -> limpiarDatosSeleccionados());
    }

    // ======================================================
    // PREGUNTAR CRITERIO
    // ======================================================
        /**
         * Muestra un diálogo para seleccionar el criterio del reporte PDF.
         */
        private void preguntarCriterioReporte() {

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Generar Reporte");
    dialog.setHeaderText("Seleccione el criterio del reporte");

    ComboBox<String> cbFiltro = new ComboBox<>();
    cbFiltro.getItems().addAll("TODOS", "CÉDULA", "NOMBRE", "FECHA");
    cbFiltro.setValue("TODOS");

    TextField txtValor = new TextField();
    txtValor.setPromptText("Nombre");
    txtValor.setVisible(false);
    txtValor.setManaged(false);
 
    TextField txtcedula = new TextField();
    txtcedula.setPromptText("Cédula");
    txtcedula.setVisible(false);
    txtcedula.setManaged(false);

    DatePicker dpFecha = new DatePicker();
    dpFecha.setVisible(false);
    dpFecha.setManaged(false);

    cbFiltro.setOnAction(e -> {
        txtValor.setVisible(false);
        txtValor.setManaged(false);
        dpFecha.setVisible(false);
        dpFecha.setManaged(false);  
        txtcedula.setVisible(false);
        txtcedula.setManaged(false);
        

      switch (cbFiltro.getValue()) {
        case "NOMBRE" -> {
            txtValor.setVisible(true);
            txtValor.setManaged(true);
            
        }
        case "FECHA" -> {
            dpFecha.setVisible(true);
            dpFecha.setManaged(true);   
          
        }
    case "CÉDULA" -> {
            txtcedula.setVisible(true);
            txtcedula.setManaged(true);
       
    }}
        dialog.getDialogPane().getScene().getWindow().sizeToScene();
    });

    VBox contenido = new VBox(10, cbFiltro, txtValor,txtcedula, dpFecha);
    dialog.getDialogPane().setContent(contenido);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    dialog.showAndWait().ifPresent(btn -> {
        if (btn == ButtonType.OK) {

            switch (cbFiltro.getValue()) {

                case "CÉDULA":
                    if (txtcedula.getText().isEmpty()) {
                        mostrarAlerta("Debe ingresar la cédula");
                        return;
                    }
                    generarReporteAdministrativos(TipoFiltro.CEDULA, txtcedula.getText());
                    break;

                case "NOMBRE":
                    if (txtValor.getText().isEmpty()) {
                        mostrarAlerta("Debe ingresar el nombre");
                        return;
                    }
                    generarReporteAdministrativos(TipoFiltro.NOMBRE, txtValor.getText());
                    break;

                case "FECHA":
                    if (dpFecha.getValue() == null) {
                        mostrarAlerta("Debe seleccionar una fecha");
                        return;
                    }
                    generarReporteAdministrativos(
                            TipoFiltro.FECHA,
                            dpFecha.getValue().toString()
                    );
                    break;

                default:
                    generarReporteAdministrativos(TipoFiltro.TODOS, "");
            }
        }
    });
}


    // ======================================================
    // GENERAR PDF
    // ======================================================
    /**
     * Genera el reporte PDF de administrativos según el filtro seleccionado.
     * @param tipoFiltro Tipo de filtro aplicado
     * @param valorFiltro Valor del filtro
     */
    private void generarReporteAdministrativos(TipoFiltro tipoFiltro, String valorFiltro) {

        try {
            List<ingresoAdministrativos> lista;

            switch (tipoFiltro) {
                case CEDULA -> lista = dao.listarPorCedula(valorFiltro);
                case NOMBRE -> lista = dao.listarPorNombre(valorFiltro);
                case FECHA -> lista = dao.listarPorFecha(LocalDate.parse(valorFiltro));
                default -> lista = dao.listarTodos();
            }

            File tempFile = File.createTempFile("reporte_administrativos_", ".pdf");
            tempFile.deleteOnExit();

            ReporteAdministrativosPDF pdf = new ReporteAdministrativosPDF();
            pdf.generarPDF(lista, tempFile, tipoFiltro, valorFiltro);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ======================================================
    // SELECCIÓN TABLA
    // ======================================================
    /**
     * Configura el evento de selección de la tabla para mostrar detalles del ingreso.
     */
    private void configurarSeleccionTabla(){
    IngresosAdmin.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        if (newSelection != null) {
            TFcedula.setText(newSelection.getCedula());
            TFnombre.setText(newSelection.getNombre());
            TFapellido.setText(newSelection.getApellido());
            TFcargo.setText(newSelection.getCargo());
            TFfecha.setText(String.valueOf(newSelection.getFecha()));
            TFhoraE.setText(String.valueOf(newSelection.getHoraIngreso()));
            TFhoraS.setText(newSelection.getHoraSalida() != null ? String.valueOf(newSelection.getHoraSalida()) : "PENDIENTE");

           byte[] fotoBytes = newSelection.getFotoFuncionario();

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

    // ======================================================
    // UTILIDADES
    // ======================================================
    /**
     * Muestra una alerta de advertencia con el mensaje proporcionado.
     * @param msg Mensaje a mostrar
     */
    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validación");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Limpia los filtros de búsqueda y recarga la tabla de ingresos.
     */
    private void limpiarFiltros() {
        TFdocumento.clear();
        Tnombre.clear();
        filtroFecha.setValue(null);
        cargarIngresos();
    }

    /**
     * Limpia los datos mostrados de la selección actual en la vista de detalles.
     */
    private void limpiarDatosSeleccionados() {
        TFcedula.setText("");
        TFnombre.setText("");
        TFapellido.setText("");
        TFcargo.setText("");
        TFfecha.setText("");
        TFhoraE.setText("--:--");
        TFhoraS.setText("--:--");
        FotoPerfil.setImage(new Image(getClass().getResourceAsStream("/com/example/usuario.png")));
    }

    /**
     * Configura los campos de texto para validaciones y formato.
     */
    private void configurarTextField() {
        Tnombre.textProperty().addListener((o, a, b) -> Tnombre.setText(b.toUpperCase()));
        TFdocumento.textProperty().addListener((o, a, b) -> TFdocumento.setText(b.replaceAll("[^\\d]", "")));
    }
}
