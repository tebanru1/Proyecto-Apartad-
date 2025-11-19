package com.example.Controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import com.example.Controlador.AutorizacionesController.UsuarioReceptor;
import com.example.Modelo.Usuario;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class PrincipalController implements Initializable {

    @FXML
    private Button btnIngreso, btnSalida, btnInicio, btnCodigo, btnAutorizaciones, btnVisitas, btnRegistrar, btnIngroAdmin;

    @FXML
    private TextField TFCedula;

    @FXML
    private AnchorPane root;

    private Usuario usuario;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarEventos();
        // No cargamos ningún módulo automáticamente aquí
    }

    /**
     * Configura los eventos de los botones
     */
    private void configurarEventos() {
        btnRegistrar.setOnAction(event -> abrirVentanaRegistro());
        btnCodigo.setOnAction(event -> cargarModulo("/com/example/generarcodigo.fxml", null));
        btnAutorizaciones.setOnAction(event -> cargarModulo("/com/example/autorizaciones.fxml", c -> {
            if (c instanceof UsuarioReceptor receptor) receptor.setUsuario(usuario);
        }));
        btnVisitas.setOnAction(event -> cargarModulo("/com/example/visitante.fxml", c -> {
            if (c instanceof UsuarioReceptor receptor) receptor.setUsuario(usuario);
        }));
        btnIngroAdmin.setOnAction(event -> cargarModulo("/com/example/IngresoAdministrativos.fxml", c -> {
            if (c instanceof UsuarioReceptor receptor) receptor.setUsuario(usuario);
        }));
        btnInicio.setOnAction(event -> volverInicio());
    }

    /**
     * Método genérico para cargar un módulo en el AnchorPane root
     */
    private <T> void cargarModulo(String fxmlPath, Consumer<T> configurador) {
        try {
            root.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent contenido = loader.load();
            T controller = loader.getController();

            if (configurador != null && controller != null) {
                configurador.accept(controller);
            }

            root.getChildren().add(contenido);
            anclarAlAnchor(contenido);

        } catch (IOException e) {
            mostrarMensaje("Error al cargar el módulo: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Ancla un nodo a todos los lados del AnchorPane
     */
    private void anclarAlAnchor(Parent nodo) {
        AnchorPane.setTopAnchor(nodo, 0.0);
        AnchorPane.setBottomAnchor(nodo, 0.0);
        AnchorPane.setLeftAnchor(nodo, 0.0);
        AnchorPane.setRightAnchor(nodo, 0.0);
    }

    /**
     * Vuelve a la pantalla de inicio
     */
    @FXML
    private void volverInicio() {
        cargarModulo("/com/example/inicio.fxml", c -> {
            if (c instanceof InicioController inicioController) {
                if (usuario != null) {
                    inicioController.setUsuario(usuario);
                    inicioController.limpiarCampos();
                }
            }
        });
    }

    /**
     * Abre la ventana de registrar usuarios de forma independiente
     */
    private void abrirVentanaRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/registrarUsuario.fxml"));
            Parent registroRoot = loader.load();

            // Pasar usuario si es necesario
            Object controller = loader.getController();
            if (controller instanceof UsuarioReceptor receptor) {
                receptor.setUsuario(usuario);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(registroRoot));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.setTitle("REGISTRO DE USUARIOS");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/Logo_Institucional.png")));
            stage.show();

        } catch (IOException e) {
            mostrarMensaje("Error al abrir ventana de registro: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
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
     * Setter del usuario
     */
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        volverInicio();
    }
}
