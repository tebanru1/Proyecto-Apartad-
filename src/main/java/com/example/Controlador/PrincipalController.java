package com.example.Controlador;


import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Controlador para el archivo principal.fxml
 * Maneja los botones btnIngreso, btnSalida y el campo TFCedula
 */
public class PrincipalController implements Initializable {
    
    @FXML
    private Button btnIngreso;
    
    @FXML
    private Button btnSalida;
    
    @FXML
    private Button btnInicio;

    @FXML
    private Button btnCodigo;

    @FXML
    private Button btnAutorizaciones;
   
    @FXML
    private Button btnVisitas;
    
    @FXML
    private TextField TFCedula;
    
    @FXML
    private AnchorPane root;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        volverInicio();

        // Configurar eventos de los botones
        configurarEventos();
        
       
    }
    
    /**
     * Configura los eventos de los botones
     */
    private void configurarEventos() {
       
        // Evento del botón de generar código
        btnCodigo.setOnAction(event -> cargarGenerarCodigo());

        btnAutorizaciones.setOnAction(event->Autorizaciones());
        
        // Evento del botón de inicio
        btnInicio.setOnAction(event -> {
            volverInicio();});
       
        btnVisitas.setOnAction(event -> ModuloVisitas());  
    }
    
    /**
     * Muestra un mensaje en la interfaz
     */
    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Sistema CPMS Apartadó");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Carga el contenido de generarcodigo.fxml dentro del AnchorPane root
     */
    @FXML
    private void cargarGenerarCodigo() {
        try {
            // Limpiar el contenido actual del root
            root.getChildren().clear();
            
            // Cargar el FXML de generar código
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/generarcodigo.fxml"));
            Parent contenidoGenerarCodigo = loader.load();
            
            // Agregar el contenido al root
            root.getChildren().add(contenidoGenerarCodigo);
            
            // Anclar el contenido al root
            AnchorPane.setTopAnchor(contenidoGenerarCodigo, 0.0);
            AnchorPane.setBottomAnchor(contenidoGenerarCodigo, 0.0);
            AnchorPane.setLeftAnchor(contenidoGenerarCodigo, 0.0);
            AnchorPane.setRightAnchor(contenidoGenerarCodigo, 0.0);
            
        } catch (Exception e) {
            mostrarMensaje("Error al cargar el generador de códigos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Vuelve a la pantalla principal
     */
   @FXML
private void volverInicio() {
    try {
        // Limpiar el contenido actual del root
        root.getChildren().clear();

        // Cargar el FXML de inicio
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/inicio.fxml"));
        AnchorPane V_inicio = loader.load(); // Asegúrate que el FXML raíz es AnchorPane

        // 🔹 Anclar V_inicio a los bordes de root
        AnchorPane.setTopAnchor(V_inicio, 0.0);
        AnchorPane.setBottomAnchor(V_inicio, 0.0);
        AnchorPane.setLeftAnchor(V_inicio, 0.0);
        AnchorPane.setRightAnchor(V_inicio, 0.0);

        // Agregar V_inicio al root
        root.getChildren().add(V_inicio);

    } catch (Exception e) {
        mostrarMensaje("Error al volver al inicio: " + e.getMessage(), Alert.AlertType.ERROR);
        e.printStackTrace();
    }
}

    @FXML
    private void ModuloVisitas() {
        try {
            // Limpiar el contenido actual del root
            root.getChildren().clear();
            
            // Cargar el FXML principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/visitante.fxml"));
            Parent contenidoVisitas = loader.load();
           
            // Agregar el contenido al root
            root.getChildren().add(contenidoVisitas);
            
            // Anclar el contenido al root
            AnchorPane.setTopAnchor(contenidoVisitas, 0.0);
            AnchorPane.setBottomAnchor(contenidoVisitas, 0.0);
            AnchorPane.setLeftAnchor(contenidoVisitas, 0.0);
            AnchorPane.setRightAnchor(contenidoVisitas, 0.0);
         
        } catch (Exception e) {
            mostrarMensaje("Error al cargar el modulo de visitas: " + e.getMessage(), Alert.AlertType.ERROR);
        e.printStackTrace();
        System.out.println(e.getMessage());
        }
    }
    
    private void Autorizaciones() {
        try {
            // Limpiar el contenido actual del root
            root.getChildren().clear();
            
            // Cargar el FXML principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/autorizaciones.fxml"));
            Parent contenidoAutorizaciones= loader.load();
            

            // Agregar el contenido al root
           
            
            // Anclar el contenido al root
            AnchorPane.setTopAnchor(contenidoAutorizaciones, 0.0);
            AnchorPane.setBottomAnchor(contenidoAutorizaciones, 0.0);
            AnchorPane.setLeftAnchor(contenidoAutorizaciones, 0.0);
            AnchorPane.setRightAnchor(contenidoAutorizaciones, 0.0);
             root.getChildren().add(contenidoAutorizaciones);
         
        } catch (Exception e) {
            mostrarMensaje("Error al cargar el modulo Autorizaciones: " + e.getMessage(), Alert.AlertType.ERROR);
        e.printStackTrace();
        System.out.println(e.getMessage());
        }
    }
}
