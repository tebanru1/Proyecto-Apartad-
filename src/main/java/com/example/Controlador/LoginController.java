package com.example.Controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.geometry.Side;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.example.DAO.LoginDAO;
import com.example.Modelo.Usuario;

public class LoginController implements Initializable {

    @FXML private TextField usuario;
    @FXML private PasswordField contraseña;
    @FXML private Button btnIniciarSesion;
    @FXML private Label MensajeError;

    private List<String> usuariosGuardados = new ArrayList<>();
    private ContextMenu menu = new ContextMenu();
    private Usuario usuarioActual;
    private int indiceSeleccionado = -1; // índice de selección actual en el menú

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usuariosGuardados = cargarUsuarios();
        configurarAutocompletado();
        QuitarMensajeError();
    }

    private void configurarAutocompletado() {
        // Listener al escribir
        usuario.textProperty().addListener((obs, oldText, newText) -> {
            mostrarSugerencias(newText);
        });

        // Mostrar menú al hacer clic
        usuario.setOnMouseClicked(e -> {
            mostrarSugerencias(usuario.getText());
        });

        // Ocultar menú si el campo pierde foco
        usuario.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) menu.hide();
        });

        // Manejo del teclado (flechas y Enter)
        usuario.setOnKeyPressed(event -> {
            if (menu.isShowing() && !menu.getItems().isEmpty()) {
                switch (event.getCode()) {
                    case DOWN:
                        event.consume();
                        if (indiceSeleccionado < menu.getItems().size() - 1) {
                            indiceSeleccionado++;
                        } else {
                            indiceSeleccionado = 0;
                        }
                        resaltarItem(indiceSeleccionado);
                        break;

                    case UP:
                        event.consume();
                        if (indiceSeleccionado > 0) {
                            indiceSeleccionado--;
                        } else {
                            indiceSeleccionado = menu.getItems().size() - 1;
                        }
                        resaltarItem(indiceSeleccionado);
                        break;

                    case ENTER:
                        event.consume();
                        if (indiceSeleccionado >= 0 && indiceSeleccionado < menu.getItems().size()) {
                            CustomMenuItem item = (CustomMenuItem) menu.getItems().get(indiceSeleccionado);
                            Label label = (Label) item.getContent();
                            usuario.setText(label.getText());
                            menu.hide();
                            indiceSeleccionado = -1;
                        }
                        break;

                    default:
                        break;
                }
            }
        });
    }
private void QuitarMensajeError() {
         usuario.textProperty().addListener((obs, oldText, newText) -> MensajeError.setText(""));
    contraseña.textProperty().addListener((obs, oldText, newText) -> MensajeError.setText(""));
    }

    private void mostrarSugerencias(String texto) {
        if (texto.isEmpty()) {
            actualizarMenu(new ArrayList<>());
        } else {
            List<String> coincidencias = usuariosGuardados.stream()
                    .filter(u -> u.toLowerCase().startsWith(texto.toLowerCase()))
                    .collect(Collectors.toList());
            actualizarMenu(coincidencias);
        }
    }

    private void actualizarMenu(List<String> items) {
        if (items.isEmpty()) {
            menu.hide();
            return;
        }

        indiceSeleccionado = -1; // reiniciar selección al actualizar
        List<CustomMenuItem> menuItems = items.stream().map(u -> {
            Label label = new Label(u);
            label.setMinWidth(usuario.getWidth()-10);
            label.setMaxWidth(usuario.getWidth()-10); 
            label.setPrefWidth(usuario.getWidth()-10); 
            label.setStyle("-fx-padding: 4 5 4 5;");
            CustomMenuItem item = new CustomMenuItem(label, true);
            item.setOnAction(e -> {
                usuario.setText(u);
                menu.hide();
            });
            return item;
        }).collect(Collectors.toList());

        menu.getItems().setAll(menuItems);
        menu.setOnShowing(e -> menu.setPrefWidth(usuario.getWidth()));

        if (!menu.isShowing()) {
            menu.show(usuario, Side.BOTTOM, 0, 0);
        }
    }

    private void resaltarItem(int index) {
        for (int i = 0; i < menu.getItems().size(); i++) {
            CustomMenuItem item = (CustomMenuItem) menu.getItems().get(i);
            Label label = (Label) item.getContent();
            if (i == index) {
                label.setStyle("-fx-background-color: #cce5ff; -fx-padding: 4 5 4 5;");
            } else {
                label.setStyle("-fx-background-color: transparent; -fx-padding: 4 5 4 5;");
            }
        }
    }

    private void cargarDatosUsuario(String username) {
        LoginDAO loginDAO = new LoginDAO();
        usuarioActual = loginDAO.obtenerDatosUsuario(username);
    }

    @FXML
    private void validarLogin() {
        String user = usuario.getText().trim();
        String pass = contraseña.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            MensajeError.setText("Por favor, ingrese usuario y contraseña.");
            return;
        }

        LoginDAO loginDAO = new LoginDAO();
        boolean esValido = loginDAO.validarUsuario(user, pass);

        if (esValido) {
            guardarUsuario(user);
            usuariosGuardados = cargarUsuarios(); // recargar lista
            cargarDatosUsuario(user);
            Ingresar();
        } else {
            MensajeError.setText("Usuario o Contraseña incorrectos.");
        }
    }

    private void mensaje(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void Ingresar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/principal.fxml"));
            Parent root = loader.load();

            PrincipalController principalController = loader.getController();
            principalController.setUsuario(usuarioActual);

            Stage stage = new Stage();
            stage.setTitle("CPMS APARTADÓ - INPEC");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/Logo_Institucional.png")));
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.show();

            Stage ventanaActual = (Stage) usuario.getScene().getWindow();
            ventanaActual.close();

        } catch (IOException e) {
            e.printStackTrace();
            mensaje("Error", "No se pudo cargar la ventana principal.", Alert.AlertType.ERROR);
        }
    }

    // --- Métodos para archivo de usuarios ---
    public List<String> cargarUsuarios() {
        List<String> usuarios = new ArrayList<>();
        Path path = Paths.get("usuarios.txt");

        if (Files.exists(path)) {
            try {
                usuarios = Files.readAllLines(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return usuarios;
    }

    public void guardarUsuario(String username) {
        Path path = Paths.get("usuarios.txt");
        try {
            List<String> usuarios = Files.exists(path) ? Files.readAllLines(path) : new ArrayList<>();
            if (!usuarios.contains(username)) {
                usuarios.add(username);
                Files.write(path, usuarios);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
