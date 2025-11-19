package com.example.Controlador;

import java.net.URL;
import java.util.ResourceBundle;

import com.example.DAO.LoginDAO;
import com.example.Modelo.Usuario;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistroUsuarioController implements Initializable {
    @FXML
    private Button btnRegistrar, btnLimpiar;
    @FXML
    private ComboBox<String> gradoComboBox, rolComboBox;
    @FXML
    private TextField nombreField, apellidoField, usuarioField, cedulaField;
    @FXML
    private PasswordField contrasenaField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ContenidoCombobox();
        ConfigurarEventos();
    }

    private void ContenidoCombobox() {
        gradoComboBox.getItems().addAll("TENIENTE", "INSPECTOR JEFE", "INSPECTOR", "DISTINGUIDO", "DRAGONEANTE");
        rolComboBox.getItems().addAll("Administrador", "Funcionario");
        gradoComboBox.setPromptText("Seleccione Grado");
        rolComboBox.setPromptText("Seleccione Rol");
    }

    private void ConfigurarEventos() {
        btnLimpiar.setOnAction(event -> limpiarcampos());
        btnRegistrar.setOnAction(event -> RegistrarUsuario());

        // 🟢 Generar automáticamente el usuario al perder el foco del campo cédula
        cedulaField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                generarUsuario();
            }
        nombreField.textProperty().addListener((obs, ov, nv) -> generarUsuario());
        apellidoField.textProperty().addListener((obs, ov, nv) -> generarUsuario());
        });
    }

    private void limpiarcampos() {
        nombreField.clear();
        apellidoField.clear();
        usuarioField.clear();
        cedulaField.clear();
        contrasenaField.clear();
        gradoComboBox.getSelectionModel().clearSelection();
        rolComboBox.getSelectionModel().clearSelection();
        gradoComboBox.setPromptText("Seleccione Grado");
        rolComboBox.setPromptText("Seleccione Rol");
    }

    private void RegistrarUsuario() {
        if (cedulaField.getText().isEmpty() || nombreField.getText().isEmpty() || apellidoField.getText().isEmpty()
                || usuarioField.getText().isEmpty() || contrasenaField.getText().isEmpty()
                || gradoComboBox.getValue() == null || rolComboBox.getValue() == null) {
            mostrarMensaje("Todos los campos son obligatorios.");
            return;
        }
        String cedula=cedulaField.getText();
        String nombre = nombreField.getText().toUpperCase();
        String apellido = apellidoField.getText().toUpperCase();
        String user = usuarioField.getText();
        String contrasena = contrasenaField.getText();
        String grado = gradoComboBox.getValue();
        String rol = rolComboBox.getValue();

        Usuario usuario = new Usuario(cedula,nombre, apellido, user, contrasena, grado, rol);
        LoginDAO dao = new LoginDAO();
        dao.registrarUsuario(usuario);
        limpiarcampos();
        mostrarMensaje("✅ Registro de usuario exitoso");
    }

    private void generarUsuario() {
        String nombre = nombreField.getText().trim();
        String apellido = apellidoField.getText().trim();
        String cedula = cedulaField.getText().trim();

        // ⚠️ Verificar que haya datos antes de generar
        if (nombre.isEmpty() || apellido.isEmpty() || cedula.isEmpty()) {
            // No mostrar mensaje aquí para no interrumpir al usuario mientras escribe
            return;
        }

        // Generar usuario: primera letra del nombre + primera letra del apellido + cédula
        String user = nombre.substring(0, 1).toUpperCase()
                + apellido.substring(0, 1).toUpperCase()
                + cedula;

        // Mostrar usuario y contraseña predeterminada
        usuarioField.setText(user);
        contrasenaField.setText("inpec2025");
    }

    private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
