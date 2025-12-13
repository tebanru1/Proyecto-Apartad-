package com.example.Controlador;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.example.DAO.IngresosAdministrativosDAO;
import com.example.Modelo.ingresoAdministrativos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import java.awt.Desktop;




public class AdministrativosController implements Initializable {
    @FXML private TableView<ingresoAdministrativos> IngresosAdmin;
    @FXML private TableColumn<ingresoAdministrativos, String> cedula, nombre, apellido, cargo, fecha, horaE, horaS;
    @FXML private Label TFcedula, TFnombre, TFapellido, TFcargo, TFfecha, TFhoraE, TFhoraS;
    @FXML private ImageView huellaE, huellaS, FotoPerfil;
    @FXML private TextField Tnombre, TFdocumento;
    @FXML private Button btnReporte,btnlimpiar,btnlimpiarDatos;
    @FXML private DatePicker filtroFecha;

    private ObservableList<ingresoAdministrativos> listaIngresoAdministrativos;
    
    IngresosAdministrativosDAO IngresosAdministrativosDAO = new IngresosAdministrativosDAO();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablaIngresos();
        cargarIngresos();
        configurarSeleccionTabla();
        ConfigurarEventos();
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
        filtroFecha.setOnAction(e -> cargarIngresos());
        TFdocumento.setOnKeyReleased(e->cargarIngresos());
        Tnombre.setOnKeyReleased(e->cargarIngresos());
    }

 private void cargarIngresos() {
    try {
        LocalDate fecha = filtroFecha.getValue();
        String doc = TFdocumento.getText().trim();
        String nom = Tnombre.getText().trim();

        List<ingresoAdministrativos> filtrados = IngresosAdministrativosDAO.FiltrarAvanzado(fecha, doc, nom);
        listaIngresoAdministrativos.setAll(filtrados);

    } catch (Exception e) {
        System.out.println("Error al cargar ingresos: " + e.getMessage());
    }
}

  private void ConfigurarEventos(){
    btnReporte.setOnAction(event -> generarReporteAdministrativos());
    btnlimpiar.setOnAction(event -> limpiarFiltros());
    btnlimpiarDatos.setOnAction(event -> limpiarDatosSeleccionados());
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
public void generarReporteAdministrativos() {
    try {
        // Obtener lista desde DAO
        IngresosAdministrativosDAO dao = new IngresosAdministrativosDAO();
        List<ingresoAdministrativos> lista = dao.listarTodos();

        // Crear archivo temporal
        File tempFile = File.createTempFile("reporte_administrativos_", ".pdf");
        tempFile.deleteOnExit(); // Se elimina automáticamente al cerrar el sistema

        // Generar PDF directamente en el archivo temporal
        ReporteAdministrativosPDF generador = new ReporteAdministrativosPDF();
        generador.generarPDF(lista, tempFile);  // <--- CORRECCIÓN: se envía FILE, NO ruta

        // Abrir automáticamente el PDF
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(tempFile);
        } else {
            System.out.println("El entorno no permite abrir archivos automáticamente.");
        }

        System.out.println("PDF temporal generado: " + tempFile.getAbsolutePath());

    } catch (Exception e) {
        System.out.println("Error al generar PDF temporal: " + e.getMessage());
        e.printStackTrace();
    }
}

private void limpiarFiltros() {
    TFdocumento.clear();
    Tnombre.clear();
    filtroFecha.setValue(null);
    cargarIngresos();
}
private void limpiarDatosSeleccionados() {
    TFcedula.setText("");
    TFnombre.setText("");
    TFapellido.setText("");
    TFcargo.setText("");
    TFfecha.setText("");
    TFhoraE.setText("--:--:--");
    TFhoraS.setText("--:--:--");
    Image ImagenporDefecto = new Image(getClass().getResourceAsStream("/com/example/usuario.png"));
    FotoPerfil.setImage(ImagenporDefecto);
}
}