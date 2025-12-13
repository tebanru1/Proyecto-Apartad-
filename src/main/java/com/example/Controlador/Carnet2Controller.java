package com.example.Controlador;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import com.example.DAO.CarnetDAO;
import com.example.Modelo.Carnet;
import com.example.Modelo.Usuario;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.sql.Date;


public class Carnet2Controller implements Initializable, UsuarioReceptor {

    @FXML private TextField TFnombre, TFapellido, TFdocumento, TFentidad, TFcargo,Fdocumento,Fnombre,Fapellido,Fentidad;
    @FXML private Label txtNombres, txtApellidos, txtDocumento, txtEntidad, txtCargo, txtRh, txtFechaVigencia;
    @FXML private DatePicker DPvigencia,FDPfechaGeneracion,FDPvigencia;
    @FXML private ComboBox<String> CBrh;
    @FXML private ImageView imagencodigo;
    @FXML private Button btnCrearCodigo, btnExportar, btnEliminarC,btnLimpiarFiltros,btnLimpiarDatosCarnet,btnNuevoCarnet,btnEditarCarnet,btnActualizarCarnet;
    @FXML TableView<Carnet> tablaCarnet;
    @FXML TableColumn<Carnet,String> documento,nombres,apellidos,entidad,rh, fecha, vigencia, usuariot;
    @FXML AnchorPane ContenedorCarnetFrente, ContenedorCarnetPosterior;
    private Usuario usuario;
    private ObservableList<Carnet> listaCarnet;
    private Carnet carnetSeleccionado;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        vincularCampos();
        cargarRh();
        configurarBotones();
        configurarTabla();
        cargarCarnetDB();
        ConfigurarEventosTabla();
        ConfigurarTextField();
        agregarListenersParaNuevaEntrada();
        
        // NO usar usuario aquí, aún no ha sido pasado
    }

    private void vincularCampos() {
        txtNombres.textProperty().bind(TFnombre.textProperty());
        txtApellidos.textProperty().bind(TFapellido.textProperty());
        txtDocumento.textProperty().bind(TFdocumento.textProperty());
        txtEntidad.textProperty().bind(TFentidad.textProperty());
        txtCargo.textProperty().bind(TFcargo.textProperty());

        DPvigencia.valueProperty().addListener((obs, oldDate, fecha) -> {
            if (fecha != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                txtFechaVigencia.setText(sdf.format(Date.valueOf(fecha)));
            } else {
                txtFechaVigencia.setText("");
            }
        });

        CBrh.valueProperty().addListener((obs, oldRh, rh) -> {
            txtRh.setText(rh != null ? rh : "");
        });
    }

    private void cargarRh() {
        CBrh.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        CBrh.setPromptText("Seleccione RH");
    }

    private void configurarBotones() {
        btnCrearCodigo.setOnAction(event -> generarCodigo());
        btnLimpiarFiltros.setOnAction(e->limpiarFiltros());
        btnNuevoCarnet.setOnAction(e->limpiarTextField());
    }
private void configurarTabla() {
        documento.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumento()));
        nombres.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        apellidos.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApellido()));
        entidad.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEntidad()));
        rh.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRh()));
        fecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaCreacion().toString()));
        vigencia.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaVigencia().toString()));
        usuariot.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        listaCarnet=FXCollections.observableArrayList();
        tablaCarnet.setItems(listaCarnet);
        TextField[] campos = {Fdocumento, Fnombre, Fapellido, Fentidad};
        for (TextField tf : campos) {
            tf.setOnKeyReleased(e -> CargarFiltro());
            }
        FDPfechaGeneracion.setOnAction(e->CargarFiltro());
        FDPvigencia.setOnAction(e->CargarFiltro());
    }
    private void ConfigurarEventosTabla() {
        tablaCarnet.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                carnetSeleccionado = newSelection;
                mostrarDetallesCarnet(newSelection);

            }
        });
    }
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
    public void ConfigurarTextField() {
    TextFieldMayusculas(Fapellido);
    TextFieldMayusculas(Fnombre);
    TextFieldMayusculas(Fentidad);
    TextFieldMayusculas(TFnombre);
    TextFieldMayusculas(TFapellido);
    TextFieldMayusculas(TFentidad);
    TextFieldMayusculas(TFcargo);
    Fdocumento.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("\\d*")) {
            Fdocumento.setText(newValue.replaceAll("[^\\d]", ""));
        }
        if (Fdocumento.getText().length() > 10) {
            String s = Fdocumento.getText().substring(0, 10);
            Fdocumento.setText(s);
        }
    }); 
    TFdocumento.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("\\d*")) {
            TFdocumento.setText(newValue.replaceAll("[^\\d]", ""));
        }
        if (TFdocumento.getText().length() > 10) {
            String s = TFdocumento.getText().substring(0, 10);
            TFdocumento.setText(s);
        }
    });
}
    private void limpiarTextField() {
        TFnombre.setText("");
        TFapellido.setText("");
        TFdocumento.setText("");
        TFentidad.setText("");
        TFcargo.setText("");
        CBrh.setValue(null);;
        DPvigencia.setValue(null);
        imagencodigo.setImage(null);
    }
    @FXML
    private void limpiarCarnet(){
        txtNombres.textProperty().unbind();
        txtApellidos.textProperty().unbind();
        txtDocumento.textProperty().unbind();
        txtEntidad.textProperty().unbind();
        txtCargo.textProperty().unbind();

        txtNombres.setText("");
        txtApellidos.setText("");
        txtDocumento.setText("");
        txtEntidad.setText("");
        txtCargo.setText("");
        txtRh.setText("");
        txtFechaVigencia.setText("");
        imagencodigo.setImage(null);
        carnetLimpiado = false;
        vincularCampos();
    }
    private void mostrarDetallesCarnet(Carnet carnet) {
        carnetLimpiado = false;
        txtNombres.textProperty().unbind();
        txtApellidos.textProperty().unbind();
        txtDocumento.textProperty().unbind();
        txtEntidad.textProperty().unbind();
        txtCargo.textProperty().unbind();

        txtNombres.setText(carnet.getNombre());
        txtApellidos.setText(carnet.getApellido());
        txtDocumento.setText(carnet.getDocumento());
        txtEntidad.setText(carnet.getEntidad());
        txtCargo.setText(carnet.getCargo());
        txtRh.setText(carnet.getRh());
        txtFechaVigencia.setText(carnet.getFechaVigencia().toString());
        mostrarImagenCodigo(carnet);
    }
private boolean validarCarnetLimpio() {
    return !txtNombres.getText().trim().isEmpty()
        || !txtApellidos.getText().trim().isEmpty()
        || !txtEntidad.getText().trim().isEmpty()
        || !txtDocumento.getText().trim().isEmpty()
        || !txtCargo.getText().trim().isEmpty()
        || !txtFechaVigencia.getText().trim().isEmpty()
        || !txtRh.getText().trim().isEmpty();
}

private boolean carnetLimpiado = false; // para evitar limpiar varias veces

private void agregarListenersParaNuevaEntrada() {
    TextField[] campos = {TFnombre, TFapellido, TFdocumento, TFentidad, TFcargo};

    for (TextField tf : campos) {
        tf.textProperty().addListener((obs, oldText, newText) -> {
            // Si aún no hemos limpiado y hay info previa en labels
            if (!carnetLimpiado && validarCarnetLimpio()) {
                limpiarCarnet();
                carnetLimpiado = true; // solo limpiar una vez por nueva edición
            }
        });
    }

    DPvigencia.valueProperty().addListener((obs, oldDate, newDate) -> {
        if (!carnetLimpiado && validarCarnetLimpio()) {
            limpiarCarnet();
            carnetLimpiado = true;
        }
    });

    CBrh.valueProperty().addListener((obs, oldRh, rh) -> {
        if (!carnetLimpiado && validarCarnetLimpio()) {
            limpiarCarnet();
            carnetLimpiado = true;
        }
    });
}

    private void generarCodigo() {
        CarnetDAO CarnetDAO = new CarnetDAO();
        
        try {
            Carnet carnet = new Carnet(
                TFnombre.getText().toUpperCase(),
                TFapellido.getText(),
                TFdocumento.getText(),
                TFentidad.getText(),
                TFcargo.getText(),
                txtRh.getText(),
                DPvigencia.getValue(),
                LocalDate.now(),
                usuario.getUsuario(),
                ""
            );

            // Generar imagen del código PDF417
            byte[] imagenCodigo = generarImagenCodigo(carnet);
            carnet.setCodigo(imagenCodigo);

            // Generar ID único para el código
            String codigoId = "PDF417_" + System.currentTimeMillis() + "_" + carnet.getDocumento();
            carnet.setCodigoPDF417(codigoId);

            // Mostrar la imagen en ImageView
            mostrarImagenCodigo(carnet);

            // Guardar en BD incluyendo el usuario actual
            if (usuario != null) {
                carnet.setUsername(usuario.getUsuario());
            }
            CarnetDAO.guardarCodigoEnBD(carnet);
            
            cargarCarnetDB();
        

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarImagenCodigo(Carnet carnet) {
        try {
            byte[] imagenCodigo = carnet.getCodigo();
            if (imagenCodigo != null && imagenCodigo.length > 0) {
                ByteArrayInputStream bais = new ByteArrayInputStream(imagenCodigo);
                Image imagen = new Image(bais);

                imagencodigo.setImage(imagen);
                imagencodigo.setPreserveRatio(false);
                imagencodigo.setFitWidth(imagencodigo.getFitWidth());
                imagencodigo.setFitHeight(imagencodigo.getFitHeight());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BitMatrix recortarBitMatrix(BitMatrix matrix) {
        int[] recorte = matrix.getEnclosingRectangle();
        int width = recorte[2];
        int height = recorte[3];
        BitMatrix recortada = new BitMatrix(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x + recorte[0], y + recorte[1])) {
                    recortada.set(x, y);
                }
            }
        }
        return recortada;
    }

    private byte[] generarImagenCodigo(Carnet carnet) {
        try {
            String texto = carnet.generarTextoParaCodigo();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, 2);

            PDF417Writer writer = new PDF417Writer();
            BitMatrix bitMatrix = writer.encode(texto, BarcodeFormat.PDF_417, 400, 200, hints);

            BitMatrix recortada = recortarBitMatrix(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(recortada, "PNG", baos);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    
private void cargarCarnetDB() {
        CarnetDAO carnetDAO = new CarnetDAO();
    try {
            listaCarnet.clear();
            listaCarnet.addAll(carnetDAO.listarTodas());
        } catch (Exception e) {
            System.out.println("Error al cargar autorizaciones: " + e.getMessage());
        }
    } 
    @FXML
public void exportarCarnet() {
    // Crear selector de archivo
    FileChooser selectorArchivo = new FileChooser();
    selectorArchivo.setTitle("Guardar Carnet");
    selectorArchivo.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen PNG", "*.png"));
    selectorArchivo.setInitialFileName("carnet_" + txtDocumento.getText() + ".png");

    Stage ventana = (Stage) btnExportar.getScene().getWindow();
    File archivo = selectorArchivo.showSaveDialog(ventana);

    if (archivo != null) {
        try {
            // 1. Tomar captura del frente y reverso del carnet
            WritableImage imagenFrente = ContenedorCarnetFrente.snapshot(new SnapshotParameters(), null);
            WritableImage imagenReverso = ContenedorCarnetPosterior.snapshot(new SnapshotParameters(), null);

            // 2. Obtener dimensiones
            int anchoFrente = (int) ContenedorCarnetFrente.getWidth();
            int altoFrente = (int) ContenedorCarnetFrente.getHeight();
            int anchoReverso = (int) ContenedorCarnetPosterior.getWidth();
            int altoReverso = (int) ContenedorCarnetPosterior.getHeight();

            // Altura máxima para la imagen combinada
            int altoCombinado = Math.max(altoFrente, altoReverso);
            int anchoCombinado = anchoFrente + anchoReverso;

            // 3. Crear imagen combinada
            WritableImage imagenCombinada = new WritableImage(anchoCombinado, altoCombinado);
            PixelWriter escritorPixeles = imagenCombinada.getPixelWriter();

            // Lectores de píxeles para copiar las caras
            var lectorFrente = imagenFrente.getPixelReader();
            var lectorReverso = imagenReverso.getPixelReader();

            // 4. Copiar el frente
            for (int y = 0; y < altoFrente; y++) {
                for (int x = 0; x < anchoFrente; x++) {
                    escritorPixeles.setArgb(x, y, lectorFrente.getArgb(x, y));
                }
            }

            // 5. Copiar el reverso
            for (int y = 0; y < altoReverso; y++) {
                for (int x = 0; x < anchoReverso; x++) {
                    escritorPixeles.setArgb(x + anchoFrente, y, lectorReverso.getArgb(x, y));
                }
            }

            // 6. Guardar la imagen combinada
            ImageIO.write(SwingFXUtils.fromFXImage(imagenCombinada, null), "png", archivo);
            mostrarInformacion("Éxito", "El carnet (frente y reverso) se ha exportado correctamente en un solo archivo.");

        } catch (IOException ex) {
            mostrarAlerta("Error al guardar", ex.getMessage());
        }
    }
}
  private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    private boolean ValidarCampos() {
        if (TFnombre.getText().isEmpty()){
            mostrarAlerta("Campo vacío","Nombres es obligatorio.");
            TFnombre.requestFocus();
            return false;}
        if(TFapellido.getText().isEmpty()){
            mostrarAlerta("Campo vacío","Apellidos es obligatorio.");
            TFapellido.requestFocus();
            return false;}
        if(TFdocumento.getText().isEmpty()){
            mostrarAlerta("Campo vacío","El Documento es obligatorio.");
            TFdocumento.requestFocus();
            return false;}
        if(TFentidad.getText().isEmpty()){
            mostrarAlerta("Campo vacío","La Entidad es obligatorio.");
            TFentidad.requestFocus();
            return false;}
        if(TFcargo.getText().isEmpty()){
            mostrarAlerta("Campo vacío","El Cargo es obligatorio.");
            TFcargo.requestFocus();
            return false;}
        if(CBrh.getValue() == null){
            mostrarAlerta("Campo vacío","El RH es obligatorio.");
            CBrh.requestFocus();
            return false;}
        if(DPvigencia.getValue() == null){
            mostrarAlerta("Campo vacío","La Fecha de Vigencia es obligatorio.");
            DPvigencia.requestFocus();
            return false;}
        return true;
    }

    @FXML
    private void eliminarCarnet() {
        Carnet carnetSeleccionado = tablaCarnet.getSelectionModel().getSelectedItem();
        if (carnetSeleccionado != null) {
            CarnetDAO carnetDAO = new CarnetDAO();
            try {
                boolean eliminado = carnetDAO.EliminarCarnet(carnetSeleccionado.getId());
                if (eliminado) {
                    listaCarnet.remove(carnetSeleccionado);
                    mostrarInformacion("Éxito", "El carnet ha sido eliminado correctamente.");
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el carnet.");
                }
            } catch (Exception e) {
                mostrarAlerta("Error", "Ocurrió un error al eliminar el carnet: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Advertencia", "Por favor, seleccione un carnet para eliminar.");
        }
    }

    private void CargarFiltro(){
        try{ 
            CarnetDAO carnetDAO=new CarnetDAO();
            String nombre=Fnombre.getText().trim();
            String apellido=Fapellido.getText().trim();
            String documento=Fdocumento.getText().trim();
            String entidad=Fentidad.getText().trim();
            LocalDate fechaGeneracion=FDPfechaGeneracion.getValue();
            LocalDate vigencia=FDPvigencia.getValue();
            List<Carnet> list=carnetDAO.listaFiltrada( nombre,apellido,documento,entidad,fechaGeneracion,vigencia);
            listaCarnet.setAll(list);

        }catch (Exception e) {
            System.out.println("Error al cargar carnet: " + e.getMessage());
        }
    }
private void limpiarFiltros(){
    try{
    CarnetDAO carnetDAO=new CarnetDAO();
    Fnombre.setText("");
    Fapellido.setText("");
    Fentidad.setText("");
    Fdocumento.setText("");
    FDPfechaGeneracion.setValue(null);
    FDPvigencia.setValue(null);
    listaCarnet.clear();
    listaCarnet.addAll(carnetDAO.listarTodas());

    }catch(Exception e){
        System.out.println(e.toString());
    }
}
    @Override
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

@FXML
public void editarCarnet() {
    if (carnetSeleccionado == null) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Sin selección");
        alert.setHeaderText(null);
        alert.setContentText("Por favor seleccione un carnet en la tabla.");
        alert.showAndWait();
        return;
    }

    // Llenar TextFields con los datos
    TFnombre.setText(carnetSeleccionado.getNombre());
    TFapellido.setText(carnetSeleccionado.getApellido());
    TFdocumento.setText(carnetSeleccionado.getDocumento());
    TFentidad.setText(carnetSeleccionado.getEntidad());
    TFcargo.setText(carnetSeleccionado.getCargo());
    CBrh.setValue(carnetSeleccionado.getRh());


    DPvigencia.setValue(carnetSeleccionado.getFechaVigencia());


}
private void Actualizar(){
    TFnombre.setText(carnetSeleccionado.getNombre());
    TFapellido.setText(carnetSeleccionado.getApellido());
    TFdocumento.setText(carnetSeleccionado.getDocumento());
    TFentidad.setText(carnetSeleccionado.getEntidad());
    TFcargo.setText(carnetSeleccionado.getCargo());
    CBrh.setValue(carnetSeleccionado.getRh());
    DPvigencia.setValue(carnetSeleccionado.getFechaVigencia());

}
@FXML
public void actualizarCarnet() {
    if (carnetSeleccionado == null) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Sin selección");
        alert.setHeaderText(null);
        alert.setContentText("Por favor seleccione un carnet para actualizar.");
        alert.showAndWait();
        return;
    }

    // Validar campos antes de actualizar
    if (!ValidarCampos()) return;

    try {
        // Actualizar los datos del carnet seleccionado
        carnetSeleccionado.setNombre(TFnombre.getText().toUpperCase());
        carnetSeleccionado.setApellido(TFapellido.getText());
        carnetSeleccionado.setDocumento(TFdocumento.getText());
        carnetSeleccionado.setEntidad(TFentidad.getText());
        carnetSeleccionado.setCargo(TFcargo.getText());
        carnetSeleccionado.setRh(CBrh.getValue());
        carnetSeleccionado.setFechaVigencia(DPvigencia.getValue());
        carnetSeleccionado.setFechaCreacion(carnetSeleccionado.getFechaCreacion()); // mantener fecha original
        carnetSeleccionado.setUsername(usuario != null ? usuario.getUsuario() : carnetSeleccionado.getUsername());

        // Generar nuevo código PDF417 si deseas actualizarlo
        byte[] imagenCodigo = generarImagenCodigo(carnetSeleccionado);
        carnetSeleccionado.setCodigo(imagenCodigo);

        String codigoId = "PDF417_" + System.currentTimeMillis() + "_" + carnetSeleccionado.getDocumento();
        carnetSeleccionado.setCodigoPDF417(codigoId);

        // Mostrar la imagen actualizada en ImageView
        mostrarImagenCodigo(carnetSeleccionado);

        // Actualizar en la base de datos
        CarnetDAO carnetDAO = new CarnetDAO();
        carnetDAO.actualizar(carnetSeleccionado);

        // Refrescar la tabla
        cargarCarnetDB();

        // Mensaje de éxito
        mostrarInformacion("Éxito", "El carnet ha sido actualizado correctamente.");

    } catch (Exception e) {
        e.printStackTrace();
        mostrarAlerta("Error", "Ocurrió un error al actualizar el carnet: " + e.getMessage());
    }
}


}
