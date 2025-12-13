package com.example.Controlador;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.ResourceBundle;

public class carnetController implements Initializable {

    // --- Mapeo de Componentes del FXML (Inputs) ---
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtEntidad;
    @FXML private TextField txtCargo;
    @FXML private ComboBox<String> cbRh;
    @FXML private DatePicker dpVigencia;
    @FXML private Button btnCargarFoto;
    @FXML private Button btnGenerarCode;
    @FXML private Button btnExportar;

    // --- Mapeo de Componentes del FXML (Vista Previa - Labels) ---
    @FXML private Label lblNombreVal;
    @FXML private Label lblApellidoVal;
    @FXML private Label lblDocumentoVal;
    @FXML private Label lblEntidadVal;
    @FXML private Label lblCargoVal;
    @FXML private Label lblRhVal;
    @FXML private Label lblVigenciaVal;
    @FXML private Label lblNoPhoto;
    @FXML private Label lblBarcodePlaceholder;

    // --- Mapeo de Contenedores y Multimedia ---
    @FXML private AnchorPane cardFront; // El contenedor del frente del carnet
    @FXML private AnchorPane cardBack;  // El contenedor del reverso
    @FXML private ImageView imgFotoView;
    @FXML private ImageView imgPdf417;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarCampos();
        configurarBindings();
    }

    /**
     * Configuración inicial de listas y restricciones.
     */
    private void configurarCampos() {
        // Llenar ComboBox de RH
        cbRh.getItems().addAll("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-");
        
        // Forzar mayúsculas en los campos de texto
        forceUpperCase(txtNombre);
        forceUpperCase(txtApellido);
        forceUpperCase(txtEntidad);
        forceUpperCase(txtCargo);
    }

    /**
     * Conecta los campos de texto con los Labels del carnet en tiempo real.
     */
    private void configurarBindings() {
        // Enlaces directos (lo que escribes en A aparece en B)
        lblNombreVal.textProperty().bind(txtNombre.textProperty());
        lblApellidoVal.textProperty().bind(txtApellido.textProperty());
        lblDocumentoVal.textProperty().bind(txtDocumento.textProperty());
        lblEntidadVal.textProperty().bind(txtEntidad.textProperty());
        
        // Cargo (si está vacío muestra "VISITANTE" por defecto)
        txtCargo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) lblCargoVal.setText("VISITANTE");
            else lblCargoVal.setText(newVal);
        });

        // RH
        cbRh.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
             lblRhVal.setText(newValue != null ? newValue : "");
        });

        // Formato de Fecha
        dpVigencia.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                lblVigenciaVal.setText(newValue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        });
    }

    /**
     * Método auxiliar para forzar mayúsculas en TextFields.
     */
    private void forceUpperCase(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
    }

    // --- ACIONES DE BOTONES ---

    @FXML
    public void cargarFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Fotografía");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );
        
        // Obtener la ventana actual para el modal
        Stage stage = (Stage) btnCargarFoto.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imgFotoView.setImage(image);
            lblNoPhoto.setVisible(false); // Ocultar el texto "Sin Foto"
            centerImage(imgFotoView); // Opcional: Centrar recorte si es necesario
        }
    }

    @FXML
    public void generarCodigo() {
        // Recopilar datos para codificar
        String data = String.format("%s|%s|%s|%s|%s",
                txtDocumento.getText(),
                txtApellido.getText(),
                txtNombre.getText(),
                cbRh.getValue(),
                lblVigenciaVal.getText()
        );

        if (txtDocumento.getText().isEmpty()) {
            mostrarAlerta("Error", "El documento es obligatorio para generar el código.");
            return;
        }

        // GENERACIÓN DEL CÓDIGO
        // Nota: Para PDF417 real se requiere la librería 'com.google.zxing'.
        // Aquí uso un generador simulado visual para que el código compile sin dependencias externas.
        
        Image barcodeImage = generarSimulacionPDF417(data); // CAMBIAR ESTO POR ZXING SI SE AGREGA LA LIBRERÍA
        
        imgPdf417.setImage(barcodeImage);
        lblBarcodePlaceholder.setVisible(false);
    }

    @FXML
public void exportarCarnet() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Guardar Carnet");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen PNG", "*.png"));
    fileChooser.setInitialFileName("carnet_" + txtDocumento.getText() + ".png");

    Stage stage = (Stage) btnExportar.getScene().getWindow();
    File file = fileChooser.showSaveDialog(stage);

    if (file != null) {
        try {
            // 1. Tomar captura del frente y reverso
            WritableImage imageFront = cardFront.snapshot(new SnapshotParameters(), null);
            WritableImage imageBack = cardBack.snapshot(new SnapshotParameters(), null);

            // 2. Obtener dimensiones
            int widthFront = (int) cardFront.getWidth();
            int heightFront = (int) cardFront.getHeight();
            // Aseguramos que la altura del reverso sea la misma (idealmente deberían ser iguales)
            int widthBack = (int) cardBack.getWidth();
            int heightBack = (int) cardBack.getHeight();

            // Usamos la altura mayor para el contenedor, aunque deberían ser las mismas
            int combinedHeight = Math.max(heightFront, heightBack);
            int combinedWidth = widthFront + widthBack;

            // 3. Crear una nueva WritableImage para la combinación horizontal
            WritableImage combinedImage = new WritableImage(combinedWidth, combinedHeight);
            PixelWriter pw = combinedImage.getPixelWriter();
            
            // Lector de píxeles para copiar las caras
            var frontReader = imageFront.getPixelReader();
            var backReader = imageBack.getPixelReader();

            // 4. Copiar el frente
            for (int y = 0; y < heightFront; y++) {
                for (int x = 0; x < widthFront; x++) {
                    pw.setArgb(x, y, frontReader.getArgb(x, y));
                }
            }

            // 5. Copiar el reverso (comienza después del ancho del frente)
            for (int y = 0; y < heightBack; y++) {
                for (int x = 0; x < widthBack; x++) {
                    pw.setArgb(x + widthFront, y, backReader.getArgb(x, y));
                }
            }
            
            // 6. Guardar la imagen combinada
            ImageIO.write(SwingFXUtils.fromFXImage(combinedImage, null), "png", file);
            mostrarInformacion("Éxito", "El carnet (frente y reverso) se ha exportado correctamente en un solo archivo.");

        } catch (IOException ex) {
            mostrarAlerta("Error al guardar", ex.getMessage());
        }
    }
}

    // --- UTILIDADES ---

    private void centerImage(ImageView imageView) {
        // Ajuste básico de imagen para que no se deforme
        imageView.setPreserveRatio(true);
        // Lógica adicional de centrado podría ir aquí
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

    /**
     * MOCK: Genera una imagen que PARECE un código de barras.
     * Útil para probar la interfaz sin instalar librerías ZXing o Barbecue.
     */
    private Image generarSimulacionPDF417(String data) {
        int width = 300;
        int height = 80;
        WritableImage wb = new WritableImage(width, height);
        PixelWriter pw = wb.getPixelWriter();
        Random r = new Random();

        // Pintar ruido tipo código de barras
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Crear patrones verticales
                boolean isBlack = r.nextInt(10) > 4; 
                // Hacerlo ver más como bloques
                if (x % 4 == 0) isBlack = r.nextBoolean();
                
                pw.setColor(x, y, isBlack ? Color.BLACK : Color.WHITE);
            }
        }
        return wb;
    }
    
    /* * --- CÓDIGO REAL PARA ZXING (SI AGREGAS LA DEPENDENCIA) ---
     * * Dependencias Maven necesarias:
     * com.google.zxing:core:3.4.1
     * com.google.zxing:javase:3.4.1
     *
     * public Image generarPDF417Real(String data) {
     * try {
     * com.google.zxing.pdf417.PDF417Writer writer = new com.google.zxing.pdf417.PDF417Writer();
     * com.google.zxing.common.BitMatrix bitMatrix = writer.encode(data, com.google.zxing.BarcodeFormat.PDF_417, 300, 80);
     * * return SwingFXUtils.toFXImage(com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix), null);
     * } catch (Exception e) {
     * e.printStackTrace();
     * return null;
     * }
     * }
     */
}