package com.example.Controlador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.example.DAO.AutorizacionesDAO;
import com.example.Modelo.Autorizaciones;

public class InicioController implements Initializable {

    @FXML
    private AnchorPane ContenedorAutorizaciones;
    @FXML
    private AnchorPane ContenedorImagen;

    private final AutorizacionesDAO dao = new AutorizacionesDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarAutorizaciones();
        cargarimagen();
        
    }

    private void cargarAutorizaciones() {
        try {
            List<Autorizaciones> lista = dao.listarTodas();
            VBox contenedor = new VBox(8);
            contenedor.getStyleClass().add("card-autorizaciones-lista");
            contenedor.setPadding(new Insets(10));
            contenedor.setAlignment(Pos.TOP_CENTER);

            for (Autorizaciones a : lista) {
                HBox fila = new HBox(10);
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setPrefWidth(Double.MAX_VALUE);
                fila.getStyleClass().add("card-autorizaciones-fila");

                // --- Icono PDF ---
                InputStream iconStream = getClass().getResourceAsStream("/com/example/pdf.png");
                ImageView pdfIcon = new ImageView();
                if (iconStream != null) {
                    pdfIcon.setImage(new Image(iconStream));
                } 
                pdfIcon.setFitWidth(24);
                pdfIcon.setFitHeight(24);

                // --- Descripción ---
                javafx.scene.control.Label lblDescripcion = new javafx.scene.control.Label(a.getDescripcion());
                lblDescripcion.getStyleClass().add("card-autorizaciones-descripcion");
                lblDescripcion.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(lblDescripcion, Priority.ALWAYS);

                // --- Fecha ---
                javafx.scene.control.Label lblFecha = new javafx.scene.control.Label(
                        a.getFechaGeneracion() != null ? a.getFechaGeneracion() : ""
                );
                lblFecha.getStyleClass().add("card-autorizaciones-fecha");

                // --- Ensamble ---
                fila.getChildren().addAll(pdfIcon, lblDescripcion, lblFecha);
                configurarEventos(fila, a);
                contenedor.getChildren().add(fila);
            }

            // --- Scroll ---
            ScrollPane scroll = new ScrollPane(contenedor);
            scroll.getStyleClass().add("card-autorizaciones-scroll");
            scroll.setFitToWidth(true);

            ContenedorAutorizaciones.getChildren().setAll(scroll);
            AnchorPane.setTopAnchor(scroll, 0.0);
            AnchorPane.setBottomAnchor(scroll, 0.0);
            AnchorPane.setLeftAnchor(scroll, 0.0);
            AnchorPane.setRightAnchor(scroll, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar autorizaciones", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void configurarEventos(HBox fila, Autorizaciones autorizacion) {
        fila.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                try {
                    byte[] datosPDF = dao.obtenerPDFPorId(autorizacion.getId());
                    if (datosPDF != null && datosPDF.length > 0) {
                        File archivoTemporal = File.createTempFile("autorizacion_" + autorizacion.getId(), ".pdf");
                        try (FileOutputStream fos = new FileOutputStream(archivoTemporal)) {
                            fos.write(datosPDF);
                        }
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(archivoTemporal);
                        } else {
                            mostrarAlerta("Advertencia", "El sistema no soporta apertura automática de PDF.", Alert.AlertType.WARNING);
                        }
                    } else {
                        mostrarAlerta("Sin PDF", "No se encontró un archivo PDF para esta autorización.", Alert.AlertType.WARNING);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Error al abrir PDF", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }
    private void cargarimagen() {
    try {
        // fondo con cover centrado
        ContenedorImagen.setStyle(
            "-fx-background-image: url('/com/example/Carcel.jpg'); " +
            "-fx-background-size: cover; " +
            "-fx-background-position: center center; " +
            "-fx-background-repeat: no-repeat;"
        );

        // clip redondeado que recorta la imagen al tamaño del contenedor (responsive)
        Rectangle clip = new Rectangle();
        double radius = 40; // ajustar radio de esquinas
        clip.setArcWidth(radius);
        clip.setArcHeight(radius);
        clip.widthProperty().bind(ContenedorImagen.widthProperty());
        clip.heightProperty().bind(ContenedorImagen.heightProperty());
        ContenedorImagen.setClip(clip);

    } catch (Exception e) {
        e.printStackTrace();
        mostrarAlerta("Error al cargar la imagen", e.getMessage(), Alert.AlertType.ERROR);
    }
}


    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
