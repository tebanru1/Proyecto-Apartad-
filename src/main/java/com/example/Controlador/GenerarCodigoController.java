package com.example.Controlador;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import com.example.Modelo.CodigoGenerado;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;

/**
 * Controlador para el archivo generarcodigo.fxml
 * Maneja la generación de códigos PDF417 y la gestión de códigos generados
 */
public class GenerarCodigoController implements Initializable {
    
    // Campos de entrada
    @FXML
    private TextField txtnombre;
    
    @FXML
    private TextField txtcedula;
    
    @FXML
    private TextField txtciudad;
    
    @FXML
    private TextField txtpabellom;
    
    @FXML
    private DatePicker fechainicio;
    
    @FXML
    private DatePicker fechaterminacion;
    
    // Botones
    @FXML
    private Button btnGenerarCodigo;
    
    @FXML
    private Button btnEliminar;
    
    @FXML
    private Button btnDescargar;
    
    // Tabla y columnas
    @FXML
    private TableView<CodigoGenerado> tablacodigo;
    
    @FXML
    private TableColumn<CodigoGenerado, String> columnaNombre;
    
    @FXML
    private TableColumn<CodigoGenerado, String> columnaCedula;
    
    @FXML
    private TableColumn<CodigoGenerado, String> columnaCiudad;
    
    @FXML
    private TableColumn<CodigoGenerado, String> columnaPabellon;
    
    @FXML
    private TableColumn<CodigoGenerado, String> columnaFechaInicio;
    
    @FXML
    private TableColumn<CodigoGenerado, String> columnaFechaTerminacion;
    
    @FXML
    private TableColumn<CodigoGenerado, String> columnaFechaGeneracion;
    
    // ImageView para mostrar el código
    @FXML
    private ImageView imagencodigo;
    
    // Lista observable para los códigos generados
    private ObservableList<CodigoGenerado> listaCodigos;
    
    // Conexión a la base de datos
    private Conexion conexion;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Inicializar conexión
            conexion = new Conexion();
            
            ConfigurarTextField();
            // Configurar la tabla de códigos
            configurarTablaCodigos();
            
            // Configurar eventos de los botones
            configurarEventos();
            
            // Configurar evento de selección en la tabla
            configurarEventoTabla();
            
            // Establecer fechas por defecto
            fechainicio.setValue(LocalDate.now());
            fechaterminacion.setValue(LocalDate.now().plusDays(30));
            
            // Cargar códigos desde la base de datos
            cargarCodigosDesdeBD();
            
        } catch (Exception e) {
            mostrarMensaje("Error al inicializar el controlador: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Configura la tabla de códigos generados
     */
    private void configurarTablaCodigos() {
        // Configurar las columnas de la tabla
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        columnaCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        columnaPabellon.setCellValueFactory(new PropertyValueFactory<>("pabellon"));
        columnaFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        columnaFechaTerminacion.setCellValueFactory(new PropertyValueFactory<>("fechaTerminacion"));
        columnaFechaGeneracion.setCellValueFactory(new PropertyValueFactory<>("fechaGeneracion"));
        
        // Inicializar la lista observable
        listaCodigos = FXCollections.observableArrayList();
        
        // Asignar la lista a la tabla
        tablacodigo.setItems(listaCodigos);
    }
    
    /**
     * Configura los eventos de los botones
     */
    private void configurarEventos() {
        btnGenerarCodigo.setOnAction(e -> {
            generarCodigo();
        });
        
        btnEliminar.setOnAction(e -> {
            eliminarCodigo();
        });
        
        btnDescargar.setOnAction(e -> {
            descargarCodigo();
        });
    }
    
    /**
     * Configura el evento de selección en la tabla
     */
    private void configurarEventoTabla() {
        tablacodigo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Código seleccionado: " + newVal.getNombre() + " - " + newVal.getCedula());
                mostrarImagenCodigo(newVal);
            }
        });
    }
    
    /**
     * Genera un código PDF417 con la información ingresada
     */
    @FXML
    private void generarCodigo() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }
        
        try {
            // Crear objeto CodigoGenerado con los datos ingresados
            CodigoGenerado codigo = new CodigoGenerado(
                txtnombre.getText().trim().toUpperCase(),
                txtcedula.getText().trim(),
                txtciudad.getText().trim().toUpperCase(),
                txtpabellom.getText().trim(),
                fechainicio.getValue(),
                fechaterminacion.getValue(),
                ""
            );
            
            // Generar la imagen del código PDF417
            byte[] imagenCodigo = generarImagenCodigo(codigo);
            codigo.setImagenCodigo(imagenCodigo);
            
            // Generar ID único para el código
            String codigoId = "PDF417_" + System.currentTimeMillis() + "_" + codigo.getCedula();
            codigo.setCodigoPDF417(codigoId);
            
            // Guardar en la base de datos
            boolean guardado = guardarCodigoEnBD(codigo);
            
            if (guardado) {
                // Recargar la tabla
                cargarCodigosDesdeBD();
                
                // Mostrar la imagen generada
                mostrarImagenCodigo(codigo);
                
                // Limpiar campos
                limpiarCampos();
                
                // Mostrar mensaje de éxito
                mostrarMensaje("✅ Código PDF417 generado y guardado exitosamente", Alert.AlertType.INFORMATION);
            } else {
                mostrarMensaje("❌ Error al guardar el código en la base de datos", Alert.AlertType.ERROR);
            }
            
        } catch (Exception e) {
            mostrarMensaje("❌ Error al generar el código: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Elimina el código seleccionado de la base de datos
     */
    @FXML
    private void eliminarCodigo() {
        CodigoGenerado codigoSeleccionado = tablacodigo.getSelectionModel().getSelectedItem();
        
        if (codigoSeleccionado == null) {
            mostrarMensaje("⚠️ Seleccione un código para eliminar", Alert.AlertType.WARNING);
            return;
        }
        
        // Confirmar eliminación
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Eliminación");
        confirmAlert.setHeaderText("¿Está seguro de que desea eliminar este código?");
        confirmAlert.setContentText("Código: " + codigoSeleccionado.getNombre() + " - " + codigoSeleccionado.getCedula());
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                boolean eliminado = eliminarCodigoDeBD(codigoSeleccionado.getCedula());
                
                if (eliminado) {

                    cargarCodigosDesdeBD();
                    imagencodigo.setImage(null); // Limpiar imagen
                    mostrarMensaje("✅ Código eliminado exitosamente", Alert.AlertType.INFORMATION);
                } else {
                    mostrarMensaje("❌ Error al eliminar el código de la base de datos", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    /**
     * Descarga el código seleccionado como imagen PNG
     */
    @FXML
    private void descargarCodigo() {
        
        CodigoGenerado codigoSeleccionado = tablacodigo.getSelectionModel().getSelectedItem();
        
        if (codigoSeleccionado == null) {
            mostrarMensaje("⚠️ Seleccione un código para descargar", Alert.AlertType.WARNING);
            return;
        }
        
        System.out.println("Código seleccionado para descargar: " + codigoSeleccionado.getNombre() + " - " + codigoSeleccionado.getCedula());
        
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar código PDF417");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PNG", "*.png")
            );
            fileChooser.setInitialFileName("codigo_" + codigoSeleccionado.getCedula() + ".png");
            
            // Obtener la ventana padre
            javafx.stage.Window ventana = btnDescargar.getScene().getWindow();
            File archivo = fileChooser.showSaveDialog(ventana);
            
            if (archivo != null) {
                guardarImagenCodigo(codigoSeleccionado, archivo);
                mostrarMensaje("✅ Código descargado exitosamente en: " + archivo.getAbsolutePath(), 
                    Alert.AlertType.INFORMATION);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al descargar código: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("❌ Error al descargar el código: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Valida que todos los campos obligatorios estén llenos
     */
    private boolean validarCampos() {
        if (txtnombre.getText().trim().isEmpty()) {
            mostrarMensaje("⚠️ El nombre es obligatorio", Alert.AlertType.WARNING);
            txtnombre.requestFocus();
            return false;
        }
        
        if (txtcedula.getText().trim().isEmpty()) {
            mostrarMensaje("⚠️ La cédula es obligatoria", Alert.AlertType.WARNING);
            txtcedula.requestFocus();
            return false;
        }
        
        if (txtciudad.getText().trim().isEmpty()) {
            mostrarMensaje("⚠️ La ciudad es obligatoria", Alert.AlertType.WARNING);
            txtciudad.requestFocus();
            return false;
        }
        
        if (txtpabellom.getText().trim().isEmpty()) {
            mostrarMensaje("⚠️ El pabellón es obligatorio", Alert.AlertType.WARNING);
            txtpabellom.requestFocus();
            return false;
        }
        
        if (fechainicio.getValue() == null) {
            mostrarMensaje("⚠️ La fecha de inicio es obligatoria", Alert.AlertType.WARNING);
            fechainicio.requestFocus();
            return false;
        }
        
        if (fechaterminacion.getValue() == null) {
            mostrarMensaje("⚠️ La fecha de terminación es obligatoria", Alert.AlertType.WARNING);
            fechaterminacion.requestFocus();
            return false;
        }
        
        if (fechaterminacion.getValue().isBefore(fechainicio.getValue())) {
            mostrarMensaje("⚠️ La fecha de terminación debe ser posterior a la fecha de inicio", 
                Alert.AlertType.WARNING);
            fechaterminacion.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Genera la imagen del código PDF417
     */
    private byte[] generarImagenCodigo(CodigoGenerado codigo) {
        try {
            String texto = codigo.generarTextoParaCodigo();
            
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, 2);
            
            PDF417Writer writer = new PDF417Writer();
            BitMatrix bitMatrix = writer.encode(texto, BarcodeFormat.PDF_417, 400, 200, hints);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            return new byte[0];
        }
    }
    public void ConfigurarTextField() {
        // Limitar el campo de cédula a solo números y máximo 10 dígitos
        txtcedula.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,10}")) {
                txtcedula.setText(oldValue);
            }
        });
    }
    /**
     * Muestra la imagen del código en el ImageView
     */
    private void mostrarImagenCodigo(CodigoGenerado codigo) {
        try {
            byte[] imagenCodigo = codigo.getImagenCodigo();
            
            if (imagenCodigo != null && imagenCodigo.length > 0) {
                ByteArrayInputStream bais = new ByteArrayInputStream(imagenCodigo);
                Image imagen = new Image(bais);
                imagencodigo.setImage(imagen);
            }
        } catch (Exception e) {
            // Error al mostrar imagen
        }
    }
    
    /**
     * Guarda la imagen del código en un archivo
     */
    private void guardarImagenCodigo(CodigoGenerado codigo, File archivo) throws IOException {
        byte[] imagenCodigo = codigo.getImagenCodigo();
        
        if (imagenCodigo != null && imagenCodigo.length > 0) {
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                fos.write(imagenCodigo);
            }
        } else {
            throw new IOException("No hay imagen para guardar");
        }
    }
    
    /**
     * Limpia todos los campos de entrada
     */
    private void limpiarCampos() {
        txtnombre.clear();
        txtcedula.clear();
        txtciudad.clear();
        txtpabellom.clear();
        fechainicio.setValue(LocalDate.now());
        fechaterminacion.setValue(LocalDate.now().plusDays(30));
    }
    
    /**
     * Muestra un mensaje en la interfaz
     */
    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Generador de Códigos PDF417");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Guarda un código en la base de datos
     */
    private boolean guardarCodigoEnBD(CodigoGenerado codigo) {
        String sql = "INSERT INTO codigo(nombre, cedula, ciudad, pabellon, fechaInicio, fechaTerminacion, codigoPDF417, fechaGeneracion) VALUES (?,?,?,?,?,?,?,?)";
        
        try (Connection con = conexion.conectar()) {
            if (con == null) {
                return false;
            }
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, codigo.getNombre());
                ps.setString(2, codigo.getCedula());
                ps.setString(3, codigo.getCiudad());
                ps.setString(4, codigo.getPabellon());
                ps.setDate(5, Date.valueOf(codigo.getFechaInicio()));
                ps.setDate(6, Date.valueOf(codigo.getFechaTerminacion()));
                ps.setBytes(7, codigo.getImagenCodigo());
                ps.setDate(8, Date.valueOf(LocalDate.now()));
                
                int filasAfectadas = ps.executeUpdate();
                System.out.println("Filas afectadas al guardar: " + filasAfectadas);
                return filasAfectadas > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error SQL al guardar código: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Elimina un código de la base de datos
     */
    private boolean eliminarCodigoDeBD(String cedula) {
        String sql = "DELETE FROM codigo WHERE cedula = ?";
        
        try (Connection con = conexion.conectar()) {
            if (con == null) {
                return false;
            }
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, cedula);
                int filasAfectadas = ps.executeUpdate();
                System.out.println("Filas afectadas al eliminar: " + filasAfectadas);
                return filasAfectadas > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error SQL al eliminar código: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Carga todos los códigos desde la base de datos
     */
    private void cargarCodigosDesdeBD() {
        
        String sql = "SELECT * FROM codigo ORDER BY fechaGeneracion DESC";
        
        try (Connection con = conexion.conectar()) {
            if (con == null) {
 
                return;
            }
            
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                listaCodigos.clear();
                int contador = 0;
                
                while (rs.next()) {
                    CodigoGenerado codigo = new CodigoGenerado();
                    codigo.setNombre(rs.getString("nombre"));
                    codigo.setCedula(rs.getString("cedula"));
                    codigo.setCiudad(rs.getString("ciudad"));
                    codigo.setPabellon(rs.getString("pabellon"));
                    codigo.setFechaInicio(rs.getDate("fechaInicio").toLocalDate());
                    codigo.setFechaTerminacion(rs.getDate("fechaTerminacion").toLocalDate());
                    codigo.setCodigoPDF417("PDF417_" + rs.getString("cedula"));
                    codigo.setFechaGeneracion(rs.getDate("fechaGeneracion").toLocalDate().toString());
                    codigo.setImagenCodigo(rs.getBytes("codigoPDF417"));
                    
                    listaCodigos.add(codigo);
                    contador++;
                }
                
            }
            
        } catch (SQLException e) {
            System.err.println("Error SQL al cargar códigos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene la lista actual de códigos
     */
    public ObservableList<CodigoGenerado> getListaCodigos() {
        return listaCodigos;
    }
}
