package com.example.Controlador;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import com.example.Modelo.Usuario;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class CambiarContrasenaController implements Initializable, UsuarioReceptor {

    @FXML private PasswordField PFContrasenaActual;
    @FXML private PasswordField PFContrasenaNueva;
    @FXML private PasswordField PFConfirmarContrasena;
    @FXML private Label lblStatus;

    private Usuario usuario;

    /**
     * Inicializa el controlador y oculta el mensaje de estado.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblStatus.setVisible(false);
    }

    // BOTÓN ACTUALIZAR
    /**
     * Evento del botón actualizar: inicia el proceso de cambio de contraseña.
     */
    @FXML
    private void handleUpdate() {
        ActualizarContrasena();
    }

    /**
     * Recibe el usuario autenticado para el cambio de contraseña.
     */
    @Override
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Realiza la validación y actualización de la contraseña del usuario.
     */
    private void ActualizarContrasena() {

        // 🔐 Validaciones básicas
        if (usuario == null) {
            mostrarError("No hay usuario autenticado.");
            return;
        }

        String actual = PFContrasenaActual.getText();
        String nueva = PFContrasenaNueva.getText();
        String confirmar = PFConfirmarContrasena.getText();

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }

        if (!nueva.equals(confirmar)) {
            mostrarError("Las contraseñas no coinciden.");
            return;
        }

        Conexion conexion = new Conexion();

        try (Connection con = conexion.conectar()) {

            // 1️⃣ Verificar contraseña actual
            String sqlVerificar = "SELECT contraseña FROM usuarios WHERE id = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlVerificar)) {
                pst.setInt(1, usuario.getId()); 
                ResultSet rs = pst.executeQuery();

                if (!rs.next() || !rs.getString("contraseña").equals(actual)) {
                    mostrarError("La contraseña actual es incorrecta.");
                    return;
                }
            }

            // 2️⃣ Actualizar contraseña
            String sqlUpdate = "UPDATE usuarios SET contraseña = ? WHERE id = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlUpdate)) {
                pst.setString(1, nueva);
                pst.setInt(2, usuario.getId());
                pst.executeUpdate();
            }

            lblStatus.setText("Contraseña actualizada exitosamente.");
            lblStatus.setStyle("-fx-text-fill: green;");
            lblStatus.setVisible(true);

            limpiarCampos();

        } catch (Exception e) {
            mostrarError("Error al actualizar la contraseña.");

        }
    }

    /**
     * Muestra un mensaje de error en la interfaz.
     * @param mensaje Mensaje a mostrar
     */
    private void mostrarError(String mensaje) {
        lblStatus.setText(mensaje);
        lblStatus.setStyle("-fx-text-fill: red;");
        lblStatus.setVisible(true);
    }

    /**
     * Limpia los campos de contraseña del formulario.
     */
    private void limpiarCampos() {
        PFContrasenaActual.clear();
        PFContrasenaNueva.clear();
        PFConfirmarContrasena.clear();
    }
}
